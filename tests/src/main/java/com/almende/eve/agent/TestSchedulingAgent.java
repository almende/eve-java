/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */

package com.almende.eve.agent;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.joda.time.DateTime;

import com.almende.eve.algorithms.simulation.SimulationScheduler;
import com.almende.eve.protocol.jsonrpc.annotation.Access;
import com.almende.eve.protocol.jsonrpc.annotation.AccessType;
import com.almende.eve.protocol.jsonrpc.annotation.Name;
import com.almende.eve.protocol.jsonrpc.annotation.Namespace;
import com.almende.eve.protocol.jsonrpc.formats.JSONRequest;
import com.almende.eve.protocol.jsonrpc.formats.Params;
import com.almende.eve.scheduling.Scheduler;
import com.almende.util.URIUtil;
import com.almende.util.callback.AsyncCallback;

/**
 * The Class TestSchedulingAgent.
 */
@Access(AccessType.PUBLIC)
public class TestSchedulingAgent extends Agent {
	private static final Logger	LOG		= Logger.getLogger(TestSchedulingAgent.class
												.getName());
	private boolean				stop	= false;

	/**
	 * Do task.
	 */
	public void doTask() {
		LOG.info(getId() + ": Doing something at:"
				+ getScheduler().nowDateTime());
	}

	/**
	 * Do task.
	 *
	 * @return the string
	 */
	public String doTaskWithReply() {
		LOG.info(getId() + ": Doing something with reply at:"
				+ getScheduler().nowDateTime());
		return "reply!";
	}

	/**
	 * Schedule task.
	 *
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void scheduleTask() throws IOException {
		if (!stop) {
			schedule(new JSONRequest("scheduleTask", null), 600000);
			doTask();
		}
	}

	/**
	 * Start remote.
	 *
	 * @param remote
	 *            the remote
	 */
	public void startRemote(final String remote) {
		final Params param = new Params();
		final URI other = URIUtil.create(remote);
		param.add("other", other);
		schedule(new JSONRequest("scheduleRemoteTask", param), 600000);
	}

	/**
	 * Start local.
	 */
	public void startLocal() {
		schedule(new JSONRequest("scheduleTask", null), 600000);
	}

	/**
	 * Schedule task.
	 *
	 * @param other
	 *            the other
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void scheduleRemoteTask(final @Name("other") URI other)
			throws IOException {
		if (!stop) {
			final Params param = new Params();
			param.add("other", other);
			schedule(new JSONRequest("scheduleRemoteTask", param), 600000);
			call(other, "doTaskWithReply", null, new AsyncCallback<String>() {

				@Override
				public void onSuccess(String result) {
					LOG.info("Got async reply:" + result + " at:"
							+ getScheduler().nowDateTime());
				}

				@Override
				public void onFailure(Exception exception) {
					LOG.log(Level.WARNING, "Got Failure at:"
							+ getScheduler().nowDateTime(), exception);
				}
			});
			LOG.info("Got reply:"
					+ callSync(other, "doTaskWithReply", null, String.class)
					+ " at:" + getScheduler().nowDateTime());

			call(other, "doTask", null);

		}
	}

	/**
	 * Do stop.
	 */
	public void doStop() {
		stop = true;
		LOG.info("Stopped at:" + getScheduler().nowDateTime() + "("
				+ DateTime.now() + ")");
	}

	/**
	 * Start.
	 */
	public void start() {
		LOG.info("Starting at:" + getScheduler().nowDateTime() + "("
				+ DateTime.now() + ")");
		((SimulationScheduler) getScheduler()).start();
	}

	/**
	 * Schedule stop.
	 *
	 * @param stopDelay
	 *            the stop delay
	 */
	public void scheduleStop(final long stopDelay) {
		schedule(new JSONRequest("doStop", null), stopDelay);
	}

	@Namespace("scheduler")
	public Scheduler getScheduler() {
		return super.getScheduler();
	}

	/**
	 * Checks if is stop.
	 *
	 * @return true, if is stop
	 */
	public boolean isStop() {
		return stop;
	}

}
