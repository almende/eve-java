/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.state;

import com.almende.eve.capabilities.CapabilityService;

/**
 * A service for managing State objects.
 */
public interface StateService extends CapabilityService {
	
	/**
	 * Delete.
	 * 
	 * @param instance
	 *            the instance
	 */
	void delete(State instance);
	
}
