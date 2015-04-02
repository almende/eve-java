/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.algorithms.test.agents;

import com.almende.eve.agent.Agent;
import com.almende.eve.algorithms.Graph;
import com.almende.eve.protocol.jsonrpc.annotation.Access;
import com.almende.eve.protocol.jsonrpc.annotation.AccessType;
import com.almende.eve.protocol.jsonrpc.annotation.Namespace;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class NodeAgent.
 */
@Access(AccessType.PUBLIC)
public class NodeAgent extends Agent {
	private Graph graph = new Graph(super.caller);
	
	/**
	 * Instantiates a new node agent.
	 *
	 * @param id
	 *            the id
	 * @param config
	 *            the config
	 */
	public NodeAgent(String id, ObjectNode config){
		super(id,config);
	}
	
	/**
	 * Gets the network.
	 *
	 * @return the network
	 */
	@Namespace("*")
	public Graph getGraph() {
		return graph;
	}
}
