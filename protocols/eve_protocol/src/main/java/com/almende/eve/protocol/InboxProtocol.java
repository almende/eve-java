/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.protocol;

import java.net.URI;

import com.almende.eve.capabilities.handler.Handler;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class InboxProtocol.
 */
public class InboxProtocol implements Protocol {

	/**
	 * Instantiates a new inbox protocol.
	 *
	 * @param params
	 *            the params
	 * @param handle
	 *            the handle (Ignored for this protocol)
	 */
	public InboxProtocol(final ObjectNode params, final Handler<Object> handle) {
		
	}
	
	/* (non-Javadoc)
	 * @see com.almende.eve.capabilities.Capability#getParams()
	 */
	@Override
	public ObjectNode getParams() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.almende.eve.capabilities.Capability#delete()
	 */
	@Override
	public void delete() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.almende.eve.protocol.Protocol#inbound(java.lang.Object, java.net.URI)
	 */
	@Override
	public Meta inbound(Object msg, URI senderUrl) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.almende.eve.protocol.Protocol#outbound(java.lang.Object, java.net.URI)
	 */
	@Override
	public Meta outbound(Object msg, URI recipientUrl) {
		// TODO Auto-generated method stub
		return null;
	}

}
