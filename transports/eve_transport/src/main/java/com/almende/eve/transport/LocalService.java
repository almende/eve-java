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

import com.almende.eve.capabilities.Capability;
import com.almende.eve.capabilities.handler.Handler;
import com.almende.util.TypeUtil;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class LocalService, transport and transportService in one.
 */
public class LocalService extends AbstractTransport implements TransportService {
	private static final Logger					LOG			= Logger.getLogger(LocalService.class
																	.getName());
	private static final Map<URI, LocalService>	INSTANCES	= new ConcurrentHashMap<URI, LocalService>();
	private static final LocalService			SINGLETON	= new LocalService();
	
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
		super(address, handle, SINGLETON, params);
	}
	
	private LocalService() {
		super(null, null, null, null);
	}
	
	/**
	 * Gets the instance by params.
	 * 
	 * @param params
	 *            the params
	 * @return the instance by params
	 */
	public static LocalService getInstanceByParams(final ObjectNode params) {
		return SINGLETON;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.almende.eve.transport.TransportService#getLocal(java.net.URI)
	 */
	@Override
	public LocalService getLocal(final URI address) {
		return INSTANCES.get(address);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.almende.eve.capabilities.CapabilityService#get(com.fasterxml.jackson
	 * .databind.node.ObjectNode, com.almende.eve.capabilities.handler.Handler,
	 * java.lang.Class)
	 */
	@Override
	public <T extends Capability, V> T get(final ObjectNode params,
			final Handler<V> handle, final Class<T> type) {
		final Handler<Receiver> newHandle = Transport.TYPEUTIL.inject(handle);
		final LocalTransportConfig config = new LocalTransportConfig(params);
		final String id = config.getId();
		if (id == null) {
			LOG.warning("Parameter 'id' is required!");
			return null;
		}
		final URI address = URI.create("local:" + config.getId());
		LocalService result = getLocal(address);
		if (result == null) {
			result = new LocalService(address, newHandle, params);
			INSTANCES.put(address, result);
		} else {
			result.getHandle().update(newHandle);
		}
		return TypeUtil.inject(result, type);
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
		sendLocal(receiverUri, message);
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
		sendLocal(receiverUri, message);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.almende.eve.transport.Transport#connect()
	 */
	@Override
	public void connect() throws IOException {
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.almende.eve.transport.Transport#disconnect()
	 */
	@Override
	public void disconnect() {
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.almende.eve.transport.Transport#getProtocols()
	 */
	@Override
	public List<String> getProtocols() {
		return Arrays.asList("local");
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
		INSTANCES.remove(instance.getAddress());
	}
	
}
