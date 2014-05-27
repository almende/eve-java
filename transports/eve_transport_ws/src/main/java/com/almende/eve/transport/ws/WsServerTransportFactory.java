/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.transport.ws;

import com.almende.eve.capabilities.CapabilityFactory;
import com.almende.eve.capabilities.handler.Handler;
import com.almende.eve.transport.Receiver;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * A factory for creating WebsocketTransport objects.
 */
public final class WsServerTransportFactory {
	
	private WsServerTransportFactory() {
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
	public static WsServerTransport get(final ObjectNode params,
			final Handler<Receiver> handle) {
		if (params.isObject()) {
			if (!params.has("class")) {
				params.put("class", WebsocketService.class.getName());
			}
			if (!params.has("server")) {
				params.put("server", true);
			}
		}
		return CapabilityFactory.get(params, handle, WsServerTransport.class);
	}
}
