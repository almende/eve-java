/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.capabilities.wake;

import com.almende.eve.capabilities.CapabilityFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * A factory for creating WakeService objects.
 */
public class WakeServiceFactory {
	
	/**
	 * Gets the.
	 * 
	 * @param params
	 *            the params
	 * @return the wake service
	 */
	public static WakeService get(final ObjectNode params) {
		return CapabilityFactory.get(new WakeServiceConfig(params), null,
				WakeService.class);
	}
	
}
