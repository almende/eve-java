/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.agent;

import com.almende.eve.capabilities.wake.WakeService;
import com.almende.eve.transform.rpc.annotation.Access;
import com.almende.eve.transform.rpc.annotation.AccessType;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class MyAgent.
 */
public class MyAgent extends WakeableAgent {
	
	/**
	 * Instantiates a new my agent.
	 */
	public MyAgent() {
	};
	
	/**
	 * Instantiates a new my agent.
	 * 
	 * @param id
	 *            the id
	 * @param ws
	 *            the ws
	 */
	public MyAgent(final String id, final WakeService ws) {
		super(new AgentConfig(id), ws);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.almende.eve.agent.WakeableAgent#wake(java.lang.String,
	 * com.fasterxml.jackson.databind.node.ObjectNode, boolean)
	 */
	@Override
	public void wake(final String wakeKey, final ObjectNode params,
			final boolean onBoot) {
		super.wake(wakeKey, params, onBoot);
		System.out.println("Wake called, so the agent was actually unloaded!");
	}
	
	/**
	 * Hello world.
	 * 
	 * @return the string
	 */
	@Access(AccessType.PUBLIC)
	public String helloWorld() {
		return("Hello World");
	}
}
