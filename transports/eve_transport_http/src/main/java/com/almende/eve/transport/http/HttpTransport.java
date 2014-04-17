/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.transport.http;

import java.io.IOException;
import java.net.URI;

import com.almende.eve.capabilities.handler.Handler;
import com.almende.eve.transport.AbstractTransport;
import com.almende.eve.transport.Receiver;
import com.almende.eve.transport.TransportService;

/**
 * The Class HttpTransport.
 */
public class HttpTransport  extends AbstractTransport {

	/**
	 * Instantiates a new http transport.
	 * 
	 * @param address
	 *            the address
	 * @param handle
	 *            the handle
	 * @param service
	 *            the service
	 */
	public HttpTransport(URI address, Handler<Receiver> handle,
			TransportService service) {
		super(address, handle, service);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see com.almende.eve.transport.Transport#send(java.net.URI, java.lang.String, java.lang.String)
	 */
	@Override
	public void send(URI receiverUri, String message, String tag)
			throws IOException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see com.almende.eve.transport.Transport#send(java.net.URI, byte[], java.lang.String)
	 */
	@Override
	public void send(URI receiverUri, byte[] message, String tag)
			throws IOException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see com.almende.eve.transport.Transport#connect()
	 */
	@Override
	public void connect() throws IOException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see com.almende.eve.transport.Transport#disconnect()
	 */
	@Override
	public void disconnect() {
		// TODO Auto-generated method stub
		
	}
	
}
