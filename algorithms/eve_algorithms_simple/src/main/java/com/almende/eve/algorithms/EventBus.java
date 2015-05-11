/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.algorithms;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.joda.time.DateTime;

import com.almende.eve.protocol.jsonrpc.annotation.Access;
import com.almende.eve.protocol.jsonrpc.annotation.AccessType;
import com.almende.eve.protocol.jsonrpc.annotation.Name;
import com.almende.eve.protocol.jsonrpc.annotation.Namespace;
import com.almende.eve.protocol.jsonrpc.formats.Caller;
import com.almende.eve.protocol.jsonrpc.formats.JSONRequest;
import com.almende.eve.protocol.jsonrpc.formats.Params;
import com.almende.eve.scheduling.Scheduler;
import com.almende.util.jackson.JOM;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class EventBus.
 */
@Namespace("event")
public class EventBus {
	private static final Logger			LOG				= Logger.getLogger(EventBus.class
																.getName());
	private Scheduler					scheduler		= null;
	private TrickleRPC					trickle			= null;
	private Caller						caller			= null;
	private Graph						neighbors		= null;
	private String						tag				= null;
	private Set<Event>					events			= new HashSet<Event>(2);

	private static final JSONRequest	EXPIRYREQUEST	= new JSONRequest(
																"event.scheduleExpiry",
																null);
	private static final JSONRequest	TRIGGERREQUEST	= new JSONRequest(
																"event.scheduleTrigger",
																null);

	/**
	 * Instantiates a new event bus.
	 *
	 * @param scheduler
	 *            the scheduler
	 * @param caller
	 *            the caller
	 * @param neighbors
	 *            the neighbors
	 * @param tag
	 *            the tag
	 */
	public EventBus(Scheduler scheduler, Caller caller, Graph neighbors,
			String tag) {
		this.scheduler = scheduler;
		this.caller = caller;
		this.neighbors = neighbors;
		this.tag = tag;
		scheduleExpiry();
		scheduleTrigger();
		setupGossip();
	}

	/**
	 * Send a message to all EventBus participants;
	 *
	 * @param message
	 *            the message
	 */
	public void sendEvent(JSONRequest message) {
		final Event event = new Event(DateTime.now().plus(60000).getMillis(),
				message, caller.getSenderUrls().get(0));
		synchronized (events) {
			events.add(event);
		}
		trickle.reset();
	}

	/**
	 * Schedule expiry.
	 */
	@Access(AccessType.PUBLIC)
	public void scheduleExpiry() {
		doExpiry();
		scheduler.schedule(EXPIRYREQUEST, DateTime.now().plus(60000));
	}

	/**
	 * Expire old events.
	 */
	@Access(AccessType.PUBLIC)
	public void doExpiry() {
		final List<Event> stillToTrigger = new ArrayList<Event>();
		synchronized (events) {
			Iterator<Event> iter = events.iterator();
			while (iter.hasNext()) {
				Event event = iter.next();
				if (event.getExpiryTime() < DateTime.now().getMillis()) {
					if (!event.isTriggered()) {
						stillToTrigger.add(event);
					}
					iter.remove();
				}
			}
		}
		for (Event event : stillToTrigger) {
			trigger(event);
		}
	}

	/**
	 * Schedule trigger.
	 */
	@Access(AccessType.PUBLIC)
	public void scheduleTrigger() {
		doTriggers();
		scheduler.schedule(TRIGGERREQUEST, DateTime.now().plus(5000));
	}

	/**
	 * Do triggers.
	 */
	@Access(AccessType.PUBLIC)
	public void doTriggers() {
		Event[] eventArray;
		synchronized (this.events) {
			eventArray = this.events.toArray(new Event[0]);
		}
		for (Event event : eventArray) {
			if (!event.isTriggered()) {
				trigger(event);
			}
		}
	}

	private void trigger(final Event event) {
		synchronized (event) {
			if (!event.isTriggered()) {
				event.setTriggered(true);
			} else {
				return;
			}
		}
		scheduler.schedule(event.getMessage(), 0);
	}

	private void setupGossip() {
		final ObjectNode config = JOM.createObjectNode();
		config.put("intervalFactor", 16);
		config.put("intervalMin", 100);
		config.put("redundancyFactor", 2);
		config.put("namespace", "event.");

		trickle = new TrickleRPC(config, scheduler, new Runnable() {
			@Override
			public void run() {}
		}, new Runnable() {
			@Override
			public void run() {
				final Edge[] neighborArray = neighbors.getByTag(tag);
				final Params params = new Params();
				synchronized (events) {
					params.add("events", events);
				}
				final JSONRequest request = new JSONRequest(
						"event.receiveEvents", params);
				for (Edge neighbor : neighborArray) {
					try {
						caller.call(neighbor.getAddress(), request);
					} catch (IOException e) {
						LOG.log(Level.WARNING, "EventBus got IO error", e);
					}
				}
			}
		});

	}

	/**
	 * Receive events.
	 *
	 * @param events
	 *            the events
	 */
	@Access(AccessType.PUBLIC)
	public void receiveEvents(final @Name("events") Set<Event> events) {
		boolean trickleReset = false;
		synchronized (this.events) {
			if (!this.events.equals(events)) {
				for (Event event: events){
					if (DateTime.now().isBefore(event.getExpiryTime())){
						this.events.add(event);
						trickleReset = true;
					}
				}
			}
		}
		if (trickleReset) {
			trickle.reset();
		} else {
			trickle.incr();
		}
		Event[] eventArray;
		synchronized (this.events) {
			eventArray = this.events.toArray(new Event[0]);
		}
		for (Event event : eventArray) {
			if (!event.isTriggered()) {
				trigger(event);
			}
		}
	}

	/**
	 * Gets the trickle.
	 *
	 * @return the trickle
	 */
	@Namespace("trickle")
	public TrickleRPC getTrickle() {
		return trickle;
	}
}
