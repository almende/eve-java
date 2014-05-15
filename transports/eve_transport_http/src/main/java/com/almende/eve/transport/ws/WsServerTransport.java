/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.transport.ws;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.websocket.RemoteEndpoint.Basic;

import com.almende.eve.capabilities.handler.Handler;
import com.almende.eve.transport.Receiver;
import com.almende.eve.transport.TransportService;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class WebsocketTransport.
 */
public class WsServerTransport extends WebsocketTransport {
	private HashMap<URI, Basic>	remotes	= new HashMap<URI, Basic>();
	
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
	public WsServerTransport(URI address, Handler<Receiver> handle,
			TransportService service, ObjectNode params) {
		super(address, handle, service, params);
	}

	/**
	 * Register remote.
	 * 
	 * @param key
	 *            the key
	 * @param remote
	 *            the remote
	 */
	protected void registerRemote(String id, Basic remote){
		final URI key = URI.create("wsclient:"+id);
		remotes.put(key, remote);
	}
	
	/* (non-Javadoc)
	 * @see com.almende.eve.transport.ws.WebsocketTransport#receive(java.lang.String, java.net.URI)
	 */
	@Override
	public void receive(final String body, final String id)
			throws IOException {
		final URI senderUrl = URI.create("wsclient:"+id);
		super.getHandle().get().receive(body, senderUrl, null);
	}
	
	/* (non-Javadoc)
	 * @see com.almende.eve.transport.Transport#send(java.net.URI, java.lang.String, java.lang.String)
	 */
	@Override
	public void send(URI receiverUri, String message, String tag)
			throws IOException {
		if (remotes.containsKey(receiverUri)) {
			Basic remote = remotes.get(receiverUri);
			System.err.println("Sending message:"+message);
			remote.sendText(message);
			remote.flushBatch();
		} else {
			throw new IOException("Remote: " + receiverUri.toASCIIString()
					+ " is currently not connected.");
		}		
	}
	
	/* (non-Javadoc)
	 * @see com.almende.eve.transport.Transport#send(java.net.URI, byte[], java.lang.String)
	 */
	@Override
	public void send(URI receiverUri, byte[] message, String tag)
			throws IOException {
		if (remotes.containsKey(receiverUri)) {
			Basic remote = remotes.get(receiverUri);
			remote.sendBinary(ByteBuffer.wrap(message));
		} else {
			throw new IOException("Remote: " + receiverUri.toASCIIString()
					+ " is currently not connected.");
		}		
	}
	
	/* (non-Javadoc)
	 * @see com.almende.eve.transport.Transport#getProtocols()
	 */
	@Override
	public List<String> getProtocols() {
		return Arrays.asList("wsclient");
	}
	
}
