/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.algorithms.simulation;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.almende.eve.capabilities.AbstractCapabilityBuilder;
import com.almende.util.uuid.UUID;

/**
 * The Class JSONRpcProtocolBuilder.
 */
public class SimulationTimeProtocolBuilder extends
		AbstractCapabilityBuilder<SimulationTimeProtocol> {
	private static final Logger									LOG			= Logger.getLogger(SimulationTimeProtocolBuilder.class
																					.getName());
	private static final Map<String, SimulationTimeProtocol>	INSTANCES	= new HashMap<String, SimulationTimeProtocol>();

	/*
	 * (non-Javadoc)
	 * @see
	 * com.almende.eve.capabilities.CapabilityService#get(com.fasterxml.jackson.
	 * databind
	 * .JsonNode, com.almende.eve.capabilities.handler.Handler, java.lang.Class)
	 */
	@Override
	public SimulationTimeProtocol build() {
		SimulationTimeProtocolConfig config = SimulationTimeProtocolConfig
				.decorate(getParams());
		String id = config.getId();
		if (id == null) {
			id = new UUID().toString();
			LOG.warning("Parameter 'id' is required for InboxProtocol. (giving temporary name: "
					+ id + ")");
		}

		SimulationTimeProtocol result;
		if (INSTANCES.containsKey(id)) {
			result = INSTANCES.get(id);
		} else {
			result = new SimulationTimeProtocol(getParams(), null);
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
