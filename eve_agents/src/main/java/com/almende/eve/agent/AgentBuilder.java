/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.agent;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.almende.eve.capabilities.wake.WakeService;
import com.almende.util.ClassUtil;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class AgentBuilder.
 */
public class AgentBuilder {
	private static final Logger	LOG			= Logger.getLogger(AgentBuilder.class
													.getName());
	private AgentConfig			parameters	= null;
	private WakeService			ws			= null;
	
	/**
	 * With config
	 * 
	 * @param config
	 *            the config
	 * @return the agent builder
	 */
	public AgentBuilder with(final ObjectNode config) {
		parameters = new AgentConfig(config);
		return this;
	}
	
	/**
	 * With WakeService ws.
	 * 
	 * @param ws
	 *            the ws
	 * @return the agent builder
	 */
	public AgentBuilder withWakeService(final WakeService ws) {
		this.ws = ws;
		return this;
	}
	
	/**
	 * Builds the.
	 * 
	 * @return the agent
	 */
	public Agent build() {
		if (parameters == null) {
			LOG.warning("AgentBuilder is not yet initialized!");
			return null;
		}
		final String className = parameters.getClassName();
		if (className == null) {
			LOG.warning("AgentBuilder requires configuration parameters: class");
			return null;
		}
		try {
			final Class<?> clazz = Class.forName(className);
			if (ClassUtil.hasSuperClass(clazz, Agent.class)) {
				if (ws != null && ClassUtil.hasSuperClass(clazz, WakeableAgent.class)){
					final WakeableAgent wagent = (WakeableAgent) clazz.newInstance();
					wagent.setConfig(parameters, ws);
					return wagent;
				} else {
					if (ws != null) {
						LOG.warning("Requested to build an agent with a WakeService, but agent doesn't extend WakeableAgent!");
					}
					final Agent agent = (Agent) clazz.newInstance();
					agent.setConfig(parameters);
					return agent;
				}
			} else {
				LOG.warning("The requested class doesn't extend Agent, which is required for the AgentBuilder");
			}
		} catch (Exception e) {
			LOG.log(Level.WARNING, "Couldn't instantiate the agentclass", e);
		}
		return null;
	}
	
}
