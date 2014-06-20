/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.state.memory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.almende.eve.capabilities.AbstractCapabilityBuilder;
import com.almende.eve.state.State;
import com.almende.eve.state.StateService;

/**
 * A service for managing MemoryState objects.
 */
public class MemoryStateBuilder extends AbstractCapabilityBuilder<MemoryState> implements StateService {
	//MemoryStates are currently a Singleton implementation:
	private static final Map<String, MemoryState>	states		= new ConcurrentHashMap<String, MemoryState>();
	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.almende.eve.capabilities.CapabilityService#get(com.fasterxml.jackson.
	 * databind
	 * .JsonNode, com.almende.eve.capabilities.handler.Handler, java.lang.Class)
	 */
	@Override
	public MemoryState build() {
		final MemoryStateConfig config = new MemoryStateConfig(getParams());
		final String id = config.getId();
		
		// Quick return for existing states
		final MemoryState state = states.get(id);
		if (state != null) {
			return state;
		} else {
			// Synchronized version for creating a new state (preventing race
			// condition)
			synchronized (states) {
				if (!states.containsKey(id)) {
					final MemoryState result = new MemoryState(id, this, getParams());
					if (result != null) {
						states.put(id, result);
					}
				}
				if (states.containsKey(id)) {
					return states.get(id);
				}
			}
		}
		return null;
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
