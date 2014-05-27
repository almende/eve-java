/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.transport.ws;

import java.io.IOException;
import java.net.URI;

import javax.websocket.RemoteEndpoint.Basic;

import com.almende.eve.capabilities.handler.Handler;
import com.almende.eve.transport.AbstractTransport;
import com.almende.eve.transport.Receiver;
import com.almende.eve.transport.TransportService;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class WebsocketTransport.
 */
public abstract class WebsocketTransport extends AbstractTransport {
	
	/**
	 * Instantiates a new websocket transport.
	 * 
	 * @param address
	 *            the address
	 * @param handle
	 *            the handle
	 * @param service
	 *            the service
	 * @param params
	 *            the params
	 */
	public WebsocketTransport(final URI address, final Handler<Receiver> handle,
			final TransportService service, final ObjectNode params) {
		super(address, handle, service, params);
	}
	
	protected abstract void registerRemote(String key, Basic remote);
	
	/**
	 * Receive.
	 * 
	 * @param body
	 *            the body
	 * @param id
	 *            the id
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public abstract void receive(final String body, final String id)
			throws IOException;
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.almende.eve.transport.Transport#connect()
	 */
	@Override
	public void connect() throws IOException {
		// Nothing to do on the server side.
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.almende.eve.transport.Transport#disconnect()
	 */
	@Override
	public void disconnect() {
		// Nothing to do on the server side.
	}
}
