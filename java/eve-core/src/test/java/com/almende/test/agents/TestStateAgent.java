package com.almende.test.agents;

import com.almende.eve.agent.Agent;
import com.almende.eve.rpc.annotation.Access;
import com.almende.eve.rpc.annotation.AccessType;

/**
 * An agent which is created to handle updates from multiple threads and saves them into the state.
 * 
 * 
 * @author ronnydealservices
 *
 */
public class TestStateAgent extends Agent {
	
	public TestStateAgent() {
		// 
	}
	
	/**
	 * a simple method to push the values onto the agent state, with roughly unique id for each new value added
	 * @param value
	 */
	@Access(AccessType.PUBLIC) 
	public synchronized void push(Object value) {
		int index = getState().size() + 1;
		String key = "attr"+index+"-"+value.hashCode();
		getState().put(key, value);
	}
	
}
