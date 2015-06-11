/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.algorithms.clustering;

import java.util.HashMap;
import java.util.Map;

import com.almende.eve.capabilities.AbstractCapabilityBuilder;

/**
 * The Class JSONRpcProtocolBuilder.
 */
public class GlobalAddressProtocolBuilder extends
		AbstractCapabilityBuilder<GlobalAddressProtocol> {
	private static final Map<String, GlobalAddressProtocol>	INSTANCES	= new HashMap<String, GlobalAddressProtocol>();

	/*
	 * (non-Javadoc)
	 * @see
	 * com.almende.eve.capabilities.CapabilityService#get(com.fasterxml.jackson.
	 * databind
	 * .JsonNode, com.almende.eve.capabilities.handler.Handler, java.lang.Class)
	 */
	@Override
	public GlobalAddressProtocol build() {
		GlobalAddressProtocolConfig config = GlobalAddressProtocolConfig.decorate(getParams());
		String id = config.getId();

		GlobalAddressProtocol result;
		if (INSTANCES.containsKey(id)) {
			result = INSTANCES.get(id);
		} else {
			result = new GlobalAddressProtocol(getParams(), null);
		}
		INSTANCES.put(id, result);
		return result;
	}

	/**
	 * Delete.
	 *
	 * @param id
	 *            the id
	 */
	public static void delete(final String id) {
		INSTANCES.remove(id);
	}
}
