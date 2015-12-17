/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.transport.amqp;

import java.net.URI;
import java.util.logging.Logger;

import com.almende.eve.transport.TransportConfig;
import com.almende.util.URIUtil;
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
	 * Instantiates a new AMQP transport config.
	 *
	 * @param node
	 *            the node
	 * @return the AMQP transport config
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
		if (!"".equals(getId())) {
			return URIUtil.create("amqp:" + getId());
		}
		return null;
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
		LOG.warning("Required field 'id' is missing!");
		return "";
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
	 * Gets the host uri.
	 *
	 * @return the host uri
	 */
	public String getHostUri() {
		if (this.has("hostUri")) {
			return this.get("hostUri").asText();
		}
		LOG.warning("Required field 'hostUri' is missing!");
		return null;
	}

	/**
	 * Sets the host uri.
	 *
	 * @param uri
	 *            the new host uri
	 */
	public void setHostUri(final String uri) {
		this.put("hostUri", uri);
	}
}
