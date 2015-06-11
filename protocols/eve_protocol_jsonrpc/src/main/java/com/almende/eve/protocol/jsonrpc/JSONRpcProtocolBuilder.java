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
import com.almende.util.uuid.UUID;

/**
 * The Class JSONRpcProtocolBuilder.
 */
public class JSONRpcProtocolBuilder extends
		AbstractCapabilityBuilder<JSONRpcProtocol> {
	private static final Logger							LOG			= Logger.getLogger(JSONRpcProtocolBuilder.class
																			.getName());
	private static final TypeUtil<Handler<Object>>		TYPEUTIL	= new TypeUtil<Handler<Object>>() {};
	private static final Map<String, JSONRpcProtocol>	INSTANCES	= new HashMap<String, JSONRpcProtocol>();

	/*
	 * (non-Javadoc)
	 * @see
	 * com.almende.eve.capabilities.CapabilityService#get(com.fasterxml.jackson.
	 * databind
	 * .JsonNode, com.almende.eve.capabilities.handler.Handler, java.lang.Class)
	 */
	@Override
	public JSONRpcProtocol build() {
		JSONRpcProtocolConfig config = JSONRpcProtocolConfig
				.decorate(getParams());
		String id = config.getId();
		if (id == null) {
			id = new UUID().toString();
			LOG.warning("Parameter 'id' is required for JSONRpcProtocol. (giving temporary name: "
					+ id + ")");
		}

		JSONRpcProtocol result;
		if (INSTANCES.containsKey(id)) {
			result = INSTANCES.get(id);
			final Handler<Object> oldHandle = result.getHandle();
			oldHandle.update(TYPEUTIL.inject(getHandle()));
		} else {
			result = new JSONRpcProtocol(getParams(),
					TYPEUTIL.inject(getHandle()));
		}
		INSTANCES.put(id, result);

		if (result.getAuth() == null || !result.getAuth().getClass().getName().equals(config.getAuthorizor())) {
			// Add authorizor:
			try {
				@SuppressWarnings("unchecked")
				final Class<Authorizor> clazz = (Class<Authorizor>) Class
						.forName(config.getAuthorizor());
				result.setAuth(clazz.newInstance());
			} catch (ClassNotFoundException | InstantiationException
					| IllegalAccessException e) {
				LOG.log(Level.WARNING, "Couldn't instantiate authorizor class:"
						+ config.getAuthorizor(), e);
			}
		}
		return result;
	}

	/**
	 * Delete.
	 *
	 * @param id
	 *            the id
	 */
	public static void delete(final String id) {
		INSTANCES.remove(id);
	}
}
