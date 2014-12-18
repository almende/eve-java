/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.protocol.jsonrpc;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.almende.eve.capabilities.AbstractCapabilityBuilder;
import com.almende.eve.capabilities.handler.Handler;
import com.almende.eve.protocol.auth.Authorizor;
import com.almende.util.TypeUtil;

/**
 * The Class JSONRpcProtocolBuilder.
 */
public class JSONRpcProtocolBuilder extends AbstractCapabilityBuilder<JSONRpcProtocol> {
	private static final Logger LOG = Logger
			.getLogger(JSONRpcProtocolBuilder.class.getName());
	private static final TypeUtil<Handler<Object>>	TYPEUTIL	= new TypeUtil<Handler<Object>>() {
																};
	private static final Map<String, JSONRpcProtocol>	INSTANCES	= new HashMap<String, JSONRpcProtocol>();
	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.almende.eve.capabilities.CapabilityService#get(com.fasterxml.jackson.
	 * databind
	 * .JsonNode, com.almende.eve.capabilities.handler.Handler, java.lang.Class)
	 */
	@Override
	public JSONRpcProtocol build() {
		JSONRpcProtocolConfig config = new JSONRpcProtocolConfig(getParams());
		
		JSONRpcProtocol result;
		if (getHandle() != null && getHandle().getKey() != null
				&& INSTANCES.containsKey(getHandle().getKey())) {
			result = INSTANCES.get(getHandle().getKey());
			final Handler<Object> oldHandle = result.getHandle();
			oldHandle.update(TYPEUTIL.inject(getHandle()));
		} else {
			result = new JSONRpcProtocol(config, TYPEUTIL.inject(getHandle()));
		}
		if (getHandle() != null && getHandle().getKey() != null) {
			INSTANCES.put(getHandle().getKey(), result);
		}
		
		//Add authorizor:
		try {
			@SuppressWarnings("unchecked")
			final Class<Authorizor> clazz = (Class<Authorizor>) Class.forName(config.getAuthorizor());
			result.setAuth(clazz.newInstance());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			LOG.log(Level.WARNING,"Couldn't instantiate authorizor class:"+config.getAuthorizor(),e);
		}
		return result;
	}
	
}
