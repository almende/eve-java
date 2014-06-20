/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.transport.ws;

import java.io.EOFException;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.websocket.CloseReason;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;

/**
 * The Class WebsocketEndpoint.
 */
public class WebsocketEndpoint extends Endpoint {
	private static final Logger	LOG			= Logger.getLogger(WebsocketEndpoint.class
													.getName());
	private WebsocketTransport	transport	= null;
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.websocket.Endpoint#onOpen(javax.websocket.Session,
	 * javax.websocket.EndpointConfig)
	 */
	@Override
	public void onOpen(final Session session, final EndpointConfig config) {
		final RemoteEndpoint.Async remote = session.getAsyncRemote();
		final URI address = (URI) config.getUserProperties().get("address");
		transport = WebsocketTransportBuilder.get(address);
		
		Map<String, List<String>> queryparms = session.getRequestParameterMap();
		String remoteId = null;
		for (final Entry<String, List<String>> param : queryparms.entrySet()) {
			if (param.getKey().equals("id")) {
				remoteId = param.getValue().get(0);
			}
		}
		if (remoteId != null) {
			session.getUserProperties().put("remoteId", remoteId);
		}
		try {
			remote.setBatchingAllowed(true);
		} catch (IOException e1) {
			LOG.log(Level.WARNING, "Failed to switch on Batching", e1);
		}
		
		transport.registerRemote(remoteId, remote);
		transport.setConnected(true);
		
		final String id = remoteId;
		session.addMessageHandler(new MessageHandler.Whole<String>() {
			@Override
			public void onMessage(final String text) {
				
				new Thread(new Runnable() {
					
					@Override
					public void run() {
						try {
							transport.receive(text, id);
						} catch (final IOException e) {
							LOG.log(Level.WARNING, "Failed to receive message",
									e);
						}
					}
				}).start();
			}
			
		});
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.websocket.Endpoint#onClose(javax.websocket.Session,
	 * javax.websocket.CloseReason)
	 */
	@Override
	public void onClose(final Session session, final CloseReason closeReason) {
		transport.onClose(session, closeReason);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.websocket.Endpoint#onError(javax.websocket.Session,
	 * java.lang.Throwable)
	 */
	@Override
	public void onError(final Session session, final Throwable throwable) {
		LOG.log(Level.WARNING, "Websocket connection error:", throwable);
		if (throwable instanceof SocketTimeoutException) {
			transport.onClose(session, new CloseReason(
					CloseReason.CloseCodes.CLOSED_ABNORMALLY,
					"Timeout on Socket!"));
		}
		if (throwable instanceof EOFException) {
			transport.onClose(session, new CloseReason(
					CloseReason.CloseCodes.CLOSED_ABNORMALLY, "EOF!"));
		}
	}
}
