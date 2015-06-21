/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.scheduling;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.almende.eve.capabilities.AbstractCapabilityBuilder;
import com.almende.eve.capabilities.handler.Handler;
import com.almende.eve.protocol.jsonrpc.formats.Caller;
import com.almende.util.TypeUtil;
import com.almende.util.uuid.UUID;

/**
 * The Class SimpleSchedulerService.
 */
public class SimpleSchedulerBuilder extends
		AbstractCapabilityBuilder<SimpleScheduler> {
	private static final Logger							LOG			= Logger.getLogger(SimpleSchedulerBuilder.class
																			.getName());
	protected static final TypeUtil<Handler<Caller>>	TYPEUTIL	= new TypeUtil<Handler<Caller>>() {};
	protected static final Map<String, SimpleScheduler>	INSTANCES	= new HashMap<String, SimpleScheduler>();

	/*
	 * (non-Javadoc)
	 * @see
	 * com.almende.eve.capabilities.CapabilityService#get(com.fasterxml.jackson
	 * .databind.node.ObjectNode, com.almende.eve.capabilities.handler.Handler,
	 * java.lang.Class)
	 */
	@Override
	public SimpleScheduler build() {
		final SimpleSchedulerConfig config = SimpleSchedulerConfig
				.decorate(getParams());
		String id = config.getId();
		if (id == null) {
			id = new UUID().toString();
			LOG.warning("Parameter 'id' is required for SimpleScheduler. (giving temporary name: "
					+ id + ")");
		}

		SimpleScheduler result = null;
		if (INSTANCES.containsKey(id)) {
			result = INSTANCES.get(id);
			final Handler<Caller> oldHandle = result.getHandle();
			oldHandle.update(TYPEUTIL.inject(getHandle()));
		} else {
			result = new SimpleScheduler(config, TYPEUTIL.inject(getHandle()));
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
