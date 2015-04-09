/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.capabilities;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Interface Capability.
 */
public interface Capability {
	
	/**
	 * Gets the params.
	 * 
	 * @return the params
	 */
	ObjectNode getParams();
	
	/**
	 * Delete this capability, releasing all resources where applicable.
	 */
	void delete();

}
