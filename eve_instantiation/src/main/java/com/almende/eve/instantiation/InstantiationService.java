/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.instantiation;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.almende.eve.capabilities.Capability;
import com.almende.eve.capabilities.handler.Handler;
import com.almende.eve.state.State;
import com.almende.eve.state.StateBuilder;
import com.almende.util.TypeUtil;
import com.almende.util.jackson.JOM;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class InstantiationService.
 */
public class InstantiationService implements Capability {
	private static final Logger							LOG					= Logger.getLogger(InstantiationService.class
																					.getName());
	private static final TypeUtil<InstantiationEntry>	INSTANTIATIONENTRY	= new TypeUtil<InstantiationEntry>() {};
	private ObjectNode									myParams			= null;
	private Map<String, InstantiationEntry>				entries				= new HashMap<String, InstantiationEntry>();
	private State										state				= null;
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
		state = new StateBuilder().withConfig(
				(ObjectNode) myParams.get("state")).build();
		InstantiationServiceBuilder.getServices().put(state.getId(), this);
		load();
	}

	@Override
	public void delete() {
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
		state = new StateBuilder().withConfig(
				(ObjectNode) myParams.get("state")).build();
		InstantiationServiceBuilder.getServices().put(state.getId(), this);
		load();
	}

	/**
	 * Boot.
	 */
	@JsonIgnore
	public void boot() {
		load();
		for (final String key : entries.keySet()) {
			init(key, true);
		}
	}

	/**
	 * Exists.
	 *
	 * @param wakeKey
	 *            the wake key
	 * @return true, if successful
	 */
	public boolean exists(final String wakeKey) {
		if (!entries.containsKey(wakeKey)) {
			load();
			return entries.containsKey(wakeKey);
		} else {
			return true;
		}
	}

	/**
	 * Init a specific initable.
	 *
	 * @param wakeKey
	 *            the wake key
	 * @return the initable
	 */
	public Initable init(final String wakeKey) {
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
	public Initable init(final String wakeKey, final boolean onBoot) {
		InstantiationEntry entry = entries.get(wakeKey);
		if (entry == null) {
			entry = load(wakeKey);
			entries.put(wakeKey, entry);
		}
		if (entry != null) {
			final String className = entry.getClassName();
			Initable instance = null;
			Handler<Initable> oldHandler = entry.getHandler();
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
					instance = (Initable) clazz.newInstance();

				} catch (final Exception e) {
					LOG.log(Level.WARNING, "Failed to instantiate entry:'"
							+ wakeKey + "'", e);
				}
			}
			if (instance != null) {
				instance.init(entry.getParams(), onBoot);
			}
			HibernationHandler<Initable> newHandler = new HibernationHandler<Initable>(
					instance, wakeKey, this);
			entry.setHandler(newHandler);
			if (oldHandler != null) {
				oldHandler.update(newHandler);
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
		store();
	}

	/**
	 * Deregister.
	 *
	 * @param wakeKey
	 *            the wake key
	 */
	public void deregister(final String wakeKey) {
		entries.remove(wakeKey);
		store();
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
		final State innerState = new StateBuilder().withConfig(
				state.getParams().put("id", key)).build();
		if (innerState != null) {
			try {
				innerState.put("entry",
						JOM.getInstance().writeValueAsString(val));
			} catch (JsonProcessingException e) {
				LOG.log(Level.WARNING, "Couldn't store entry:" + key, e);
			}
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
				state.getParams().put("id", key)).build();
		return innerState.get("entry", INSTANTIATIONENTRY);
	}

	/**
	 * Store.
	 */
	private void store() {
		if (state != null) {
			for (Entry<String, InstantiationEntry> entry : entries.entrySet()) {
				if (entry.getValue() != null) {
					store(entry.getKey(), entry.getValue());
				}
			}
		}
	}

	/**
	 * Load.
	 */
	private void load() {
		if (state != null) {
			for (String key : state.keySet()) {
				entries.put(key, null);
			}
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
