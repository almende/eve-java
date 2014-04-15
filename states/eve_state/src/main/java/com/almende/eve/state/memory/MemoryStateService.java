/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.state.memory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.almende.eve.capabilities.handler.Handler;
import com.almende.eve.state.State;
import com.almende.eve.state.StateService;
import com.almende.util.TypeUtil;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * A service for managing MemoryState objects.
 */
public class MemoryStateService implements StateService {
	/** Singleton containing all states, stored per id. */
	private final Map<String, State>	states		= new ConcurrentHashMap<String, State>();
	private static MemoryStateService	singleton	= new MemoryStateService();
	
	/**
	 * Gets the instance by params.
	 * 
	 * @param params
	 *            the params
	 * @return the instance by params
	 */
	public static MemoryStateService getInstanceByParams(final JsonNode params) {
		return singleton;
	}
	
	@Override
	public <T, V> T get(final JsonNode params, final Handler<V> handle,
			final Class<T> type) {
		final String agentId = params.get("id").asText();
		if (states.containsKey(agentId)) {
			return TypeUtil.inject(states.get(agentId), type);
		} else {
			final MemoryState result = new MemoryState(agentId, this);
			states.put(agentId, result);
			return TypeUtil.inject(result, type);
		}
	}
	
	@Override
	public void delete(final State instance) {
		states.remove(instance.getAgentId());
	}
	
}
