/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.state.memory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.almende.eve.capabilities.Capability;
import com.almende.eve.capabilities.handler.Handler;
import com.almende.eve.state.State;
import com.almende.eve.state.StateService;
import com.almende.util.TypeUtil;
import com.fasterxml.jackson.databind.node.ObjectNode;

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
	public static MemoryStateService getInstanceByParams(final ObjectNode params) {
		return singleton;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.almende.eve.capabilities.CapabilityService#get(com.fasterxml.jackson.
	 * databind
	 * .JsonNode, com.almende.eve.capabilities.handler.Handler, java.lang.Class)
	 */
	@Override
	public <T extends Capability, V> T get(final ObjectNode params,
			final Handler<V> handle, final Class<T> type) {
		final MemoryStateConfig config = new MemoryStateConfig(params);
		final String id = config.getId();
		
		if (states.containsKey(id)) {
			return TypeUtil.inject(states.get(id), type);
		} else {
			final MemoryState result = new MemoryState(id, this, params);
			states.put(id, result);
			return TypeUtil.inject(result, type);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.almende.eve.state.StateService#delete(com.almende.eve.state.State)
	 */
	@Override
	public void delete(final State instance) {
		states.remove(instance.getId());
	}
	
}
