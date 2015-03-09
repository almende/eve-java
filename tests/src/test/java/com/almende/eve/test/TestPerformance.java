/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Vector;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.junit.Test;

/**
 * The Class TestThreads.
 */
public class TestPerformance extends TestCase {
	private static final Logger	LOG	= Logger.getLogger(TestPerformance.class
											.getName());

	final HashMap<String,String> x = new HashMap<String,String>();
	HashMap<String,String> y = new HashMap<String,String>();
	
	
	class Tester implements Comparable<Tester> {
		Class<Object> c;
		String a;
		String b;

		HashMap<String,String> test;
		
		@Override
		public int compareTo(Tester o) {
			return test.size() - o.test.size();
		}

	}

	/**
	 * Test scheduling.
	 */
	@Test
	public void testPerformance() {
		Vector<Tester> test = new Vector<Tester>();
		x.put("Some key", "Some value");
		
		final int NOFTESTERS = 2;
		final int NOFRUNS	= 100;
		
		for (int i = 0; i < NOFRUNS; i++) {
			test = new Vector<Tester>();

			for (int j = 0; j < NOFTESTERS; j++) {
				Tester a = new Tester();
				a.a="a";
				a.b="some longer string with substring:"+j+"!";
				a.c = Object.class;
				
				if (j % 2 == 0){
				x.put("Some other string", "My value");
				} else {
					x.remove("Some other string");
				}
				a.test = x;
				
				Tester b = new Tester();
				b.a="b";
				b.b="some longer string with substring:"+j+"!";
				b.c = Object.class;
				b.test = y;
				
				test.add(a);
				test.add(b);
			}

			long start = System.currentTimeMillis();
			Collections.sort(test);
			LOG.warning("Run " + i + " took "
					+ (System.currentTimeMillis() - start) + " ms");
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
			}
		}
	}
}
