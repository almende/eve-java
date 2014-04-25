/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.capabilities;

import com.almende.util.jackson.JOM;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class Config.
 */
public class Config extends ObjectNode {
	
	/**
	 * Instantiates a new config.
	 */
	public Config() {
		super(JOM.getInstance().getNodeFactory());
	}
	
	/**
	 * Instantiates a new config.
	 * 
	 * @param node
	 *            the node
	 */
	public Config(final ObjectNode node) {
		super(JOM.getInstance().getNodeFactory());
		if (node != null) {
			this.setAll(node);
		}
	}
	
	/**
	 * Sets the class path. (Required)
	 * 
	 * @param className
	 *            the new class
	 */
	public void setClassName(final String className) {
		this.put("class", className);
	}
	
	/**
	 * Gets the class path.
	 * 
	 * @return the class path
	 */
	public String getClassName() {
		if (this.has("class")) {
			return this.get("class").asText();
		}
		return null;
	}
}
