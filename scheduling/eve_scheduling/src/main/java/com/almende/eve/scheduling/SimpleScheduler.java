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
import com.almende.util.URIUtil;
import com.almende.util.uuid.UUID;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class SimpleScheduler.
 */
public class SimpleScheduler implements Scheduler {
	private static final Logger	LOG				= Logger.getLogger(SimpleScheduler.class
														.getName());
	protected URI				schedulerUrl	= null;
	protected Handler<Receiver>	handle			= null;
	protected Clock				clock			= null;
	protected ObjectNode		myParams		= null;

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
				schedulerUrl = URIUtil.parse(params.get("senderUrl").asText());
			} catch (final URISyntaxException e) {
				LOG.log(Level.WARNING,
						"Couldn't parse scheduler senderUrl from parameters.",
						e);
			}
		}
		if (schedulerUrl == null) {
			schedulerUrl = URIUtil.create("local:unnamed_scheduler");
		}
		this.handle = handle;
		myParams = params;
	}

	protected void handleTrigger(final Object msg, final String triggerId) {
		handle.get().receive(msg, schedulerUrl, null);
	}

	/*
	 * (non-Javadoc)
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
				handleTrigger(msg, uuid);
			}

		});
		return uuid;
	}

	/*
	 * (non-Javadoc)
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
	protected Clock getClock() {
		return clock;
	}

	/**
	 * @param clock
	 *            the clock to set
	 */
	protected void setClock(Clock clock) {
		this.clock = clock;
	}

	/*
	 * (non-Javadoc)
	 * @see com.almende.eve.capabilities.Capability#getParams()
	 */
	@Override
	public ObjectNode getParams() {
		return myParams;
	}

	@Override
	public String schedule(Object msg, int delay) {
		return schedule(msg, nowDateTime().plus(delay));
	}

	@Override
	public String schedule(Object msg, long delay) {
		return schedule(msg, nowDateTime().plus(delay));
	}

	@Override
	public long now() {
		if (clock == null) {
			clock = new RunnableClock();
		}
		return clock.now();
	}

	@Override
	public DateTime nowDateTime() {
		if (clock == null) {
			clock = new RunnableClock();
		}
		return clock.nowDateTime();
	}

	@Override
	public void delete() {
		clear();
		SimpleSchedulerConfig config = SimpleSchedulerConfig
				.decorate(getParams());
		SimpleSchedulerBuilder.delete(config.getId());
	}

}
