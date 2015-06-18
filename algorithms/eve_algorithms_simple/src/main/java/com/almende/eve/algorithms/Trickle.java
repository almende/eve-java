/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.algorithms;

/**
 * The Class Trickle, implementing: https://tools.ietf.org/html/rfc6206
 * This class is the underlying state machine, users should still understand and
 * support the algorithm at the communication side of things. The "consistancy"
 * definition is also completely a use responsibility;
 */
public class Trickle {
	// Milliseconds
	private long	intervalMin			= 100;
	private long	intervalMax			= (long) (Math.pow(2, 16) * intervalMin);
	private int		redundancyFactor	= 3;

	private long	currentInterval		= 100;
	private int		counter				= 0;

	/**
	 * Instantiates a new trickle.
	 */
	public Trickle() {}

	/**
	 * Instantiates a new trickle.
	 *
	 * @param intervalMin
	 *            the interval min
	 * @param intervalFactor
	 *            the interval factor
	 * @param redundancyFactor
	 *            the redundancy factor
	 */
	public Trickle(long intervalMin, int intervalFactor, int redundancyFactor) {
		this.intervalMin = intervalMin;
		this.intervalMax = (long) (Math.pow(2, intervalFactor) * intervalMin);
		this.redundancyFactor = redundancyFactor;
	}

	/**
	 * Start next interval, generating next random sending opportunity.
	 *
	 * @return the duration in ms to the next send, and the duration in ms to
	 *         the next interval
	 */
	public long[] next() {
		currentInterval = Math.min(intervalMax, currentInterval * 2);
		counter = 0;
		return new long[] {
				currentInterval
						- Math.round(Math.random() * (currentInterval / 2)),
				currentInterval };
	}

	/**
	 * Increment on consistent data receival
	 */
	public void incr() {
		counter++;
	}

	/**
	 * Check if allowed to send at this opportunity.
	 *
	 * @return true, if successful
	 */
	public boolean check() {
		return counter < redundancyFactor;
	}

	/**
	 * Reset interval on receival of inconsistent data, starting next interval;
	 *
	 * @return the duration in ms to the next send.
	 */
	public long[] reset() {
		if (currentInterval <= intervalMin) {
			currentInterval = intervalMin;
			return new long[0];
		} else {
			currentInterval = intervalMin / 2;
			return next();
		}
	}
}
