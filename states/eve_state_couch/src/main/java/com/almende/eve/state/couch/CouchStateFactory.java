/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.state.couch;

import com.almende.eve.capabilities.CapabilityFactory;
import com.almende.eve.state.State;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * A factory for creating CouchState objects.
 */
public class CouchStateFactory {
	/**
	 * Gets the state.
	 * 
	 * @param params
	 *            the params
	 * @return the state
	 */
	public static State get(final ObjectNode params) {
		return CapabilityFactory.get(new CouchStateConfig(params), null,
				State.class);
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
