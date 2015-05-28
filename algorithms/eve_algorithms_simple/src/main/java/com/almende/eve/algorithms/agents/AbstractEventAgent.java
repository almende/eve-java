/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.algorithms.agents;

import com.almende.eve.algorithms.EventBus;
import com.almende.eve.protocol.jsonrpc.annotation.Namespace;
import com.almende.eve.protocol.jsonrpc.formats.JSONRequest;
import com.almende.eve.protocol.jsonrpc.formats.Params;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class AbstractEventAgent, implements the full EventBus system, except for
 * the "receiveEvent" method.
 */
public abstract class AbstractEventAgent extends NodeAgent implements
		EventAgentInterface {

	private EventBus	events	= null;
	private String		tag		= "SFN";

	/**
	 * Instantiates a new event agent.
	 *
	 * @param id
	 *            the id
	 * @param config
	 *            the config
	 */
	public AbstractEventAgent(String id, ObjectNode config) {
		super(id, config);
		if (config.has("tag")) {
			this.tag = config.get("tag").asText();
		}
		events = new EventBus(getScheduler(), caller, getGraph(), tag);
		addNode2SFN(tag);

	}

	public void sendEvent(String message, long expiryAge) {
		getState().clear();

		final Params params = new Params();
		params.add("message", message);
		events.sendEvent(new JSONRequest("receiveEvent", params), expiryAge);
	}

	@Namespace("event")
	public EventBus getEventBus() {
		return events;
	}
}
