/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.algorithms.test.agents;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.almende.eve.algorithms.EventBus;
import com.almende.eve.algorithms.agents.NodeAgent;
import com.almende.eve.protocol.jsonrpc.annotation.Access;
import com.almende.eve.protocol.jsonrpc.annotation.AccessType;
import com.almende.eve.protocol.jsonrpc.annotation.Name;
import com.almende.eve.protocol.jsonrpc.annotation.Namespace;
import com.almende.eve.protocol.jsonrpc.annotation.Sender;
import com.almende.eve.protocol.jsonrpc.formats.JSONRequest;
import com.almende.eve.protocol.jsonrpc.formats.Params;
import com.almende.util.URIUtil;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class EventAgent.
 */
public class EventAgent extends NodeAgent {
	private static final Logger	LOG		= Logger.getLogger(EventAgent.class
												.getName());

	private EventBus			events	= null;

	/**
	 * Instantiates a new event agent.
	 *
	 * @param id
	 *            the id
	 * @param config
	 *            the config
	 */
	public EventAgent(String id, ObjectNode config) {
		super(id, config);
		events = new EventBus(getScheduler(), caller, getGraph(), "SFN");
		addNode2SFN("SFN");

	}
	
	/**
	 * Receive event.
	 *
	 * @param message
	 *            the message
	 */
	@Access(AccessType.PUBLIC)
	public void receiveEvent(@Name("message") String message) {
		if (message.startsWith("report_")) {
			final String target = message.substring(7);
			if (target.equals("*") || target.equals(getId())) {
				try {
					caller.call(URIUtil.create("local:0"), "reportEventReceived",
							null);
				} catch (IOException e) {
					LOG.log(Level.WARNING, "Couldn't send report?", e);
				}
			}
		}
	}

	/**
	 * Send event.
	 *
	 * @param message
	 *            the message
	 */
	public void sendEvent(String message) {
		getState().clear();
		
		final Params params = new Params();
		params.add("message", message);
		events.sendEvent(new JSONRequest("receiveEvent", params));
	}

	/**
	 * Gets the event bus.
	 *
	 * @return the event bus
	 */
	@Namespace("event")
	public EventBus getEventBus() {
		return events;
	}

	/**
	 * Report event received.
	 *
	 * @param sender
	 *            the sender
	 */
	@Access(AccessType.PUBLIC)
	public void reportEventReceived(@Sender URI sender) {
		getState().put(sender.toASCIIString().replace("local:", ""), true);
	}

	/**
	 * Count received.
	 *
	 * @param max
	 *            the max
	 * @return the int
	 */
	public int countReceived(int max) {
		int count = 0;
		for (int i = 0; i < max; i++) {
			if (getState().containsKey("" + i)) {
				count++;
			}
		}
		return count;
	}
}
