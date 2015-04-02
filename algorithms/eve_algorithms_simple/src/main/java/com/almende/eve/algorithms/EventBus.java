/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.algorithms;

import java.io.IOException;
import java.net.URI;
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
	private URI[]				neighbors	= null;
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
	 */
	public EventBus(Scheduler scheduler, Caller caller, URI[] neighbors) {
		this.scheduler = scheduler;
		this.caller = caller;
		this.neighbors = neighbors;
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
	public void sendEvent(Object message) {
		final Event event = new Event(DateTime.now().plus(60000).getMillis(),
				message, caller.getSenderUrls().get(0));
		synchronized (events) {
			events.add(event);
		}
	}

	/**
	 * Schedule expiry.
	 */
	@Access(AccessType.PUBLIC)
	public void scheduleExpiry() {
		doExpiry();
		scheduler.schedule("event.scheduleExpiry", DateTime.now().plus(60000));
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
		scheduler.schedule("event.doTriggers", DateTime.now().plus(5000));
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
				scheduler.schedule(event.getMessage(), 0);
				event.setTriggered(true);
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
				for (URI neighbor : neighbors) {
					final Params params = new Params();
					params.add("events", events);
					try {
						caller.call(neighbor, "receiveEvents", params);
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
		if (this.events.equals(events)) {
			trickle.incr();
		} else {
			events.addAll(events);
			trickle.reset();
			for (Event event : events) {
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
	@Namespace("trickle")
	public TrickleRPC getTrickle() {
		return trickle;
	}
}
