package com.almende.eve.entity.activity;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Location implements Serializable, Cloneable {
	public Location() {
	}
	
	public String getSummary() {
		return summary;
	}
	
	public void setSummary(final String summary) {
		this.summary = summary;
	}
	
	public Double getLat() {
		return lat;
	}
	
	public void setLat(final Double lat) {
		this.lat = lat;
	}
	
	public Double getLng() {
		return lng;
	}
	
	public void setLng(final Double lng) {
		this.lng = lng;
	}
	
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
