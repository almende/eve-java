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
import com.almende.eve.protocol.jsonrpc.annotation.Access;
import com.almende.eve.protocol.jsonrpc.annotation.AccessType;
import com.almende.eve.protocol.jsonrpc.annotation.Name;
import com.almende.eve.protocol.jsonrpc.annotation.Namespace;
import com.almende.eve.protocol.jsonrpc.formats.JSONRequest;
import com.almende.eve.protocol.jsonrpc.formats.Params;
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
	 * @param start
	 *            the start
	 */
	public EventAgent(String id, ObjectNode config, URI start) {
		super(id, config);
		if (start != null) {
			try {
				getGraph().addNode2SFN(start, "SFN", 3, 7);
			} catch (IOException e) {
				LOG.log(Level.WARNING, "Couldn't join SFN!", e);
			}
		}
		events = new EventBus(getScheduler(), caller, getGraph(), "SFN");
	}

	/**
	 * Receive event.
	 *
	 * @param message
	 *            the message
	 */
	@Access(AccessType.PUBLIC)
	public void receiveEvent(@Name("message") String message) {
		LOG.warning(getId() + ": Received event:" + message);
	}

	/**
	 * Send event.
	 *
	 * @param message
	 *            the message
	 */
	public void sendEvent(String message) {
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
	public EventBus getEventBus(){
		return events;
	}
}
