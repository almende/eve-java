/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.protocol;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.almende.eve.capabilities.AbstractCapabilityBuilder;
import com.almende.util.uuid.UUID;

/**
 * The Class JSONRpcProtocolBuilder.
 */
public class TraceProtocolBuilder extends
		AbstractCapabilityBuilder<TraceProtocol> {
	private static final Logger						LOG			= Logger.getLogger(TraceProtocolBuilder.class
																		.getName());
	private static final Map<String, TraceProtocol>	INSTANCES	= new HashMap<String, TraceProtocol>();

	/*
	 * (non-Javadoc)
	 * @see
	 * com.almende.eve.capabilities.CapabilityService#get(com.fasterxml.jackson.
	 * databind
	 * .JsonNode, com.almende.eve.capabilities.handler.Handler, java.lang.Class)
	 */
	@Override
	public TraceProtocol build() {
		TraceProtocolConfig config = TraceProtocolConfig.decorate(getParams());
		String id = config.getId();
		if (id == null) {
			id = new UUID().toString();
			LOG.warning("Parameter 'id' is required for InboxProtocol. (giving temporary name: "
					+ id + ")");
		}

		TraceProtocol result;
		if (INSTANCES.containsKey(id)) {
			result = INSTANCES.get(id);
		} else {
			result = new TraceProtocol(getParams(), null);
		}
		INSTANCES.put(id, result);
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
