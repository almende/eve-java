/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.scheduling;

import java.net.URI;

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
	 * Returns a (virtual) DateTime representation of the current time. For
	 * real-time this maps to Unixtime, for simulated time this can be offset,
	 * different tempo, or even discrete timestamps.
	 *
	 * @return the date time
	 */
	DateTime nowDateTime();

	/**
	 * Schedule.
	 *
	 * @param id
	 *            The id for this task, optional, leave null to generate UUID in
	 *            scheduler
	 * @param msg
	 *            the msg
	 * @param due
	 *            the due
	 * @return the scheduled id, can be used to cancel the schedule
	 */
	String schedule(final String id, Object msg, final DateTime due);

	/**
	 * Schedule.
	 *
	 * @param id
	 *            The id for this task, optional, leave null to generate UUID in
	 *            scheduler
	 * @param msg
	 *            the msg
	 * @param delay
	 *            the delay
	 * @return the scheduled id, can be used to cancel the schedule
	 */
	String schedule(final String id, final Object msg, final int delay);

	/**
	 * Schedule.
	 *
	 * @param id
	 *            The id for this task, optional, leave null to generate UUID in
	 *            scheduler
	 * @param msg
	 *            the msg
	 * @param delay
	 *            the delay
	 * @return the scheduled id, can be used to cancel the schedule
	 */
	String schedule(final String id, final Object msg, final long delay);

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

	/**
	 * Gets the scheduler url.
	 *
	 * @return the scheduler url
	 */
	URI getSchedulerUrl();
}
