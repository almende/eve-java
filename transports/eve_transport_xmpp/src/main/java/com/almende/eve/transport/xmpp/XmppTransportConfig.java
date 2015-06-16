/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.transport.xmpp;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Logger;

import com.almende.eve.transport.TransportConfig;
import com.almende.util.URIUtil;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class XmppTransportConfig.
 */
public class XmppTransportConfig extends TransportConfig {
	private static final Logger	LOG		= Logger.getLogger(XmppTransportConfig.class
												.getName());
	private static final String	BUILDER	= XmppTransportBuilder.class.getName();

	/**
	 * Instantiates a new xmpp transport config.
	 */
	protected XmppTransportConfig() {
		super();
	}

	/**
	 * Instantiates a new xmpp transport config.
	 *
	 * @return the xmpp transport config
	 */
	public static XmppTransportConfig create() {
		final XmppTransportConfig res = new XmppTransportConfig();
		res.setBuilder(BUILDER);
		return res;
	}

	/**
	 * Instantiates a new xmpp transport config.
	 *
	 * @param node
	 *            the node
	 * @return the xmpp transport config
	 */
	public static XmppTransportConfig decorate(final ObjectNode node) {
		final XmppTransportConfig res = new XmppTransportConfig();
		res.extend(node);
		return res;
	}

	/**
	 * Gets the address.
	 * 
	 * @return the address
	 */
	public URI getAddress() {
		if (this.has("address")) {
			try {
				return URIUtil.parse(this.get("address").asText());
			} catch (final URISyntaxException e) {
				LOG.warning("Couldn't parse URI from: "
						+ this.get("address").asText());
			}
		}
		return null;
	}

	/**
	 * Sets the address.
	 * 
	 * @param address
	 *            the new address
	 */
	public void setAddress(final String address) {
		this.put("address", address);
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
