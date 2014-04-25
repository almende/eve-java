/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.state.memory;

import com.almende.eve.state.StateConfig;
import com.almende.util.jackson.JOM;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class MemoryStateConfig.
 */
public class MemoryStateConfig extends StateConfig {
	
	/**
	 * Instantiates a new memory state config.
	 */
	public MemoryStateConfig() {
		this(JOM.createObjectNode());
	}
	
	/**
	 * Instantiates a new memory state config.
	 * 
	 * @param node
	 *            the node
	 */
	public MemoryStateConfig(final ObjectNode node) {
		super(node);
		if (!node.has("class")) {
			this.put("class", MemoryStateService.class.getName());
		}
	}
	
}
