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
import com.almende.eve.algorithms.simulation.SimulationInboxProtocolConfig;
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
	public void testSchedulingStrong() throws IOException {
		final SimulationSchedulerConfig params = new SimulationSchedulerConfig();
		params.setSenderUrl("local:testSim1s");
		// params.setStrongConsistency(true);

		final AgentConfig config = new AgentConfig();
		config.setClassName(TestSchedulingAgent.class.getName());
		config.put("id", "testSim1s");

		final ArrayNode protocols = JOM.createArrayNode();
		final SimulationProtocolConfig simprot = new SimulationProtocolConfig();
		final SimulationInboxProtocolConfig simInprot = new SimulationInboxProtocolConfig();
		simInprot.setSupportSynccalls(true);

		final TraceProtocolConfig traceprot = new TraceProtocolConfig();
		traceprot.setFileName(".");
		protocols.add(simprot);
		protocols.add(simInprot);
		// protocols.add(traceprot);
		// protocols.add(traceprot);
		config.setProtocols(protocols);
		config.setScheduler(params);

		final AgentConfig config2 = new AgentConfig();
		config2.setClassName(TestSchedulingAgent.class.getName());
		config2.put("id", "testSim2s");
		final SimulationSchedulerConfig params2 = new SimulationSchedulerConfig();
		params2.setSenderUrl("local:testSim2s");
		// params2.setStrongConsistency(true);

		final ArrayNode protocols2 = JOM.createArrayNode();
		final SimulationProtocolConfig simprot2 = new SimulationProtocolConfig();
		final SimulationInboxProtocolConfig simInprot2 = new SimulationInboxProtocolConfig();
		simInprot2.setSupportSynccalls(true);

		final TraceProtocolConfig traceprot2 = new TraceProtocolConfig();
		traceprot2.setFileName(".");
		// protocols2.add(traceprot2);
		protocols2.add(simprot2);
		protocols2.add(simInprot2);
		// protocols2.add(traceprot2);

		config2.setProtocols(protocols2);
		config2.setScheduler(params2);
		new AgentBuilder().withConfig(config2).build();

		TestSchedulingAgent agent = (TestSchedulingAgent) new AgentBuilder()
				.withConfig(config).build();
		agent.startLocal();
		agent.startRemote("local:testSim2s");

		agent.scheduleStop(6000000);
		agent.start();

		try {
			Thread.sleep(5000);
		} catch (final InterruptedException e) {}

		assertTrue(agent.isStop());
	}

	/**
	 * Test scheduling.
	 *
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void testSchedulingWeak() throws IOException {
		final SimulationSchedulerConfig params = new SimulationSchedulerConfig();
		params.setSenderUrl("local:testSim1w");
		params.setStrongConsistency(false);

		final AgentConfig config = new AgentConfig();
		config.setClassName(TestSchedulingAgent.class.getName());
		config.put("id", "testSim1w");

		final ArrayNode protocols = JOM.createArrayNode();
		final SimulationProtocolConfig simprot = new SimulationProtocolConfig();

		final TraceProtocolConfig traceprot = new TraceProtocolConfig();
		traceprot.setFileName(".");
		protocols.add(simprot);
		protocols.add(traceprot);
		// protocols.add(traceprot);
		config.setProtocols(protocols);
		config.setScheduler(params);

		final AgentConfig config2 = new AgentConfig();
		config2.setClassName(TestSchedulingAgent.class.getName());
		config2.put("id", "testSim2w");
		final SimulationSchedulerConfig params2 = new SimulationSchedulerConfig();
		params2.setSenderUrl("local:testSim2w");
		params2.setStrongConsistency(false);

		final ArrayNode protocols2 = JOM.createArrayNode();
		final SimulationProtocolConfig simprot2 = new SimulationProtocolConfig();

		final TraceProtocolConfig traceprot2 = new TraceProtocolConfig();
		traceprot2.setFileName(".");
		// protocols2.add(traceprot2);
		protocols2.add(simprot2);
		protocols2.add(traceprot2);

		config2.setProtocols(protocols2);
		config2.setScheduler(params2);
		new AgentBuilder().withConfig(config2).build();

		TestSchedulingAgent agent = (TestSchedulingAgent) new AgentBuilder()
				.withConfig(config).build();
		agent.startLocal();
		agent.startRemote("local:testSim2w");

		agent.scheduleStop(6000000);
		agent.start();

		try {
			Thread.sleep(5000);
		} catch (final InterruptedException e) {}

		assertTrue(agent.isStop());
	}
}
