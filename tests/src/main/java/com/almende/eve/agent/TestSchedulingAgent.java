/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */

package com.almende.eve.agent;

import java.util.logging.Logger;

import org.joda.time.DateTime;

import com.almende.eve.algorithms.simulation.SimulationScheduler;
import com.almende.eve.protocol.jsonrpc.annotation.Access;
import com.almende.eve.protocol.jsonrpc.annotation.AccessType;
import com.almende.eve.protocol.jsonrpc.annotation.Namespace;
import com.almende.eve.protocol.jsonrpc.formats.JSONRequest;
import com.almende.eve.scheduling.Scheduler;

/**
 * The Class TestSchedulingAgent.
 */
@Access(AccessType.PUBLIC)
public class TestSchedulingAgent extends Agent {
	private static final Logger LOG = Logger
			.getLogger(TestSchedulingAgent.class.getName());
	private boolean stop = false;
	
	/**
	 * Do task.
	 */
	public void doTask(){
		LOG.info("Doing something at:"+getScheduler().nowDateTime()+"("+DateTime.now()+")");
	}
	
	/**
	 * Schedule task.
	 */
	public void scheduleTask(){
		if (!stop){
			getScheduler().schedule(new JSONRequest("scheduleTask",null), 600000);
			doTask();
		}
	}
	
	/**
	 * Do stop.
	 */
	public void doStop(){
		LOG.info("Stopping at:"+getScheduler().nowDateTime()+"("+DateTime.now()+")");
		stop=true;
	}

	/**
	 * Start.
	 */
	public void start(){
		((SimulationScheduler)getScheduler()).start();
	}
	
	/**
	 * Schedule stop.
	 *
	 * @param stopDelay
	 *            the stop delay
	 */
	public void scheduleStop(final long stopDelay){
		getScheduler().schedule(new JSONRequest("doStop",null), stopDelay);
	}
	
	@Namespace("scheduler")
	public Scheduler getScheduler(){
		return super.getScheduler();
	}
	
}
