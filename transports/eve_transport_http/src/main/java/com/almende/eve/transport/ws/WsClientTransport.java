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
	private ClientManager		client		= null;
	
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
	public WsClientTransport(URI address, Handler<Receiver> handle,
			TransportService service, ObjectNode params) {
		super(address, handle, service, params);
		final WebsocketTransportConfig config = new WebsocketTransportConfig(
				params);
		String sURL = config.getServerUrl();
		if (sURL != null) {
			try {
				serverUrl = new URI(sURL);
			} catch (URISyntaxException e) {
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
	protected void registerRemote(String key, Basic remote) {
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
	
	/**
	 * Send.
	 * 
	 * @param message
	 *            the message
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void send(byte[] message) throws IOException {
		send(serverUrl, message, null);
	}
	
	/**
	 * Send.
	 * 
	 * @param message
	 *            the message
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void send(String message) throws IOException {
		send(serverUrl, message, null);
	}
	
	@Override
	public void send(URI receiverUri, String message, String tag)
			throws IOException {
		if (!receiverUri.equals(serverUrl)) {
			throw new IOException(
					"Currently it's only possible to send to the server agent directly, not other agents:"
							+ receiverUri.toASCIIString() + " serverUrl:"+serverUrl.toASCIIString());
		}
		if (remote == null || !isConnected()) {
			connect();
		}
		if (remote != null) {
			remote.sendText(message);
		} else {
			throw new IOException("Not connected?");
		}
	}
	
	@Override
	public void send(URI receiverUri, byte[] message, String tag)
			throws IOException {
		if (!receiverUri.equals(serverUrl)) {
			throw new IOException(
					"Currently it's only possible to send to the server agent directly, not other agents:"
							+ receiverUri.toASCIIString());
		}
		if (remote == null || !isConnected()) {
			connect();
		}
		if (remote != null) {
			remote.sendBinary(ByteBuffer.wrap(message));
		} else {
			throw new IOException("Not connected?");
		}
	}
	
	@Override
	public void connect() throws IOException {
		if (client == null) {
			client = ClientManager.createClient();
			client.setDefaultMaxSessionIdleTimeout(-1);
		}
		try {
			ClientEndpointConfig cec = ClientEndpointConfig.Builder.create()
					.build();
			cec.getUserProperties().put("address", getAddress());
			
			client.connectToServer(WebsocketEndpoint.class, cec, new URI(
					serverUrl + "?id=" + myId));
		} catch (DeploymentException e) {
			LOG.log(Level.WARNING, "Can't connect to server", e);
		} catch (URISyntaxException e) {
			LOG.log(Level.WARNING, "Can't parse server address", e);
		}
		
	}
	
	@Override
	public void disconnect() {
	}
	
	@Override
	public List<String> getProtocols() {
		return Arrays.asList("wss", "ws");
	}
	
}
