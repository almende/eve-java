/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.entity.activity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * The Class Constraints.
 */
@SuppressWarnings("serial")
public class Constraints implements Serializable, Cloneable {
	
	/**
	 * Instantiates a new constraints.
	 */
	public Constraints() {
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
	 * Gets the attendees.
	 * 
	 * @return the attendees
	 */
	public List<Attendee> getAttendees() {
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
	 * With attendee.
	 * 
	 * @param agent
	 *            the agent
	 * @return the attendee
	 */
	public Attendee withAttendee(final String agent) {
		Attendee attendee = getAttendee(agent);
		if (attendee == null) {
			attendee = new Attendee();
			attendee.setAgent(agent);
		}
		return attendee;
	}
	
	/**
	 * Gets the attendee.
	 * 
	 * @param agent
	 *            the agent
	 * @return the attendee
	 */
	public Attendee getAttendee(final String agent) {
		if (attendees != null) {
			for (final Attendee attendee : attendees) {
				if (attendee.getAgent().equals(agent)) {
					return attendee;
				}
			}
		}
		return null;
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
	 * With time.
	 * 
	 * @return the time
	 */
	public Time withTime() {
		if (time == null) {
			time = new Time();
		}
		return time;
	}
	
	/**
	 * Gets the time.
	 * 
	 * @return the time
	 */
	public Time getTime() {
		return time;
	}
	
	/**
	 * Sets the time.
	 * 
	 * @param time
	 *            the new time
	 */
	public void setTime(final Time time) {
		this.time = time;
	}
	
	/**
	 * Merge two Constraints objects.
	 * Note that the complete attendee list will be replaced if other.attendees
	 * is defined
	 * 
	 * @param other
	 *            the other
	 */
	public void merge(final Constraints other) {
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
		
		if (other.time != null) {
			if (time != null) {
				time.merge(other.time);
			} else {
				time = other.time.clone();
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Constraints clone() {
		final Constraints clone = new Constraints();
		
		if (attendees != null) {
			clone.attendees = new ArrayList<Attendee>();
			for (final Attendee attendee : attendees) {
				clone.attendees.add(attendee != null ? attendee.clone() : null);
			}
		}
		if (location != null) {
			clone.location = location.clone();
		}
		if (time != null) {
			clone.time = time.clone();
		}
		
		return clone;
	}
	
	private List<Attendee>	attendees	= null;
	private Location		location	= null;
	private Time			time		= null;
}
