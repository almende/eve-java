/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.agent;

import junit.framework.TestCase;

import org.junit.Test;

import com.almende.eve.capabilities.CapabilityFactory;
import com.almende.eve.state.State;
import com.almende.eve.state.StateFactory;
import com.almende.util.jackson.JOM;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class TestState.
 */
public class TestState extends TestCase {
	
	
	private State		myState		= null;
	private State		myState2	= null;
	private ObjectNode	params		= null;
	
	/**
	 * Create new agent.
	 */
	public TestState() {
		params = JOM.createObjectNode();
		params.put("class", "com.almende.eve.state.MemoryStateService");
		params.put("id", "TestAgent");
		
		myState = CapabilityFactory.get(params, null, State.class);
		myState2 = StateFactory.getState(params);
	}
	
	/**
	 * Test me.
	 */
	@Test
	public void testState() {
		myState.put("msg", "Hi There!");
		assertEquals(myState2.get("msg",String.class),"Hi There!");
		
		myState.delete();
		
		myState = CapabilityFactory.get(params, null, State.class);
		myState2 = StateFactory.getState(params);
		assertNull(myState2.get("msg",String.class));
		
		myState.put("msg", "Hi There!");
		assertEquals(myState2.get("msg",String.class),"Hi There!");
		
	}	
}
