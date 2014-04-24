/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.transport;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.almende.eve.capabilities.handler.Handler;

/**
 * The Class Router, outbound transport selection based on protocol scheme.
 * 
 */
public class Router implements Transport {
	
	private Map<String, Transport>	transports	= new HashMap<String, Transport>();
	
	/**
	 * Register new transport. If a given protocol is already known, this will
	 * overwrite the reference.
	 * 
	 * @param transport
	 *            the transport
	 */
	public void register(Transport transport) {
		for (String protocol : transport.getProtocols()) {
			transports.put(protocol, transport);
		}
	}
	
	@Override
	public void send(URI receiverUri, String message, String tag)
			throws IOException {
		Transport transport = transports.get(receiverUri.getScheme()
				.toLowerCase());
		if (transport != null) {
			transport.send(receiverUri, message, tag);
		} else {
			throw new IOException("No transport known for scheme:"
					+ receiverUri.getScheme());
		}
	}
	
	@Override
	public void send(URI receiverUri, byte[] message, String tag)
			throws IOException {
		Transport transport = transports.get(receiverUri.getScheme()
				.toLowerCase());
		if (transport != null) {
			transport.send(receiverUri, message, tag);
		} else {
			throw new IOException("No transport known for scheme:"
					+ receiverUri.getScheme());
		}
	}
	
	@Override
	public void connect() throws IOException {
		
	}
	
	@Override
	public void disconnect() {
		
	}
	
	@Override
	public void delete() {
		
	}
	
	@Override
	public Handler<Receiver> getHandle() {
		return null;
	}
	
	@Override
	public URI getAddress() {
		return null;
	}
	
	@Override
	public List<String> getProtocols() {
		return new ArrayList<String>(transports.keySet());
	}
	
}
