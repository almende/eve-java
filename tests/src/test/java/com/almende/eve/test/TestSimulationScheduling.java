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
import com.almende.eve.algorithms.simulation.SimulationSchedulerConfig;
import com.almende.eve.algorithms.simulation.SimulationTimeProtocolConfig;
import com.almende.eve.protocol.TraceProtocolConfig;
import com.almende.util.jackson.JOM;
import com.fasterxml.jackson.databind.node.ArrayNode;

/**
 * The Class TestScheduling.
 */
public class TestSimulationScheduling extends TestCase {

	private void run(boolean strong, String names) {
		final boolean supportSyncCalls = strong;
		final boolean atomicNetwork = strong;

		final SimulationSchedulerConfig params = SimulationSchedulerConfig
				.create();
		params.setStrongConsistency(strong);

		final AgentConfig config = AgentConfig.create();
		config.setClassName(TestSchedulingAgent.class.getName());
		config.put("id", names + "1");

		final ArrayNode protocols = JOM.createArrayNode();
		final SimulationTimeProtocolConfig simprot = SimulationTimeProtocolConfig
				.create();
		final SimulationInboxProtocolConfig simInprot = SimulationInboxProtocolConfig
				.create();
		simInprot.setSupportSynccalls(supportSyncCalls);
		simInprot.setAtomicNetwork(atomicNetwork);

		final TraceProtocolConfig traceprot = TraceProtocolConfig.create();
		traceprot.setFileName(".");
		// protocols.add(traceprot);

		protocols.add(simprot);
		if (strong) {
			protocols.add(simInprot);
		}
		config.setProtocols(protocols);
		config.setScheduler(params);

		final AgentConfig config2 = AgentConfig.create();
		config2.setClassName(TestSchedulingAgent.class.getName());
		config2.put("id", names + "2");
		final SimulationSchedulerConfig params2 = SimulationSchedulerConfig
				.create();
		params2.setStrongConsistency(strong);

		final ArrayNode protocols2 = JOM.createArrayNode();
		final SimulationTimeProtocolConfig simprot2 = SimulationTimeProtocolConfig
				.create();
		final SimulationInboxProtocolConfig simInprot2 = SimulationInboxProtocolConfig
				.create();
		simInprot2.setSupportSynccalls(supportSyncCalls);
		simInprot2.setAtomicNetwork(atomicNetwork);

		final TraceProtocolConfig traceprot2 = TraceProtocolConfig.create();
		traceprot2.setFileName(".");
		// protocols2.add(traceprot2);

		protocols2.add(simprot2);
		if (strong) {
			protocols2.add(simInprot2);
		}

		config2.setProtocols(protocols2);
		config2.setScheduler(params2);
		new AgentBuilder().withConfig(config2).build();

		TestSchedulingAgent agent = (TestSchedulingAgent) new AgentBuilder()
				.withConfig(config).build();
		agent.startLocal();
		agent.startRemote("local:" + names + "2");

		agent.scheduleStop(6000000);
		agent.start();

		int count = 0;
		try {
			while (!agent.isStop() && count < 100) {
				count++;
				Thread.sleep(500);
			}
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
	public void testSchedulingStrong() throws IOException {
		run(true, "testStrong");
	}

	/**
	 * Test scheduling.
	 *
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void testSchedulingWeak() throws IOException {
		run(true, "testWeak");
	}
}
