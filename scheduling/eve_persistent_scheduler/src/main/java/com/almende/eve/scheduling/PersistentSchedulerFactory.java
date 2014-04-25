/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.scheduling;

import com.almende.eve.capabilities.CapabilityFactory;
import com.almende.eve.capabilities.handler.Handler;
import com.almende.eve.transport.Receiver;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * A factory for creating SimpleScheduler objects.
 */
public class PersistentSchedulerFactory {
	/**
	 * Gets the scheduler.
	 * 
	 * @param params
	 *            the params
	 * @param handle
	 *            the handle
	 * @return the scheduler
	 */
	public static PersistentScheduler getScheduler(final ObjectNode params,
			final Handler<Receiver> handle) {
		if (params.isObject() && !params.has("class")) {
			params.put("class", PersistentSchedulerFactory.class.getPackage()
					.getName() + ".PersistentSchedulerService");
		}
		return CapabilityFactory.get(params, handle, PersistentScheduler.class);
	}
}
