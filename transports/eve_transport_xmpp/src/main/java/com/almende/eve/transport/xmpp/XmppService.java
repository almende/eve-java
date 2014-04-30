/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.transport.xmpp;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jivesoftware.smack.SmackConfiguration;

import com.almende.eve.capabilities.Capability;
import com.almende.eve.capabilities.handler.Handler;
import com.almende.eve.transport.Receiver;
import com.almende.eve.transport.Transport;
import com.almende.eve.transport.TransportService;
import com.almende.util.TypeUtil;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class XmppService.
 */
public class XmppService implements TransportService {
	private final Map<URI, Transport>	instances		= new ConcurrentHashMap<URI, Transport>();
	private boolean						doesShortcut	= true;
	private static final XmppService	singleton		= new XmppService();
	
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
	public static XmppService getInstanceByParams(final ObjectNode params) {
		// TODO: return different instance if doesShortcut does not agree with
		// current singleton.
		singleton.doesShortcut = new XmppTransportConfig(params)
				.getDoShortcut();
		return singleton;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.almende.eve.capabilities.CapabilityService#get(com.fasterxml.jackson.
	 * databind
	 * .JsonNode, com.almende.eve.capabilities.handler.Handler, java.lang.Class)
	 */
	@Override
	public <T extends Capability, V> T get(final ObjectNode params,
			final Handler<V> handle, final Class<T> type) {
		final Handler<Receiver> newHandle = Transport.TYPEUTIL.inject(handle);
		final XmppTransportConfig config = new XmppTransportConfig(params);
		final URI address = config.getAddress();
		Transport result = instances.get(address);
		
		if (result == null) {
			result = new XmppTransport(config, newHandle, this);
			instances.put(address, result);
		} else {
			result.getHandle().update(newHandle);
		}
		return TypeUtil.inject(result, type);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.almende.eve.transport.TransportService#delete(com.almende.eve.transport
	 * .Transport)
	 */
	@Override
	public void delete(final Transport instance) {
		instances.remove(instance.getAddress());
	}
	
	@Override
	public Transport getLocal(final URI address) {
		if (doesShortcut && instances.containsKey(address)) {
			return instances.get(address);
		}
		return null;
	}
	
}
