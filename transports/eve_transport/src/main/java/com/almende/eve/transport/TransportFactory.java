package com.almende.eve.transport;

import java.lang.invoke.MethodHandle;

import com.almende.eve.capabilities.CapabilityFactory;
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
	public static Transport getTransport(JsonNode params, MethodHandle handle) {
		return CapabilityFactory.get(params, handle, Transport.class);
	}
}
