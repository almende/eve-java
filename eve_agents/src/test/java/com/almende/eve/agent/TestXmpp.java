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

import com.almende.eve.capabilities.handler.Handler;
import com.almende.eve.capabilities.handler.SimpleHandler;
import com.almende.eve.transport.Receiver;
import com.almende.eve.transport.Transport;
import com.almende.eve.transport.TransportFactory;
import com.almende.util.jackson.JOM;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class TestXmpp.
 */
public class TestXmpp extends TestCase {
	private static final Logger LOG = Logger.getLogger(TestXmpp.class.getName()); 
	/**
	 * Test file state.
	 * @throws IOException 
	 */
	@Test
	public void testXmpp() throws IOException {
		String testAccount = "xmpp://alex@openid.almende.org/test";
		String password = "alex";
		
		ObjectNode params = JOM.createObjectNode();
		params.put("class","com.almende.eve.transport.xmpp.XmppService");
		params.put("address", testAccount);
		params.put("password", password);
		
		Handler<Receiver> myHandler = new SimpleHandler<Receiver>(new myReceiver());
		Transport transport = TransportFactory.getTransport(params, myHandler);
		transport.reconnect();
		
		transport.send(URI.create("xmpp:gloria@openid.almende.org"), "Hello World", null);
		
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {}
	}
	
	/**
	 * The Class myReceiver.
	 */
	public class myReceiver implements Receiver{
		@Override
		public void receive(Object msg, URI senderUrl, String tag) {
			
			LOG.warning("Received msg:'"+msg + "' from: "+senderUrl.toASCIIString());
		}
		
	}
}
