/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.transport.xmpp;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
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

/**
 * The Class XmppTransport.
 */
public class XmppTransport extends AbstractTransport implements PacketListener {
	private static final Logger	LOG			= Logger.getLogger(XmppTransport.class
													.getSimpleName());
	private final List<String>	protocols	= Arrays.asList("xmpp");
	
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
	 * @param config
	 *            the config
	 * @param handle
	 *            the handle
	 * @param service
	 *            the service
	 */
	public XmppTransport(final XmppTransportConfig config,
			final Handler<Receiver> handle, final TransportService service) {
		// TODO: support more parameter structures.
		super(config.getAddress(), handle, service, config);
		
		final URI address = super.getAddress();
		host = address.getHost();
		port = address.getPort();
		if (port < 0) {
			port = 5222;
		}
		username = address.getUserInfo();
		resource = address.getPath().substring(1);
		if (serviceName == null) {
			serviceName = host;
		}
		password = config.getPassword();
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
		// Check and deliver local shortcut.
		if (sendLocal(receiverUri, message)) {
			return;
		}
		if (!isConnected()) {
			connect();
		}
		if (isConnected()) {
			
			// send the message
			final Message msg = new Message();
			msg.setTo(receiverUri.toASCIIString().replace("xmpp:", ""));
			msg.setBody(message);
			conn.sendPacket(msg);
		} else {
			throw new IOException("Cannot send request, not connected");
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
		send(receiverUri, Base64.encodeBase64String(message), tag);
	}
	
	private boolean isConnected() {
		return (conn != null) ? conn.isConnected() : false;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.almende.eve.transport.Transport#connect()
	 */
	@Override
	public void connect() throws IOException {
		if (isConnected()) {
			// this is a reconnect.
			disconnect();
		}
		LOG.warning("Reconnect called:" + toString());
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
			
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {}
			// login
			if (resource == null) {
				conn.login(username, password);
			} else {
				conn.login(username, password, resource);
			}
			// instantiate a packet listener
			conn.addPacketListener(this, null);
			
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
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.almende.eve.transport.Transport#disconnect()
	 */
	@Override
	public void disconnect() {
		if (isConnected()) {
			conn.disconnect();
			conn = null;
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jivesoftware.smack.PacketListener#processPacket(org.jivesoftware.
	 * smack.packet.Packet)
	 */
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
	
	/**
	 * Checks if is available.
	 * 
	 * @param receiver
	 *            the receiver
	 * @return true, if is available
	 */
	public boolean isAvailable(String receiver) {
		// split url (xmpp:user/resource) into parts
		if (receiver.startsWith("xmpp:")) {
			receiver = receiver.substring(5, receiver.length());
		}
		final int slash = receiver.indexOf('/');
		if (slash <= 0) {
			return false;
		}
		final String res = receiver.substring(slash + 1, receiver.length());
		final String user = receiver.substring(0, slash);
		
		final Roster roster = conn.getRoster();
		
		final org.jivesoftware.smack.RosterEntry re = roster.getEntry(user);
		if (re == null) {
			LOG.info("subscribing to " + receiver);
			final Presence subscribe = new Presence(Presence.Type.subscribe);
			subscribe.setTo(receiver);
			conn.sendPacket(subscribe);
		}
		
		final Presence p = roster.getPresenceResource(user + "/" + res);
		LOG.info("Presence for " + user + "/" + res + " : " + p.getType());
		if (p.isAvailable()) {
			return true;
		}
		return false;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.almende.eve.transport.Transport#getProtocols()
	 */
	@Override
	public List<String> getProtocols() {
		return protocols;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "XmppTransport:" + super.getAddress().toASCIIString() + " ("
				+ username + "@" + host + ":" + port + "/" + resource + ")";
	}
}
