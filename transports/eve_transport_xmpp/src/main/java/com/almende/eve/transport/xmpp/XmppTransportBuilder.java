/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.transport.xmpp;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jivesoftware.smack.SmackConfiguration;

import com.almende.eve.capabilities.AbstractCapabilityBuilder;
import com.almende.eve.capabilities.Capability;
import com.almende.eve.capabilities.handler.Handler;
import com.almende.eve.transport.Receiver;
import com.almende.eve.transport.Transport;
import com.almende.eve.transport.TransportService;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class XmppService.
 */
public class XmppTransportBuilder extends
		AbstractCapabilityBuilder<XmppTransport> {
	private final Map<URI, XmppTransport>	instances	= new ConcurrentHashMap<URI, XmppTransport>();
	private static XmppService				singleton	= null;
	
	// Needed to force Android loading the ReconnectionManager....
	static {
		try {
			Class.forName("org.jivesoftware.smack.ReconnectionManager");
		} catch (final ClassNotFoundException ex) {
			// problem loading reconnection manager
		}
		SmackConfiguration.setPacketReplyTimeout(15000);
	}
	
	@Override
	public XmppTransport build() {
		if (singleton == null) {
			singleton = new XmppService();
			singleton.doesShortcut = new XmppTransportConfig(getParams())
					.getDoShortcut();
		}
		return singleton.get(getParams(), getHandle());
	}
	
	class XmppService implements TransportService {
		private boolean	doesShortcut	= true;
		
		/**
		 * Gets the actual XMPP transport
		 * 
		 * @param <T>
		 *            the generic type
		 * @param <V>
		 *            the value type
		 * @param params
		 *            the params
		 * @param handle
		 *            the handle
		 * @return the xmpp transport
		 */
		public <T extends Capability, V> XmppTransport get(
				final ObjectNode params, final Handler<V> handle) {
			final Handler<Receiver> newHandle = Transport.TYPEUTIL
					.inject(handle);
			final XmppTransportConfig config = new XmppTransportConfig(params);
			final URI address = config.getAddress();
			XmppTransport result = instances.get(address);
			
			if (result == null) {
				result = new XmppTransport(config, newHandle, this);
				instances.put(address, result);
			} else {
				result.getHandle().update(newHandle);
			}
			return result;
		}
		
		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * com.almende.eve.transport.TransportService#delete(com.almende.eve.
		 * transport
		 * .Transport)
		 */
		@Override
		public void delete(final Transport instance) {
			instances.remove(instance.getAddress());
		}
		
		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * com.almende.eve.transport.TransportService#getLocal(java.net.URI)
		 */
		@Override
		public Transport getLocal(final URI address) {
			if (doesShortcut && instances.containsKey(address)) {
				return instances.get(address);
			}
			return null;
		}
	}
}
