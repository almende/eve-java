/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.algorithms.clustering;

import java.net.URI;

import com.almende.eve.capabilities.handler.Handler;
import com.almende.eve.protocol.Meta;
import com.almende.eve.protocol.Protocol;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class InboxProtocol, provides an easy way to get a single threaded agent,
 * only one inbound message in a single thread at a time.
 */
public class GlobalAddressProtocol implements Protocol {
	private GlobalAddressProtocolConfig	params	= null;

	/**
	 * Instantiates a new protocol tracer.
	 *
	 * @param params
	 *            the params
	 * @param handle
	 *            the handle
	 */
	public GlobalAddressProtocol(final ObjectNode params,
			final Handler<Object> handle) {
		this.params = GlobalAddressProtocolConfig.decorate(params);
	}

	@Override
	public ObjectNode getParams() {
		return this.params;
	}

	@Override
	public void delete() {}

	@Override
	public boolean inbound(Meta msg) {
		// just forwarding...
		return msg.nextIn();
	}

	@Override
	public boolean outbound(Meta msg) {
		// just forwarding...
		if (msg.getPeer().getScheme().equals("eve")) {
		    URI uri = GlobalAddressMapper.get().get(msg.getPeer().toASCIIString());
		    if(uri==null) {
		       throw new GlobalAddressMappingNotFoundException(); 
		    }
		    msg.setPeer(uri);
		}
		return msg.nextOut();
	}

}
