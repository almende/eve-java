/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.transport;

import com.almende.eve.capabilities.Capability;

/**
 * The Interface TransportService.
 */
public interface TransportService extends Capability {
	
	/**
	 * Delete.
	 * 
	 * @param instance
	 *            the instance
	 */
	void delete(Transport instance);
	
}
