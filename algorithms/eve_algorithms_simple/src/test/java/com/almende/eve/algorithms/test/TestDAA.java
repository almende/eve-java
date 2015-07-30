/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.algorithms.test;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.junit.Test;

import com.almende.eve.algorithms.DAAValueBean;
import com.almende.eve.algorithms.test.agents.DAAAgent;
import com.almende.util.URIUtil;

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
		final double value = 0.345;
		DAAValueBean bean = new DAAValueBean(width, 10);
		bean.generate(value).setTTL(10);

		assertTrue(Math.abs(value - bean.computeSum()) < 0.1);
//		assertEquals(10, bean.getTtlArray()[15]);

		DAAValueBean bean2 = new DAAValueBean(width, 10);
		bean2.generate(value).setTTL(10);
		bean2.minimum(bean);

		double sum = Math.abs(bean2.computeSum() - 2 * bean.computeSum());
		LOG.warning("bean1:" + bean.computeSum() + " bean2:"
				+ bean2.computeSum() + " sum:" + sum + " ("
				+ (sum * 100.0 / bean2.computeSum()) + "%)");
	}

	/**
	 * Test keys.
	 */
	@Test
	public void test2Bean() {

		final int width = 10000;
		final double value = 100E43;
		DAAValueBean bean = new DAAValueBean(width, 10);
		bean.generate(value).setTTL(10);

		assertTrue(Math.abs(value / bean.computeSum()) < 1.1);
		assertTrue(Math.abs(value / bean.computeSum()) > 0.9);

//		assertEquals(new Integer(10), bean.getTtlArray()[15]);

		DAAValueBean bean2 = new DAAValueBean(width, 10);
		bean2.generate(value).setTTL(10);
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

		int[][] edges = { { 0, 1 }, { 0, 4 }, { 1, 2 }, { 1, 3 }, { 2, 4 },{ 3, 2 },
				{ 2, 1 },  { 3, 0 }, { 4, 1 }, { 4, 2 } };

		for (int[] edge : edges) {
			agents[edge[0]].getGraph().addEdges(Arrays.asList(new URI[]{URIUtil.create("local:" + edge[1])}),"daa");
		}

		for (final DAAAgent agent : agents) {
			agent.start(1.0);
		}

		for (int i = 0; i < 10; i++) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				LOG.log(Level.WARNING, "interrupted", e);
			}
			LOG.warning("Current estimate at agent 1 :" + agents[1].getValue()
					+ " -> " + Math.round(agents[1].getValue()) + " ("
					+ (agents[1].getTTLAvg()) + ")");
		}

		LOG.warning("changing sum to 7");
		agents[3].changeValue(3.0);
		for (int i = 0; i < 20; i++) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				LOG.log(Level.WARNING, "interrupted", e);
			}
			LOG.warning("Current estimate at agent 1 :" + agents[1].getValue()
					+ " -> " + Math.round(agents[1].getValue()) + " ("
					+ (agents[1].getTTLAvg()) + ")");
		}
		LOG.warning("changing sum to 6");
		agents[2].changeValue(0.0);
		for (int i = 0; i < 20; i++) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				LOG.log(Level.WARNING, "interrupted", e);
			}
			LOG.warning("Current estimate at agent 1 :" + agents[1].getValue()
					+ " -> " + Math.round(agents[1].getValue()) + " ("
					+ (agents[1].getTTLAvg()) + ")");
		}
		
		LOG.warning("changing sum to 5");
		agents[0].destroy(false);
		for (int i = 0; i < 60; i++) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				LOG.log(Level.WARNING, "interrupted", e);
			}
			LOG.warning("Current estimate at agent 1 :" + agents[1].getValue()
					+ " -> " + Math.round(agents[1].getValue()) + " ("
					+ (agents[1].getTTLAvg()) + ")");
		}
	}
}
