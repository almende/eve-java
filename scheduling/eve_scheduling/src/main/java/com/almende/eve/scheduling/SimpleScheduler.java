/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.scheduling;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.joda.time.DateTime;

import com.almende.eve.capabilities.handler.Handler;
import com.almende.eve.protocol.jsonrpc.formats.Caller;
import com.almende.eve.scheduling.clock.Clock;
import com.almende.eve.scheduling.clock.RunnableClock;
import com.almende.util.uuid.UUID;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class SimpleScheduler.
 */
public class SimpleScheduler implements Scheduler {
	private static final Logger	LOG			= Logger.getLogger(SimpleScheduler.class
													.getName());
	protected URI				myUrl		= null;
	protected Handler<Caller>	handle		= null;
	protected Clock				clock		= null;
	protected ObjectNode		myParams	= null;

	/**
	 * Instantiates a new abstract scheduler.
	 * 
	 * @param params
	 *            the params
	 * @param handle
	 *            the handle
	 */
	public SimpleScheduler(final ObjectNode params, final Handler<Caller> handle) {
		this.handle = handle;
		myUrl = handle.get().getSenderUrlByScheme("local");
		myParams = params;
	}

	protected void handleTrigger(final Object msg, final String triggerId) {
//		LOG.warning("Handle trigger:" + triggerId + " : " + msg.toString());
		try {
			handle.get().call(myUrl, msg);
		} catch (IOException e) {
			LOG.log(Level.WARNING,
					"Scheduler got IOException, couldn't send request", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.almende.eve.scheduling.Scheduler#schedule(java.lang.Object,
	 * org.joda.time.DateTime)
	 */
	@Override
	public String schedule(final String triggerId, final Object msg,
			final DateTime due) {
		if (clock == null) {
			clock = new RunnableClock();
		}
		final String uuid = triggerId != null ? triggerId : new UUID()
				.toString();

		// LOG.warning("Requesting trigger:" + uuid + " : " + msg.toString()
		// + " at: " + due);
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
	 * Gets the handle.
	 * 
	 * @return the handle
	 */
	public Handler<Caller> getHandle() {
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
	public String schedule(final String id, final Object msg, final int delay) {
		return schedule(id, msg, nowDateTime().plus(delay));
	}

	@Override
	public String schedule(final String id, final Object msg, final long delay) {
		return schedule(id, msg, nowDateTime().plus(delay));
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
