/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.protocol;

import com.almende.eve.capabilities.Config;
import com.almende.eve.protocol.auth.DefaultAuthorizor;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class TransformConfig.
 */
public class ProtocolConfig extends Config {

	/**
	 * Instantiates a new protocol config.
	 */
	public ProtocolConfig() {
		super();
	}

	/**
	 * Instantiates a new protocol config.
	 *
	 * @param node
	 *            the node
	 * @return the protocol config
	 */
	public static ProtocolConfig decorate(final ObjectNode node) {
		if (node != null && node instanceof ProtocolConfig) {
			return (ProtocolConfig) node;
		}
		final ProtocolConfig res = new ProtocolConfig();
		res.copy(node);
		return res;
	}

	/**
	 * Gets the authorizor.
	 *
	 * @return the authorizor
	 */
	public String getAuthorizor() {
		if (this.has("authorizor")) {
			return this.get("authorizor").asText();
		}
		return DefaultAuthorizor.class.getName();
	}

	/**
	 * Sets the authorizor.
	 *
	 * @param authorizor
	 *            the new authorizor
	 */
	public void setAuthorizor(final String authorizor) {
		this.put("authorizor", authorizor);
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
