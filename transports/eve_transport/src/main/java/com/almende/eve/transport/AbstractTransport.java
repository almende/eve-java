/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.transport;

import java.net.URI;

import com.almende.eve.capabilities.handler.Handler;

/**
 * The Class AbstractTransport.
 */
public abstract class AbstractTransport implements Transport {
	private TransportService	service	= null;
	private Handler<Receiver>	handle	= null;
	private URI					address	= null;
	
	/**
	 * Instantiates a new abstract transport.
	 * 
	 * @param address
	 *            the address
	 * @param handle
	 *            the handler method of this transport owner
	 * @param service
	 *            the service that created this transport instance
	 */
	
	public AbstractTransport(final URI address, final Handler<Receiver> handle,
			final TransportService service) {
		this.address = address;
		this.service = service;
		this.handle = handle;
	}
	
	/* (non-Javadoc)
	 * @see com.almende.eve.transport.Transport#delete()
	 */
	@Override
	public void delete() {
		if (service != null) {
			disconnect();
			service.delete(this);
		}
	}
	
	/**
	 * Gets the handle.
	 * 
	 * @return the handle
	 */
	@Override
	public Handler<Receiver> getHandle() {
		return handle;
	}
	
	/* (non-Javadoc)
	 * @see com.almende.eve.transport.Transport#getAddress()
	 */
	@Override
	public URI getAddress() {
		return address;
	}
	
	/**
	 * Sets the address.
	 * 
	 * @param address
	 *            the new address
	 */
	public void setAddress(final URI address) {
		this.address = address;
	}
}
