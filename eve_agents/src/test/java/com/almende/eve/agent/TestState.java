/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.agent;

import junit.framework.TestCase;

import org.junit.Test;

import com.almende.eve.capabilities.CapabilityFactory;
import com.almende.eve.defaults.Config;
import com.almende.eve.state.State;
import com.almende.eve.state.StateFactory;
import com.almende.util.jackson.JOM;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class TestState.
 */
public class TestState extends TestCase {
	
	/**
	 * Create new agent.
	 */
	public TestState() {
		ObjectNode params = JOM.createObjectNode();
		params.put("class", "com.almende.eve.state.memory.MemoryStateService");
		Config.addConfig(params, "state","memTest");
		
		params.put("class", "com.almende.eve.state.file.FileStateService");
		params.put("json", false);
		Config.addConfig(params, "state","fileTest");
		
		System.out.println(Config.getConfig());
	}
	
	/**
	 * Run test.
	 * 
	 * @param myState
	 *            the my state
	 * @param myState2
	 *            the my state2
	 */
	public void runTest(final State myState, final State myState2) {
		myState.put("msg", "Hi There!");
		assertEquals(myState2.get("msg", String.class), "Hi There!");
		
		myState.delete();
		assertNull(myState.get("msg", String.class));
	}
	
	/**
	 * Test me.
	 */
	@Test
	public void testState() {
		ObjectNode params = Config.getConfig("state","memTest");
		params.put("id", "TestAgent");
		
		State myState = CapabilityFactory.get(params, null, State.class);
		State myState2 = StateFactory.getState(params);
		runTest(myState, myState2);
		
		myState = CapabilityFactory.get(params, null, State.class);
		myState2 = StateFactory.getState(params);
		runTest(myState, myState2);
	}
	
	/**
	 * Test file state.
	 */
	@Test
	public void testFileState() {
		ObjectNode params = Config.getConfig("state","fileTest");
		params.put("id", "TestAgent");
		
		State myState = CapabilityFactory.get(params, null, State.class);
		State myState2 = StateFactory.getState(params);
		runTest(myState, myState2);
		myState = CapabilityFactory.get(params, null, State.class);
		myState2 = StateFactory.getState(params);
		runTest(myState, myState2);
		
		params.put("json", true);
		myState = CapabilityFactory.get(params, null, State.class);
		myState2 = StateFactory.getState(params);
		runTest(myState, myState2);
		
	}
}
