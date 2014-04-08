/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.agent;

import com.almende.eve.capabilities.CapabilityFactory;
import com.almende.eve.state.State;
import com.almende.eve.state.StateFactory;
import com.almende.util.jackson.JOM;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class Agent.
 * 
 * @author ludo
 */
public class Agent {
	
	private State	myState		= null;
	private State	myState2	= null;
	
	/**
	 * Create new agent.
	 */
	public Agent() {
		ObjectNode params = JOM.createObjectNode();
		params.put("class", "com.almende.eve.state.MemoryStateService");
		params.put("id", "TestAgent");
		
		myState = CapabilityFactory.get(params, null, State.class);
		
		myState2 = StateFactory.getState(params);
	}
	
	/**
	 * Test me.
	 */
	public void testMe() {
		myState.put("msg", "Hi There!");
		System.out.println("Agent said:" + myState2.get("msg", String.class));
	}
	
}
