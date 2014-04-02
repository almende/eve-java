/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.entity;

import java.io.Serializable;

import org.joda.time.DateTime;
import org.joda.time.Interval;

/**
 * The Class Weight.
 */
@SuppressWarnings("serial")
public class Weight implements Serializable {
	protected Weight() {
	}
	
	/**
	 * Instantiates a new weight.
	 * 
	 * @param start
	 *            the start
	 * @param end
	 *            the end
	 * @param weight
	 *            the weight
	 */
	public Weight(final DateTime start, final DateTime end, final Double weight) {
		interval = new Interval(start, end);
		this.weight = weight;
	}
	
	/**
	 * Instantiates a new weight.
	 * 
	 * @param interval
	 *            the interval
	 * @param weight
	 *            the weight
	 */
	public Weight(final Interval interval, final Double weight) {
		this.interval = interval;
		this.weight = weight;
	}
	
	/**
	 * Instantiates a new weight.
	 * 
	 * @param other
	 *            the other
	 */
	public Weight(final Weight other) {
		interval = new Interval(other.interval);
		weight = new Double(other.weight);
	}
	
	/**
	 * Gets the interval.
	 * 
	 * @return the interval
	 */
	public Interval getInterval() {
		return interval;
	}
	
	/**
	 * Gets the start.
	 * 
	 * @return the start
	 */
	public DateTime getStart() {
		return interval.getStart();
	}
	
	/**
	 * Gets the end.
	 * 
	 * @return the end
	 */
	public DateTime getEnd() {
		return interval.getEnd();
	}
	
	/**
	 * Gets the weight.
	 * 
	 * @return the weight
	 */
	public Double getWeight() {
		return weight;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode(){
		if (interval != null && weight != null){
			return interval.hashCode() + weight.hashCode();
		} else if (interval != null){
			return interval.hashCode();
		} else if (weight != null){
			return weight.hashCode();
		} 
		return 1;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object o) {
		Weight other = null;
		if (o == null){
			return false;
		}
		if (o instanceof Weight){
			other = (Weight) o;
		}
		if (interval != null && other.interval != null) {
			// Do not use the normal interval.equals here,
			// sometimes the Chronology of two intervals differ, while the
			// start and end are equal. See also DateTime.compareTo
			// TODO: figure out this issue with Interval.equals
			// boolean equal = interval.equals(other.interval);
			final boolean equal = interval.getStartMillis() == other.interval
					.getStartMillis()
					&& interval.getEndMillis() == other.interval.getEndMillis();
			
			if (!equal) {
				return false;
			}
		} else if (interval != null || other.interval != null) {
			return false;
		}
		
		if (weight != null && other.weight != null) {
			return (weight.equals(other.weight));
		} else if (weight != null || other.weight != null) {
			return false;
		}
		
		return true;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return interval.toString() + "/" + weight.toString();
	}
	
	private Interval	interval	= null;
	private Double		weight		= null;
}
