/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.scheduling;

import java.util.HashMap;

import com.almende.eve.capabilities.handler.Handler;
import com.almende.eve.transport.Receiver;
import com.almende.util.TypeUtil;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class PersistentSchedulerService.
 */
public class PersistentSchedulerService implements SchedulerService {
	private static final PersistentSchedulerService				singleton	= new PersistentSchedulerService();
	private static final TypeUtil<Handler<Receiver>>			TYPEUTIL	= new TypeUtil<Handler<Receiver>>() {
																			};
	private static final HashMap<String, PersistentScheduler>	instances	= new HashMap<String, PersistentScheduler>();
	
	/**
	 * Gets the instance by params.
	 * 
	 * @param params
	 *            the params
	 * @return the instance by params
	 */
	public static PersistentSchedulerService getInstanceByParams(
			final ObjectNode params) {
		return singleton;
	}
	
	@Override
	public <T, V> T get(ObjectNode params, Handler<V> handle, Class<T> type) {
		PersistentScheduler result = null;
		if (handle.getKey() != null && instances.containsKey(handle.getKey())) {
			result = instances.get(handle.getKey());
			Handler<Receiver> oldHandle = result.getHandle();
			oldHandle.update(TYPEUTIL.inject(handle));
		} else {
			result = new PersistentScheduler(params, TYPEUTIL.inject(handle));
		}
		if (handle.getKey() != null) {
			instances.put(handle.getKey(), result);
		}
		return TypeUtil.inject(result, type);
	}
	
}
