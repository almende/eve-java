/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.scheduling;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class PersistentSchedulerConfig.
 */
public class PersistentSchedulerConfig extends SimpleSchedulerConfig {
	private static final String	BUILDER	= PersistentSchedulerBuilder.class
												.getName();

	/**
	 * Instantiates a new simple scheduler config.
	 */
	protected PersistentSchedulerConfig() {
		super();
	}

	/**
	 * Instantiates a new persistent scheduler config.
	 *
	 * @return the persistent scheduler config
	 */
	public static PersistentSchedulerConfig create() {
		final PersistentSchedulerConfig res = new PersistentSchedulerConfig();
		res.setBuilder(BUILDER);
		return res;
	}

	/**
	 * Instantiates a new persistent scheduler config.
	 * 
	 * @param node
	 *            the node
	 */
	public static PersistentSchedulerConfig decorate(final ObjectNode node) {
		final PersistentSchedulerConfig res = new PersistentSchedulerConfig();
		res.extend(node);
		return res;
	}

	/**
	 * Sets the state.
	 * 
	 * @param node
	 *            the new state
	 */
	public void setState(final ObjectNode node) {
		this.set("state", node);
	}

	/**
	 * Gets the state.
	 * 
	 * @return the state
	 */
	public ObjectNode getState() {
		if (this.has("state")) {
			return (ObjectNode) this.get("state");
		}
		return null;
	}
}
