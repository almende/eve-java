/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.state.memory;

import com.almende.eve.capabilities.CapabilityFactory;
import com.almende.eve.state.State;
import com.almende.util.jackson.JOM;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * A factory for creating MemoryState objects.
 */
public class MemoryStateFactory {
	
	/**
	 * Gets the state.
	 * 
	 * @param params
	 *            the params
	 * @return the state
	 */
	public static State get(ObjectNode params) {
		if (params == null || params.equals(JOM.createNullNode())
				|| params.isNull()) {
			params = JOM.createObjectNode();
		}
		if (params.isObject() && !params.has("class")) {
			params.put("class", MemoryStateFactory.class
					.getPackage().getName() + ".MemoryStateService");
		}
		return CapabilityFactory.get(params, null, State.class);
	}
	
	/**
	 * Gets the.
	 * 
	 * @return the state
	 */
	public static State get() {
		return get(null);
	}
}
