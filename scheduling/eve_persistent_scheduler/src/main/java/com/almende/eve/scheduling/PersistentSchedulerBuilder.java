/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.scheduling;

import java.util.logging.Logger;

import com.almende.eve.capabilities.handler.Handler;
import com.almende.eve.transport.Receiver;
import com.almende.util.uuid.UUID;

/**
 * The Class PersistentSchedulerService.
 */
public class PersistentSchedulerBuilder extends SimpleSchedulerBuilder {
	private static final Logger	LOG	= Logger.getLogger(PersistentSchedulerBuilder.class
											.getName());

	/*
	 * (non-Javadoc)
	 * @see
	 * com.almende.eve.capabilities.CapabilityService#get(com.fasterxml.jackson
	 * .databind.node.ObjectNode, com.almende.eve.capabilities.handler.Handler,
	 * java.lang.Class)
	 */
	@Override
	public PersistentScheduler build() {
		final PersistentSchedulerConfig config = PersistentSchedulerConfig
				.decorate(getParams());
		String id = config.getId();
		if (id == null) {
			id = new UUID().toString();
			LOG.warning("Parameter 'id' is required for PersistentScheduler. (giving temporary name: "
					+ id + ")");
		}
		PersistentScheduler result = null;
		if (INSTANCES.containsKey(id)) {
			result = (PersistentScheduler) INSTANCES.get(id);
			final Handler<Receiver> oldHandle = result.getHandle();
			oldHandle.update(TYPEUTIL.inject(getHandle()));
		} else {
			result = new PersistentScheduler(config,
					TYPEUTIL.inject(getHandle()));
		}
		INSTANCES.put(id, result);
		return result;
	}

}
