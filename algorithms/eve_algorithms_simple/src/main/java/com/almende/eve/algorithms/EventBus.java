/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.algorithms;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
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
	private static final Logger	LOG			= Logger.getLogger(EventBus.class
													.getName());
	private Scheduler			scheduler	= null;
	private TrickleRPC			trickle		= null;
	private Caller				caller		= null;
	private Graph				neighbors	= null;
	private String				tag			= null;
	private Set<Event>			events		= new HashSet<Event>();

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
		scheduler.schedule(new JSONRequest("event.scheduleExpiry", null),
				DateTime.now().plus(60000));
	}

	/**
	 * Expire old events.
	 */
	@Access(AccessType.PUBLIC)
	public void doExpiry() {
		synchronized (events) {
			Iterator<Event> iter = events.iterator();
			while (iter.hasNext()) {
				Event event = iter.next();
				if (event.getExpiryTime() < DateTime.now().getMillis()) {
					if (!event.isTriggered()) {
						trigger(event);
					}
					iter.remove();
				}
			}
		}
	}

	/**
	 * Schedule trigger.
	 */
	@Access(AccessType.PUBLIC)
	public void scheduleTrigger() {
		doTriggers();
		scheduler.schedule(new JSONRequest("event.doTriggers", null), DateTime
				.now().plus(5000));
	}

	/**
	 * Do triggers.
	 */
	@Access(AccessType.PUBLIC)
	public void doTriggers() {
		synchronized (events) {
			for (Event event : events) {
				if (!event.isTriggered()) {
					trigger(event);
				}
			}
		}
	}

	private void trigger(final Event event) {
		synchronized (event) {
			if (!event.isTriggered()) {
				event.setTriggered(true);
				scheduler.schedule(event.getMessage(), 0);
			}
		}
	}

	private void setupGossip() {
		final ObjectNode config = JOM.createObjectNode();
		config.put("intervalFactor", 16);
		config.put("intervalMin", 100);
		config.put("redundancyFactor", 4);
		config.put("namespace", "event.");

		trickle = new TrickleRPC(config, scheduler, new Runnable() {
			@Override
			public void run() {}
		}, new Runnable() {
			@Override
			public void run() {
				final Edge[] neighborArray = neighbors.getByTag(tag);
				for (Edge neighbor : neighborArray) {
					final Params params = new Params();
					params.add("events", events);
					try {
						caller.call(neighbor.getAddress(),
								"event.receiveEvents", params);
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
		synchronized (this.events) {
			if (this.events.equals(events)) {
				trickle.incr();
			} else {
				this.events.addAll(events);
				trickle.reset();
			}
		}
		synchronized (this.events) {
			for (Event event : this.events) {
				if (!event.isTriggered()) {
					trigger(event);
				}
			}
		}
	}

	/**
	 * Gets the trickle.
	 *
	 * @return the trickle
	 */
	@Namespace("*")
	public TrickleRPC getTrickle() {
		return trickle;
	}
}
