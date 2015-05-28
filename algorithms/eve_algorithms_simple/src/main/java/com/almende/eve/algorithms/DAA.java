/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.algorithms;

import java.util.Arrays;

import org.joda.time.DateTime;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The API class for the Distributed Aggregation Algorithm (Separable Function Computation)
 */
public class DAA {
	protected int			width			= 1000;
	protected int			initialTTL		= 10;
	protected int			evictionFactor	= 10;
	protected DAAValueBean	localValue		= null;
	protected DAAValueBean	currentEstimate	= null;

	/**
	 * Instantiates a new api.
	 */
	public DAA() {}

	/**
	 * Configure this library, using a json DOM structure to wrap the
	 * configuration.
	 *
	 * @param config
	 *            the config
	 */
	public void configure(ObjectNode config) {
		if (config.has("width")) {
			this.width = config.get("width").asInt();
		}
		if (config.has("initialTTL")) {
			this.initialTTL = config.get("initialTTL").asInt();
		}
		if (config.has("evictionFactor")) {
			this.evictionFactor = config.get("evictionFactor").asInt();
		}
	}

	/**
	 * Compute sum, returning estimated sum value of network.
	 *
	 * @return the double
	 */
	public Double computeSum() {
		return currentEstimate.computeSum();
	}

	/**
	 * Sets this nodes new value.
	 *
	 * @param value
	 *            the new new value
	 */
	public void setNewValue(final double value) {
		localValue = new DAAValueBean(width, evictionFactor);
		localValue.generate(value).setTTL(DateTime.now().plus(100).getMillis());
		if (currentEstimate == null) {
			currentEstimate = new DAAValueBean(width, evictionFactor);
			Arrays.fill(currentEstimate.valueArray, Double.MAX_VALUE);
		}
		currentEstimate.minimum(localValue);
	}

	/**
	 * Receive the new estimate (from a neighbor), updating local estimate.
	 *
	 * @param bean
	 *            the bean
	 */
	public void receive(DAAValueBean bean) {
		if (currentEstimate != null) {
			currentEstimate.minimum(bean);
			currentEstimate.minimum(localValue);
		}
	}

	/**
	 * Gets the current network estimate.
	 *
	 * @return the value
	 */
	public DAAValueBean getCurrentEstimate() {
		return currentEstimate;
	}

	/**
	 * Gets the original local value.
	 *
	 * @return the local value
	 */
	public DAAValueBean getLocalValue() {
		return localValue;
	}
}