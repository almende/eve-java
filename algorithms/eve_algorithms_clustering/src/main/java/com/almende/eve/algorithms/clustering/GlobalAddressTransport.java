/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.algorithms.clustering;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.almende.eve.capabilities.handler.Handler;
import com.almende.eve.transport.AbstractTransport;
import com.almende.eve.transport.Receiver;
import com.almende.eve.transport.TransportService;
import com.almende.util.URIUtil;
import com.almende.util.callback.AsyncCallback;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class GlobalAddressTransport.
 */
public class GlobalAddressTransport extends AbstractTransport {
	private static final Logger	LOG				= Logger.getLogger(GlobalAddressTransport.class
														.getName());
	private URI					mappedAddress	= null;

	/**
	 * Instantiates a new global address transport.
	 *
	 * @param address
	 *            the address
	 * @param handle
	 *            the handle
	 * @param service
	 *            the service
	 * @param params
	 *            the params
	 */
	public GlobalAddressTransport(URI address, Handler<Receiver> handle,
			TransportService service, ObjectNode params) {
		super(address, handle, service, params);
		GlobalAddressTransportConfig config = GlobalAddressTransportConfig
				.decorate(params);
		try {
			mappedAddress = URIUtil.parse(config.getRealAddressPattern()
					+ config.getId());
		} catch (URISyntaxException e) {
			LOG.log(Level.WARNING,
					"Failed to create URI for global address mapping:"
							+ (config.getRealAddressPattern() + config.getId()),
					e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.almende.eve.transport.Transport#send(java.net.URI,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public <T> void send(URI receiverUri, String message, String tag, AsyncCallback<T> callback)
			throws IOException {
		throw new IOException("This transport can't send anything!");
	}

	/*
	 * (non-Javadoc)
	 * @see com.almende.eve.transport.Transport#send(java.net.URI, byte[],
	 * java.lang.String)
	 */
	@Override
	public <T> void send(URI receiverUri, byte[] message, String tag, AsyncCallback<T> callback)
			throws IOException {
		throw new IOException("This transport can't send anything!");
	}

	/*
	 * (non-Javadoc)
	 * @see com.almende.eve.transport.Transport#connect()
	 */
	@Override
	public void connect() throws IOException {
		GlobalAddressMapper.get().put(getAddress().toASCIIString(),
				mappedAddress);
	}

	/*
	 * (non-Javadoc)
	 * @see com.almende.eve.transport.Transport#disconnect()
	 */
	@Override
	public void disconnect() {
		GlobalAddressMapper.get().remove(getAddress().toASCIIString());
	}

	/*
	 * (non-Javadoc)
	 * @see com.almende.eve.transport.Transport#getProtocols()
	 */
	@Override
	public List<String> getProtocols() {
		return Arrays.asList(new String[] { "eve" });
	}

}
