/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.daa;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The API class for the Distributed Aggregation Algorithm.
 */
public class Api {
	private int			width			= 1000;
	private int			initialTTL		= 10;
	private int			evictionFactor	= 10;
	private ValueBean	localValue		= null;
	private ValueBean	currentEstimate	= null;

	/**
	 * Instantiates a new api.
	 */
	public Api() {}

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
	public void setNewValue(Double value) {
		localValue = new ValueBean(width, evictionFactor);
		localValue.generate(value, initialTTL);
	}

	/**
	 * Receive the new estimate (from a neighbor), updating local estimate.
	 *
	 * @param bean
	 *            the bean
	 */
	public void receive(ValueBean bean) {
		currentEstimate.minimum(bean).minimum(localValue);
	}

	/**
	 * Gets the current network estimate.
	 *
	 * @return the value
	 */
	public ValueBean getCurrentEstimate() {
		return currentEstimate;
	}

	/**
	 * Gets the original local value.
	 *
	 * @return the local value
	 */
	public ValueBean getLocalValue() {
		return localValue;
	}

	/**
	 * Negate and get the original local value.
	 *
	 * @return the value bean
	 */
	public ValueBean negateValue() {
		return localValue.negate();
	}
}