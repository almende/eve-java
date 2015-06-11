/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.algorithms.clustering;

import com.almende.eve.transport.TransportConfig;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class LocalTransportConfig.
 */
public class GlobalAddressTransportConfig extends TransportConfig {

	/**
	 * Instantiates a new local transport config.
	 */
	public GlobalAddressTransportConfig() {
		super();
		setClassName(GlobalAddressTransportBuilder.class.getName());
	}

	/**
	 * Instantiates a new local transport config.
	 * 
	 * @param node
	 *            the node
	 */
	public static GlobalAddressTransportConfig decorate(final ObjectNode node) {
		final GlobalAddressTransportConfig res = new GlobalAddressTransportConfig();
		res.extend(node);
		return res;
	}

	/**
	 * Instantiates a new local transport config.
	 * 
	 * @param id
	 *            the id
	 */
	public GlobalAddressTransportConfig(final String id) {
		this();
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
	
	/**
	 * Sets the actual address this virtual address maps to.
	 *
	 * @param realAddressPattern
	 *            the new real address pattern
	 */
	public void setRealAddressPattern(final String realAddressPattern) {
		this.put("realAddressPattern", realAddressPattern);
	}

	/**
	 * Gets the actual address this virtual address maps to.
	 * 
	 * @return the id
	 */
	public String getRealAddressPattern() {
		if (this.has("realAddressPattern")) {
			return this.get("realAddressPattern").asText();
		}
		return null;
	}

}
