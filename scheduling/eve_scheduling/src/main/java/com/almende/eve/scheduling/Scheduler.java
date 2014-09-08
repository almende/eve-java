/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.scheduling;

import org.joda.time.DateTime;

import com.almende.eve.capabilities.Capability;

/**
 * The Interface Scheduler.
 */
public interface Scheduler extends Capability {

	/**
	 * Number of (virtual) milliseconds since midnight 1-1-1970 (UTC). For
	 * real-time this maps to Unixtime, for simulated time this can be offset,
	 * different tempo, or even discrete timestamps.
	 *
	 * @return the long
	 */
	long now();

	/**
	 * Schedule.
	 * 
	 * @param msg
	 *            the msg
	 * @param due
	 *            the due
	 * @return the scheduled id, can be used to cancel the schedule
	 */
	String schedule(Object msg, final DateTime due);

	/**
	 * Schedule.
	 * 
	 * @param msg
	 *            the msg
	 * @param delay
	 *            the delay
	 * @return the scheduled id, can be used to cancel the schedule
	 */
	String schedule(Object msg, final int delay);

	/**
	 * Cancel task with given id.
	 * 
	 * @param id
	 *            the id
	 */
	void cancel(String id);

	/**
	 * Cancel all scheduled tasks, clear queue.
	 */
	void clear();

}
