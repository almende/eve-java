/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.test.agents;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.almende.eve.agent.Agent;
import com.almende.eve.rpc.annotation.Access;
import com.almende.eve.rpc.annotation.AccessType;

/**
 * An agent which is created to handle updates from multiple threads and saves them into the state.
 * In effect, this agent behaves like a Vector.
 * 
 * @author ronnydealservices
 *
 */
public class TestStateAgent extends Agent {
	
	private static final Logger	LOG = Logger.getLogger("TestStateAgent");
	
	/**
	 * Instantiates a new test state agent.
	 */
	public TestStateAgent() {
		// 
	}

	/**
	 * a simple method to push the values onto the agent state, with roughly
	 * unique id for each new value added.
	 * 
	 * @param value
	 *            the value
	 * @return the object
	 */
	@Access(AccessType.PUBLIC) 
	public synchronized Object push(Object value) {
		int index = getState().size() + 1;
		String key = "a"+index+"-"+UUID.randomUUID();
		LOG.log(Level.INFO, "put ["+key+"] = "+value);
		return getState().put(key, value);
	}
	
}



