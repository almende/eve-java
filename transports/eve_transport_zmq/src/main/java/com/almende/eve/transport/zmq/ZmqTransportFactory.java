/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.transport.zmq;

import com.almende.eve.capabilities.CapabilityFactory;
import com.almende.eve.capabilities.handler.Handler;
import com.almende.eve.transport.Receiver;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * A factory for creating ZmqTransport objects.
 */
public class ZmqTransportFactory {
	/**
	 * Gets the transport.
	 * 
	 * @param params
	 *            the params
	 * @param handle
	 *            the handle
	 * @return the state
	 */
	public static ZmqTransport get(final ObjectNode params,
			final Handler<Receiver> handle) {
		if (params.isObject() && !params.has("class")) {
			params.put("class", ZmqTransportFactory.class.getPackage()
					.getName() + ".ZmqService");
		}
		return CapabilityFactory.get(params, handle, ZmqTransport.class);
	}
}
