/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.capabilities.wake;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.almende.eve.state.State;
import com.almende.eve.state.StateFactory;
import com.almende.util.TypeUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class WakeService.
 * This is a "resurrection" service, to which agents can register themselves to
 * be woken when the system reboots, WakeHandlers are called, etc.
 */
public class WakeService {
	private static final Logger	LOG			= Logger.getLogger(WakeService.class
													.getName());
	private ObjectNode			myParams	= null;
	private Map<String, Entry>	agents		= new HashMap<String, Entry>();
	
	private State				state		= null;
	
	/**
	 * Instantiates a new wake service.
	 */
	public WakeService() {
	};
	
	/**
	 * Instantiates a new wake service.
	 * 
	 * @param params
	 *            the params, containing at least a "state" field, with a
	 *            specific State configuration.
	 */
	public WakeService(ObjectNode params) {
		this.myParams = params;
		state = StateFactory.getState((ObjectNode) myParams.get("state"));
	}
	
	/**
	 * Gets the my params.
	 * 
	 * @return the my params
	 */
	public ObjectNode getMyParams() {
		return myParams;
	}
	
	/**
	 * Sets the my params.
	 * 
	 * @param myParams
	 *            the new my params
	 */
	public void setMyParams(ObjectNode myParams) {
		this.myParams = myParams;
		state = StateFactory.getState((ObjectNode) myParams.get("state"));
	}
	
	/**
	 * Wake.
	 * 
	 * @param wakeKey
	 *            the wake key
	 */
	@JsonIgnore
	public void wake(final String wakeKey) {
		Entry entry = agents.get(wakeKey);
		if (entry == null) {
			// Retry from file
			load();
			entry = agents.get(wakeKey);
		}
		if (entry != null) {
			final String className = entry.getClassName();
			Wakeable instance = null;
			try {
				final Class<?> clazz = Class.forName(className);
				instance = (Wakeable) clazz.newInstance();
			} catch (Exception e) {
				LOG.log(Level.WARNING, "Failed to instantiate Wakeable:'"
						+ wakeKey + "'", e);
			}
			if (instance != null) {
				instance.wake(wakeKey);
			}
		} else {
			LOG.warning("Sorry, I don't know any Wakeable called:'" + wakeKey
					+ "'");
		}
	}
	
	/**
	 * Register.
	 * 
	 * @param wakeKey
	 *            the wake key
	 * @param className
	 *            the class name
	 */
	@JsonIgnore
	public void register(String wakeKey, String className) {
		final Entry entry = new Entry(wakeKey, className);
		agents.put(wakeKey, entry);
		store();
	}
	
	/**
	 * Store.
	 */
	public void store() {
		if (state != null) {
			state.put("agents", agents);
		}
	}
	
	/**
	 * Load.
	 */
	public void load() {
		if (state != null) {
			agents = state.get("agents",
					new TypeUtil<HashMap<String, Entry>>() {
					});
		}
	}
	
	/**
	 * Gets the agents.
	 * 
	 * @return the agents
	 */
	public Map<String, Entry> getAgents() {
		return agents;
	}
	
	/**
	 * Sets the agents.
	 * 
	 * @param agents
	 *            the agents
	 */
	public void setAgents(Map<String, Entry> agents) {
		this.agents = agents;
	}
	
	/**
	 * The Class Entry.
	 */
	public class Entry {
		private String	wakeKey		= null;
		private String	className	= null;
		
		/**
		 * Instantiates a new entry.
		 */
		public Entry() {
		}
		
		/**
		 * Instantiates a new entry.
		 * 
		 * @param wakeKey
		 *            the wake key
		 * @param className
		 *            the class name
		 */
		public Entry(String wakeKey, String className) {
			this.wakeKey = wakeKey;
			this.className = className;
		}
		
		/**
		 * Gets the wake key.
		 * 
		 * @return the wake key
		 */
		public String getWakeKey() {
			return wakeKey;
		}
		
		/**
		 * Sets the wake key.
		 * 
		 * @param wakeKey
		 *            the new wake key
		 */
		public void setWakeKey(final String wakeKey) {
			this.wakeKey = wakeKey;
		}
		
		/**
		 * Gets the className.
		 * 
		 * @return the className
		 */
		public String getClassName() {
			return className;
		}
		
		/**
		 * Sets the className.
		 * 
		 * @param className
		 *            the new className
		 */
		public void setClassName(final String className) {
			this.className = className;
		}
	}
}
