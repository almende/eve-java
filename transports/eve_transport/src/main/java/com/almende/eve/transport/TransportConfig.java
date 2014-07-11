/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.transport;

import com.almende.eve.capabilities.Config;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class TransportConfig.
 */
public class TransportConfig extends Config {
	
	/**
	 * Instantiates a new transport config.
	 * 
	 * @param node
	 *            the node
	 */
	public TransportConfig(final ObjectNode node) {
		super(node);
	}
	
	/**
	 * Sets the do shortcut. (Optional, default is true)
	 * 
	 * @param doShortcut
	 *            the new do shortcut
	 */
	public void setDoShortcut(final boolean doShortcut) {
		this.put("doShortcut", doShortcut);
	}
	
	/**
	 * Gets the do shortcut.
	 * 
	 * @return the do shortcut
	 */
	public boolean getDoShortcut() {
		if (this.has("doShortcut")) {
			return this.get("doShortcut").asBoolean();
		}
		return true;
	}
	
	/**
	 * Sets the do authentication. (Optional, default is true)
	 * 
	 * @param doAuthentication
	 *            the new do authentication
	 */
	public void setDoAuthentication(final boolean doAuthentication) {
		this.put("doAuthentication", doAuthentication);
	}
	
	/**
	 * Gets the do authentication.
	 * 
	 * @return the do authentication
	 */
	public boolean getDoAuthentication() {
		if (this.has("doAuthentication")) {
			return this.get("doAuthentication").asBoolean();
		}
		return true;
	}
}
