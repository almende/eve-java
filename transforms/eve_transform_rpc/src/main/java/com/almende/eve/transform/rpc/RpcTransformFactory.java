/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.transform.rpc;

import com.almende.eve.capabilities.CapabilityFactory;
import com.almende.eve.capabilities.handler.Handler;
import com.almende.util.jackson.JOM;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * A factory for creating RpcTransform objects.
 */
public class RpcTransformFactory {
	/**
	 * Gets the transport.
	 * 
	 * @param params
	 *            the params
	 * @param handle
	 *            the handle
	 * @return the state
	 */
	public static RpcTransform get(final JsonNode params, final Handler<Object> handle) {
		return CapabilityFactory.get(params, handle, RpcTransform.class);
	}
	
	/**
	 * Gets the.
	 * 
	 * @param handle
	 *            the handle
	 * @return the transform
	 */
	public static RpcTransform get(final Handler<Object> handle) {
		return CapabilityFactory.get(JOM.createNullNode(), handle, RpcTransform.class);
	}

}
