/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.transport.pubnub;

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
 * The Class PubNubTransportBuilder.
 */
public class PubNubTransportBuilder extends
		AbstractCapabilityBuilder<PubNubTransport> {
	private final Map<URI, PubNubTransport>	instances	= new ConcurrentHashMap<URI, PubNubTransport>();
	private static PubNubService				singleton	= null;

	@Override
	public PubNubTransport build() {
		if (singleton == null) {
			singleton = new PubNubService();
			singleton.doesShortcut = PubNubTransportConfig.decorate(getParams())
					.getDoShortcut();
		}
		return singleton.get(getParams(), getHandle());
	}

	class PubNubService implements TransportService {
		private boolean	doesShortcut	= true;

		/**
		 * Gets the actual PubNub transport
		 * 
		 * @param <T>
		 *            the generic type
		 * @param <V>
		 *            the value type
		 * @param params
		 *            the params
		 * @param handle
		 *            the handle
		 * @return the pubnub transport
		 */
		public <T extends Capability, V> PubNubTransport get(
				final ObjectNode params, final Handler<V> handle) {
			final Handler<Receiver> newHandle = Transport.TYPEUTIL
					.inject(handle);
			final PubNubTransportConfig config = PubNubTransportConfig
					.decorate(params);
			final URI address = config.getAddress();
			PubNubTransport result = instances.get(address);

			if (result == null) {
				result = new PubNubTransport(config, newHandle, this);
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
