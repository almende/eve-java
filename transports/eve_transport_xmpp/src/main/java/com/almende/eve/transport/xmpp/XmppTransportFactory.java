/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.transport.xmpp;

import com.almende.eve.capabilities.CapabilityFactory;
import com.almende.eve.capabilities.handler.Handler;
import com.almende.eve.transport.Receiver;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * A factory for creating XmppTransport objects.
 */
public class XmppTransportFactory {
	/**
	 * Gets the transport.
	 * 
	 * @param params
	 *            the params
	 * @param handle
	 *            the handle
	 * @return the state
	 */
	public static XmppService get(final JsonNode params,
			final Handler<Receiver> handle) {
		if (params.isObject() && !params.has("class")) {
			((ObjectNode) params).put("class", XmppTransportFactory.class
					.getPackage().getName() + ".XmppService");
		}
		return CapabilityFactory.get(params, handle, XmppService.class);
	}
}
