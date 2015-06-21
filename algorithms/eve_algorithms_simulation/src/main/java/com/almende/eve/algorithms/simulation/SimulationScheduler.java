/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.algorithms.simulation;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.almende.eve.capabilities.handler.Handler;
import com.almende.eve.protocol.jsonrpc.annotation.Access;
import com.almende.eve.protocol.jsonrpc.annotation.AccessType;
import com.almende.eve.protocol.jsonrpc.annotation.Name;
import com.almende.eve.protocol.jsonrpc.formats.Caller;
import com.almende.eve.protocol.jsonrpc.formats.JSONMessage;
import com.almende.eve.scheduling.SimpleScheduler;
import com.almende.eve.scheduling.clock.Clock;
import com.almende.util.jackson.JOM;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class SimulationScheduler.
 */
public class SimulationScheduler extends SimpleScheduler {
	private static final Logger	LOG			= Logger.getLogger(SimulationScheduler.class
													.getName());
	private static Clock		sharedClock	= null;

	/**
	 * Instantiates a new simulation scheduler.
	 *
	 * @param params
	 *            the params
	 * @param handle
	 *            the handle
	 */
	public SimulationScheduler(ObjectNode params, Handler<Caller> handle) {
		super(params, handle);
		if (sharedClock == null) {
			sharedClock = new SimulationClock(0);
		}
		clock = sharedClock;
	}

	/**
	 * Start.
	 */
	public void start() {
		clock.start();
	}

	/**
	 * Receive tracer report.
	 *
	 * @param tracer
	 *            the tracer
	 */
	@Access(AccessType.PUBLIC)
	public void receiveTracerReport(final @Name("tracer") Tracer tracer) {
		clock.done(tracer.getId());
	}

	private Tracer createTracer() {
		final Tracer tracer = new Tracer();
		tracer.setOwner(myUrl);
		return tracer;
	}

	@Override
	protected void handleTrigger(final Object msg, final String triggerId) {
		JSONMessage message = JSONMessage.jsonConvert(msg);
		if (message != null) {
			final Tracer tracer = createTracer();
			tracer.setId(triggerId);
			final ObjectNode extra = JOM.createObjectNode();
			extra.set("@simtracer", JOM.getInstance().valueToTree(tracer));
			if (message.getExtra() == null) {
				message.setExtra(extra);
			} else {
				message.getExtra().setAll(extra);
			}
			try {
				handle.get().call(myUrl, msg);
			} catch (IOException e) {
				LOG.log(Level.WARNING,
						"Scheduler got IOException, couldn't send request", e);
			}
		} else {
			LOG.warning("Scheduler tries to send Non-JSON-RPC message, doesn't work with SimulationScheduler.");
			try {
				handle.get().call(myUrl, msg);
			} catch (IOException e) {
				LOG.log(Level.WARNING,
						"Scheduler got IOException, couldn't send request", e);
			}
		}

	}

	@Override
	public void delete() {
		clear();
		SimulationSchedulerConfig config = SimulationSchedulerConfig
				.decorate(getParams());
		SimulationSchedulerBuilder.delete(config.getId());
	}

}
