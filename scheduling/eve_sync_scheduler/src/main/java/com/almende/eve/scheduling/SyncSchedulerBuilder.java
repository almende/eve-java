/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.scheduling;

import java.util.HashMap;
import java.util.Map;

import com.almende.eve.capabilities.AbstractCapabilityBuilder;
import com.almende.eve.capabilities.handler.Handler;
import com.almende.eve.transport.Receiver;
import com.almende.util.TypeUtil;

/**
 * The Class PersistentSchedulerService.
 */
public class SyncSchedulerBuilder extends
		AbstractCapabilityBuilder<SyncScheduler> {
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
		SyncScheduler result = null;
		if (getHandle().getKey() != null
				&& INSTANCES.containsKey(getHandle().getKey())) {
			result = INSTANCES.get(getHandle().getKey());
			final Handler<Receiver> oldHandle = result.getHandle();
			oldHandle.update(TYPEUTIL.inject(getHandle()));
		} else {
			result = new SyncScheduler(getParams(),
					TYPEUTIL.inject(getHandle()));
		}
		if (getHandle().getKey() != null) {
			INSTANCES.put(getHandle().getKey(), result);
		}
		return result;
	}

}
