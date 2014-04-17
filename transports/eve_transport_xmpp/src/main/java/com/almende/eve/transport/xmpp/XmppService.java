/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.transport.xmpp;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jivesoftware.smack.SmackConfiguration;

import com.almende.eve.capabilities.handler.Handler;
import com.almende.eve.transport.Receiver;
import com.almende.eve.transport.Transport;
import com.almende.eve.transport.TransportService;
import com.almende.util.TypeUtil;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * The Class XmppService.
 */
public class XmppService implements TransportService {
	private final Map<URI, Transport>			instances	= new ConcurrentHashMap<URI, Transport>();
	private static final XmppService	singleton	= new XmppService();
	
	// Needed to force Android loading the ReconnectionManager....
	static {
		try {
			Class.forName("org.jivesoftware.smack.ReconnectionManager");
		} catch (final ClassNotFoundException ex) {
			// problem loading reconnection manager
		}
		SmackConfiguration.setPacketReplyTimeout(15000);
	}
	
	/**
	 * Gets the instance by params.
	 * 
	 * @param params
	 *            the params
	 * @return the instance by params
	 */
	public static XmppService getInstanceByParams(final JsonNode params) {
		return singleton;
	}
	
	/* (non-Javadoc)
	 * @see com.almende.eve.capabilities.Capability#get(com.fasterxml.jackson.databind.JsonNode, com.almende.eve.capabilities.handler.Handler, java.lang.Class)
	 */
	@Override
	public <T, V> T get(final JsonNode params, final Handler<V> handle,
			final Class<T> type) {
		final Handler<Receiver> newHandle = Transport.TYPEUTIL.inject(handle);
		final URI address = URI.create(params.get("address").asText());
		Transport result = instances.get(address);
		
		if (result == null) {
			result = new XmppTransport(params, newHandle, this);
			instances.put(address, result);
		} else {
			result.getHandle().update(newHandle);
		}
		return TypeUtil.inject(result, type);
	}
	
	/* (non-Javadoc)
	 * @see com.almende.eve.transport.TransportService#delete(com.almende.eve.transport.Transport)
	 */
	@Override
	public void delete(final Transport instance) {
		instances.remove(instance.getAddress());
	}
	
}
