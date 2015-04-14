/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.transport.amqp;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
public class AmqpTransportBuilder extends
		AbstractCapabilityBuilder<AmqpTransport> {
	private final Map<URI, AmqpTransport>	instances	= new ConcurrentHashMap<URI, AmqpTransport>();
	private static AmqpService				singleton	= null;

	@Override
	public AmqpTransport build() {
		if (singleton == null) {
			singleton = new AmqpService();
			singleton.doesShortcut = AmqpTransportConfig.decorate(getParams())
					.getDoShortcut();
		}
		return singleton.get(getParams(), getHandle());
	}

	class AmqpService implements TransportService {
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
		public <T extends Capability, V> AmqpTransport get(
				final ObjectNode params, final Handler<V> handle) {
			final Handler<Receiver> newHandle = Transport.TYPEUTIL
					.inject(handle);
			final AmqpTransportConfig config = AmqpTransportConfig
					.decorate(params);
			final URI address = config.getAddress();
			AmqpTransport result = instances.get(address);

			if (result == null) {
				result = new AmqpTransport(config, newHandle, this);
				instances.put(address, result);
			} else {
				result.getHandle().update(newHandle);
			}
			return result;
		}

		/*
		 * (non-Javadoc)
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
