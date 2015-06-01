/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.scheduling.clock;

import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.joda.time.DateTime;
import org.joda.time.Interval;

import com.almende.util.threads.ThreadPool;

/**
 * The Class RunnableClock.
 */
public class RunnableClock implements Runnable, Clock {
	private static final Logger								LOG			= Logger.getLogger(RunnableClock.class
																				.getName());
	protected final NavigableMap<ClockEntry, ClockEntry>	TIMELINE	= new TreeMap<ClockEntry, ClockEntry>();
	protected static final ScheduledExecutorService			SCHEDULER	= ThreadPool
																				.getScheduledPool();
	protected static final Executor							RUNNER		= ThreadPool
																				.getPool();
	protected ScheduledFuture<?>							future		= null;

	/*
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		final List<Runnable> toRun = new ArrayList<Runnable>();
		synchronized (TIMELINE) {
			while (!TIMELINE.isEmpty()) {
				final ClockEntry ce = TIMELINE.firstEntry().getValue();
				final DateTime now = DateTime.now();
				if (ce.getDue().isEqual(now) || ce.getDue().isBefore(now)) {
					TIMELINE.remove(ce);
					toRun.add(ce.getCallback());
					continue;
				}
				final long interval = new Interval(now, ce.getDue())
						.toDurationMillis();
				if (interval <= 0) {
					continue;
				}
				if (future == null
						|| future.getDelay(TimeUnit.MILLISECONDS) != interval) {
					if (future != null) {
						future.cancel(false);
					}
					future = SCHEDULER.schedule(this, interval,
							TimeUnit.MILLISECONDS);
				}
				break;
			}
			if (future == null && !TIMELINE.isEmpty()) {
				LOG.warning("Lost trigger, should never happen!");
			}
		}
		for (Runnable run : toRun) {
			RUNNER.execute(run);
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
			} else {
				LOG.warning(ce.getTriggerId()
						+ ": Skip adding ce, because has old value earlier than current. "
						+ oldVal.getTriggerId());
			}
		}
		RUNNER.execute(this);
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

	@Override
	public DateTime nowDateTime() {
		// Nothing todo, time progresses by itself:)
		return DateTime.now();
	}

	@Override
	public long now() {
		return nowDateTime().getMillis();
	}

	@Override
	public void start() {
		// Nothing todo, time progresses by itself:)
	}
	
	@Override
	public void stop() {
		// Nothing todo, time progresses by itself:)
	}

	@Override
	public void done(final String triggerId) {
		// Nothing todo, timeprogresses by itself:)
	}
}
