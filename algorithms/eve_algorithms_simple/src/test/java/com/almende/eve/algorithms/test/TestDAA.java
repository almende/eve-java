/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.algorithms.test;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.junit.Test;

import com.almende.eve.algorithms.DAAValueBean;
import com.almende.eve.algorithms.test.agents.DAAAgent;

/**
 * The Class TestValueBean.
 */
public class TestDAA extends TestCase {
	private static final Logger	LOG	= Logger.getLogger(TestDAA.class.getName());

	/**
	 * Test keys.
	 */
	@Test
	public void testValueBean() {

		final int width = 1000;
		final double value = 125.0;
		DAAValueBean bean = new DAAValueBean(width, 10);
		bean.generate(value, 10);

		assertTrue(Math.abs(value - bean.computeSum()) < 0.1);
		assertEquals(new Integer(10), bean.getTtlArray()[15]);

		DAAValueBean bean2 = new DAAValueBean(width, 10);
		bean2.generate(value, 10);
		bean2.minimum(bean);

		double sum = Math.abs(bean2.computeSum() - 2 * bean.computeSum());
		LOG.warning("bean1:" + bean.computeSum() + " bean2:"
				+ bean2.computeSum() + " sum:" + sum + " ("
				+ (sum * 100.0 / bean2.computeSum()) + "%)");
	}

	/**
	 * Test agents.
	 *
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void testAgents() throws IOException {
		final DAAAgent[] agents = new DAAAgent[5];
		for (int i = 0; i < agents.length; i++) {
			final DAAAgent agent = new DAAAgent(i + "");
			agents[i] = agent;
		}
		
		int[][] edges = { { 0, 1 }, { 0, 4 }, { 1, 2 }, { 1, 3 }, { 2, 4 },
				{ 2, 1 }, { 3, 2 }, { 3, 0 }, { 4, 1 }, { 4, 2 } };

		for (int[] edge : edges) {
			agents[edge[0]].addNeighbor(URI.create("local:" + edge[1]));
		}
		
		for (final DAAAgent agent : agents){
			agent.start(1.0);
		}
		
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			LOG.log(Level.WARNING, "interrupted", e);
		}
		LOG.warning("Current estimate at 1:" + agents[1].getValue()+ " -> "+ Math.round(agents[1].getValue()));

		agents[3].changeValue(3.0);

		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			LOG.log(Level.WARNING, "interrupted", e);
		}
		LOG.warning("Current estimate at 1:" + agents[1].getValue()+ " -> "+ Math.round(agents[1].getValue()));
		
		agents[2].destroy();
		
		LOG.warning("agent config:"+agents[3].getConfig().toString());
		agents[3].doScheduleTest();
		agents[4].doScheduleTest();
		try {
			Thread.sleep(15000);
		} catch (InterruptedException e) {
			LOG.log(Level.WARNING, "interrupted", e);
		}
		LOG.warning("Current estimate at 1:" + agents[1].getValue()+ " -> "+ Math.round(agents[1].getValue()));
		
	}
}
