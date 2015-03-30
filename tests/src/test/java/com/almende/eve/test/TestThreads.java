/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.test;

import java.util.logging.Logger;


import junit.framework.TestCase;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.junit.Test;

import com.almende.util.threads.ThreadPool;

/**
 * The Class TestThreads.
 */
public class TestThreads extends TestCase {
	private static final Logger	LOG	= Logger.getLogger(TestThreads.class
											.getName());

	/**
	 * Test scheduling.
	 */
	@Test
	public void testThreads() {
		int nofjobs = 1000000;
		int duration = 50000;
		final boolean[] flags = new boolean[nofjobs];

		for (int i = 0; i < nofjobs; i++) {
			final int j = i;
			flags[j] = true;
			ThreadPool.getPool().execute(new Runnable() {
				@Override
				public void run() {
					int count = 0;
					while (count < 10000) {
						count++;
						String.valueOf(count);
					}
					flags[j] = false;
				}
			});
		}
		try {
			Thread.sleep(duration);
		} catch (InterruptedException e) {}
		int count = 0;
		for (int i = 0; i < nofjobs; i++) {
			if (flags[i]) {
				count++;
			}
		}
		LOG.warning(count + " jobs didn't finish in time (" + duration
				+ " ms for " + nofjobs + " jobs)");
	}

	/**
	 * Test scheduling.
	 */
	@Test
	public void testSleepingThreads() {
		int nofjobs = 2000;
		DateTime start = DateTime.now();
		final boolean[] flags = new boolean[nofjobs];

		for (int i = 0; i < nofjobs; i++) {
			final int j = i;
			flags[j] = false;
			ThreadPool.getPool().execute(new Runnable() {
				@Override
				public void run() {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {}
					flags[j] = true;
				}
			});
		}
		int count = 0;
		while (count < nofjobs) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {}
			count = 0;
			for (int i = 0; i < nofjobs; i++) {
				if (flags[i]) {
					count++;
				}
			}
		}
		LOG.warning(count + " jobs took: "+ (new Duration(start,DateTime.now()).getMillis())+ " ms");
	}
}
