/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.test;

import junit.framework.TestCase;

import org.junit.Test;

import com.almende.eve.agent.MyAgent;
import com.almende.eve.capabilities.wake.WakeService;
import com.almende.util.jackson.JOM;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class TestWake.
 */
public class TestWake extends TestCase {
	
	/**
	 * Test wake.
	 */
	@Test
	public void testWake() {
		final ObjectNode params = JOM.createObjectNode();
		final ObjectNode state = JOM.createObjectNode();
		state.put("class", "com.almende.eve.state.file.FileStateService");
		state.put("json", true);
		state.put("path", ".wakeservices");
		state.put("id", "testWakeService");
		params.put("state", state);
		
		final WakeService ws = new WakeService(params);
		
		// Create agent without external references, hopefully!
		new MyAgent("testWakeAgent", ws).init();
		// Try to get rid of the agent instance from memory
		System.gc();
		System.gc();
		
		// Sleep for 10seconds, allowing external XMPP call.
		try {
			Thread.sleep(20000);
		} catch (final InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
