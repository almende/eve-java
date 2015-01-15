/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.agent;

import java.util.HashMap;
import java.util.Map;

import com.almende.eve.scheduling.SimpleSchedulerConfig;
import com.almende.eve.state.memory.MemoryStateConfig;
import com.almende.util.TypeUtil;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class AgentHost, this is a backward compatibility class for easier
 * migration of Eve2X implementations to Eve3.
 */
@Deprecated
public class AgentHost {
	private static final AgentHost	SINGLETON	= new AgentHost();
	private Map<String, Agent>		agents		= new HashMap<String, Agent>();

	/**
	 * Gets the single instance of AgentHost.
	 *
	 * @return single instance of AgentHost
	 */
	public static AgentHost getInstance() {
		return SINGLETON;
	}

	/**
	 * Gets the agent.
	 *
	 * @param agentId
	 *            the agent id
	 * @return the agent
	 */
	public Agent getAgent(final String agentId) {
		Agent result = null;
		if (agents.containsKey(agentId)) {
			result = agents.get(agentId);
		}
		return result;
	}

	/**
	 * Delete agent.
	 *
	 * @param agentId
	 *            the agent id
	 */
	public void deleteAgent(final String agentId) {
		if (agents.containsKey(agentId)) {
			agents.remove(agentId);
		}
	}

	/**
	 * Adds the agent.
	 *
	 * @param agentId
	 *            the agent id
	 * @param agent
	 *            the agent
	 */
	public void addAgent(final String agentId, final Agent agent) {
		agents.put(agentId, agent);
	}

	/**
	 * Creates the agent.
	 *
	 * @param agentType
	 *            the agent type
	 * @param agentId
	 *            the agent id
	 * @return the agent
	 */
	public Agent createAgent(String agentType, String agentId) {
		final AgentConfig config = new AgentConfig();
		return createAgent(agentType, agentId, config);
	}

	/**
	 * Creates the agent.
	 *
	 * @param <T>
	 *            the generic type
	 * @param agentType
	 *            the agent type
	 * @param agentId
	 *            the agent id
	 * @return the t
	 */
	public <T extends Agent> T createAgent(Class<T> agentType, String agentId) {
		final AgentConfig config = new AgentConfig();
		return createAgent(agentType, agentId, config);
	}

	/**
	 * Creates the agent.
	 *
	 * @param agentType
	 *            the agent type
	 * @param agentId
	 *            the agent id
	 * @param config
	 *            the config
	 * @return the agent
	 */
	public Agent createAgent(String agentType, String agentId,
			final ObjectNode config) {
		final AgentConfig conf = new AgentConfig(config);
		conf.setClassName(agentType);
		conf.setId(agentId);
		conf.setScheduler(new SimpleSchedulerConfig());
		conf.setState(new MemoryStateConfig());

		final Agent agent = new AgentBuilder().with(conf).build();

		addAgent(agentId, agent);
		return agent;
	}

	/**
	 * Creates the agent.
	 *
	 * @param <T>
	 *            the generic type
	 * @param agentType
	 *            the agent type
	 * @param agentId
	 *            the agent id
	 * @param config
	 *            the config
	 * @return the t
	 */
	public <T extends Agent> T createAgent(Class<T> agentType, String agentId,
			final ObjectNode config) {
		return new TypeUtil<T>() {}.inject(createAgent(agentType.getName(),
				agentId, config));
	}

}
