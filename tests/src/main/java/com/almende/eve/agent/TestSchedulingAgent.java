/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */

package com.almende.eve.agent;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.joda.time.DateTime;

import com.almende.eve.algorithms.simulation.SimulationScheduler;
import com.almende.eve.protocol.jsonrpc.annotation.Access;
import com.almende.eve.protocol.jsonrpc.annotation.AccessType;
import com.almende.eve.protocol.jsonrpc.annotation.Namespace;
import com.almende.eve.protocol.jsonrpc.formats.JSONRequest;
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
				+ getScheduler().nowDateTime() + "(" + DateTime.now() + ")");
	}

	/**
	 * Do task.
	 *
	 * @return the string
	 */
	public String doTaskWithReply() {
		LOG.info(getId() + ": Doing something with reply at:"
				+ getScheduler().nowDateTime() + "(" + DateTime.now() + ")");
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
			getScheduler().schedule(new JSONRequest("scheduleTask", null),
					600000);
			doTask();
		}
	}

	/**
	 * Start remote.
	 */
	public void startRemote() {
		getScheduler().schedule(
				new JSONRequest("scheduleRemoteTask", null), 600000);
	}
	
	/**
	 * Start local.
	 */
	public void startLocal(){
		getScheduler().schedule(new JSONRequest("scheduleTask", null),
				600000);
	}
	/**
	 * Schedule task.
	 *
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void scheduleRemoteTask() throws IOException {
		if (!stop) {
			getScheduler().schedule(
					new JSONRequest("scheduleRemoteTask", null), 600000);
			call(URIUtil.create("local:testSim2"), "doTaskWithReply", null,
					new AsyncCallback<String>() {

						@Override
						public void onSuccess(String result) {
							LOG.info("Got async reply:" + result + " at:"
									+ getScheduler().nowDateTime() + "("
									+ DateTime.now() + ")");
						}

						@Override
						public void onFailure(Exception exception) {
							LOG.log(Level.WARNING, "Got Failure at:"
									+ getScheduler().nowDateTime() + "("
									+ DateTime.now() + ")", exception);
						}
					});
			LOG.info("Got reply:"
					+ callSync(URIUtil.create("local:testSim2"),
							"doTaskWithReply", null, String.class) + " at:"
					+ getScheduler().nowDateTime() + "(" + DateTime.now() + ")");

			call(URIUtil.create("local:testSim2"), "doTask", null);

		}
	}

	/**
	 * Do stop.
	 */
	public void doStop() {
		LOG.info("Stopping at:" + getScheduler().nowDateTime() + "("
				+ DateTime.now() + ")");
		stop = true;
	}

	/**
	 * Start.
	 */
	public void start() {
		((SimulationScheduler) getScheduler()).start();
	}

	/**
	 * Schedule stop.
	 *
	 * @param stopDelay
	 *            the stop delay
	 */
	public void scheduleStop(final long stopDelay) {
		getScheduler().schedule(new JSONRequest("doStop", null), stopDelay);
	}

	@Namespace("scheduler")
	public Scheduler getScheduler() {
		return super.getScheduler();
	}

}
