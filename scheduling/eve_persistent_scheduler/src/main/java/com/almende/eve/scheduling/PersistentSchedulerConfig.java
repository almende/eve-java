/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.scheduling;

import com.almende.util.jackson.JOM;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class PersistentSchedulerConfig.
 */
public class PersistentSchedulerConfig extends SimpleSchedulerConfig {
	
	/**
	 * Instantiates a new simple scheduler config.
	 */
	public PersistentSchedulerConfig() {
		this(JOM.createObjectNode());
	}
	
	/**
	 * Instantiates a new simple scheduler config.
	 * 
	 * @param node
	 *            the node
	 */
	public PersistentSchedulerConfig(final ObjectNode node) {
		super(node);
		if (!node.has("class")) {
			setClassName(PersistentSchedulerBuilder.class.getName());
		}
	}
	
	/**
	 * Sets the state.
	 * 
	 * @param node
	 *            the new state
	 */
	public void setState(final ObjectNode node) {
		this.set("state", node);
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
