/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.algorithms.test;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.junit.Test;

import com.almende.eve.agent.AgentConfig;
import com.almende.eve.algorithms.test.agents.EventAgent;
import com.almende.eve.state.memory.MemoryStateConfig;

/**
 * The Class TestEventBus.
 */
public class TestEventBus extends TestCase {
	private static final Logger	LOG	= Logger.getLogger(TestEventBus.class
											.getName());

	/**
	 * Test events.
	 */
	@Test
	public void testEvents() {
		final int nofAgents = 5000;
		final int pause = 10000;

		LOG.warning("Starting with:" + nofAgents + " agents");
		DateTime timestamp = DateTime.now();

		final AgentConfig config = new AgentConfig();
		config.setClassName(EventAgent.class.getName());
		config.setState(new MemoryStateConfig());

		final List<EventAgent> agents = new ArrayList<EventAgent>(nofAgents);
		for (int i = 0; i < nofAgents; i++) {
			agents.add(new EventAgent("" + i, config));
		}

		LOG.warning("Network generation in:"
				+ (new Duration(timestamp, DateTime.now()).getMillis()) + " ms");

		/*
		 * LOG.warning("Pausing for a minute!");
		 * try {
		 * Thread.sleep(60000);
		 * } catch (InterruptedException e) {
		 * // TODO Auto-generated catch block
		 * e.printStackTrace();
		 * }
		 * LOG.warning("Going forward again!");
		 * start.sendEvent("report_*");
		 * timestamp = DateTime.now();
		 * while (start.countReceived(nofAgents) < nofAgents) {
		 * try {
		 * Thread.sleep(10);
		 * } catch (InterruptedException e) {
		 * // TODO Auto-generated catch block
		 * e.printStackTrace();
		 * }
		 * }
		 * LOG.warning("All agents ("+nofAgents+") reporting on event in:"
		 * + (new Duration(timestamp, DateTime.now()).getMillis()) + " ms");
		 */

		LOG.warning("Pausing for:" + pause + "ms!");
		try {
			Thread.sleep(pause);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		LOG.warning("Going forward again!");

		int i = 10;
		long[] results = new long[i];
		for (int j = 0; j < i; j++) {
			int agentId = (int) Math.floor(Math.random() * nofAgents);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			timestamp = DateTime.now();
			agents.get(0).sendEvent("report_" + agentId);

			while (agents.get(0).countReceived(nofAgents) < 1) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			long duration = (new Duration(timestamp, DateTime.now())
					.getMillis());
			LOG.warning("Single agent (#" + agentId
					+ ") reporting on event in:" + duration + " ms");
			results[j] = duration;
		}
		long res = 0;
		for (long j : results) {
			res += j;
		}
		LOG.warning("average time:" + res / i + " ms");
	}
}
