/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.entity.activity;

import java.io.Serializable;

/**
 * The Class Attendee.
 */
@SuppressWarnings("serial")
public class Attendee implements Serializable, Cloneable {
	private String			displayName		= null;
	private String			email			= null;
	private String			agent			= null; // eve agent url
	private Boolean			optional		= null; // if false, attendee must
													// attend
	private RESPONSE_STATUS	responseStatus	= null;
	
	/**
	 * Instantiates a new attendee.
	 */
	public Attendee() {
	}
	
	/**
	 * Gets the display name.
	 * 
	 * @return the display name
	 */
	public String getDisplayName() {
		return displayName;
	}
	
	/**
	 * Sets the display name.
	 * 
	 * @param displayName
	 *            the new display name
	 */
	public void setDisplayName(final String displayName) {
		this.displayName = displayName;
	}
	
	/**
	 * Gets the email.
	 * 
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}
	
	/**
	 * Sets the email.
	 * 
	 * @param email
	 *            the new email
	 */
	public void setEmail(final String email) {
		this.email = email;
	}
	
	/**
	 * Gets the agent.
	 * 
	 * @return the agent
	 */
	public String getAgent() {
		return agent;
	}
	
	/**
	 * Sets the agent.
	 * 
	 * @param agent
	 *            the new agent
	 */
	public void setAgent(final String agent) {
		this.agent = agent;
	}
	
	/**
	 * Gets the optional.
	 * 
	 * @return the optional
	 */
	public Boolean getOptional() {
		return optional;
	}
	
	/**
	 * Sets the optional.
	 * 
	 * @param optional
	 *            the new optional
	 */
	public void setOptional(final Boolean optional) {
		this.optional = optional;
	}
	
	/**
	 * Sets the response status.
	 * 
	 * @param responseStatus
	 *            the new response status
	 */
	public void setResponseStatus(final RESPONSE_STATUS responseStatus) {
		this.responseStatus = responseStatus;
	}
	
	/**
	 * Gets the response status.
	 * 
	 * @return the response status
	 */
	public RESPONSE_STATUS getResponseStatus() {
		return responseStatus;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Attendee clone() {
		final Attendee clone = new Attendee();
		
		clone.displayName = displayName;
		clone.email = email;
		clone.agent = agent;
		clone.optional = optional;
		clone.responseStatus = responseStatus;
		
		return clone;
	}
	
	/**
	 * The Enum RESPONSE_STATUS.
	 */
	public enum RESPONSE_STATUS {
		
		/**
		 * The needs action.
		 */
		needsAction, 
 /**
	 * The declined.
	 */
 declined, 
 /**
	 * The tentative.
	 */
 tentative, 
 /**
	 * The accepted.
	 */
 accepted
	};
	
}
