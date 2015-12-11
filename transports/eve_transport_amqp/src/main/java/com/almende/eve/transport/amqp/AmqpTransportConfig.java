/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.transport.amqp;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Logger;

import com.almende.eve.transport.TransportConfig;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class AmqpTransportConfig.
 */
public class AmqpTransportConfig extends TransportConfig {
	private static final Logger	LOG		= Logger.getLogger(AmqpTransportConfig.class
												.getName());
	private static final String	BUILDER	= AmqpTransportBuilder.class.getName();

	/**
	 * Instantiates a new AMQP transport config.
	 */
	protected AmqpTransportConfig() {
		super();
	}

	/**
	 * Instantiates a new AMQP transport config.
	 *
	 * @return the amqp transport config
	 */
	public static AmqpTransportConfig create() {
		final AmqpTransportConfig res = new AmqpTransportConfig();
		res.setBuilder(BUILDER);
		return res;
	}

	/**
	 * Instantiates a new AMQ transport config.
	 *
	 * @param node
	 *            the node
	 * @return the amqp transport config
	 */
	public static AmqpTransportConfig decorate(final ObjectNode node) {
		final AmqpTransportConfig res = new AmqpTransportConfig();
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
				return new URI(this.get("address").asText());
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
