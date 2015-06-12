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
 * The Class AgentConfig, containing a java bean representation of the various
 * JSON items that can be configured for each agent.
 */
public class AgentConfig extends Config {
	private static final Logger	LOG	= Logger.getLogger(AgentConfig.class
											.getName());

	/**
	 * Instantiates a new config.
	 */
	public AgentConfig() {
		super();
	}

	/**
	 * Instantiates a new config.
	 * 
	 * @param id
	 *            the id
	 */
	public AgentConfig(final String id) {
		super();
		setId(id);
	}

	/**
	 * Decorate.
	 *
	 * @param node
	 *            the node
	 * @return the agent config
	 */
	public static AgentConfig decorate(final ObjectNode node) {
		final AgentConfig res = new AgentConfig();
		res.extend(node);
		if (!res.has("id")) {
			res.put("id", new UUID().toString());
		}
		return res;
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
	 * @param transports
	 *            the new transport config
	 */
	public void setTransports(final ArrayNode transports) {
		this.set("transports", transports);
	}

	/**
	 * Adds the transport.
	 *
	 * @param transport
	 *            the transport
	 */
	public void addTransport(final ObjectNode transport) {
		if (this.getTransports() == null) {
			this.setTransports(JOM.createArrayNode());
		}
		this.getTransports().add(transport);
	}

	/**
	 * Sets the transport.
	 *
	 * @deprecated Please use setTransports(transport[]) or
	 *             addTransport(transport) instead
	 * @param transport
	 *            the new transport
	 */
	@Deprecated
	public void setTransport(final JsonNode transport) {
		if (transport == null) {
			setTransports(JOM.createArrayNode());
		} else if (transport.isArray()) {
			setTransports((ArrayNode) transport);
		} else {
			ArrayNode other = JOM.createArrayNode();
			other.add(transport);
			setTransports(other);
		}
	}

	/**
	 * Gets the transport config.
	 * 
	 * @return the transport config
	 */
	public ArrayNode getTransports() {
		final JsonNode res = this.get("transports");
		if (res != null && !res.isArray()) {
			LOG.warning("This configuration is deprecated! Transports have to be an array!");
			final ArrayNode other = JOM.createArrayNode();
			other.add(res);
			return other;
		}
		if (res == null && this.has("transport")) {
			LOG.warning("This configuration is deprecated! Transport should be renamed to 'transports' and needs to be an array.");
			final ArrayNode other = JOM.createArrayNode();
			final JsonNode transport = this.get("transport");
			if (transport.isArray()) {
				other.addAll((ArrayNode) transport);
			} else {
				other.add(transport);
			}
			return other;
		}
		return (ArrayNode) res;
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
