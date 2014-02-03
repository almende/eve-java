/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.entity.activity;

import java.io.Serializable;
import java.net.URI;

import org.joda.time.DateTime;

/**
 * The Class Activity.
 */
@SuppressWarnings("serial")
public class Activity implements Serializable, Cloneable {
	
	private String		summary		= null;
	private String		description	= null;
	private URI			agent		= null; // The agent managing the activity
	private Constraints	constraints	= null;
	private Status		status		= null;
	
	/**
	 * Instantiates a new activity.
	 */
	public Activity() {
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
	 * Gets the summary.
	 * 
	 * @return the summary
	 */
	public String getSummary() {
		return summary;
	}
	
	/**
	 * Sets the description.
	 * 
	 * @param description
	 *            the new description
	 */
	public void setDescription(final String description) {
		this.description = description;
	}
	
	/**
	 * Gets the description.
	 * 
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * Sets the agent.
	 * 
	 * @param agent
	 *            the new agent
	 */
	public void setAgent(final URI agent) {
		this.agent = agent;
	}
	
	/**
	 * Gets the agent.
	 * 
	 * @return the agent
	 */
	public URI getAgent() {
		return agent;
	}
	
	/**
	 * Sets the constraints.
	 * 
	 * @param constraints
	 *            the new constraints
	 */
	public void setConstraints(final Constraints constraints) {
		this.constraints = constraints != null ? constraints
				: new Constraints();
	}
	
	/**
	 * Gets the constraints.
	 * 
	 * @return the constraints
	 */
	public Constraints getConstraints() {
		return constraints;
	}
	
	/**
	 * With constraints.
	 * 
	 * @return the constraints
	 */
	public Constraints withConstraints() {
		if (constraints == null) {
			constraints = new Constraints();
		}
		return constraints;
	}
	
	/**
	 * Sets the status.
	 * 
	 * @param status
	 *            the new status
	 */
	public void setStatus(final Status status) {
		this.status = status != null ? status : new Status();
	}
	
	/**
	 * With status.
	 * 
	 * @return the status
	 */
	public Status withStatus() {
		if (status == null) {
			status = new Status();
		}
		return status;
	}
	
	/**
	 * Gets the status.
	 * 
	 * @return the status
	 */
	public Status getStatus() {
		return status;
	}
	
	/**
	 * Merge.
	 * 
	 * @param other
	 *            the other
	 */
	public void merge(final Activity other) {
		if (other.summary != null) {
			summary = other.summary;
		}
		if (other.description != null) {
			description = other.description;
		}
		if (other.agent != null) {
			agent = other.agent;
		}
		if (other.constraints != null) {
			if (constraints != null) {
				constraints.merge(other.constraints);
			} else {
				constraints = other.constraints.clone();
			}
		}
		if (other.status != null) {
			if (status != null) {
				status.merge(other.status);
			} else {
				status = other.status.clone();
			}
		}
		status.merge(other.status);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Activity clone() {
		final Activity clone = new Activity();
		
		clone.summary = summary;
		clone.description = description;
		clone.agent = agent;
		if (constraints != null) {
			clone.constraints = constraints.clone();
		}
		if (status != null) {
			clone.status = status.clone();
		}
		
		return clone;
	}
	
	/**
	 * Check if this Activity is updated more recently than an other Activity.
	 * 
	 * @param other
	 *            the other
	 * @return true, if is newer than
	 */
	public boolean isNewerThan(final Activity other) {
		DateTime updatedThis = null;
		if (getStatus() != null && getStatus().getUpdated() != null) {
			updatedThis = new DateTime(getStatus().getUpdated());
		}
		DateTime updatedOther = null;
		if (other.getStatus() != null && other.getStatus().getUpdated() != null) {
			updatedOther = new DateTime(other.getStatus().getUpdated());
		}
		
		if (updatedOther == null) {
			// take this as newest
			return true;
		} else if (updatedThis == null) {
			// take other as newest
			return false;
		} else if (updatedThis.isAfter(updatedOther)) {
			// take this as newest
			return true;
		} else {
			// take other as newest
			return false;
		}
	}
	
	/**
	 * Synchronize two activities.
	 * The newest activity will be merged into a clone of the oldest activity.
	 * 
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 * @return the activity
	 */
	public static Activity sync(final Activity a, final Activity b) {
		Activity clone;
		if (a.isNewerThan(b)) {
			clone = b.clone();
			clone.merge(a);
		} else {
			clone = a.clone();
			clone.merge(b);
		}
		
		return clone;
	}
	
}
