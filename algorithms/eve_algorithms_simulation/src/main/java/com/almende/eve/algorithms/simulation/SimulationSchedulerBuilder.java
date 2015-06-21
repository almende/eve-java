/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.algorithms.simulation;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.almende.eve.capabilities.AbstractCapabilityBuilder;
import com.almende.eve.capabilities.handler.Handler;
import com.almende.eve.protocol.jsonrpc.formats.Caller;
import com.almende.util.TypeUtil;
import com.almende.util.uuid.UUID;

/**
 * The Class PersistentSchedulerService.
 */
public class SimulationSchedulerBuilder extends
		AbstractCapabilityBuilder<SimulationScheduler> {
	private static final Logger								LOG			= Logger.getLogger(SimulationSchedulerBuilder.class
																				.getName());
	private static final TypeUtil<Handler<Caller>>			TYPEUTIL	= new TypeUtil<Handler<Caller>>() {};
	private static final Map<String, SimulationScheduler>	INSTANCES	= new HashMap<String, SimulationScheduler>();

	/*
	 * (non-Javadoc)
	 * @see
	 * com.almende.eve.capabilities.CapabilityService#get(com.fasterxml.jackson
	 * .databind.node.ObjectNode, com.almende.eve.capabilities.handler.Handler,
	 * java.lang.Class)
	 */
	@Override
	public SimulationScheduler build() {
		final SimulationSchedulerConfig config = SimulationSchedulerConfig
				.decorate(getParams());
		String id = config.getId();
		if (id == null) {
			id = new UUID().toString();
			LOG.warning("Parameter 'id' is required for SyncScheduler. (giving temporary name: "
					+ id + ")");
		}

		SimulationScheduler result = null;
		if (INSTANCES.containsKey(id)) {
			result = (SimulationScheduler) INSTANCES.get(id);
			final Handler<Caller> oldHandle = result.getHandle();
			oldHandle.update(TYPEUTIL.inject(getHandle()));
		} else {
			result = new SimulationScheduler(config,
					TYPEUTIL.inject(getHandle()));
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
