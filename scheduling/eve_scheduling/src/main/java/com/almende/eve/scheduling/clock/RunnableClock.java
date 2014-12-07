/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.scheduling.clock;

import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.joda.time.DateTime;
import org.joda.time.Interval;

import com.almende.util.threads.ThreadPool;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * The Class RunnableClock.
 */
public class RunnableClock implements Runnable, Clock {
	private static final NavigableMap<ClockEntry, ClockEntry>	TIMELINE	= new TreeMap<ClockEntry, ClockEntry>();
	private static final ScheduledExecutorService				SCHEDULER	= ThreadPool
																					.getScheduledPool();
	private static final Executor								RUNNER		= ThreadPool
																					.getPool();
	private static ScheduledFuture<?>							future		= null;

	/*
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		synchronized (TIMELINE) {
			while (!TIMELINE.isEmpty()) {
				final ClockEntry ce = TIMELINE.firstEntry().getValue();
				if (future != null) {
					future.cancel(false);
					future = null;
				}
				final DateTime now = DateTime.now();
				if (ce.getDue().isEqual(now) || ce.getDue().isBefore(now)) {
					TIMELINE.remove(ce);
					RUNNER.execute(ce.getCallback());
					continue;
				}
				final long interval = new Interval(now, ce.getDue())
						.toDurationMillis();
				if (interval <= 0){
					continue;
				}
				while (future == null) {
					future = SCHEDULER.schedule(this, interval,
							TimeUnit.MILLISECONDS);
				}
				break;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.almende.eve.scheduler.clock.Clock#requestTrigger(java.lang.String,
	 * org.joda.time.DateTime, java.lang.Runnable)
	 */
	@Override
	public void requestTrigger(final String triggerId, final DateTime due,
			final Runnable callback) {
		synchronized (TIMELINE) {
			final ClockEntry ce = new ClockEntry(triggerId, due, callback);
			final ClockEntry oldVal = TIMELINE.get(ce);
			if (oldVal == null || oldVal.getDue().isAfter(due)) {
				TIMELINE.put(ce, ce);
				RUNNER.execute(this);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.almende.eve.scheduling.clock.Clock#cancel(java.lang.String)
	 */
	@Override
	public void cancel(final String triggerId) {
		synchronized (TIMELINE) {
			final ClockEntry ce = new ClockEntry(triggerId, null, null);
			TIMELINE.remove(ce);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.almende.eve.scheduling.clock.Clock#clear()
	 */
	@Override
	public void clear() {
		synchronized (TIMELINE) {
			TIMELINE.clear();
			if (future != null) {
				future.cancel(false);
				future = null;
			}
		}
	}
}

/**
 * @author Almende
 */
class ClockEntry implements Comparable<ClockEntry> {
	private String		triggerId;
	private DateTime	due;
	private Runnable	callback;

	public ClockEntry(){};
	
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
		if (!(o instanceof ClockEntry)) {
			return false;
		}
		final ClockEntry other = (ClockEntry) o;
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
			if (equals(o)) {
				return 0;
			} else {
				return -1;
			}
		}
		if (due.equals(o.due)) {
			return 0;
		}
		return due.compareTo(o.due);
	}
}
