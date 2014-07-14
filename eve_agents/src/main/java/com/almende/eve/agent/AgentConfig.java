/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.agent;

import com.almende.eve.capabilities.Config;
import com.almende.util.jackson.JOM;
import com.almende.util.uuid.UUID;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class AgentConfig.
 */
public class AgentConfig extends Config {
	
	/**
	 * Instantiates a new memory state config.
	 */
	public AgentConfig() {
		this(JOM.createObjectNode());
	}
	
	/**
	 * Instantiates a new memory state config.
	 * 
	 * @param id
	 *            the id
	 */
	public AgentConfig(final String id) {
		super(JOM.createObjectNode());
		setId(id);
	}
	
	/**
	 * Instantiates a new memory state config.
	 * 
	 * @param node
	 *            the node
	 */
	public AgentConfig(final ObjectNode node) {
		super(node);
		if (!node.has("id")) {
			this.put("id", new UUID().toString());
		}
	}
	
	/**
	 * Instantiates a new memory state config.
	 * 
	 * @param id
	 *            the id
	 * @param node
	 *            the node
	 */
	public AgentConfig(final String id, final ObjectNode node) {
		super(node);
		setId(id);
	}
	
	/**
	 * Sets the id.
	 * 
	 * @param id
	 *            the new id
	 */
	public void setId(final String id) {
		this.put("id", id);
	}
	
	/**
	 * Gets the id.
	 * 
	 * @return the id
	 */
	public String getId() {
		if (this.has("id")) {
			return this.get("id").asText();
		}
		return null;
	}
	
	/**
	 * Sets the transport config.
	 * 
	 * @param transport
	 *            the new transport config (can be ObjectNode or
	 *            ArrayNode, the latter indicating multiple transports)
	 */
	public void setTransport(final JsonNode transport) {
		this.set("transport", transport);
	}
	
	/**
	 * Gets the transport config.
	 * 
	 * @return the transport config
	 */
	public JsonNode getTransport() {
		if (this.has("transport")) {
			return this.get("transport");
		}
		return null;
	}
	
	/**
	 * Sets the state config.
	 * 
	 * @param state
	 *            config
	 *            the new state config
	 */
	public void setState(final ObjectNode state) {
		this.set("state", state);
	}
	
	/**
	 * Gets the state config.
	 * 
	 * @return the state config
	 */
	public ObjectNode getState() {
		if (this.has("state")) {
			return (ObjectNode) this.get("state");
		}
		return null;
	}
	
	/**
	 * Sets the scheduler config.
	 * 
	 * @param scheduler
	 *            config
	 *            the new scheduler config
	 */
	public void setScheduler(final ObjectNode scheduler) {
		this.set("scheduler", scheduler);
	}
	
	/**
	 * Gets the scheduler config.
	 * 
	 * @return the schedule configr
	 */
	public ObjectNode getScheduler() {
		if (this.has("scheduler")) {
			return (ObjectNode) this.get("scheduler");
		}
		return null;
	}
}
