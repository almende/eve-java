/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.instantiation;

import java.util.HashMap;
import java.util.Map;

import com.almende.eve.capabilities.AbstractCapabilityBuilder;
import com.almende.eve.state.StateConfig;

/**
 * The Class WakeService.
 * This is a "resurrection" service, to which agents can register themselves to
 * be woken when the system reboots, WakeHandlers are called, etc.
 */
public class InstantiationServiceBuilder extends
		AbstractCapabilityBuilder<InstantiationService> {
	private static final Map<String, InstantiationService>	SERVICES	= new HashMap<String, InstantiationService>();
	private ClassLoader										cl			= null;

	@Override
	public InstantiationService build() {
		final InstantiationServiceConfig config = new InstantiationServiceConfig(
				getParams());
		final String id = new StateConfig(config.getState()).getId();

		InstantiationService service = null;
		if (SERVICES.containsKey(id)) {
			service = SERVICES.get(id);
		} else {
			service = new InstantiationService(config, cl);
		}
		return service;
	}

	/**
	 * With class loader.
	 *
	 * @param cl
	 *            the cl
	 * @return the instantiation service builder
	 */
	public InstantiationServiceBuilder withClassLoader(final ClassLoader cl) {
		this.cl = cl;
		return this;
	}

	static final Map<String, InstantiationService> getServices() {
		return SERVICES;
	}

}
