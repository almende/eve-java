/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.algorithms.agents;

import com.almende.eve.agent.AgentInterface;
import com.almende.eve.algorithms.Graph;
import com.almende.eve.protocol.jsonrpc.annotation.Access;
import com.almende.eve.protocol.jsonrpc.annotation.AccessType;
import com.almende.eve.protocol.jsonrpc.annotation.Namespace;

/**
 * The Class NodeAgent.
 */
interface NodeAgentInterface extends AgentInterface {

	/**
	 * Adds the node to the scale free network.
	 *
	 * @param tag
	 *            the tag
	 */
	@Access(AccessType.PUBLIC)
	void addNode2SFN(final String tag);

	/**
	 * Gets the network.
	 *
	 * @return the network
	 */
	@Access(AccessType.PUBLIC)
	@Namespace("graph")
	Graph getGraph();
}
