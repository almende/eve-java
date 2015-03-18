/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.agent;

import com.almende.eve.instantiation.CanHibernate;
import com.almende.eve.protocol.jsonrpc.annotation.Access;
import com.almende.eve.protocol.jsonrpc.annotation.AccessType;
import com.almende.eve.protocol.jsonrpc.annotation.Namespace;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class MyAgent.
 */
@CanHibernate
public class MyAgent extends Agent {

	/**
	 * Instantiates a new my agent.
	 */
	public MyAgent() {};

	/**
	 * Instantiates a new my agent.
	 *
	 * @param id
	 *            the id
	 */
	public MyAgent(final String id) {
		super(new AgentConfig(id));
	}

	/**
	 * Instantiates a new my agent.
	 *
	 * @param config
	 *            the config
	 */
	public MyAgent(final ObjectNode config) {
		super(config);
	}

	/**
	 * Hello world.
	 * 
	 * @return the string
	 */
	@Access(AccessType.PUBLIC)
	public String helloWorld() {
		return ("Hello World");
	}

	@Access(AccessType.PUBLIC)
	class MySubAgent {
		public String helloWorld() {
			return ("Hello World");
		}
	}

	/**
	 * Gets the my sub agent.
	 *
	 * @return the my sub agent
	 */
	@Namespace("sub")
	public MySubAgent getMySubAgent() {
		return new MySubAgent();
	}
	
	/**
	 * Throw exception.
	 *
	 * @throws IllegalStateException
	 *             the illegal state exception
	 */
	@Access(AccessType.PUBLIC)
	public void throwException() throws IllegalStateException {
		throw new IllegalStateException("Hi there!");
	}
}
