/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.scheduling.clock;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * The Class ClockEntry.
 */
public class ClockEntry implements Comparable<ClockEntry> {
	private String		triggerId;
	private DateTime	due;
	private Runnable	callback;

	/**
	 * Instantiates a new clock entry.
	 */
	public ClockEntry() {};

	/**
	 * Instantiates a new clock entry.
	 *
	 * @param triggerId
	 *            the trigger id
	 * @param due
	 *            the due
	 * @param callback
	 *            the callback
	 */
	public ClockEntry(final String triggerId, final DateTime due,
			final Runnable callback) {
		this.triggerId = triggerId;
		this.due = due;
		this.callback = callback;
	}

	/**
	 * Gets the trigger id.
	 *
	 * @return TriggerId
	 */
	public String getTriggerId() {
		return triggerId;
	}

	/**
	 * Sets the trigger id.
	 *
	 * @param triggerId
	 *            the new trigger id
	 */
	public void setTriggerId(final String triggerId) {
		this.triggerId = triggerId;
	}

	/**
	 * Gets the due.
	 *
	 * @return Due date
	 */
	public DateTime getDue() {
		return due;
	}

	/**
	 * Sets the due.
	 *
	 * @param due
	 *            the new due
	 */
	public void setDue(final DateTime due) {
		this.due = due;
	}

	/**
	 * Gets the callback.
	 *
	 * @return This tasks callback.
	 */
	@JsonIgnore
	public Runnable getCallback() {
		return callback;
	}

	/**
	 * Sets the callback.
	 *
	 * @param callback
	 *            the new callback
	 */
	public void setCallback(final Runnable callback) {
		this.callback = callback;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null) {
			return false;
		}
		if (!(o instanceof ClockEntry)) {
			return false;
		}
		final ClockEntry other = (ClockEntry) o;
		if (triggerId == null || other.triggerId == null) {
			return false;
		}
		return triggerId.equals(other.triggerId);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return triggerId.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(final ClockEntry o) {
		if (due == null || o.due == null) {
			// Become consistent with equals:
			if (equals(o)) {
				return 0;
			} else {
				return -1;
			}
		}
		if (due.equals(o.due)) {
			// Become consistent with equals:
			if (equals(o)) {
				return 0;
			} else {
				return -1;
			}
		}
		return due.compareTo(o.due);
	}
}
