/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.algorithms.clustering;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * The Class GlobalAddressMapper.
 */
public final class GlobalAddressMapper {

	private static final GlobalAddressMapper	INSTANCE	= new GlobalAddressMapper();
	private Map<String, URI>					map			= new HashMap<String, URI>();

	private GlobalAddressMapper() {}

	/**
	 * Sets the map, allowing different map storage strategies.
	 *
	 * @param map
	 *            the map
	 */
	public static void set(final Map<String, URI> map) {
		INSTANCE.map = map;
	}

	/**
	 * Gets the map.
	 *
	 * @return the map
	 */
	public static Map<String, URI> get() {
		return INSTANCE.map;
	}
}
