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
import com.almende.eve.transport.Receiver;
import com.almende.eve.transport.Transport;
import com.almende.eve.transport.TransportBuilder;
import com.almende.eve.transport.http.HttpTransportConfig;
import com.almende.util.jackson.JOM;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class TestHttp.
 */
public class TestHttp extends TestCase {
	private static final Logger	LOG	= Logger.getLogger(TestHttp.class.getName());
	
	/**
	 * Test http.
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void testHttp() throws IOException {
		final HttpTransportConfig config = new HttpTransportConfig();
		config.setServletUrl("http://localhost:8080/agents/");
		config.setId("testAgent");
		
		config.setServletLauncher("JettyLauncher");
		final ObjectNode jettyParms = JOM.createObjectNode();
		jettyParms.put("port", 8080);
		config.set("jetty", jettyParms);
		
		final Transport transport = new TransportBuilder().withConfig(config)
				.withHandle(new myReceiver()).build();
		
		transport.send(URI.create("http://localhost:8080/agents/testAgent"),
				"Hello World", null);
	}
	
	/**
	 * The Class myReceiver.
	 */
	public class myReceiver implements Receiver, Handler<Receiver> {
		
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
