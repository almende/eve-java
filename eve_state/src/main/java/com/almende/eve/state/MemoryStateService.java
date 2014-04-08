/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.state;

import java.lang.invoke.MethodHandle;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.almende.util.TypeUtil;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * A service for managing MemoryState objects.
 */
public class MemoryStateService implements StateService {
	/** Singleton containing all states, stored per id. */
	private final Map<String, State>	states	= new ConcurrentHashMap<String, State>();
	private static MemoryStateService singleton = new MemoryStateService(); 
	
	/**
	 * Gets the instance by params.
	 * 
	 * @param params
	 *            the params
	 * @return the instance by params
	 */
	public static MemoryStateService getInstanceByParams(JsonNode params){
		return singleton;
	}

	@Override
	public <T> T get(JsonNode params, MethodHandle handle, Class<T> type) {
		String agentId = params.get("id").asText();
		if (states.containsKey(agentId)){
			return TypeUtil.inject(states.get(agentId),type);
		} else {
			MemoryState result = new MemoryState(agentId);
			states.put(agentId, result);
			return TypeUtil.inject(result,type);
		}
	}



	@Override
	public void delete(State instance) {
		states.remove(instance.getAgentId());
	}
	
}
