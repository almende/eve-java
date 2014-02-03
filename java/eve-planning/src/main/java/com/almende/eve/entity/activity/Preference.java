/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.entity.activity;

import java.io.Serializable;

/**
 * The Class Preference.
 */
@SuppressWarnings("serial")
public class Preference implements Serializable, Cloneable {
	
	/**
	 * Instantiates a new preference.
	 */
	public Preference() {
	}
	
	/**
	 * Sets the start.
	 * 
	 * @param start
	 *            the new start
	 */
	public void setStart(final String start) {
		this.start = start;
	}
	
	/**
	 * Gets the start.
	 * 
	 * @return the start
	 */
	public String getStart() {
		return start;
	}
	
	/**
	 * Sets the end.
	 * 
	 * @param end
	 *            the new end
	 */
	public void setEnd(final String end) {
		this.end = end;
	}
	
	/**
	 * Gets the end.
	 * 
	 * @return the end
	 */
	public String getEnd() {
		return end;
	}
	
	/**
	 * Sets the weight.
	 * 
	 * @param weight
	 *            the new weight
	 */
	public void setWeight(final Double weight) {
		this.weight = weight;
	}
	
	/**
	 * Gets the weight.
	 * 
	 * @return the weight
	 */
	public Double getWeight() {
		return weight;
	}
	
	/**
	 * Merge.
	 * 
	 * @param other
	 *            the other
	 */
	public void merge(final Preference other) {
		if (other.start != null) {
			start = other.start;
		}
		if (other.end != null) {
			end = other.end;
		}
		if (other.weight != null) {
			weight = other.weight;
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Preference clone() {
		final Preference clone = new Preference();
		clone.start = start;
		clone.end = end;
		clone.weight = weight;
		return clone;
	}
	
	private String	start	= null;
	private String	end		= null;
	private Double	weight	= null; // positive for preferred intervals,
									// negative for undesirable intervals
}
