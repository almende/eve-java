/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.agent;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.almende.eve.capabilities.Config;
import com.almende.eve.instantiation.CanHibernate;
import com.almende.util.AnnotationUtil;
import com.almende.util.AnnotationUtil.AnnotatedClass;
import com.almende.util.jackson.JOM;
import com.almende.util.uuid.UUID;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class AgentConfig.
 */
public class AgentConfig extends Config {
	private static final Logger	LOG	= Logger.getLogger(AgentConfig.class
											.getName());

	/**
	 * Instantiates a new config.
	 */
	public AgentConfig() {
		this(JOM.createObjectNode());
	}

	/**
	 * Instantiates a new config.
	 * 
	 * @param id
	 *            the id
	 */
	public AgentConfig(final String id) {
		super(JOM.createObjectNode());
		setId(id);
	}

	/**
	 * Instantiates a new config.
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
	 * Instantiates a new config.
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
	 * Sets the instantiation service.
	 *
	 * @param config
	 *            the new instantiation service
	 */
	public void setInstantiationService(final ObjectNode config) {
		this.set("instantiationService", config);
	}

	/**
	 * Gets the instantiation service.
	 *
	 * @return the instantiation service
	 */
	public ObjectNode getInstantiationService() {
		if (this.has("instantiationService")) {
			return (ObjectNode) this.get("instantiationService");
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

	/**
	 * Sets the protocols config. The agent will also add a JSONRpcProtocol to
	 * the
	 * stack.
	 *
	 * @param protocols
	 *            the new protocols
	 */
	public void setProtocols(final ArrayNode protocols) {
		this.set("protocols", protocols);
	}

	/**
	 * Gets the protocols.
	 *
	 * @return the protocols
	 */
	public ArrayNode getProtocols() {
		if (this.has("protocols")) {
			return (ArrayNode) this.get("protocols");
		}
		return null;
	}

	/**
	 * Checks if this agent can hibernate.
	 *
	 * @return true, if this agent can hibernate
	 */
	public boolean isCanHibernate() {
		if (this.has("canHibernate")) {
			return this.get("canHibernate").asBoolean();
		}
		AnnotatedClass ac = null;
		String className = this.getClassName();
		if (className != null) {
			try {
				ac = AnnotationUtil.get(Class.forName(className));
			} catch (ClassNotFoundException e) {
				LOG.log(Level.WARNING, "Couldn't load agent class", e);
			}
		}
		return (ac != null && ac.getAnnotation(CanHibernate.class) != null);
	}

	/**
	 * Sets the canHibernate value.
	 *
	 * @param canHibernate
	 *            the new canHibernate value
	 */
	public void setCanHibernate(boolean canHibernate) {
		this.put("canHibernate", canHibernate);
	}

}
