/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.scheduling;

import java.util.HashMap;

import com.almende.eve.capabilities.Capability;
import com.almende.eve.capabilities.handler.Handler;
import com.almende.eve.transport.Receiver;
import com.almende.util.TypeUtil;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class SimpleSchedulerService.
 */
public class SimpleSchedulerService implements SchedulerService {
	private static final SimpleSchedulerService				singleton	= new SimpleSchedulerService();
	private static final TypeUtil<Handler<Receiver>>		TYPEUTIL	= new TypeUtil<Handler<Receiver>>() {
																		};
	private static final HashMap<String, SimpleScheduler>	instances	= new HashMap<String, SimpleScheduler>();
	
	/**
	 * Gets the instance by params.
	 * 
	 * @param params
	 *            the params
	 * @return the instance by params
	 */
	public static SimpleSchedulerService getInstanceByParams(
			final ObjectNode params) {
		return singleton;
	}
	
	/* (non-Javadoc)
	 * @see com.almende.eve.capabilities.CapabilityService#get(com.fasterxml.jackson.databind.node.ObjectNode, com.almende.eve.capabilities.handler.Handler, java.lang.Class)
	 */
	@Override
	public <T extends Capability, V> T get(final ObjectNode params,
			final Handler<V> handle, final Class<T> type) {
		SimpleScheduler result = null;
		if (handle.getKey() != null && instances.containsKey(handle.getKey())) {
			result = instances.get(handle.getKey());
			final Handler<Receiver> oldHandle = result.getHandle();
			oldHandle.update(TYPEUTIL.inject(handle));
		} else {
			result = new SimpleScheduler(params, TYPEUTIL.inject(handle));
		}
		if (handle.getKey() != null) {
			instances.put(handle.getKey(), result);
		}
		return TypeUtil.inject(result, type);
	}
	
}
