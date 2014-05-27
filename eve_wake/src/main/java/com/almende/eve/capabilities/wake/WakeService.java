/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.capabilities.wake;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.almende.eve.capabilities.Capability;
import com.almende.eve.capabilities.CapabilityService;
import com.almende.eve.capabilities.Config;
import com.almende.eve.capabilities.handler.Handler;
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
public class WakeService implements Capability, CapabilityService {
	private static final Logger						LOG			= Logger.getLogger(WakeService.class
																		.getName());
	private static final Map<String, WakeService>	services	= new HashMap<String, WakeService>();
	private ObjectNode								myParams	= null;
	private Map<String, WakeEntry>					agents		= new HashMap<String, WakeEntry>();
	
	private State									state		= null;
	private static final WakeService				singleton	= new WakeService();
	
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
	public WakeService(final ObjectNode params) {
		myParams = params;
		state = StateFactory.getState((ObjectNode) myParams.get("state"));
		services.put(state.getId(), this);
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
	public void setMyParams(final ObjectNode myParams) {
		this.myParams = myParams;
		state = StateFactory.getState((ObjectNode) myParams.get("state"));
		services.put(state.getId(), this);
	}
	
	/**
	 * Boot.
	 */
	@JsonIgnore
	public void boot() {
		load();
		for (final WakeEntry entry : agents.values()) {
			wake(entry.getWakeKey(), true);
		}
	}
	
	/**
	 * Wake.
	 * 
	 * @param wakeKey
	 *            the wake key
	 */
	public void wake(final String wakeKey) {
		wake(wakeKey, false);
	}
	
	/**
	 * Wake.
	 * 
	 * @param wakeKey
	 *            the wake key
	 * @param onBoot
	 *            the on boot
	 */
	@JsonIgnore
	public void wake(final String wakeKey, final boolean onBoot) {
		WakeEntry entry = agents.get(wakeKey);
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
			} catch (final Exception e) {
				LOG.log(Level.WARNING, "Failed to instantiate Wakeable:'"
						+ wakeKey + "'", e);
			}
			if (instance != null) {
				instance.wake(wakeKey, entry.getParams(), onBoot);
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
	public void register(final String wakeKey, final String className) {
		final WakeEntry entry = new WakeEntry(wakeKey, null, className);
		agents.put(wakeKey, entry);
		store();
	}
	
	/**
	 * Register.
	 * 
	 * @param wakeKey
	 *            the wake key
	 * @param params
	 *            the params
	 * @param className
	 *            the class name
	 */
	@JsonIgnore
	public void register(final String wakeKey, final ObjectNode params,
			final String className) {
		final WakeEntry entry = new WakeEntry(wakeKey, params, className);
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
					new TypeUtil<HashMap<String, WakeEntry>>() {
					});
		}
	}
	
	/**
	 * Gets the agents.
	 * 
	 * @return the agents
	 */
	public Map<String, WakeEntry> getAgents() {
		return agents;
	}
	
	/**
	 * Sets the agents.
	 * 
	 * @param agents
	 *            the agents
	 */
	public void setAgents(final Map<String, WakeEntry> agents) {
		this.agents = agents;
	}
	
	/**
	 * Gets the instance by params.
	 * 
	 * @param params
	 *            the params
	 * @return the instance by params
	 */
	public static WakeService getInstanceByParams(final ObjectNode params) {
		return singleton;
	}
	
	/* (non-Javadoc)
	 * @see com.almende.eve.capabilities.CapabilityService#get(com.fasterxml.jackson.databind.node.ObjectNode, com.almende.eve.capabilities.handler.Handler, java.lang.Class)
	 */
	@Override
	public <T extends Capability, V> T get(final ObjectNode params,
			final Handler<V> handle, final Class<T> type) {
		final Config config = new Config(params);
		final String id = config.get("state", "id");
		WakeService service = null;
		if (services.containsKey(id)) {
			service = services.get(id);
		}
		service = new WakeService(params);
		
		return TypeUtil.inject(service, type);
	}
	
	/* (non-Javadoc)
	 * @see com.almende.eve.capabilities.Capability#getParams()
	 */
	@Override
	public ObjectNode getParams() {
		return myParams;
	}
	
}
