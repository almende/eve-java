/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.scheduling;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.almende.eve.capabilities.AbstractCapabilityBuilder;
import com.almende.eve.capabilities.handler.Handler;
import com.almende.eve.transport.Receiver;
import com.almende.util.TypeUtil;
import com.almende.util.uuid.UUID;

/**
 * The Class PersistentSchedulerService.
 */
public class SyncSchedulerBuilder extends
		AbstractCapabilityBuilder<SyncScheduler> {
	private static final Logger							LOG			= Logger.getLogger(SyncSchedulerBuilder.class
																			.getName());
	private static final TypeUtil<Handler<Receiver>>	TYPEUTIL	= new TypeUtil<Handler<Receiver>>() {};
	private static final Map<String, SyncScheduler>		INSTANCES	= new HashMap<String, SyncScheduler>();

	/*
	 * (non-Javadoc)
	 * @see
	 * com.almende.eve.capabilities.CapabilityService#get(com.fasterxml.jackson
	 * .databind.node.ObjectNode, com.almende.eve.capabilities.handler.Handler,
	 * java.lang.Class)
	 */
	@Override
	public SyncScheduler build() {
		final SyncSchedulerConfig config = new SyncSchedulerConfig(getParams());
		String id = config.getId();
		if (id == null) {
			id = new UUID().toString();
			LOG.warning("Parameter 'id' is required for SyncScheduler. (giving temporary name: "
					+ id + ")");
		}

		SyncScheduler result = null;
		if (INSTANCES.containsKey(id)) {
			result = INSTANCES.get(id);
			final Handler<Receiver> oldHandle = result.getHandle();
			oldHandle.update(TYPEUTIL.inject(getHandle()));
		} else {
			result = new SyncScheduler(config, TYPEUTIL.inject(getHandle()));
		}
		INSTANCES.put(id, result);
		return result;
	}

}
