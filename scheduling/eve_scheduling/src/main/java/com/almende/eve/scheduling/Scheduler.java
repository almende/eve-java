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
