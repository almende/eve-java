/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.test;

import junit.framework.TestCase;

import org.junit.Test;

import com.almende.eve.agent.AgentBuilder;
import com.almende.eve.agent.AgentConfig;
import com.almende.eve.agent.SchedulingAgent;
import com.almende.eve.scheduling.SimpleSchedulerConfig;

/**
 * The Class TestScheduling.
 */
public class TestScheduling extends TestCase {

	/**
	 * Test scheduling.
	 */
	@Test
	public void testScheduling() {
		final SimpleSchedulerConfig params = SimpleSchedulerConfig
				.create();
		params.setId("TestScheduler");

		final AgentConfig agentconf = AgentConfig.create("testScheduling");
		agentconf.setScheduler(params);
		agentconf.setClassName(SchedulingAgent.class.getName());

		new AgentBuilder().withConfig(agentconf).build();

		try {
			Thread.sleep(11000);
		} catch (final InterruptedException e) {}

	}
}
