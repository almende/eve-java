/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.agent;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.almende.util.ClassUtil;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class AgentBuilder.
 */
public class AgentBuilder {
	private static final Logger	LOG			= Logger.getLogger(AgentBuilder.class
													.getName());
	private ObjectNode			parameters	= null;
	private ClassLoader			cl			= getClass().getClassLoader();

	/**
	 * With config
	 * 
	 * @param config
	 *            the config
	 * @return the agent builder
	 */
	public AgentBuilder with(final ObjectNode config) {
		parameters = config;
		return this;
	}

	/**
	 * Set a specific classloader for creating this agent.
	 * 
	 * @param cl
	 *            the cl
	 * @return the agent builder
	 */
	public AgentBuilder withClassLoader(ClassLoader cl) {
		if (cl != null) {
			this.cl = cl;
		}
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
		final AgentConfig params = AgentConfig.decorate(parameters);
		final String className = params.getClassName();
		if (className == null) {
			LOG.warning("AgentBuilder requires configuration parameters: class");
			return null;
		}
		try {
			final Class<?> clazz = Class.forName(className, true, cl);
			if (clazz.equals(Agent.class)
					|| ClassUtil.hasSuperClass(clazz, Agent.class)) {
				final Agent agent = (Agent) clazz.newInstance();
				agent.setConfig(parameters);
				return agent;
			} else {
				LOG.warning("The requested class doesn't extend Agent, which is required for the AgentBuilder");
			}
		} catch (Exception e) {
			LOG.log(Level.WARNING, "Couldn't instantiate the agentclass", e);
		}
		return null;
	}

}
