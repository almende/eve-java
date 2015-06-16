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
import com.almende.eve.algorithms.simulation.SimulationTimeProtocolConfig;
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
		final boolean supportSyncCalls = true;
		final boolean atomicNetwork = true;

		final SimulationSchedulerConfig params = SimulationSchedulerConfig
				.create();
		params.setSenderUrl("local:testSim1s");

		final AgentConfig config = AgentConfig.create();
		config.setClassName(TestSchedulingAgent.class.getName());
		config.put("id", "testSim1s");

		final ArrayNode protocols = JOM.createArrayNode();
		final SimulationTimeProtocolConfig simprot = SimulationTimeProtocolConfig
				.create();
		final SimulationInboxProtocolConfig simInprot = SimulationInboxProtocolConfig
				.create();
		simInprot.setSupportSynccalls(supportSyncCalls);
		simInprot.setAtomicNetwork(atomicNetwork);

		final TraceProtocolConfig traceprot = TraceProtocolConfig.create();
		traceprot.setFileName(".");
		protocols.add(simprot);
		protocols.add(simInprot);
		config.setProtocols(protocols);
		config.setScheduler(params);

		final AgentConfig config2 = AgentConfig.create();
		config2.setClassName(TestSchedulingAgent.class.getName());
		config2.put("id", "testSim2s");
		final SimulationSchedulerConfig params2 = SimulationSchedulerConfig
				.create();
		params2.setSenderUrl("local:testSim2s");

		final ArrayNode protocols2 = JOM.createArrayNode();
		final SimulationTimeProtocolConfig simprot2 = SimulationTimeProtocolConfig
				.create();
		final SimulationInboxProtocolConfig simInprot2 = SimulationInboxProtocolConfig
				.create();
		simInprot2.setSupportSynccalls(supportSyncCalls);
		simInprot2.setAtomicNetwork(atomicNetwork);

		final TraceProtocolConfig traceprot2 = TraceProtocolConfig.create();
		traceprot2.setFileName(".");
		protocols2.add(simprot2);
		protocols2.add(simInprot2);

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
		final SimulationSchedulerConfig params = SimulationSchedulerConfig
				.create();
		params.setSenderUrl("local:testSim1w");
		params.setStrongConsistency(false);

		final AgentConfig config = AgentConfig.create();
		config.setClassName(TestSchedulingAgent.class.getName());
		config.put("id", "testSim1w");

		final ArrayNode protocols = JOM.createArrayNode();
		final SimulationTimeProtocolConfig simprot = SimulationTimeProtocolConfig
				.create();

		final TraceProtocolConfig traceprot = TraceProtocolConfig.create();
		traceprot.setFileName(".");
		protocols.add(simprot);

		config.setProtocols(protocols);
		config.setScheduler(params);

		final AgentConfig config2 = AgentConfig.create();
		config2.setClassName(TestSchedulingAgent.class.getName());
		config2.put("id", "testSim2w");
		final SimulationSchedulerConfig params2 = SimulationSchedulerConfig
				.create();
		params2.setSenderUrl("local:testSim2w");
		params2.setStrongConsistency(false);

		final ArrayNode protocols2 = JOM.createArrayNode();
		final SimulationTimeProtocolConfig simprot2 = SimulationTimeProtocolConfig
				.create();

		final TraceProtocolConfig traceprot2 = TraceProtocolConfig.create();
		traceprot2.setFileName(".");
		protocols2.add(simprot2);

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
