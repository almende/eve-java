package com.almende.eve.test.agents;

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
		// :: creating pseudo-unique key
		String key = "attr"+index+"-"+System.nanoTime();
		if (getState().containsKey(key)) {
			// :: collision avoidance by using relative slowness of recursion
			push(value); 
		} else {
			getState().put(key, value);
		}
	}
	
}
