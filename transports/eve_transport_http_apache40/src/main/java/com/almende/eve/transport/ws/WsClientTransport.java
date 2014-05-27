/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.transport.ws;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.websocket.ClientEndpointConfig;
import javax.websocket.DeploymentException;
import javax.websocket.RemoteEndpoint.Basic;

import org.glassfish.tyrus.client.ClientManager;

import com.almende.eve.capabilities.handler.Handler;
import com.almende.eve.transport.Receiver;
import com.almende.eve.transport.TransportService;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class WebsocketTransport.
 */
public class WsClientTransport extends WebsocketTransport {
	private static final Logger	LOG			= Logger.getLogger(WsClientTransport.class
													.getName());
	private Basic				remote		= null;
	private URI					serverUrl	= null;
	private String				myId		= null;
	
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
	public WsClientTransport(final URI address, final Handler<Receiver> handle,
			final TransportService service, final ObjectNode params) {
		super(address, handle, service, params);
		final WebsocketTransportConfig config = new WebsocketTransportConfig(
				params);
		final String sURL = config.getServerUrl();
		if (sURL != null) {
			try {
				serverUrl = new URI(sURL);
			} catch (final URISyntaxException e) {
				LOG.log(Level.WARNING,
						"'serverUrl' parameter couldn't be parsed", e);
			}
		} else {
			LOG.warning("'serverUrl' parameter is required!");
		}
		myId = config.getId();
	}
	
	/**
	 * Sets the remote.
	 * 
	 * @param remote
	 *            the new remote
	 */
	@Override
	protected void registerRemote(final String key, final Basic remote) {
		this.remote = remote;
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
		super.getHandle().get().receive(body, serverUrl, null);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.almende.eve.transport.Transport#send(java.net.URI,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public void send(final URI receiverUri, final String message, final String tag)
			throws IOException {
		if (!receiverUri.equals(serverUrl)) {
			throw new IOException(
					"Currently it's only possible to send to the server agent directly, not other agents:"
							+ receiverUri.toASCIIString());
		}
		if (remote != null) {
			remote.sendText(message);
		} else {
			throw new IOException("Not connected?");
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.almende.eve.transport.Transport#send(java.net.URI, byte[],
	 * java.lang.String)
	 */
	@Override
	public void send(final URI receiverUri, final byte[] message, final String tag)
			throws IOException {
		if (!receiverUri.equals(serverUrl)) {
			throw new IOException(
					"Currently it's only possible to send to the server agent directly, not other agents:"
							+ receiverUri.toASCIIString());
		}
		if (remote != null) {
			remote.sendBinary(ByteBuffer.wrap(message));
		} else {
			throw new IOException("Not connected?");
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.almende.eve.transport.ws.WebsocketTransport#connect()
	 */
	@Override
	public void connect() throws IOException {
		final ClientManager client = ClientManager.createClient();
		try {
			final ClientEndpointConfig cec = ClientEndpointConfig.Builder.create()
					.build();
			cec.getUserProperties().put("address", getAddress());
			
			client.connectToServer(WebsocketEndpoint.class, cec, new URI(
					serverUrl + "?id=" + myId));
		} catch (final DeploymentException e) {
			LOG.log(Level.WARNING, "Can't connect to server", e);
		} catch (final URISyntaxException e) {
			LOG.log(Level.WARNING, "Can't parse server address", e);
		}
		
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.almende.eve.transport.ws.WebsocketTransport#disconnect()
	 */
	@Override
	public void disconnect() {
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.almende.eve.transport.Transport#getProtocols()
	 */
	@Override
	public List<String> getProtocols() {
		return Arrays.asList("wss", "ws");
	}
	
}
