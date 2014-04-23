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
import com.fasterxml.jackson.databind.JsonNode;

/**
 * The Class SimpleScheduler.
 */
public class SimpleScheduler implements Scheduler {
	private static final Logger	LOG				= Logger.getLogger(SimpleScheduler.class
														.getName());
	private URI					schedulerUrl	= null;
	private Handler<Receiver>	handle			= null;
	protected Clock				clock			= null;
	
	/**
	 * Instantiates a new abstract scheduler.
	 * 
	 * @param params
	 *            the params
	 * @param handle
	 *            the handle
	 */
	public SimpleScheduler(final JsonNode params, final Handler<Receiver> handle) {
		if (params.has("url")) {
			try {
				this.schedulerUrl = new URI(params.get("url").asText());
			} catch (URISyntaxException e) {
				LOG.log(Level.WARNING,
						"Couln't parse scheduler url from parameters.", e);
			}
		} else {
			LOG.log(Level.WARNING, "Parameter 'url' is required, falling back to: 'local:unknown_scheduler'");
		}
		if (this.schedulerUrl == null) {
			try {
				this.schedulerUrl = new URI("local:unknown_scheduler");
			} catch (URISyntaxException e1) {
				LOG.log(Level.SEVERE, "", e1);
			}
		}
		this.handle = handle;
	}
	
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
	
	@Override
	public void cancel(String id) {
		if (clock == null) {
			clock = new RunnableClock();
			return;
		}
		clock.cancel(id);
	}
	
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
	
}
