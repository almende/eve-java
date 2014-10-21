/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.instantiation;

import com.almende.eve.capabilities.Config;
import com.almende.util.jackson.JOM;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class WakeServiceConfig.
 */
public class InstantiationServiceConfig extends Config {
	/**
	 * Instantiates a new memory state config.
	 */
	public InstantiationServiceConfig() {
		this(JOM.createObjectNode());
	}

	/**
	 * Instantiates a new memory state config.
	 * 
	 * @param node
	 *            the node
	 */
	public InstantiationServiceConfig(final ObjectNode node) {
		super(node);
		if (!node.has("class")) {
			this.put("class", InstantiationServiceBuilder.class.getName());
		}
	}

	/**
	 * Sets the state.
	 * 
	 * @param state
	 *            the new state
	 */
	public void setState(final ObjectNode state) {
		this.set("state", state);
	}

	/**
	 * Gets the state.
	 * 
	 * @return the state
	 */
	public ObjectNode getState() {
		if (this.has("state")) {
			return (ObjectNode) this.get("state");
		}
		return null;
	}

}
