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
import com.almende.eve.test.TestWake;
import com.almende.eve.transport.Receiver;
import com.almende.eve.transport.Transport;
import com.almende.eve.transport.TransportFactory;
import com.almende.eve.transport.xmpp.XmppTransportConfig;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class MyAgent.
 */
public class MyAgent implements Wakeable, Receiver {
	private static final Logger	LOG			= Logger.getLogger(TestWake.class
													.getName());
	private WakeService			ws			= new WakeService();
	private Transport			transport	= null;
	private String				wakeKey		= null;
	
	/**
	 * Instantiates a new my agent.
	 */
	public MyAgent() {
	}
	
	/**
	 * Instantiates a new my agent.
	 * 
	 * @param wakeKey
	 *            the wake key
	 * @param ws
	 *            the ws
	 */
	public MyAgent(final String wakeKey, final WakeService ws) {
		this.wakeKey = wakeKey;
		if (ws != null) {
			this.ws = ws;
		}
		
	}
	
	/**
	 * Inits the agent.
	 */
	public void init() {
		
		final XmppTransportConfig config = new XmppTransportConfig();
		config.setAddress("xmpp://alex@openid.almende.org/" + wakeKey);
		config.setPassword("alex");
		
		ws.register(wakeKey, config, MyAgent.class.getName());
		
		transport = TransportFactory.getTransport(config,
				new WakeHandler<Receiver>(this, wakeKey, ws));
		try {
			transport.connect();
			transport.send(URI.create("xmpp:gloria@openid.almende.org"),
					"I'm awake!", null);
		} catch (final IOException e) {
			LOG.log(Level.WARNING, "Failed to connect XMPP.", e);
		}
	}
	
	@Override
	public void wake(final String wakeKey, final ObjectNode params,
			final boolean onBoot) {
		this.wakeKey = wakeKey;
		transport = TransportFactory.getTransport(params,
				new WakeHandler<Receiver>(this, this.wakeKey, ws));
		
		if (onBoot) {
			try {
				transport.connect();
			} catch (final IOException e) {
				LOG.log(Level.WARNING, "Failed to connect XMPP.", e);
			}
		}
	}
	
	@Override
	public void receive(final Object msg, final URI senderUrl, final String tag) {
		LOG.warning("Received msg:'" + msg + "' from: "
				+ senderUrl.toASCIIString());
	}
	
}
