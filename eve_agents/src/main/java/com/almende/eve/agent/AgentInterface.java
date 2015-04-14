/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.agent;

import java.net.URI;
import java.util.List;

/**
 * The Interface AgentInterface.
 */
public interface AgentInterface {
	/**
	 * Retrieve the agents id.
	 * 
	 * @return id
	 */
	String getId();

	/**
	 * Retrieve the agents type (its simple class name).
	 * 
	 * @return version
	 */
	String getType();

	/**
	 * Retrieve a list with all the available methods.
	 * 
	 * @return methods
	 */
	List<Object> getMethods();

	/**
	 * Retrieve an array with the agents urls (can be one or multiple), and
	 * depends on the configured transport services.
	 * 
	 * @return urls
	 */
	List<URI> getUrls();

	/**
	 * Gets the config of this Agent, expanded, with no external references.
	 * 
	 * @return the config
	 */
	AgentConfig getConfig();

}
