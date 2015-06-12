/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.agent;

import java.net.URI;
import java.util.List;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Interface AgentInterface, these are methods that the DebugServlet uses.
 */
public interface AgentInterface {
	/**
	 * Retrieve the agents id.
	 * 
	 * @return id
	 */
	String getId();

	/**
	 * Gets the runtime class type of this agent.
	 * 
	 * @return the type
	 */
	String getType();

	/**
	 * Retrieve a list with all the available methods.
	 * Defined according to: http://www.simple-is-better.org/json-rpc/jsonrpc20-schema-service-descriptor.html
	 * 
	 * @return methods
	 */
	ObjectNode getMethods();

	/**
	 * Retrieve an array with the agents urls (can be one or multiple), and
	 * depends on the configured transport services.
	 * 
	 * @return urls
	 */
	List<URI> getUrls();

}
