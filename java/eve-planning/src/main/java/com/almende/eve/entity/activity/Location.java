/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.entity.activity;

import java.io.Serializable;

/**
 * The Class Location.
 */
@SuppressWarnings("serial")
public class Location implements Serializable, Cloneable {
	
	/**
	 * Instantiates a new location.
	 */
	public Location() {
	}
	
	/**
	 * Gets the summary.
	 * 
	 * @return the summary
	 */
	public String getSummary() {
		return summary;
	}
	
	/**
	 * Sets the summary.
	 * 
	 * @param summary
	 *            the new summary
	 */
	public void setSummary(final String summary) {
		this.summary = summary;
	}
	
	/**
	 * Gets the lat.
	 * 
	 * @return the lat
	 */
	public Double getLat() {
		return lat;
	}
	
	/**
	 * Sets the lat.
	 * 
	 * @param lat
	 *            the new lat
	 */
	public void setLat(final Double lat) {
		this.lat = lat;
	}
	
	/**
	 * Gets the lng.
	 * 
	 * @return the lng
	 */
	public Double getLng() {
		return lng;
	}
	
	/**
	 * Sets the lng.
	 * 
	 * @param lng
	 *            the new lng
	 */
	public void setLng(final Double lng) {
		this.lng = lng;
	}
	
	/**
	 * Merge.
	 * 
	 * @param other
	 *            the other
	 */
	public void merge(final Location other) {
		if (other.summary != null) {
			summary = other.summary;
		}
		if (other.lat != null) {
			lat = other.lat;
		}
		if (other.lng != null) {
			lng = other.lng;
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Location clone() {
		final Location clone = new Location();
		clone.summary = summary;
		clone.lat = lat;
		clone.lng = lng;
		return clone;
	}
	
	private String	summary	= null;
	private Double	lat		= null; // latitude
	private Double	lng		= null; // longitude
}
