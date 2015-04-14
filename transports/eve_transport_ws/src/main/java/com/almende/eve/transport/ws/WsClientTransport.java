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
import javax.websocket.CloseReason;
import javax.websocket.DeploymentException;
import javax.websocket.RemoteEndpoint.Async;
import javax.websocket.Session;

import org.glassfish.tyrus.client.ClientManager;

import com.almende.eve.capabilities.handler.Handler;
import com.almende.eve.transport.Receiver;
import com.almende.eve.transport.TransportService;
import com.almende.util.URIUtil;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class WebsocketTransport.
 */
public class WsClientTransport extends WebsocketTransport {
	private static final Logger	LOG			= Logger.getLogger(WsClientTransport.class
													.getName());
	private Async				remote		= null;
	private URI					serverUrl	= null;
	private String				myId		= null;
	private ClientManager		client		= null;
	private Session				session		= null;
	private Boolean				shouldClose	= false;

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
		final WebsocketTransportConfig config = WebsocketTransportConfig
				.decorate(params);
		final String sURL = config.getServerUrl();
		if (sURL != null) {
			try {
				serverUrl = URIUtil.parse(sURL);
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
	protected void registerRemote(final String key, final Async remote) {
		this.remote = remote;
	}

	/*
	 * (non-Javadoc)
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
	public void send(final byte[] message) throws IOException {
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
	public void send(final String message) throws IOException {
		send(serverUrl, message, null);
	}

	/*
	 * (non-Javadoc)
	 * @see com.almende.eve.transport.Transport#send(java.net.URI,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public void send(final URI receiverUri, final String message,
			final String tag) throws IOException {
		if (!receiverUri.equals(serverUrl)) {
			throw new IOException(
					"Currently it's only possible to send to the server agent directly, not other agents:"
							+ receiverUri.toASCIIString()
							+ " serverUrl:"
							+ serverUrl.toASCIIString());
		}
		if (remote == null || !isConnected()) {
			connect();
		}
		if (remote != null) {
			try {
				remote.sendText(message);
				remote.flushBatch();
			} catch (RuntimeException rte) {
				if (rte.getMessage().equals("Socket is not connected.")) {
					remote = null;
					// retry!
					send(receiverUri, message, tag);
				}
			}
		} else {
			throw new IOException("Not connected?");
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.almende.eve.transport.Transport#send(java.net.URI, byte[],
	 * java.lang.String)
	 */
	@Override
	public void send(final URI receiverUri, final byte[] message,
			final String tag) throws IOException {
		if (!receiverUri.equals(serverUrl)) {
			throw new IOException(
					"Currently it's only possible to send to the server agent directly, not other agents:"
							+ receiverUri.toASCIIString());
		}
		if (remote == null || !isConnected()) {
			connect();
		}
		if (remote != null) {
			try {
				remote.sendBinary(ByteBuffer.wrap(message));
				remote.flushBatch();
			} catch (RuntimeException rte) {
				if (rte.getMessage().equals("Socket is not connected.")) {
					remote = null;
					// retry!
					send(receiverUri, message, tag);
				}
			}
		} else {
			throw new IOException("Not connected?");
		}
	}

	@Override
	public void onClose(final Session session, final CloseReason closeReason) {
		if (!shouldClose) {
			try {
				connect();
			} catch (IOException e) {
				LOG.log(Level.WARNING, "Failed to reconnect", e);
				super.onClose(session, closeReason);
			}
		} else {
			super.onClose(session, closeReason);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.almende.eve.transport.ws.WebsocketTransport#connect()
	 */
	@Override
	public void connect() throws IOException {
		if (session != null) {
			return;
		}
		if (client == null) {
			client = ClientManager.createClient();
			client.setDefaultMaxSessionIdleTimeout(-1);
		}
		try {
			final ClientEndpointConfig cec = ClientEndpointConfig.Builder
					.create().build();
			cec.getUserProperties().put("address", getAddress());
			session = client.connectToServer(WebsocketEndpoint.class, cec,
					URIUtil.parse(serverUrl + "?id=" + myId));

		} catch (final DeploymentException e) {
			LOG.log(Level.WARNING, "Can't connect to server", e);
		} catch (final URISyntaxException e) {
			LOG.log(Level.WARNING, "Can't parse server address", e);
		}

	}

	/*
	 * (non-Javadoc)
	 * @see com.almende.eve.transport.ws.WebsocketTransport#disconnect()
	 */
	@Override
	public void disconnect() {
		try {
			shouldClose = true;

			session.close();

			shouldClose = false;
		} catch (IOException e) {
			LOG.log(Level.WARNING, "Failed to normally close session", e);
		}
		session = null;
	}

	/**
	 * Update config.
	 *
	 * @param config
	 *            the config
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void updateConfig(WebsocketTransportConfig config)
			throws IOException {
		setParams(config);
		final String sURL = config.getServerUrl();
		if (sURL != null) {
			try {
				serverUrl = URIUtil.parse(sURL);
			} catch (final URISyntaxException e) {
				LOG.log(Level.WARNING,
						"'serverUrl' parameter couldn't be parsed", e);
			}
		} else {
			LOG.warning("'serverUrl' parameter is required!");
		}
		myId = config.getId();
		connect();
	}

	/*
	 * (non-Javadoc)
	 * @see com.almende.eve.transport.Transport#getProtocols()
	 */
	@Override
	public List<String> getProtocols() {
		return Arrays.asList("wss", "ws");
	}

}
