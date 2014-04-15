package com.almende.eve.transport;

import com.almende.eve.capabilities.CapabilityFactory;
import com.almende.eve.capabilities.handler.Handler;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author ludo
 * 
 */
public class TransportFactory {
	
	/**
	 * Gets the transport.
	 * 
	 * @param params
	 *            the params
	 * @param handle
	 * @return the state
	 */
	public static Transport getTransport(final JsonNode params,
			final Handler<Receiver> handle) {
		return CapabilityFactory.get(params, handle, Transport.class);
	}
}
