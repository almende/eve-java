/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.test;

import java.io.IOException;

import junit.framework.TestCase;

import org.junit.Test;

import com.almende.eve.agent.AgentBuilder;
import com.almende.eve.agent.AgentConfig;
import com.almende.eve.agent.TestSchedulingAgent;
import com.almende.eve.algorithms.simulation.SimulationProtocolConfig;
import com.almende.eve.algorithms.simulation.SimulationSchedulerConfig;
import com.almende.eve.protocol.TraceProtocolConfig;
import com.almende.util.jackson.JOM;
import com.fasterxml.jackson.databind.node.ArrayNode;

/**
 * The Class TestScheduling.
 */
public class TestSimulationScheduling extends TestCase {

	/**
	 * Test scheduling.
	 *
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void testScheduling() throws IOException {
		final SimulationSchedulerConfig params = new SimulationSchedulerConfig();
		params.setSenderUrl("local:testSim1");

		final AgentConfig config = new AgentConfig();
		config.setClassName(TestSchedulingAgent.class.getName());
		config.put("id", "testSim1");
		config.setScheduler(params);

		final ArrayNode protocols = JOM.createArrayNode();
		final SimulationProtocolConfig simprot = new SimulationProtocolConfig();
		final TraceProtocolConfig traceprot = new TraceProtocolConfig();
		traceprot.setFileName(".");
		protocols.add(traceprot);
		protocols.add(simprot);
//		protocols.add(traceprot);
		config.setProtocols(protocols);

		final AgentConfig config2 = new AgentConfig();
		config2.setClassName(TestSchedulingAgent.class.getName());
		config2.put("id", "testSim2");
		final SimulationSchedulerConfig params2 = new SimulationSchedulerConfig();
		params2.setSenderUrl("local:testSim2");
		config2.setScheduler(params2);

		final ArrayNode protocols2 = JOM.createArrayNode();
		final SimulationProtocolConfig simprot2 = new SimulationProtocolConfig();
		final TraceProtocolConfig traceprot2 = new TraceProtocolConfig();
		traceprot2.setFileName(".");
//		protocols2.add(traceprot2);
		protocols2.add(simprot2);
		protocols2.add(traceprot2);
		
		config2.setProtocols(protocols2);
		new AgentBuilder().withConfig(config2).build();

		TestSchedulingAgent agent = (TestSchedulingAgent) new AgentBuilder()
				.withConfig(config).build();
		agent.startLocal();
		agent.startRemote();

		agent.scheduleStop(6000000);
		agent.start();

		try {
			Thread.sleep(5000);
		} catch (final InterruptedException e) {}
		
		assertTrue(agent.isStop());
	}
}
