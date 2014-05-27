/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.test;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.junit.Test;

import com.almende.eve.capabilities.handler.Handler;
import com.almende.eve.transport.LocalTransportConfig;
import com.almende.eve.transport.LocalTransportFactory;
import com.almende.eve.transport.Receiver;
import com.almende.eve.transport.Transport;
import com.almende.eve.transport.TransportFactory;
import com.almende.eve.transport.ws.WebsocketTransportConfig;
import com.almende.eve.transport.ws.WsClientTransport;
import com.almende.eve.transport.ws.WsClientTransportFactory;
import com.almende.eve.transport.ws.WsServerTransportFactory;
import com.almende.eve.transport.zmq.ZmqTransportConfig;
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
				new MyReceiver());
		transport.connect();
		
		transport.send(URI.create("xmpp:gloria@openid.almende.org"),
				"Hello World", null);
		
		try {
			Thread.sleep(10000);
		} catch (final InterruptedException e) {
		}
	}
	
	/**
	 * Test Zmq.
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void testZmq() throws IOException {
		final ZmqTransportConfig config = new ZmqTransportConfig();
		config.setAddress("zmq://tcp://127.0.0.1:5678");
		
		final Transport transport = TransportFactory.getTransport(config,
				new MyReceiver());
		transport.connect();
		
		transport.send(URI.create("zmq://tcp://127.0.0.1:5678"), "Hello World",
				null);
	}
	
	/**
	 * Test local transport.
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void testLocal() throws IOException {
		final LocalTransportConfig config = new LocalTransportConfig("testMe");
		
		final Transport transport = LocalTransportFactory.get(config,
				new MyReceiver());
		
		transport.send(URI.create("local:testMe"), "Hello World", null);
	}
	
	/**
	 * Test Websocket transport.
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void testWs() throws IOException {
		final WebsocketTransportConfig serverConfig = new WebsocketTransportConfig();
		serverConfig.setAddress("ws://localhost:8082/ws/testServer");
		
		serverConfig.setServletLauncher("JettyLauncher");
		final ObjectNode jettyParms = JOM.createObjectNode();
		jettyParms.put("port", 8082);
		serverConfig.put("jetty", jettyParms);
		
		final Transport server = WsServerTransportFactory.get(serverConfig,
				new MyReceiver());
		
		final WebsocketTransportConfig clientConfig = new WebsocketTransportConfig();
		clientConfig.setId("testClient");
		clientConfig.setServerUrl("ws://localhost:8082/ws/testServer");
		
		final WsClientTransport client = WsClientTransportFactory.get(
				clientConfig, new MyReceiver());
		client.connect();
		
		server.send(URI.create("wsclient:testClient"), "Hi there!", null);
		
		client.send(URI.create("ws://localhost:8082/ws/testServer"),
				"Good day to you!", null);
		
	}
	
	/**
	 * The Class myReceiver.
	 */
	public class MyReceiver implements Receiver, Handler<Receiver> {
		
		/*
		 * (non-Javadoc)
		 * 
		 * @see com.almende.eve.transport.Receiver#receive(java.lang.Object,
		 * java.net.URI, java.lang.String)
		 */
		@Override
		public void receive(final Object msg, final URI senderUrl,
				final String tag) {
			
			LOG.warning("Received msg:'" + msg + "' from: "
					+ senderUrl.toASCIIString());
		}
		
		/*
		 * (non-Javadoc)
		 * 
		 * @see com.almende.eve.capabilities.handler.Handler#get()
		 */
		@Override
		public Receiver get() {
			return this;
		}
		
		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * com.almende.eve.capabilities.handler.Handler#update(com.almende.eve
		 * .capabilities.handler.Handler)
		 */
		@Override
		public void update(final Handler<Receiver> newHandler) {
			// Not used, data should be the same.
		}
		
		/*
		 * (non-Javadoc)
		 * 
		 * @see com.almende.eve.capabilities.handler.Handler#getKey()
		 */
		@Override
		public String getKey() {
			// Not used, data should be the same.
			return null;
		}
		
	}
}
