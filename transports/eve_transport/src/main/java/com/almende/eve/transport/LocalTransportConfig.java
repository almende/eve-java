/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.transport;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class LocalTransportConfig.
 */
public class LocalTransportConfig extends TransportConfig {
	private static final String	BUILDER	= LocalTransportBuilder.class.getName();

	/**
	 * Instantiates a new local transport config.
	 */
	protected LocalTransportConfig() {
		super();
	}

	/**
	 * Instantiates a new local transport config.
	 *
	 * @return the local transport config
	 */
	public static LocalTransportConfig create() {
		final LocalTransportConfig res = new LocalTransportConfig();
		res.setBuilder(BUILDER);
		return res;
	}

	/**
	 * Instantiates a new local transport config.
	 *
	 * @param node
	 *            the node
	 * @return the local transport config
	 */
	public static LocalTransportConfig decorate(final ObjectNode node) {
		final LocalTransportConfig res = new LocalTransportConfig();
		res.extend(node);
		return res;
	}

	/**
	 * Instantiates a new local transport config.
	 * 
	 * @param id
	 *            the id
	 */
	public LocalTransportConfig(final String id) {
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
}
