/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.transform;

import com.almende.eve.capabilities.CapabilityFactory;
import com.almende.eve.capabilities.handler.Handler;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * A factory for creating Transform objects.
 */
public class TransformFactory {
	
	/**
	 * Gets the transport.
	 * 
	 * @param params
	 *            the params
	 * @param handle
	 *            the handle
	 * @return the state
	 */
	public static Transform getTransform(final JsonNode params,
			final Handler<Object> handle) {
		return CapabilityFactory.get(params, handle, Transform.class);
	}
}
