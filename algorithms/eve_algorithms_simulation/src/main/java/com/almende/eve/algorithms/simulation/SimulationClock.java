/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.algorithms.simulation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.joda.time.DateTime;

import com.almende.eve.scheduling.clock.ClockEntry;
import com.almende.eve.scheduling.clock.RunnableClock;

/**
 * The Class SimulationClock.
 */
public class SimulationClock extends RunnableClock {
	private DateTime	now				= null;
	private Set<String>	activeTriggers	= new HashSet<String>();

	/**
	 * Instantiates a new simulation clock.
	 *
	 * @param initMillis
	 *            the init millis
	 */
	public SimulationClock(final long initMillis) {
		super();
		this.now = new DateTime(initMillis);
	}

	@Override
	public DateTime nowDateTime() {
		return this.now;
	}

	@Override
	public long now() {
		return this.now.getMillis();
	}

	@Override
	public void start() {
		startNextTriggers();
	}

	@Override
	public void done(final String triggerId) {
		if (triggerId != null) {
			synchronized (activeTriggers) {
				activeTriggers.remove(triggerId);
				if (activeTriggers.isEmpty()) {
					startNextTriggers();
				}
			}
		}
	}

	@Override
	public void run() {
		final List<Runnable> toRun = new ArrayList<Runnable>();
		synchronized (TIMELINE) {
			while (!TIMELINE.isEmpty()) {
				final ClockEntry ce = TIMELINE.firstEntry().getValue();
				if (ce.getDue().isEqual(now) || ce.getDue().isBefore(now)) {
					TIMELINE.remove(ce);
					toRun.add(ce.getCallback());
					synchronized (activeTriggers) {
						activeTriggers.add(ce.getTriggerId());
					}
					continue;
				}
				if (!toRun.isEmpty()) {
					for (Runnable run : toRun) {
						RUNNER.execute(run);
					}
				}
				return;
			}
		}
	}

	private void startNextTriggers() {
		final List<Runnable> toRun = new ArrayList<Runnable>();
		synchronized (TIMELINE) {
			while (!TIMELINE.isEmpty()) {
				final ClockEntry ce = TIMELINE.firstEntry().getValue();
				if (ce.getDue().isEqual(now) || ce.getDue().isBefore(now)) {
					TIMELINE.remove(ce);
					toRun.add(ce.getCallback());
					synchronized (activeTriggers) {
						activeTriggers.add(ce.getTriggerId());
					}
					continue;
				}
				if (!toRun.isEmpty()) {
					for (Runnable run : toRun) {
						RUNNER.execute(run);
					}
					return;
				} else {
					now = ce.getDue();
				}
			}
			if (!toRun.isEmpty()) {
				for (Runnable run : toRun) {
					RUNNER.execute(run);
				}
				return;
			}
		}
	}
}
