package com.almende.eve.entity;

import java.io.Serializable;

import org.joda.time.DateTime;
import org.joda.time.Interval;

@SuppressWarnings("serial")
public class Weight implements Serializable {
	protected Weight() {
	}
	
	public Weight(final DateTime start, final DateTime end, final Double weight) {
		interval = new Interval(start, end);
		this.weight = weight;
	}
	
	public Weight(final Interval interval, final Double weight) {
		this.interval = interval;
		this.weight = weight;
	}
	
	public Weight(final Weight other) {
		interval = new Interval(other.interval);
		weight = new Double(other.weight);
	}
	
	public Interval getInterval() {
		return interval;
	}
	
	public DateTime getStart() {
		return interval.getStart();
	}
	
	public DateTime getEnd() {
		return interval.getEnd();
	}
	
	public Double getWeight() {
		return weight;
	}
	
	public boolean equals(final Weight other) {
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
	
	@Override
	public String toString() {
		return interval.toString() + "/" + weight.toString();
	}
	
	private Interval	interval	= null;
	private Double		weight		= null;
}
