/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.defaults;

import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.almende.util.jackson.JOM;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class Config.
 */
public class Config {
	private static final Logger	LOG			= Logger.getLogger(Config.class
													.getName());
	private static ObjectNode	configTree	= JOM.createObjectNode();
	
	static {
		// Add default memory state:
		final ObjectNode defaultState = JOM.createObjectNode();
		defaultState.put("class",
				"com.almende.eve.state.memory.MemoryStateService");
		addConfig(defaultState, "state", "default");
	}
	
	/**
	 * Gets the config.
	 * 
	 * @return the config
	 */
	public static ObjectNode getConfig() {
		return configTree.deepCopy();
	}
	
	/**
	 * Gets the config.
	 * 
	 * @param configPath
	 *            the config path
	 * @return the config
	 */
	public static ObjectNode getConfig(final String... configPath) {
		ObjectNode result = configTree;
		for (final String elem : configPath) {
			result = (ObjectNode) result.get(elem);
			if (result == null) {
				LOG.log(Level.WARNING, "Invalid configuration path given",
						new NoSuchElementException());
			}
		}
		return result.deepCopy();
	}
	
	/**
	 * Adds the config.
	 * 
	 * @param config
	 *            the config
	 */
	public static void addConfig(final ObjectNode config) {
		addConfig(config, (String) null);
	}
	
	/**
	 * Adds the config.
	 * 
	 * @param config
	 *            the config
	 * @param configPath
	 *            the config path
	 */
	public static void addConfig(final ObjectNode config,
			final String... configPath) {
		switch (configPath.length) {
			case 0:
				configTree = config.deepCopy();
				break;
			case 1:
				configTree.put(configPath[0], config.deepCopy());
				break;
			default:
				ObjectNode position = configTree;
				for (int i = 0; i < configPath.length - 1; i++) {
					position = position.with(configPath[i]);
				}
				position.put(configPath[configPath.length - 1],
						config.deepCopy());
		}
	}
	
}
