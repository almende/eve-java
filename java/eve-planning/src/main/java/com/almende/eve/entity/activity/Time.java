/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.entity.activity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * The Class Time.
 */
@SuppressWarnings("serial")
public class Time implements Serializable, Cloneable {
	
	/**
	 * Instantiates a new time.
	 */
	public Time() {
	}
	
	/**
	 * Gets the duration.
	 * 
	 * @return the duration
	 */
	public Long getDuration() {
		return duration;
	}
	
	/**
	 * Sets the duration.
	 * 
	 * @param duration
	 *            the new duration
	 */
	public void setDuration(final Long duration) {
		this.duration = duration;
	}
	
	/**
	 * Gets the duration min.
	 * 
	 * @return the duration min
	 */
	public Long getDurationMin() {
		return durationMin;
	}
	
	/**
	 * Sets the duration min.
	 * 
	 * @param durationMin
	 *            the new duration min
	 */
	public void setDurationMin(final Long durationMin) {
		this.durationMin = durationMin;
	}
	
	/**
	 * Gets the duration max.
	 * 
	 * @return the duration max
	 */
	public Long getDurationMax() {
		return durationMax;
	}
	
	/**
	 * Sets the duration max.
	 * 
	 * @param durationMax
	 *            the new duration max
	 */
	public void setDurationMax(final Long durationMax) {
		this.durationMax = durationMax;
	}
	
	/**
	 * Gets the period start.
	 * 
	 * @return the period start
	 */
	public String getPeriodStart() {
		return periodStart;
	}
	
	/**
	 * Sets the period start.
	 * 
	 * @param periodStart
	 *            the new period start
	 */
	public void setPeriodStart(final String periodStart) {
		this.periodStart = periodStart;
	}
	
	/**
	 * Gets the period end.
	 * 
	 * @return the period end
	 */
	public String getPeriodEnd() {
		return periodEnd;
	}
	
	/**
	 * Sets the period end.
	 * 
	 * @param periodEnd
	 *            the new period end
	 */
	public void setPeriodEnd(final String periodEnd) {
		this.periodEnd = periodEnd;
	}
	
	/**
	 * With preferences.
	 * 
	 * @return the list
	 */
	public List<Preference> withPreferences() {
		if (preferences == null) {
			preferences = new ArrayList<Preference>();
		}
		return preferences;
	}
	
	/**
	 * Gets the preferences.
	 * 
	 * @return the preferences
	 */
	public List<Preference> getPreferences() {
		return preferences;
	}
	
	/**
	 * Sets the preferences.
	 * 
	 * @param preferences
	 *            the new preferences
	 */
	public void setPreferences(final List<Preference> preferences) {
		this.preferences = preferences;
	}
	
	/**
	 * Adds the preference.
	 * 
	 * @param preference
	 *            the preference
	 */
	public void addPreference(final Preference preference) {
		final List<Preference> preferences = withPreferences();
		preferences.add(preference);
	}
	
	/**
	 * Merge.
	 * 
	 * @param other
	 *            the other
	 */
	public void merge(final Time other) {
		if (other.duration != null) {
			duration = other.duration;
		}
		if (other.durationMin != null) {
			durationMin = other.durationMin;
		}
		if (other.durationMax != null) {
			durationMax = other.durationMax;
		}
		if (other.periodStart != null) {
			periodStart = other.periodStart;
		}
		if (other.periodEnd != null) {
			periodEnd = other.periodEnd;
		}
		
		if (other.preferences != null) {
			preferences = new ArrayList<Preference>();
			for (final Preference preference : other.preferences) {
				preferences.add(preference != null ? preference.clone() : null);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Time clone() {
		final Time clone = new Time();
		
		clone.duration = duration;
		clone.durationMin = durationMin;
		clone.durationMax = durationMax;
		clone.periodStart = periodStart;
		clone.periodEnd = periodEnd;
		
		if (preferences != null) {
			clone.preferences = new ArrayList<Preference>();
			for (final Preference preference : preferences) {
				clone.preferences.add(preference != null ? preference.clone()
						: null);
			}
		}
		
		return clone;
	}
	
	private Long				duration	= null; // milliseconds
	private Long				durationMin	= null; // milliseconds
	private Long				durationMax	= null; // milliseconds
	private String				periodStart	= null;
	private String				periodEnd	= null;
	private List<Preference>	preferences	= null;
}
