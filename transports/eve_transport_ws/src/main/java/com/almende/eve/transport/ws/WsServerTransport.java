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
import java.util.Map;
import java.util.Set;

import javax.websocket.CloseReason;
import javax.websocket.RemoteEndpoint.Async;
import javax.websocket.Session;

import com.almende.eve.capabilities.handler.Handler;
import com.almende.eve.transport.Receiver;
import com.almende.eve.transport.TransportService;
import com.almende.util.URIUtil;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class WebsocketTransport.
 */
public class WsServerTransport extends WebsocketTransport {
	private final Map<URI, Async>	remotes	= new HashMap<URI, Async>();
	
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
	public WsServerTransport(final URI address, final Handler<Receiver> handle,
			final TransportService service, final ObjectNode params) {
		super(address, handle, service, params);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.almende.eve.transport.ws.WebsocketTransport#onClose(javax.websocket
	 * .Session, javax.websocket.CloseReason)
	 */
	@Override
	public void onClose(final Session session, final CloseReason closeReason) {
		super.onClose(session, closeReason);
		if (session.getUserProperties().containsKey("remoteId")) {
			final String remoteId = (String) session.getUserProperties().get(
					"remoteId");
			final URI key = URIUtil.create("wsclient:" + remoteId);
			remotes.remove(key);
		}
	}
	
	/**
	 * Gets the remotes.
	 * 
	 * @return the remotes
	 */
	public Set<URI> getRemotes() {
		return remotes.keySet();
	}
	
	/**
	 * Register remote.
	 * 
	 * @param key
	 *            the key
	 * @param remote
	 *            the remote
	 */
	@Override
	protected void registerRemote(final String id, final Async remote) {
		final URI key = URI.create("wsclient:" + id);
		remotes.put(key, remote);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.almende.eve.transport.ws.WebsocketTransport#receive(java.lang.String,
	 * java.net.URI)
	 */
	@Override
	public void receive(final String body, final String id) throws IOException {
		final URI senderUrl = URI.create("wsclient:" + id);
		super.getHandle().get().receive(body, senderUrl, null);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.almende.eve.transport.Transport#send(java.net.URI,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public void send(final URI receiverUri, final String message,
			final String tag) throws IOException {
		if (remotes.containsKey(receiverUri)) {
			final Async remote = remotes.get(receiverUri);
			remote.sendText(message);
			remote.flushBatch();
		} else {
			throw new IOException("Remote: " + receiverUri.toASCIIString()
					+ " is currently not connected. (" + getAddress() + " / "
					+ remotes.keySet() + ")");
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.almende.eve.transport.Transport#send(java.net.URI, byte[],
	 * java.lang.String)
	 */
	@Override
	public void send(final URI receiverUri, final byte[] message,
			final String tag) throws IOException {
		if (remotes.containsKey(receiverUri)) {
			final Async remote = remotes.get(receiverUri);
			remote.sendBinary(ByteBuffer.wrap(message));
			remote.flushBatch();
		} else {
			throw new IOException("Remote: " + receiverUri.toASCIIString()
					+ " is currently not connected.");
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.almende.eve.transport.Transport#getProtocols()
	 */
	@Override
	public List<String> getProtocols() {
		return Arrays.asList("wsclient");
	}
	
}
