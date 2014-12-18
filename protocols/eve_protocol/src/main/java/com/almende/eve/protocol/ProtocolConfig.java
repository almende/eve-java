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
	 *
	 * @param config
	 *            the config
	 */
	public ProtocolConfig(final ObjectNode config) {
		super(config);
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
}
