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
	private boolean		paused			= false;

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
		paused = false;
		startNextTriggers();
	}

	@Override
	public void stop() {
		paused = true;
	}

	@Override
	public void done(final String triggerId) {
		if (triggerId != null) {
			synchronized (activeTriggers) {
				activeTriggers.remove(triggerId);
			}
			startNextTriggers();
		}
	}

	@Override
	public void run() {
		final List<Runnable> toRun = new ArrayList<Runnable>();
		synchronized (TIMELINE) {
			while (!TIMELINE.isEmpty() && !paused) {
				final ClockEntry ce = TIMELINE.firstEntry().getValue();
				if (ce.getDue().isEqual(now) || ce.getDue().isBefore(now)) {
					TIMELINE.remove(ce);
					toRun.add(ce.getCallback());
					synchronized (activeTriggers) {
						activeTriggers.add(ce.getTriggerId());
					}
					continue;
				} else {
					break;
				}
			}
		}
		if (!toRun.isEmpty()) {
			//Contribute to atomic network support:
			SimulationInboxProtocol.schedulerOut(toRun.size());
			for (Runnable run : toRun) {
				RUNNER.execute(run);
			}
		}
	}

	private void startNextTriggers() {
		run();
		synchronized (TIMELINE) {
			if (!TIMELINE.isEmpty() && !paused) {
				synchronized (activeTriggers) {
					if (activeTriggers.isEmpty()) {
						final ClockEntry ce = TIMELINE.firstEntry().getValue();
						now = ce.getDue();
						run();
					}
				}
			}
		}
	}
}
