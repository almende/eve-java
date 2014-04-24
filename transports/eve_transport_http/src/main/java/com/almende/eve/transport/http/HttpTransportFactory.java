/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.transport.http;

import com.almende.eve.capabilities.CapabilityFactory;
import com.almende.eve.capabilities.handler.Handler;
import com.almende.eve.transport.Receiver;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * A factory for creating HttpTransport objects.
 */
public class HttpTransportFactory {
	
	/**
	 * Gets the transport.
	 * 
	 * @param params
	 *            the params
	 * @param handle
	 *            the handle
	 * @return the state
	 */
	public static HttpService get(final JsonNode params,
			final Handler<Receiver> handle) {
		if (params.isObject() && !params.has("class")) {
			((ObjectNode) params).put("class", HttpTransportFactory.class
					.getPackage().getName() + ".HttpService");
		}
		return CapabilityFactory.get(params, handle, HttpService.class);
	}
}
