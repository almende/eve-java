/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.algorithms.simulation;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

import com.almende.eve.scheduling.clock.ClockEntry;
import com.almende.eve.scheduling.clock.RunnableClock;

/**
 * The Class SimulationClock.
 */
public class SimulationClock extends RunnableClock {
	private DateTime	now	= null;

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
	public void run() {
		//Doesn't do anything
	}

	private void startNextTriggers() {
		final List<Runnable> toRun = new ArrayList<Runnable>();
		synchronized (TIMELINE) {
			while (!TIMELINE.isEmpty()) {
				final ClockEntry ce = TIMELINE.firstEntry().getValue();
				if (ce.getDue().isEqual(now) || ce.getDue().isBefore(now)) {
					TIMELINE.remove(ce);
					toRun.add(ce.getCallback());
					continue;
				}
				// TODO: if (toRun is empty, move clock to new TIMELINE stamp,
				// fill with all at that level.
				if (!toRun.isEmpty()) {
					for (Runnable run : toRun) {
						RUNNER.execute(run);
					}
					return;
				} else {
					now = ce.getDue();
				}
			}
		}
	}

	@Override
	public DateTime progressTime() {
		startNextTriggers();
		return now;
	}

}
