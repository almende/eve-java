/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.state.redis;

import com.almende.eve.state.StateConfig;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class MemoryStateConfig.
 */
public class RedisStateConfig extends StateConfig {
	private static final String	BUILDER	= RedisStateBuilder.class.getName();

	/**
	 * Instantiates a new memory state config.
	 */
	protected RedisStateConfig() {
		super();
	}

	/**
	 * Instantiates a new redis state config.
	 *
	 * @return the redis state config
	 */
	public static RedisStateConfig create() {
		final RedisStateConfig res = new RedisStateConfig();
		res.setBuilder(BUILDER);
		return res;
	}

	/**
	 * Instantiates a new redis state config.
	 *
	 * @param node
	 *            the node
	 * @return the redis state config
	 */
	public static RedisStateConfig decorate(final ObjectNode node) {
		final RedisStateConfig res = new RedisStateConfig();
		res.extend(node);
		return res;
	}

	/**
	 * Gets the host.
	 *
	 * @return the host
	 */
	public String getHost() {
		if (this.has("host")) {
			return this.get("host").asText();
		}
		return "localhost";
	}

	/**
	 * Sets the host.
	 *
	 * @param host
	 *            the new host
	 */
	public void setHost(final String host) {
		this.put("host", host);
	}

	/**
	 * Gets the db id.
	 *
	 * @return the db id
	 */
	public int getDbId() {
		if (this.has("dbId")) {
			return this.get("dbId").asInt();
		}
		return 0;
	}

	/**
	 * Sets the db id.
	 *
	 * @param id
	 *            the new db id
	 */
	public void setDbId(final int id) {
		this.put("dbId", id);
	}
}
