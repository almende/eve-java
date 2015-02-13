/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.algorithms.test;

import java.util.logging.Logger;

import junit.framework.TestCase;

import org.junit.Test;

import com.almende.eve.algorithms.DAAValueBean;

/**
 * The Class TestValueBean.
 */
public class TestValueBean extends TestCase {
	private static final Logger	LOG	= Logger.getLogger(TestValueBean.class
											.getName());

	/**
	 * Test keys.
	 */
	@Test
	public void testBeans() {

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
}
