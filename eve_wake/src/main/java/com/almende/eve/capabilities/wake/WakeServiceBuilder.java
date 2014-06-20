/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.capabilities.wake;

import java.util.HashMap;
import java.util.Map;

import com.almende.eve.capabilities.AbstractCapabilityBuilder;
import com.almende.eve.capabilities.Config;

/**
 * The Class WakeService.
 * This is a "resurrection" service, to which agents can register themselves to
 * be woken when the system reboots, WakeHandlers are called, etc.
 */
public class WakeServiceBuilder extends AbstractCapabilityBuilder<WakeService> {
	private static final Map<String, WakeService>	SERVICES	= new HashMap<String, WakeService>();
	
	@Override
	public WakeService build(){
		final Config config = new Config(getParams());
		final String id = config.get("state", "id");
		WakeService service = null;
		if (SERVICES.containsKey(id)) {
			service = SERVICES.get(id);
		}
		service = new WakeService(getParams());
		
		return service;
	}

	static final Map<String, WakeService> getServices() {
		return SERVICES;
	}
	
	
}
