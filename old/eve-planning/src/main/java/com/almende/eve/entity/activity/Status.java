/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.entity.activity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * The Class Status.
 */
@SuppressWarnings("serial")
public class Status implements Serializable, Cloneable {
	
	/**
	 * Instantiates a new status.
	 */
	public Status() {
	}
	
	/**
	 * Gets the attendees.
	 * 
	 * @return the attendees
	 */
	public List<Attendee> getAttendees() {
		return attendees;
	}
	
	/**
	 * With attendees.
	 * 
	 * @return the list
	 */
	public List<Attendee> withAttendees() {
		if (attendees == null) {
			attendees = new ArrayList<Attendee>();
		}
		return attendees;
	}
	
	/**
	 * Sets the attendees.
	 * 
	 * @param attendees
	 *            the new attendees
	 */
	public void setAttendees(final List<Attendee> attendees) {
		this.attendees = attendees;
	}
	
	/**
	 * With location.
	 * 
	 * @return the location
	 */
	public Location withLocation() {
		if (location == null) {
			location = new Location();
		}
		return location;
	}
	
	/**
	 * Gets the location.
	 * 
	 * @return the location
	 */
	public Location getLocation() {
		return location;
	}
	
	/**
	 * Sets the location.
	 * 
	 * @param location
	 *            the new location
	 */
	public void setLocation(final Location location) {
		this.location = location;
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
	 * Sets the start.
	 * 
	 * @param start
	 *            the new start
	 */
	public void setStart(final String start) {
		this.start = start;
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
	 * Sets the end.
	 * 
	 * @param end
	 *            the new end
	 */
	public void setEnd(final String end) {
		this.end = end;
	}
	
	/**
	 * Sets the activity status.
	 * 
	 * @param activityStatus
	 *            the new activity status
	 */
	public void setActivityStatus(final ACTIVITY_STATUS activityStatus) {
		this.activityStatus = activityStatus;
	}
	
	/**
	 * Gets the activity status.
	 * 
	 * @return the activity status
	 */
	public ACTIVITY_STATUS getActivityStatus() {
		return activityStatus;
	}
	
	/**
	 * Gets the updated.
	 * 
	 * @return the updated
	 */
	public String getUpdated() {
		return updated;
	}
	
	/**
	 * Sets the updated.
	 * 
	 * @param updated
	 *            the new updated
	 */
	public void setUpdated(final String updated) {
		this.updated = updated;
	}
	
	/**
	 * Merge.
	 * 
	 * @param other
	 *            the other
	 */
	public void merge(final Status other) {
		if (other.attendees != null) {
			attendees = new ArrayList<Attendee>();
			for (final Attendee attendee : other.attendees) {
				attendees.add(attendee != null ? attendee.clone() : null);
			}
		}
		
		if (other.location != null) {
			if (location != null) {
				location.merge(other.location);
			} else {
				location = other.location.clone();
			}
		}
		if (other.start != null) {
			start = other.start;
		}
		if (other.end != null) {
			end = other.end;
		}
		if (other.activityStatus != null) {
			activityStatus = other.activityStatus;
		}
		if (other.updated != null) {
			updated = other.updated;
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Status clone() {
		final Status clone = new Status();
		
		if (attendees != null) {
			clone.attendees = new ArrayList<Attendee>();
			for (final Attendee attendee : attendees) {
				clone.attendees.add(attendee != null ? attendee.clone() : null);
			}
		}
		if (location != null) {
			clone.location = location.clone();
		}
		clone.start = start;
		clone.end = end;
		clone.activityStatus = activityStatus;
		clone.updated = updated;
		
		return clone;
	}
	
	/**
	 * The Enum ACTIVITY_STATUS.
	 */
	public static enum ACTIVITY_STATUS {
		
		/**
		 * The progress.
		 */
		progress, 
 /**
	 * The planned.
	 */
 planned, 
 /**
	 * The executed.
	 */
 executed, 
 /**
	 * The error.
	 */
 error
	};
	
	private List<Attendee>	attendees		= null;
	private Location		location		= null;
	private String			start			= null;
	private String			end				= null;
	private ACTIVITY_STATUS	activityStatus	= null;
	private String			updated			= null;
}
