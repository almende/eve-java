/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.algorithms;

import java.util.Arrays;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * The Class ValueBean.
 */
public class DAAValueBean {
	protected int		evictionFactor	= 10;
	protected int		width			= 0;
	protected double	offset			= 0.0;
	protected double[]	valueArray		= null;
	protected long[]	ttlArray		= null;

	/**
	 * Instantiates a new value bean.
	 */
	public DAAValueBean() {}

	/**
	 * Instantiates a new value bean.
	 *
	 * @param width
	 *            the width
	 * @param evictionFactor
	 *            the eviction factor
	 */
	public DAAValueBean(final int width, final int evictionFactor) {
		this.width = width;
		this.evictionFactor = evictionFactor;
		valueArray = new double[width];
		ttlArray = new long[width];
	}

	/**
	 * Gets the eviction factor.
	 *
	 * @return the eviction factor
	 */
	public int getEvictionFactor() {
		return evictionFactor;
	}

	/**
	 * Sets the eviction factor.
	 *
	 * @param evictionFactor
	 *            the new eviction factor
	 */
	public void setEvictionFactor(int evictionFactor) {
		this.evictionFactor = evictionFactor;
	}

	/**
	 * Gets the width.
	 *
	 * @return the width
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * Sets the width.
	 *
	 * @param width
	 *            the new width
	 */
	public void setWidth(int width) {
		this.width = width;
	}

	/**
	 * Gets the value array.
	 *
	 * @return the value array
	 */
	public double[] getValueArray() {
		return valueArray;
	}

	/**
	 * Sets the value array.
	 *
	 * @param valueArray
	 *            the new value array
	 */
	public void setValueArray(final double[] valueArray) {
		this.valueArray = valueArray;
	}

	/**
	 * Gets the ttl array.
	 *
	 * @return the ttl array
	 */
	public long[] getTtlArray() {
		return ttlArray;
	}

	/**
	 * Sets the ttl array.
	 *
	 * @param ttlArray
	 *            the new ttl array
	 */
	public void setTtlArray(final long[] ttlArray) {
		this.ttlArray = ttlArray;
	}

	/**
	 * Gets the offset.
	 *
	 * @return the offset
	 */
	@JsonIgnore
	public double getOffset() {
		return offset;
	}

	/**
	 * Compute sum.
	 *
	 * @return the double
	 */
	public Double computeSum() {
		return (1.0 / computeMean());
	}

	private double computeMean() {
		double result = 0.0;
		for (double i : valueArray) {
			result += Math.abs(i);
		}
		return result / width;
	}

	/**
	 * Generate the random array to represent this value;.
	 *
	 * @param value
	 *            the value
	 * @return the value bean
	 */
	public DAAValueBean generate(final double value) {
		if (value <= 0.0) {
			Arrays.fill(valueArray, Double.MAX_VALUE);
		} else {
			for (int i = 0; i < width; i++) {
				Double expRand = -Math.log(Math.random()) / value;
				valueArray[i] = expRand;
			}
			// Noise cancelation:
			final Double mean = computeMean();
			final Double goal = 1.0 / value;
			offset = goal / mean;
			for (int i = 0; i < width; i++) {
				valueArray[i] = valueArray[i] * offset;
			}
		}
		return this;
	}

	/**
	 * Sets the ttl.
	 *
	 * @param initialTTL
	 *            the initial ttl
	 * @return the DAA value bean
	 */
	public DAAValueBean setTTL(final long initialTTL) {
		Arrays.fill(ttlArray, initialTTL);
		return this;
	}

	/**
	 * Make this ValueBean represent the minimum with regard to the given other.
	 * Returns reference to itself, for chaining.
	 *
	 * @param other
	 *            the other
	 * @return the value bean
	 * @throws IllegalArgumentException
	 *             the illegal argument exception
	 */
	public DAAValueBean minimum(final DAAValueBean other)
			throws IllegalArgumentException {
		if (this.width != other.width) {
			throw new IllegalArgumentException(
					"ValueBeans aren't of the same length:(" + this.width + "/"
							+ other.width + ")!");
		}
		for (int i = 0; i < width; i++) {
			if (other.valueArray[i] == valueArray[i]) {
				ttlArray[i] = Math.max(ttlArray[i], other.ttlArray[i]);
			} else {
				ttlArray[i] = Math.min(ttlArray[i], other.ttlArray[i]);
				if (other.valueArray[i] < valueArray[i]) {
					valueArray[i] = other.valueArray[i];
					ttlArray[i] = other.ttlArray[i];
				}
			}
			final long now = DateTime.now().getMillis();
			if (ttlArray[i] < now && other.ttlArray[i] > now) {
				valueArray[i] = other.valueArray[i];
				ttlArray[i] = other.ttlArray[i];
			}
		}
		return this;
	}

	/**
	 * Avg ttl.
	 *
	 * @return the int
	 */
	public long avgTTL() {
		long res = 0;
		for (int i = 0; i < ttlArray.length; i++) {
			res += ttlArray[i];
		}
		return res / ttlArray.length;
	}
}
