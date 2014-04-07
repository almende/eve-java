/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.agent;

import java.io.IOException;

import com.almende.eve.state.State;
import com.almende.eve.state.StateFactory;

/**
 * The Class Agent.
 * 
 * @author ludo
 */
public class Agent {
	
	private State myState = null;
	
	/**
	 * Create new agent.
	 */
	public Agent() {
		try {
			myState = StateFactory.getStateService(
					"com.almende.eve.agent.state.MemoryState", null).create("TestAgent");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Test me.
	 */
	public void testMe(){
		myState.put("msg", "Hi There !");
		System.out.println("Agent said:"+myState.get("msg",String.class));
	}
	
}
