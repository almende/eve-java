/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.agent;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.almende.eve.capabilities.wake.WakeHandler;
import com.almende.eve.capabilities.wake.WakeService;
import com.almende.eve.capabilities.wake.Wakeable;
import com.almende.eve.transport.Receiver;
import com.almende.eve.transport.Transport;
import com.almende.eve.transport.TransportFactory;
import com.almende.util.jackson.JOM;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class MyAgent.
 */
public class MyAgent implements Wakeable, Receiver {
	private static final Logger			LOG			= Logger.getLogger(TestWake.class
															.getName());
	private static final WakeService	ws			= new WakeService();
	private Transport					transport	= null;
	final ObjectNode					params		= JOM.createObjectNode();
	
	/**
	 * Instantiates a new my agent.
	 */
	public MyAgent() {
		
		params.put("class", "com.almende.eve.transport.xmpp.XmppService");
		params.put("address", "xmpp://alex@openid.almende.org/test");
		params.put("password", "alex");
		
	}
	
	/**
	 * Inits the agent.
	 */
	public void init() {
		ws.register("TestAgent", MyAgent.class.getName());
		
		transport = TransportFactory.getTransport(params,
				new WakeHandler<Receiver>(this, "TestAgent", ws));
		try {
			transport.connect();
			transport.send(URI.create("xmpp:gloria@openid.almende.org"),
					"I'm awake!", null);
		} catch (IOException e) {
			LOG.log(Level.WARNING, "Failed to connect XMPP.", e);
		}
	}
	
	@Override
	public void wake(String wakeKey) {
		transport = TransportFactory.getTransport(params,
				new WakeHandler<Receiver>(this, wakeKey, ws));
		
	}
	
	@Override
	public void receive(Object msg, URI senderUrl, String tag) {
		LOG.warning("Received msg:'" + msg + "' from: "
				+ senderUrl.toASCIIString());
	}
	
}
