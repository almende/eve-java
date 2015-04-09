/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.transport;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import com.almende.eve.capabilities.AbstractCapabilityBuilder;
import com.almende.eve.capabilities.handler.Handler;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class LocalTransportBuilder.
 */
public class LocalTransportBuilder extends AbstractCapabilityBuilder<Transport> {
	private static final Logger					LOG			= Logger.getLogger(LocalTransportBuilder.class
																	.getName());
	private static final Map<URI, LocalService>	INSTANCES	= new ConcurrentHashMap<URI, LocalService>(10);

	@Override
	public Transport build() {
		final Handler<Receiver> newHandle = Transport.TYPEUTIL
				.inject(getHandle());
		final LocalTransportConfig config = new LocalTransportConfig(
				getParams());
		final String id = config.getId();
		if (id == null) {
			LOG.warning("Parameter 'id' is required!");
			return null;
		}
		final URI address = URI.create("local:" + config.getId());
		LocalService result = getLocal(address);
		if (result == null) {
			result = new LocalService(address, newHandle, getParams());
			INSTANCES.put(address, result);
		} else {
			result.getHandle().update(newHandle);
		}
		return result;
	}

	/**
	 * Gets the local.
	 * 
	 * @param address
	 *            the address
	 * @return the local
	 */
	public LocalService getLocal(final URI address) {
		return INSTANCES.get(address);
	}

	/**
	 * The Class LocalService.
	 */
	class LocalService extends AbstractTransport implements TransportService {
		/**
		 * Instantiates a new local transport.
		 * 
		 * @param address
		 *            the address
		 * @param handle
		 *            the handle
		 * @param params
		 *            the params
		 */
		public LocalService(final URI address, final Handler<Receiver> handle,
				final ObjectNode params) {
			super(address, handle, null, params);
			setService(this);
		}

		private LocalService() {
			super(null, null, null, null);
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * com.almende.eve.transport.TransportService#getLocal(java.net.URI)
		 */
		@Override
		public LocalService getLocal(final URI address) {
			return INSTANCES.get(address);
		}

		/*
		 * (non-Javadoc)
		 * @see com.almende.eve.transport.Transport#send(java.net.URI,
		 * java.lang.String, java.lang.String)
		 */
		@Override
		public void send(final URI receiverUri, final String message,
				final String tag) throws IOException {
			sendLocal(receiverUri, message);
		}

		/*
		 * (non-Javadoc)
		 * @see com.almende.eve.transport.Transport#send(java.net.URI, byte[],
		 * java.lang.String)
		 */
		@Override
		public void send(final URI receiverUri, final byte[] message,
				final String tag) throws IOException {
			sendLocal(receiverUri, message);
		}

		/*
		 * (non-Javadoc)
		 * @see com.almende.eve.transport.Transport#send(java.net.URI, byte[],
		 * java.lang.String)
		 */
		@Override
		public void send(final URI receiverUri, final Object message,
				final String tag) throws IOException {
			sendLocal(receiverUri, message);
		}

		/*
		 * (non-Javadoc)
		 * @see com.almende.eve.transport.Transport#connect()
		 */
		/**
		 * Connect.
		 * 
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		@Override
		public void connect() throws IOException {}

		/*
		 * (non-Javadoc)
		 * @see com.almende.eve.transport.Transport#disconnect()
		 */
		/**
		 * Disconnect.
		 */
		@Override
		public void disconnect() {}

		/*
		 * (non-Javadoc)
		 * @see com.almende.eve.transport.Transport#getProtocols()
		 */
		/**
		 * Gets the protocols.
		 * 
		 * @return the protocols
		 */
		@Override
		public List<String> getProtocols() {
			return Arrays.asList("local");
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
			INSTANCES.remove(instance.getAddress());
		}

	}
}