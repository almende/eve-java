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
	private static final String	BUILDER	= MemoryStateBuilder.class.getName();

	/**
	 * Instantiates a new memory state config.
	 */
	protected MemoryStateConfig() {
		super();
	}

	/**
	 * Instantiates a new memory state config.
	 *
	 * @return the memory state config
	 */
	public static MemoryStateConfig create() {
		final MemoryStateConfig res = new MemoryStateConfig();
		res.setBuilder(BUILDER);
		return res;
	}

	/**
	 * Clone the given ObjectNode to this Config.
	 * 
	 * @param node
	 *            the node
	 */
	public static MemoryStateConfig decorate(final ObjectNode node) {
		final MemoryStateConfig res = new MemoryStateConfig();
		res.extend(node);
		return res;
	}

}
