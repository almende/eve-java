/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.transport.xmpp;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Logger;

import com.almende.eve.transport.TransportConfig;
import com.almende.util.jackson.JOM;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class XmppTransportConfig.
 */
public class XmppTransportConfig extends TransportConfig {
	private static final Logger	LOG	= Logger.getLogger(XmppTransportConfig.class
											.getName());
	
	/**
	 * Instantiates a new xmpp transport config.
	 */
	public XmppTransportConfig() {
		this(JOM.createObjectNode());
	}
	
	/**
	 * Instantiates a new xmpp transport config.
	 * 
	 * @param node
	 *            the node
	 */
	public XmppTransportConfig(final ObjectNode node) {
		super(node);
		if (!this.has("class")) {
			setClassName(XmppService.class.getName());
		}
	}
	
	/**
	 * Gets the address.
	 * 
	 * @return the address
	 */
	public URI getAddress() {
		if (this.has("address")) {
			try {
				return new URI(this.get("address").asText());
			} catch (final URISyntaxException e) {
				LOG.warning("Couldn't parse URI from: "
						+ this.get("address").asText());
			}
		}
		return null;
	}
	
	/**
	 * Gets the password.
	 * 
	 * @return the password
	 */
	public String getPassword() {
		if (this.has("password")) {
			return this.get("password").asText();
		}
		return null;
	}
	
	/**
	 * Sets the password.
	 * 
	 * @param password
	 *            the new password
	 */
	public void setPassword(final String password) {
		this.put("password", password);
	}
}
