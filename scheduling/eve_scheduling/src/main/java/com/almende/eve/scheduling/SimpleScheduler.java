/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.scheduling;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.joda.time.DateTime;

import com.almende.eve.capabilities.handler.Handler;
import com.almende.eve.scheduling.clock.Clock;
import com.almende.eve.scheduling.clock.RunnableClock;
import com.almende.eve.transport.Receiver;
import com.almende.util.uuid.UUID;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class SimpleScheduler.
 */
public class SimpleScheduler implements Scheduler {
	private static final Logger	LOG				= Logger.getLogger(SimpleScheduler.class
														.getName());
	private URI					schedulerUrl	= null;
	private Handler<Receiver>	handle			= null;
	private Clock				clock			= null;
	private ObjectNode			myParams		= null;
	
	/**
	 * Instantiates a new abstract scheduler.
	 * 
	 * @param params
	 *            the params
	 * @param handle
	 *            the handle
	 */
	public SimpleScheduler(final ObjectNode params,
			final Handler<Receiver> handle) {
		if (params.has("senderUrl")) {
			try {
				schedulerUrl = new URI(params.get("senderUrl").asText());
			} catch (final URISyntaxException e) {
				LOG.log(Level.WARNING,
						"Couldn't parse scheduler senderUrl from parameters.",
						e);
			}
		}
		if (schedulerUrl == null) {
			try {
				schedulerUrl = new URI("local:unnamed_scheduler");
			} catch (final URISyntaxException e1) {
				LOG.log(Level.SEVERE, "", e1);
			}
		}
		this.handle = handle;
		myParams = params;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.almende.eve.scheduling.Scheduler#schedule(java.lang.Object,
	 * org.joda.time.DateTime)
	 */
	@Override
	public String schedule(final Object msg, final DateTime due) {
		if (clock == null) {
			clock = new RunnableClock();
		}
		final String uuid = new UUID().toString();
		clock.requestTrigger(uuid, due, new Runnable() {
			
			@Override
			public void run() {
				handle.get().receive(msg, schedulerUrl, null);
			}
			
		});
		return uuid;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.almende.eve.scheduling.Scheduler#cancel(java.lang.String)
	 */
	@Override
	public void cancel(final String id) {
		if (clock == null) {
			clock = new RunnableClock();
			return;
		}
		clock.cancel(id);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.almende.eve.scheduling.Scheduler#clear()
	 */
	@Override
	public void clear() {
		if (clock == null) {
			clock = new RunnableClock();
			return;
		}
		clock.clear();
	}
	
	/**
	 * Gets the scheduler url.
	 * 
	 * @return the scheduler url
	 */
	public URI getSchedulerUrl() {
		return schedulerUrl;
	}
	
	/**
	 * Gets the handle.
	 * 
	 * @return the handle
	 */
	public Handler<Receiver> getHandle() {
		return handle;
	}
	
	/**
	 * Gets the clock.
	 * 
	 * @return the clock
	 */
	public Clock getClock() {
		return clock;
	}
	
	/**
	 * @param clock the clock to set
	 */
	protected void setClock(Clock clock) {
		this.clock = clock;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.almende.eve.capabilities.Capability#getParams()
	 */
	@Override
	public ObjectNode getParams() {
		return myParams;
	}

	@Override
	public String schedule(Object msg, int delay) {
		return schedule(msg,DateTime.now().plus(delay));
	}

	@Override
	public long now() {
		return System.currentTimeMillis();
	}

	@Override
	public void delete() {
		clear();
		clock.clear();
	}
	
}
