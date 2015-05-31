/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.test;

import junit.framework.TestCase;

import org.junit.Test;

import com.almende.eve.agent.AgentBuilder;
import com.almende.eve.agent.AgentConfig;
import com.almende.eve.agent.TestSchedulingAgent;
import com.almende.eve.algorithms.simulation.SimulationProtocolConfig;
import com.almende.eve.algorithms.simulation.SimulationSchedulerConfig;
import com.almende.util.jackson.JOM;
import com.fasterxml.jackson.databind.node.ArrayNode;

/**
 * The Class TestScheduling.
 */
public class TestSimulationScheduling extends TestCase {

	/**
	 * Test scheduling.
	 */
	@Test
	public void testScheduling() {
		final SimulationSchedulerConfig params = new SimulationSchedulerConfig();
		params.setSenderUrl("local:testSim1");

		final AgentConfig config = new AgentConfig();
		config.setClassName(TestSchedulingAgent.class.getName());
		config.put("id", "testSim1");
		config.setScheduler(params);

		final ArrayNode protocols = JOM.createArrayNode();
		final SimulationProtocolConfig simprot = new SimulationProtocolConfig();
		// final TraceProtocolConfig traceprot = new TraceProtocolConfig();
		// traceprot.setFileName(".");
		// protocols.add(traceprot);
		protocols.add(simprot);
		config.setProtocols(protocols);

		TestSchedulingAgent agent = (TestSchedulingAgent) new AgentBuilder()
				.withConfig(config).build();
		agent.scheduleTask();
		agent.scheduleStop(6000000);
		agent.start();

		try {
			Thread.sleep(5000);
		} catch (final InterruptedException e) {}

	}
}
