/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.instantiation;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.almende.eve.capabilities.Capability;
import com.almende.eve.capabilities.handler.Handler;
import com.almende.eve.state.State;
import com.almende.eve.state.StateBuilder;
import com.almende.eve.state.StateConfig;
import com.almende.eve.state.StateService;
import com.almende.util.TypeUtil;
import com.almende.util.jackson.JOM;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class InstantiationService.
 */
public class InstantiationService implements Capability {
	private static final Logger							LOG					= Logger.getLogger(InstantiationService.class
																					.getName());
	private static final TypeUtil<InstantiationEntry>	INSTANTIATIONENTRY	= new TypeUtil<InstantiationEntry>() {};
	private ObjectNode									myParams			= null;
	private String										myId				= null;
	private Map<String, InstantiationEntry>				entries				= new HashMap<String, InstantiationEntry>();
	private StateService								stateService		= null;
	private ClassLoader									cl					= null;

	/**
	 * Instantiates a new wake service.
	 */
	public InstantiationService() {};

	/**
	 * Instantiates a new InstantiationService.
	 *
	 * @param params
	 *            the params, containing at least a "state" field, with a
	 *            specific State configuration.
	 * @param cl
	 *            the cl
	 */
	public InstantiationService(final ObjectNode params, final ClassLoader cl) {
		this.cl = cl;
		myParams = params;
		
		final InstantiationServiceConfig config = InstantiationServiceConfig.decorate(params);
		final State state = new StateBuilder().withConfig(
				(ObjectNode) config.get("state")).build();
		stateService = state.getService();
		myId = state.getId();
		InstantiationServiceBuilder.getServices().put(myId, this);
		load();
	}

	@Override
	public void delete() {
		// TODO: clear out all state files
		final State state = new StateBuilder().withConfig(
				(ObjectNode) myParams.get("state")).build();
		if (state != null) {
			state.delete();
		}
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
		load();
	}

	/**
	 * Boot.
	 */
	@JsonIgnore
	public void boot() {
		load();
		int cnt = 0;
		for (final String key : entries.keySet()) {
			Object res = init(key, true);
			if (res != null) {
				cnt++;
			}
		}
		LOG.info("Booted " + cnt + " agents");
	}

	/**
	 * Exists.
	 *
	 * @param wakeKey
	 *            the wake key
	 * @return true, if successful
	 */
	public boolean exists(final String wakeKey) {
		return entries.containsKey(wakeKey);
	}

	/**
	 * Init a specific initable.
	 *
	 * @param wakeKey
	 *            the wake key
	 * @return the initable
	 */
	public Configurable init(final String wakeKey) {
		return init(wakeKey, false);
	}

	/**
	 * Wake.
	 *
	 * @param wakeKey
	 *            the wake key
	 * @param onBoot
	 *            the on boot
	 * @return the initable
	 */
	@JsonIgnore
	public Configurable init(final String wakeKey, final boolean onBoot) {
		InstantiationEntry entry = entries.get(wakeKey);
		if (entry == null) {
			entry = load(wakeKey);
			entries.put(wakeKey, entry);
		}
		if (entry != null) {
			final String className = entry.getClassName();
			Configurable instance = null;
			Handler<Configurable> oldHandler = entry.getHandler();
			if (oldHandler != null) {
				instance = oldHandler.getNoWait();
			}
			if (instance == null) {
				try {
					Class<?> clazz = null;
					if (cl != null) {
						clazz = cl.loadClass(className);
					} else {
						clazz = Class.forName(className);
					}
					instance = (Configurable) clazz.newInstance();
					instance.setConfig(entry.getParams());
				} catch (final Exception e) {
					LOG.log(Level.WARNING, "Failed to instantiate entry:'"
							+ wakeKey + "'", e);
				}
			}
			if (instance != null) {
				entry.setHandler(instance.getHandler());
				if (oldHandler != null) {
					oldHandler.update(instance.getHandler());
				}
				entries.put(wakeKey, entry);
			}
			return instance;
		} else {
			LOG.warning("Sorry, I don't know any entry called:'" + wakeKey
					+ "'");
		}
		return null;
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
		final InstantiationEntry entry = new InstantiationEntry(wakeKey, null,
				className);
		entries.put(wakeKey, entry);
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
		final InstantiationEntry entry = new InstantiationEntry(wakeKey,
				params, className);
		entries.put(wakeKey, entry);
		store(wakeKey, entry);
	}

	/**
	 * Deregister.
	 *
	 * @param wakeKey
	 *            the wake key
	 */
	public void deregister(final String wakeKey) {
		final InstantiationEntry entry = entries.remove(wakeKey);
		remove(wakeKey, entry);
	}

	/**
	 * Store.
	 *
	 * @param key
	 *            the key
	 * @param val
	 *            the val
	 */
	private void store(final String key, final InstantiationEntry val) {
		State innerState = null;
		if (val != null) {
			innerState = val.getState();
		}
		if (innerState == null) {
			innerState = new StateBuilder().withConfig(
					StateConfig.decorate((ObjectNode) myParams.get("state"))
							.put("id", key)).build();
		}
		if (innerState != null) {
			innerState.put("entry", JOM.getInstance().valueToTree(val));
		}
	}

	/**
	 * Removes the specific state.
	 *
	 * @param key
	 *            the key
	 * @param val
	 *            the val
	 */
	private void remove(final String key, final InstantiationEntry val) {
		State innerState = null;
		if (val != null) {
			innerState = val.getState();
		}
		if (innerState == null) {
			innerState = new StateBuilder().withConfig(
					StateConfig.decorate((ObjectNode) myParams.get("state"))
							.put("id", key)).build();
		}
		if (innerState != null) {
			innerState.delete();
		}
	}

	/**
	 * Load.
	 *
	 * @param key
	 *            the key
	 * @return the instantiation entry
	 */
	private InstantiationEntry load(final String key) {
		final State innerState = new StateBuilder().withConfig(
				StateConfig.decorate((ObjectNode) myParams.get("state")).put(
						"id", key)).build();
		final InstantiationEntry result = innerState.get("entry",
				INSTANTIATIONENTRY);
		if (result != null) {
			result.setState(innerState);
		}
		return result;
	}

	/**
	 * Store.
	 */
	private void store() {
		for (Entry<String, InstantiationEntry> entry : entries.entrySet()) {
			if (entry.getValue() != null) {
				store(entry.getKey(), entry.getValue());
			}
		}
	}

	/**
	 * Load.
	 */
	private void load() {
		final Set<String> stateIds = stateService.getStateIds();
		for (String key : stateIds) {
			if (key.equals(myId)) {
				continue;
			}
			entries.put(key, null);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.almende.eve.capabilities.Capability#getParams()
	 */
	@Override
	public ObjectNode getParams() {
		return myParams;
	}
}
