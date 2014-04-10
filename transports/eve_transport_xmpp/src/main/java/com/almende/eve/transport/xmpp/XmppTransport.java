/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.transport.xmpp;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.codec.binary.Base64;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;

import com.almende.eve.capabilities.handler.Handler;
import com.almende.eve.transport.AbstractTransport;
import com.almende.eve.transport.Receiver;
import com.almende.eve.transport.TransportService;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * The Class XmppTransport.
 */
public class XmppTransport extends AbstractTransport implements PacketListener {
	private static final Logger	LOG			= Logger.getLogger(XmppTransport.class
													.getSimpleName());
	private XMPPConnection		conn		= null;
	private String				serviceName	= null;
	private String				host		= null;
	private String				username	= null;
	private int					port		= 0;
	private String				resource	= null;
	private String				password	= null;
	
	/**
	 * Instantiates a new xmpp transport.
	 * 
	 * @param service
	 *            the service
	 * @param params
	 * @param handle
	 */
	public <V> XmppTransport(final JsonNode params,
			final Handler<Receiver> handle, final TransportService service) {
		super(URI.create(params.get("address").asText()), handle, service);
		
		URI address = super.getAddress();
		host = address.getHost();
		port = address.getPort();
		username = address.getUserInfo();
		resource = address.getPath();
		if (serviceName == null) {
			serviceName = host;
		}
	}
	
	@Override
	public void send(URI receiverUri, String message, String tag)
			throws IOException {
		if (!isConnected()) {
			reconnect();
		}
		if (isConnected()) {
			
			// send the message
			final Message reply = new Message();
			reply.setTo(receiverUri.toASCIIString().replace("xmpp:", ""));
			reply.setBody(message);
			conn.sendPacket(reply);
		} else {
			throw new IOException("Cannot send request, not connected");
		}
		
	}
	
	@Override
	public void send(URI receiverUri, byte[] message, String tag)
			throws IOException {
		send(receiverUri, Base64.encodeBase64String(message), tag);
	}
	
	private boolean isConnected() {
		return (conn != null) ? conn.isConnected() : false;
	}
	
	@Override
	public void reconnect() throws IOException {
		if (isConnected()) {
			// this is a reconnect.
			disconnect();
		}
		
		// configure and connect
		final ConnectionConfiguration connConfig = new ConnectionConfiguration(
				host, port, serviceName);
		
		connConfig.setSASLAuthenticationEnabled(true);
		connConfig.setReconnectionAllowed(true);
		connConfig.setCompressionEnabled(true);
		connConfig.setRosterLoadedAtLogin(false);
		conn = new XMPPConnection(connConfig);
		try {
			conn.connect();
			
			// login
			if (resource == null) {
				conn.login(username, password);
			} else {
				conn.login(username, password, resource);
			}
			
			// set presence to available
			final Presence presence = new Presence(Presence.Type.available);
			conn.sendPacket(presence);
			
			// set acceptance to all
			conn.getRoster().setSubscriptionMode(
					Roster.SubscriptionMode.accept_all);
		} catch (final XMPPException e) {
			LOG.log(Level.WARNING, "", e);
			throw new IOException("Failed to connect to messenger", e);
		}
	}
	
	@Override
	public void disconnect() {
		if (isConnected()) {
			conn.disconnect();
			conn = null;
		}
	}
	
	@Override
	public void processPacket(final Packet packet) {
		final Message message = (Message) packet;
		
		// Check if resource is given and matches local resource. If not
		// equal, silently drop packet.
		final String to = message.getTo();
		if (resource != null && to != null) {
			final int index = to.indexOf('/');
			if (index > 0) {
				final String res = to.substring(index + 1);
				if (!resource.equals(res)) {
					LOG.warning("Received stanza meant for another agent, disregarding. "
							+ res);
					return;
				}
			}
		}
		final String body = message.getBody();
		final URI senderUrl = URI.create("xmpp:" + message.getFrom());
		if (body != null) {
			super.getHandle().get().receive(body, senderUrl, null);
		}
	}
}
