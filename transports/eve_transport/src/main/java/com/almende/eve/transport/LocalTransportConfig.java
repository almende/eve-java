/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.transport;

import com.almende.util.jackson.JOM;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class LocalTransportConfig.
 */
public class LocalTransportConfig extends TransportConfig {
	
	/**
	 * Instantiates a new local transport config.
	 * 
	 * @param node
	 *            the node
	 */
	public LocalTransportConfig(final ObjectNode node) {
		super(node);
		if (!node.has("class")) {
			this.put("class", LocalTransportBuilder.class.getName());
		}
	}
	
	/**
	 * Instantiates a new local transport config.
	 */
	public LocalTransportConfig() {
		this(JOM.createObjectNode());
	}
	
	/**
	 * Instantiates a new local transport config.
	 * 
	 * @param id
	 *            the id
	 */
	public LocalTransportConfig(final String id) {
		this(JOM.createObjectNode());
		setId(id);
	}
	
	/**
	 * Sets the id.
	 * 
	 * @param id
	 *            the new id
	 */
	public void setId(final String id) {
		this.put("id", id);
	}
	
	/**
	 * Gets the id.
	 * 
	 * @return the id
	 */
	public String getId() {
		if (this.has("id")) {
			return this.get("id").asText();
		}
		return null;
	}
}
