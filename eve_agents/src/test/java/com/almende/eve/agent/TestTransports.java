/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.agent;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.junit.Test;

import com.almende.eve.capabilities.data.Receiver;
import com.almende.eve.capabilities.handler.Handler;
import com.almende.eve.transport.Transport;
import com.almende.eve.transport.TransportFactory;
import com.almende.util.jackson.JOM;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class TestTransports.
 */
public class TestTransports extends TestCase {
	private static final Logger	LOG	= Logger.getLogger(TestTransports.class
											.getName());
	
	/**
	 * Test Xmpp.
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void testXmpp() throws IOException {
		final ObjectNode params = JOM.createObjectNode();
		params.put("class", "com.almende.eve.transport.xmpp.XmppService");
		params.put("address", "xmpp://alex@openid.almende.org/test");
		params.put("password", "alex");
		
		final Transport transport = TransportFactory.getTransport(params,
				new myReceiver());
		transport.connect();
		
		transport.send(URI.create("xmpp:gloria@openid.almende.org"),
				"Hello World", null);
		
		try {
			Thread.sleep(10000);
		} catch (final InterruptedException e) {
		}
	}
	
	/**
	 * Test Xmpp.
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void testZmq() throws IOException {
		final ObjectNode params = JOM.createObjectNode();
		params.put("class", "com.almende.eve.transport.zmq.ZmqService");
		params.put("address", "zmq://tcp://127.0.0.1:5678");
		
		final Transport transport = TransportFactory.getTransport(params,
				new myReceiver());
		transport.connect();
		
		transport.send(URI.create("zmq://tcp://127.0.0.1:5678"), "Hello World",
				null);
	}
	
	/**
	 * The Class myReceiver.
	 */
	public class myReceiver implements Receiver, Handler<Receiver> {
		
		/* (non-Javadoc)
		 * @see com.almende.eve.transport.Receiver#receive(java.lang.Object, java.net.URI, java.lang.String)
		 */
		@Override
		public void receive(final Object msg, final URI senderUrl, final String tag) {
			
			LOG.warning("Received msg:'" + msg + "' from: "
					+ senderUrl.toASCIIString());
		}
		
		/* (non-Javadoc)
		 * @see com.almende.eve.capabilities.handler.Handler#get()
		 */
		@Override
		public Receiver get() {
			return this;
		}
		
		/* (non-Javadoc)
		 * @see com.almende.eve.capabilities.handler.Handler#update(com.almende.eve.capabilities.handler.Handler)
		 */
		@Override
		public void update(final Handler<Receiver> newHandler) {
			// Not used, data should be the same.
		}
		
	}
}
