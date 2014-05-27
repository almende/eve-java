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
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.websocket.CloseReason;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

/**
 * The Class WebsocketEndpoint.
 */
public class WebsocketEndpoint extends Endpoint {
	private static final Logger	LOG			= Logger.getLogger(WebsocketEndpoint.class
													.getName());
	private WebsocketTransport	transport	= null;
	
	/* (non-Javadoc)
	 * @see javax.websocket.Endpoint#onOpen(javax.websocket.Session, javax.websocket.EndpointConfig)
	 */
	@Override
	public void onOpen(Session session, EndpointConfig config) {
		final RemoteEndpoint.Basic remote = session.getBasicRemote();
		final URI address = (URI) config.getUserProperties().get("address");
		transport = WebsocketService.get(address);
		
		final URI requestURI = session.getRequestURI();
		final List<NameValuePair> queryparms = URLEncodedUtils.parse(
				requestURI, "UTF-8");
		
		String remoteId = null;
		for (NameValuePair param : queryparms) {
			if (param.getName().equals("id")) {
				remoteId = param.getValue();
			}
		}
		if (remoteId != null) {
			session.getUserProperties().put("remoteId", remoteId);
		}
		transport.registerRemote(remoteId, remote);
		transport.setConnected(true);
		
		final String id = remoteId;
		session.addMessageHandler(new MessageHandler.Whole<String>() {
			public void onMessage(String text) {
				try {
					transport.receive(text, id);
				} catch (IOException e) {
					LOG.log(Level.WARNING, "Failed to receive message", e);
				}
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
		if (throwable instanceof SocketTimeoutException){
			transport.onClose(session, new CloseReason(CloseReason.CloseCodes.CLOSED_ABNORMALLY, "Timeout on Socket!"));
		};
		if (throwable instanceof EOFException){
			transport.onClose(session, new CloseReason(CloseReason.CloseCodes.CLOSED_ABNORMALLY, "EOF!"));
		};

	}
}
