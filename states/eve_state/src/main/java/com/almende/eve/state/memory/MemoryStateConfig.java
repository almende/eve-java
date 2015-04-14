/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.state.memory;

import com.almende.eve.state.StateConfig;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class MemoryStateConfig.
 */
public class MemoryStateConfig extends StateConfig {

	/**
	 * Instantiates a new memory state config.
	 */
	public MemoryStateConfig() {
		super();
		setClassName(MemoryStateBuilder.class.getName());
	}

	/**
	 * Instantiates a new memory state config.
	 * 
	 * @param node
	 *            the node
	 */
	public static MemoryStateConfig decorate(final ObjectNode node) {
		if (node != null && node instanceof MemoryStateConfig) {
			return (MemoryStateConfig) node;
		}
		final MemoryStateConfig res = new MemoryStateConfig();
		res.copy(node);
		if (!res.has("class")) {
			res.setClassName(MemoryStateBuilder.class.getName());
		}
		return res;
	}

}
