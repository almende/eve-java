/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.transport.ws;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Logger;

import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.RemoteEndpoint.Basic;
import javax.websocket.Session;

import com.almende.eve.capabilities.handler.Handler;
import com.almende.eve.transport.AbstractTransport;
import com.almende.eve.transport.Receiver;
import com.almende.eve.transport.TransportService;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class WebsocketTransport.
 */
public abstract class WebsocketTransport extends AbstractTransport {
	private static final Logger	LOG			= Logger.getLogger(WebsocketTransport.class
													.getName());
	private boolean				connected	= false;
	
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
	public WebsocketTransport(URI address, Handler<Receiver> handle,
			TransportService service, ObjectNode params) {
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
	
	/**
	 * On close.
	 * 
	 * @param session
	 *            the session
	 * @param closeReason
	 *            the close reason
	 */
	public void onClose(final Session session, final CloseReason closeReason) {
		LOG.warning("Connection closed:" + closeReason.getReasonPhrase() + "("
				+ closeReason.getCloseCode().getCode() + ":"
				+ closeReason.getCloseCode().toString() + ")");
		setConnected(false);
	}
	
	/**
	 * On close.
	 * 
	 * @param session
	 *            the session
	 * @param config
	 *            the config
	 */
	public void onOpen(Session session, EndpointConfig config) {
		setConnected(true);
	}
	
	@Override
	public void connect() throws IOException {
		// Nothing to do on the server side.
	}
	
	@Override
	public void disconnect() {
		// Nothing to do on the server side.
	}

	/**
	 * Checks if is connected.
	 * 
	 * @return true, if is connected
	 */
	public boolean isConnected() {
		return connected;
	}

	/**
	 * Sets the connected.
	 * 
	 * @param connected
	 *            the new connected
	 */
	public void setConnected(boolean connected) {
		this.connected = connected;
	}
}
