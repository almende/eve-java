/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.transport.pubnub;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Logger;

import com.almende.eve.transport.TransportConfig;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class PubNubTransportConfig.
 */
public class PubNubTransportConfig extends TransportConfig {
	private static final Logger	LOG		= Logger.getLogger(PubNubTransportConfig.class
												.getName());
	private static final String	BUILDER	= PubNubTransportBuilder.class
												.getName();

	/**
	 * Instantiates a new PubNub transport config.
	 */
	protected PubNubTransportConfig() {
		super();
	}

	/**
	 * Instantiates a new PubNub transport config.
	 *
	 * @return the amqp transport config
	 */
	public static PubNubTransportConfig create() {
		final PubNubTransportConfig res = new PubNubTransportConfig();
		res.setBuilder(BUILDER);
		return res;
	}

	/**
	 * Instantiates a new PubNub transport config.
	 *
	 * @param node
	 *            the node
	 * @return the PubNub transport config
	 */
	public static PubNubTransportConfig decorate(final ObjectNode node) {
		final PubNubTransportConfig res = new PubNubTransportConfig();
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
		LOG.warning("PubNub requires an 'address' field in its config.");
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
	 * Gets the publish key.
	 *
	 * @return the publish key
	 */
	public String getPublishKey() {
		if (this.has("publishKey")) {
			return this.get("publishKey").asText();
		}
		return null;

	}

	/**
	 * Sets the publish key.
	 *
	 * @param publishKey
	 *            the new publish key
	 */
	public void setPublishKey(final String publishKey) {
		this.put("publishKey", publishKey);
	}

	/**
	 * Gets the subscribe key.
	 *
	 * @return the subscribe key
	 */
	public String getSubscribeKey() {
		if (this.has("subscribeKey")) {
			return this.get("subscribeKey").asText();
		}
		return null;
	}

	/**
	 * Sets the subscribe key.
	 *
	 * @param subscribeKey
	 *            the new subscribe key
	 */
	public void setSubscribeKey(final String subscribeKey) {
		this.put("subscribeKey", subscribeKey);
	}

	/**
	 * Checks if is use ssl.
	 *
	 * @return true, if is use ssl
	 */
	public boolean isUseSSL() {
		if (this.has("useSSL")) {
			return this.get("useSSL").asBoolean();
		}
		return true;
	}

	/**
	 * Sets the use ssl.
	 *
	 * @param useSSL
	 *            the new use ssl
	 */
	public void setUseSSL(final boolean useSSL) {
		this.put("useSSL", useSSL);
	}
}
