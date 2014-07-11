/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.scheduling.clock;

import org.joda.time.DateTime;

/**
 * The Interface Clock.
 */
public interface Clock {
	
	/**
	 * Request trigger.
	 * 
	 * @param triggerId
	 *            the agent id
	 * @param due
	 *            the due
	 * @param callback
	 *            the callback
	 */
	void requestTrigger(String triggerId, DateTime due, Runnable callback);
	
	/**
	 * Cancel a triggerRequest.
	 * 
	 * @param triggerId
	 *            the trigger id
	 */
	void cancel(String triggerId);
	
	/**
	 * Clear all triggerRequests!.
	 */
	void clear();
	
}
