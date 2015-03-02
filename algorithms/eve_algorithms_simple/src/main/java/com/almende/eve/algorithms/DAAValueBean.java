/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.algorithms;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * The Class ValueBean.
 */
public class DAAValueBean {
	private int			evictionFactor	= 10;
	private int			width			= 0;
	private double		offset			= 0.0;
	private double[]	valueArray		= null;
	private int[]	ttlArray		= null;

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
		ttlArray = new int[width];
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
	public int[] getTtlArray() {
		return ttlArray;
	}

	/**
	 * Sets the ttl array.
	 *
	 * @param ttlArray
	 *            the new ttl array
	 */
	public void setTtlArray(final int[] ttlArray) {
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

	private Double computeMean() {
		Double result = 0.0;
		for (Double i : valueArray) {
			result += Math.abs(i);
		}
		return result / width;
	}

	/**
	 * Generate the random array to represent this value;.
	 *
	 * @param value
	 *            the value
	 * @param initialTTL
	 *            the initial ttl
	 * @return the value bean
	 */
	public DAAValueBean generate(final Double value, final int initialTTL) {
		Arrays.fill(ttlArray, initialTTL + 1);
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
		StringBuilder log = new StringBuilder();
		if (this.width != other.width) {
			throw new IllegalArgumentException(
					"ValueBeans aren't of the same length:(" + this.width + "/"
							+ other.width + ")!");
		}
		log.append("a:"+this.avgTTL());
		for (int i = 0; i < width; i++) {
			if (other.ttlArray[i] <= 1) {
				log.append("_");
				continue;
			}
			if (valueArray[i] < 0 && ttlArray[i] > 1){
				log.append("=");
				continue;
			}
			if (other.valueArray[i] < 0) {
				if ((other.valueArray[i] * -1) == valueArray[i]) {
					log.append("-");
					valueArray[i] = other.valueArray[i];
					ttlArray[i] = (other.ttlArray[i] / 2) - 1;
				}
			} else {
				if (other.valueArray[i] == valueArray[i]) {
					if (ttlArray[i] == (other.ttlArray[i] - 1)) {
//						log.append("'");
					} else if (ttlArray[i] < (other.ttlArray[i]-1)) {
						if (other.ttlArray[i] == 11) {
							log.append(":");
						} else {
							log.append("|");
						}
						ttlArray[i] = other.ttlArray[i]-1;
					} else {
//						log.append(";");
					}

//					ttlArray[i] = Math.max(ttlArray[i], other.ttlArray[i] - 1);
				}
				if (other.valueArray[i] < valueArray[i]) {
					log.append(".");
					valueArray[i] = other.valueArray[i];
					ttlArray[i] = other.ttlArray[i] - 1;
				}
			}
			if (ttlArray[i] <= 0 && other.ttlArray[i] > 1) {
				log.append(",");
				valueArray[i] = other.valueArray[i];
				ttlArray[i] = other.ttlArray[i] - 1;
			}
		}
		log.append("b:"+this.avgTTL());
//		System.out.println(log.toString());
		return this;
	}

	/**
	 * Decrease ttl.
	 */
	public void decreaseTTL() {
		for (int i = 0; i < ttlArray.length; i++) {
			if (valueArray[i] < 0) {
				ttlArray[i] = (ttlArray[i] / 2) - 1;
			} else {
				ttlArray[i] = ttlArray[i] - 1;
			}
		}
	}

	/**
	 * Avg ttl.
	 *
	 * @return the int
	 */
	public int avgTTL() {
		int res = 0;
		for (int i = 0; i < ttlArray.length; i++) {
			res += ttlArray[i];
		}
		return res/ttlArray.length;
	}

	/**
	 * Negate.
	 *
	 * @param other
	 *            the other
	 * @return the value bean
	 */
	public DAAValueBean negate(DAAValueBean other) {
		if (this.width != other.width) {
			throw new IllegalArgumentException(
					"ValueBeans aren't of the same length:(" + this.width + "/"
							+ other.width + ")!");
		}
		for (int i = 0; i < width; i++) {
			if (valueArray[i] == other.valueArray[i]) {
				valueArray[i] = -valueArray[i];
				ttlArray[i] = evictionFactor * ttlArray[i];
			}
		}
		return this;
	}
}
