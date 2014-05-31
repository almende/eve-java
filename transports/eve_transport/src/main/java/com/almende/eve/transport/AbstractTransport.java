/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.transport;

import java.net.URI;

import com.almende.eve.capabilities.handler.Handler;
import com.almende.util.threads.ThreadPool;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class AbstractTransport.
 */
public abstract class AbstractTransport implements Transport {
	private TransportService	service		= null;
	private Handler<Receiver>	handle		= null;
	private URI					address		= null;
	private ObjectNode			myParams	= null;
	
	/**
	 * Instantiates a new abstract transport.
	 * 
	 * @param address
	 *            the address
	 * @param handle
	 *            the handler method of this transport owner
	 * @param service
	 *            the service that created this transport instance
	 * @param params
	 *            the params
	 */
	
	public AbstractTransport(final URI address, final Handler<Receiver> handle,
			final TransportService service, final ObjectNode params) {
		this.address = address;
		this.service = service;
		this.handle = handle;
		myParams = params;
	}
	
	/*
	 * (non-Javadoc)
	 * 
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
	
	/*
	 * (non-Javadoc)
	 * 
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
	
	/**
	 * Gets the service.
	 * 
	 * @return the service
	 */
	public TransportService getService() {
		return service;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.almende.eve.capabilities.Capability#getParams()
	 */
	@Override
	public ObjectNode getParams() {
		return myParams;
	}

	/**
	 * Sets the params.
	 * 
	 * @param params
	 *            the new params
	 */
	public void setParams(ObjectNode params) {
		this.myParams = params;
	}

	/**
	 * Send local.
	 * 
	 * @param receiverUri
	 *            the receiver uri
	 * @param message
	 *            the message
	 * @return true, if successful
	 */
	public boolean sendLocal(final URI receiverUri, final Object message) {
		final Transport local = getService().getLocal(receiverUri);
		if (local != null) {
			// Do local shortcut.
			ThreadPool.getPool().execute(new Runnable() {
				@Override
				public void run() {
					local.getHandle().get()
							.receive(message, getAddress(), null);
				}
			});
			return true;
		}
		return false;
	}
}
