/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.capabilities;

import com.almende.eve.capabilities.handler.Handler;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Interface CapabilityService.
 */
public interface CapabilityService {
	
	/**
	 * Gets the resulting capability instance.
	 * 
	 * @param <T>
	 *            the generic type
	 * @param <V>
	 *            the value type
	 * @param params
	 *            the params
	 * @param handle
	 *            the handle
	 * @param type
	 *            the type
	 * @return the t
	 */
	<T extends Capability, V> T get(ObjectNode params, Handler<V> handle,
			Class<T> type);
}
