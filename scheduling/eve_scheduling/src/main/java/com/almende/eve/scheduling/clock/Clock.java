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
	void requestTrigger(final String triggerId, final DateTime due,
			final Runnable callback);

	/**
	 * Get time, for clocks that wait for next timestamp, reports current
	 * time;.
	 *
	 * @return the current timestamp
	 */
	DateTime nowDateTime();

	/**
	 * Get time, for clocks that wait for next timestamp, reports current
	 * time;.
	 *
	 * @return the current timestamp
	 */
	long now();

	/**
	 * Report that a trigger finished, for clocks that have discrete time.
	 *
	 * @param triggerId
	 *            the trigger id
	 */
	void done(final String triggerId);

	/**
	 * Start this clock, for clocks that have discrete time.
	 */
	void start();

	/**
	 * Stop this clock, for clocks that have discrete time.
	 */
	void stop();
	
	/**
	 * Cancel a triggerRequest.
	 * 
	 * @param triggerId
	 *            the trigger id
	 */
	void cancel(final String triggerId);

	/**
	 * Clear all triggerRequests!.
	 */
	void clear();

}
