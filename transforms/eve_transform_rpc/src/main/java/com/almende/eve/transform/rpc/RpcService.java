/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.transform.rpc;

import java.util.HashMap;

import com.almende.eve.capabilities.Capability;
import com.almende.eve.capabilities.handler.Handler;
import com.almende.eve.transform.TransformService;
import com.almende.util.TypeUtil;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class RpcService.
 */
public class RpcService implements TransformService {
	private static final RpcService						singleton	= new RpcService();
	private static final TypeUtil<Handler<Object>>		TYPEUTIL	= new TypeUtil<Handler<Object>>() {
																	};
	private static final HashMap<String, RpcTransform>	instances	= new HashMap<String, RpcTransform>();
	
	/**
	 * Gets the instance by params.
	 * 
	 * @param params
	 *            the params
	 * @return the instance by params
	 */
	public static RpcService getInstanceByParams(final ObjectNode params) {
		return singleton;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.almende.eve.capabilities.CapabilityService#get(com.fasterxml.jackson.
	 * databind
	 * .JsonNode, com.almende.eve.capabilities.handler.Handler, java.lang.Class)
	 */
	@Override
	public <T extends Capability, V> T get(final ObjectNode params,
			final Handler<V> handle, final Class<T> type) {
		RpcTransform result;
		if (handle != null && handle.getKey() != null
				&& instances.containsKey(handle.getKey())) {
			result = instances.get(handle.getKey());
			final Handler<Object> oldHandle = result.getHandle();
			oldHandle.update(TYPEUTIL.inject(handle));
		} else {
			result = new RpcTransform(params, TYPEUTIL.inject(handle), this);
		}
		if (handle != null && handle.getKey() != null) {
			instances.put(handle.getKey(), result);
		}
		return TypeUtil.inject(result, type);
	}
	
}
