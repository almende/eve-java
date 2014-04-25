/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.transport;

import com.almende.eve.capabilities.CapabilityFactory;
import com.almende.eve.capabilities.handler.Handler;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * A factory for creating Transport objects.
 * 
 * @author ludo
 */
public class TransportFactory {
	
	/**
	 * Gets the transport.
	 * 
	 * @param params
	 *            the params
	 * @param handle
	 *            the handle
	 * @return the state
	 */
	public static Transport getTransport(final ObjectNode params,
			final Handler<Receiver> handle) {
		return CapabilityFactory.get(params, handle, Transport.class);
	}
}
