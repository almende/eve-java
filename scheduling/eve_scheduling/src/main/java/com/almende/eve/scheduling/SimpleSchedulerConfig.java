/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.scheduling;

import com.almende.eve.config.Config;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class SimpleSchedulerConfig.
 */
public class SimpleSchedulerConfig extends Config {
	private static final String	BUILDER	= SimpleSchedulerBuilder.class
												.getName();

	/**
	 * Instantiates a new simple scheduler config.
	 */
	protected SimpleSchedulerConfig() {
		super();
	}

	/**
	 * Creates the.
	 *
	 * @return the simple scheduler config
	 */
	public static SimpleSchedulerConfig create() {
		final SimpleSchedulerConfig res = new SimpleSchedulerConfig();
		res.setBuilder(BUILDER);
		return res;
	}

	/**
	 * Instantiates a new simple scheduler config.
	 * 
	 * @param node
	 *            the node
	 */
	public static SimpleSchedulerConfig decorate(final ObjectNode node) {
		final SimpleSchedulerConfig res = new SimpleSchedulerConfig();
		res.extend(node);
		return res;
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
