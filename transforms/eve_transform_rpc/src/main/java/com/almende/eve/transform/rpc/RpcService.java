/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.transform.rpc;

import com.almende.eve.capabilities.handler.Handler;
import com.almende.eve.transform.TransformService;
import com.almende.util.TypeUtil;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * The Class RpcService.
 */
public class RpcService implements TransformService {
	private static final RpcService	singleton	= new RpcService();
	private static final TypeUtil<Handler<Object>> TYPEUTIL = new TypeUtil<Handler<Object>>(){};
	/**
	 * Gets the instance by params.
	 * 
	 * @param params
	 *            the params
	 * @return the instance by params
	 */
	public static RpcService getInstanceByParams(final JsonNode params) {
		return singleton;
	}
	
	/* (non-Javadoc)
	 * @see com.almende.eve.capabilities.Capability#get(com.fasterxml.jackson.databind.JsonNode, com.almende.eve.capabilities.handler.Handler, java.lang.Class)
	 */
	@Override
	public <T, V> T get(JsonNode params, Handler<V> handle, Class<T> type) {
		return TypeUtil.inject(new RpcTransform(params, TYPEUTIL.inject(handle), this), type);
	}
	
}
