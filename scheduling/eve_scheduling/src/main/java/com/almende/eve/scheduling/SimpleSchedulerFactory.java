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
public class SimpleSchedulerFactory {
	/**
	 * Gets the scheduler.
	 * 
	 * @param params
	 *            the params
	 * @param handle
	 *            the handle
	 * @return the scheduler
	 */
	public static SimpleScheduler getScheduler(final ObjectNode params, Handler<Receiver> handle) {
		if (params.isObject() && !params.has("class")) {
			((ObjectNode) params).put("class", SimpleSchedulerFactory.class
					.getPackage().getName() + ".SimpleSchedulerService");
		}
		return CapabilityFactory.get(params, handle, SimpleScheduler.class);
	}
}
