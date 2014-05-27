/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.transport;

import com.almende.eve.capabilities.CapabilityFactory;
import com.almende.eve.capabilities.handler.Handler;
import com.almende.eve.transport.Receiver;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * A factory for creating HttpTransport objects.
 */
public final class LocalTransportFactory {
	
	private LocalTransportFactory() {
	}
	
	/**
	 * Gets the transport.
	 * 
	 * @param params
	 *            the params
	 * @param handle
	 *            the handle
	 * @return the state
	 */
	public static LocalService get(final ObjectNode params,
			final Handler<Receiver> handle) {
		return CapabilityFactory.get(new LocalTransportConfig(params), handle,
				LocalService.class);
	}
}
