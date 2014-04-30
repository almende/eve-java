/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.transport.zmq;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.almende.eve.capabilities.Capability;
import com.almende.eve.capabilities.handler.Handler;
import com.almende.eve.transport.Receiver;
import com.almende.eve.transport.Transport;
import com.almende.eve.transport.TransportService;
import com.almende.util.TypeUtil;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class ZmqService.
 */
public class ZmqService implements TransportService {
	private final Map<URI, Transport>	instances		= new ConcurrentHashMap<URI, Transport>();
	private boolean						doesShortcut	= true;
	private static final ZmqService		singleton		= new ZmqService();
	
	/**
	 * Gets the instance by params.
	 * 
	 * @param params
	 *            the params
	 * @return the instance by params
	 */
	public static ZmqService getInstanceByParams(final ObjectNode params) {
		// TODO: return different instance if doesShortcut does not agree with
		// current singleton.
		singleton.doesShortcut = new ZmqTransportConfig(params).getDoShortcut();
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
		final ZmqTransportConfig config = new ZmqTransportConfig(params);
		final URI address = config.getAddress();
		Transport result = instances.get(address);
		
		if (result == null) {
			result = new ZmqTransport(config, newHandle, this);
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
