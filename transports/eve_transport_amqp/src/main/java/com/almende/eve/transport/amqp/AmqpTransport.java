/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.transport.amqp;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

import com.almende.eve.capabilities.handler.Handler;
import com.almende.eve.transport.AbstractTransport;
import com.almende.eve.transport.Receiver;
import com.almende.eve.transport.amqp.AmqpTransportBuilder.AmqpService;
import com.almende.util.callback.AsyncCallback;

/**
 * The Class XmppTransport.
 */
public class AmqpTransport extends AbstractTransport  {

	/**
	 * Instantiates a new amqp transport.
	 *
	 * @param config
	 *            the config
	 * @param newHandle
	 *            the new handle
	 * @param amqpService
	 *            the amqp service
	 */
	public AmqpTransport(AmqpTransportConfig config,
			Handler<Receiver> newHandle, AmqpService amqpService) {
		super(URI.create(config.get("address").asText()), newHandle, amqpService, config);
	}

	/* (non-Javadoc)
	 * @see com.almende.eve.transport.Transport#send(java.net.URI, java.lang.String, java.lang.String)
	 */
	@Override
	public <T> void send(final URI receiverUri, final String message, final String tag, final AsyncCallback<T> callback)
			throws IOException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see com.almende.eve.transport.Transport#send(java.net.URI, byte[], java.lang.String)
	 */
	@Override
	public <T> void send(final URI receiverUri, final byte[] message, final String tag, final AsyncCallback<T> callback)
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

	/* (non-Javadoc)
	 * @see com.almende.eve.transport.Transport#getProtocols()
	 */
	@Override
	public List<String> getProtocols() {
		return Arrays.asList("amqp");
	}

}
