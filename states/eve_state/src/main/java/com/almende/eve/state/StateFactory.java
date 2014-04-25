/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.state;

import com.almende.eve.capabilities.CapabilityFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * A factory for creating State objects.
 * 
 * @author ludo
 */
public class StateFactory {
	
	/**
	 * Gets the state.
	 * 
	 * @param params
	 *            the params
	 * @return the state
	 */
	public static State getState(final ObjectNode params) {
		return CapabilityFactory.get(params, null, State.class);
	}
}
