/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.transport;

import java.net.URI;

import com.almende.eve.capabilities.CapabilityService;

/**
 * The Interface TransportService.
 */
public interface TransportService extends CapabilityService {
	
	/**
	 * Delete.
	 * 
	 * @param instance
	 *            the instance
	 */
	void delete(Transport instance);
	
	/**
	 * Looks for local transports, returns null if non is found. For local
	 * shortcut support.
	 * 
	 * @param address
	 *            the address
	 * @return the local
	 */
	Transport getLocal(URI address);
}
