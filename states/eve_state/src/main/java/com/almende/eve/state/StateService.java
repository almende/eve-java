/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.state;

import java.util.Set;


/**
 * A service for managing State objects.
 */
public interface StateService {
	
	/**
	 * Delete.
	 * 
	 * @param instance
	 *            the instance
	 */
	void delete(State instance);
	
	/**
         * Delete. and only instance
         * 
         * @param instance
         *            the instance
         */
        void delete(State instance, Boolean instanceOnly);
	
	/**
	 * Gets the existing stateIds.
	 *
	 * @return the stateIds
	 */
	Set<String> getStateIds();
}
