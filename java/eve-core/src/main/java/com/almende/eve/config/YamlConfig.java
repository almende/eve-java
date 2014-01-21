/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.config;

import java.io.InputStream;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

/**
 * The Class YamlConfig.
 */
public class YamlConfig implements EveConfig {
	private Map<String, Object>	config	= null;
	
	/**
	 * Instantiates a new yaml config.
	 */
	public YamlConfig() {
	}
	
	
	/**
	 * Load the configuration file from input stream.
	 * 
	 * @param inputStream
	 *            the input stream
	 */
	public YamlConfig(final InputStream inputStream) {
		load(inputStream);
	}
	
	/**
	 * Load the configuration from a map.
	 * 
	 * @param config
	 *            the config
	 */
	public YamlConfig(final Map<String, Object> config) {
		this.config = config;
	}
	
	/**
	 * Load the configuration file from input stream.
	 * 
	 * @param inputStream
	 *            the input stream
	 */
	@SuppressWarnings("unchecked")
	public final void load(final InputStream inputStream) {
		final Yaml yaml = new Yaml();
		config = yaml.loadAs(inputStream, Map.class);
	}
	

	/* (non-Javadoc)
	 * @see com.almende.eve.config.EveConfig#get()
	 */
	@Override
	public Map<String, Object> get() {
		return config;
	}
	
	/* (non-Javadoc)
	 * @see com.almende.eve.config.EveConfig#get(java.lang.String[])
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T> T get(final String... params) {
		if (config == null) {
			return null;
		}
		
		Map<String, Object> c = config;
		for (int i = 0; i < params.length - 1; i++) {
			final String key = params[i];
			// FIXME: check instance
			c = (Map<String, Object>) c.get(key);
			if (c == null) {
				return null;
			}
		}
		
		// FIXME: check instance
		return (T) c.get(params[params.length - 1]);
	}
}
