package com.almende.eve.entity.activity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class Constraints implements Serializable, Cloneable {
	public Constraints() {
	}
	
	public List<Attendee> withAttendees() {
		if (attendees == null) {
			attendees = new ArrayList<Attendee>();
		}
		return attendees;
	}
	
	public List<Attendee> getAttendees() {
		return attendees;
	}
	
	public void setAttendees(final List<Attendee> attendees) {
		this.attendees = attendees;
	}
	
	public Attendee withAttendee(final String agent) {
		Attendee attendee = getAttendee(agent);
		if (attendee == null) {
			attendee = new Attendee();
			attendee.setAgent(agent);
		}
		return attendee;
	}
	
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
	
	public Location withLocation() {
		if (location == null) {
			location = new Location();
		}
		return location;
	}
	
	public Location getLocation() {
		return location;
	}
	
	public void setLocation(final Location location) {
		this.location = location;
	}
	
	public Time withTime() {
		if (time == null) {
			time = new Time();
		}
		return time;
	}
	
	public Time getTime() {
		return time;
	}
	
	public void setTime(final Time time) {
		this.time = time;
	}
	
	/**
	 * Merge two Constraints objects.
	 * Note that the complete attendee list will be replaced if other.attendees
	 * is defined
	 * 
	 * @param other
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
