package com.almende.eve.config;

import java.util.Map;

/**
 * @author Almende
 *
 */
public interface EveConfig {
	
	/**
	 * Get the full configuration
	 * returns null if no configuration file is loaded.
	 * 
	 * @return the map
	 */
	Map<String, Object> get() ;
	
	/**
	 * retrieve a (nested) parameter from the config
	 * the parameter name can be a simple name like config.get("url"),
	 * or nested parameter like config.get("servlet", "config", "url")
	 * null is returned when the parameter is not found, or when no
	 * configuration file is loaded.
	 * 
	 * @param <T>
	 *            the generic type
	 * @param params
	 *            One or multiple (nested) parameters
	 * @return the t
	 */
	<T> T get(final String... params);
	
}
