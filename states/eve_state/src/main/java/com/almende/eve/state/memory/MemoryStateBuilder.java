/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.state.memory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import com.almende.eve.capabilities.AbstractCapabilityBuilder;
import com.almende.eve.state.State;
import com.almende.eve.state.StateService;

/**
 * A service for managing MemoryState objects.
 */
public class MemoryStateBuilder extends AbstractCapabilityBuilder<MemoryState>
		implements StateService {
	private static final Logger						LOG		= Logger.getLogger(MemoryStateBuilder.class
																	.getName());
	// MemoryStates are currently a Singleton implementation:
	private static final Map<String, MemoryState>	STATES	= new ConcurrentHashMap<String, MemoryState>();
	
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
		if (id == null) {
			LOG.warning("Parameter 'id' is required for MemoryState.");
			return null;
		}
		
		// Quick return for existing states
		final MemoryState state = STATES.get(id);
		if (state != null) {
			return state;
		} else {
			// Synchronized version for creating a new state (preventing race
			// condition)
			synchronized (STATES) {
				if (!STATES.containsKey(id)) {
					final MemoryState result = new MemoryState(id, this,
							getParams());
					if (result != null) {
						STATES.put(id, result);
					}
				}
				if (STATES.containsKey(id)) {
					return STATES.get(id);
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
		STATES.remove(instance.getId());
	}
	
}
