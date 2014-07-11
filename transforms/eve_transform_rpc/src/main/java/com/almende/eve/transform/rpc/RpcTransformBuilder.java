/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.transform.rpc;

import java.util.HashMap;
import java.util.Map;

import com.almende.eve.capabilities.AbstractCapabilityBuilder;
import com.almende.eve.capabilities.handler.Handler;
import com.almende.util.TypeUtil;

/**
 * The Class RpcService.
 */
public class RpcTransformBuilder extends AbstractCapabilityBuilder<RpcTransform> {
	private static final TypeUtil<Handler<Object>>	TYPEUTIL	= new TypeUtil<Handler<Object>>() {
																};
	private static final Map<String, RpcTransform>	INSTANCES	= new HashMap<String, RpcTransform>();
	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.almende.eve.capabilities.CapabilityService#get(com.fasterxml.jackson.
	 * databind
	 * .JsonNode, com.almende.eve.capabilities.handler.Handler, java.lang.Class)
	 */
	@Override
	public RpcTransform build() {
		if (getParams() == null){
			withConfig(new RpcTransformConfig());
		}
		RpcTransform result;
		if (getHandle() != null && getHandle().getKey() != null
				&& INSTANCES.containsKey(getHandle().getKey())) {
			result = INSTANCES.get(getHandle().getKey());
			final Handler<Object> oldHandle = result.getHandle();
			oldHandle.update(TYPEUTIL.inject(getHandle()));
		} else {
			result = new RpcTransform(getParams(), TYPEUTIL.inject(getHandle()));
		}
		if (getHandle() != null && getHandle().getKey() != null) {
			INSTANCES.put(getHandle().getKey(), result);
		}
		return result;
	}
	
}
