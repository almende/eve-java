/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.agent;

import junit.framework.TestCase;

import org.junit.Test;

/**
 * The Class TestWake.
 */
public class TestWake extends TestCase {
	
	/**
	 * Test wake.
	 */
	@Test
	public void testWake() {
		// Create agent without external references, hopefully!
		new MyAgent().init();
		// Try to get rid of the agent instance from memory
		System.gc();
		System.gc();
		
		// Sleep for 10seconds, allowing external XMPP call.
		try {
			Thread.sleep(20000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
