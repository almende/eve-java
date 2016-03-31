/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.state.memory;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.almende.eve.state.AbstractState;
import com.almende.eve.state.State;
import com.almende.eve.state.StateService;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class MemoryState.
 * 
 * @author jos
 * @class MemoryState
 *        A state for an Eve Agent, which stores the data in memory.
 *        (After a restart of the application, the data will be gone!)
 *        The state provides general information for the agent (about itself,
 *        the environment, and the system configuration), and the agent can
 *        store its
 *        state in the state.
 *        The state extends a standard Java Map.
 *        Usage:<br>
 *        AgentHost factory = AgentHost.getInstance(config);<br>
 *        FileState state = new State("agentId");<br>
 *        state.put("key", "value");<br>
 *        System.out.println(state.get("key")); // "value"<br>
 */
public class MemoryState extends AbstractState<Serializable> implements State {
	private static final Logger								LOG			= Logger.getLogger(MemoryState.class
																				.getName());
	private final ConcurrentHashMap<String, Serializable>	properties	= new ConcurrentHashMap<String, Serializable>(
																				10);

	/**
	 * Instantiates a new memory state.
	 */
	public MemoryState() {}

	/**
	 * Instantiates a new memory state.
	 * 
	 * @param agentId
	 *            the agent id
	 * @param service
	 *            the service
	 * @param params
	 *            the params
	 */
	public MemoryState(final String agentId, final StateService service,
			final ObjectNode params) {
		super(agentId, service, params);
	}

	/*
	 * (non-Javadoc)
	 * @see com.almende.eve.state.State#clear()
	 */
	@Override
	public void clear() {
		properties.clear();
	}

	/*
	 * (non-Javadoc)
	 * @see com.almende.eve.state.State#keySet()
	 */
	@Override
	public Set<String> keySet() {
		return new HashSet<String>(properties.keySet());
	}

	/*
	 * (non-Javadoc)
	 * @see com.almende.eve.state.State#containsKey(java.lang.String)
	 */
	@Override
	public boolean containsKey(final String key) {
		return properties.containsKey(key);
	}

	/*
	 * (non-Javadoc)
	 * @see com.almende.eve.state.AbstractState#get(java.lang.String)
	 */
	@Override
	public Serializable get(final String key) {
		try {
			return properties.get(key);
		} catch (final Exception e) {
			LOG.log(Level.WARNING, "Couldn't clone object: " + key
					+ ", returning pointer to original object.", e);
			return properties.get(key);
		}
	}

	/**
	 * Checks if is empty.
	 * 
	 * @return true, if is empty
	 */
	public boolean isEmpty() {
		return properties.isEmpty();
	}

	/*
	 * (non-Javadoc)
	 * @see com.almende.eve.state.AbstractState#locPut(java.lang.String,
	 * java.io.Serializable)
	 */
	@Override
	public Serializable locPut(final String key, final Serializable value) {
		return properties.put(key, value);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.almende.eve.state.AbstractState#locPutIfUnchanged(java.lang.String,
	 * java.io.Serializable, java.io.Serializable)
	 */
	@Override
	public boolean locPutIfUnchanged(final String key,
			final Serializable newVal, final Serializable oldVal) {
		if (newVal == null && oldVal == null) {
			return !properties.containsKey(key);
		} else if (newVal == null) {
			return properties.remove(key, oldVal);
		} else if (oldVal == null) {
			return properties.putIfAbsent(key, newVal) == null;
		} else {
			return properties.replace(key, oldVal, newVal);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.almende.eve.state.State#remove(java.lang.String)
	 */
	@Override
	public Serializable remove(final String key) {
		return properties.remove(key);
	}

	/*
	 * (non-Javadoc)
	 * @see com.almende.eve.state.State#size()
	 */
	@Override
	public int size() {
		return properties.size();
	}

}
