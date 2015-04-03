/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.algorithms;

import java.net.URI;

import com.almende.eve.protocol.jsonrpc.formats.JSONRequest;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * The Class Event.
 */
public class Event {
	private long				expiryTime	= -1;
	private JSONRequest			message		= null;
	private URI					sender		= null;
	transient private boolean	triggered	= false;

	/**
	 * Instantiates a new event.
	 */
	public Event() {}

	/**
	 * Instantiates a new event.
	 *
	 * @param expiryTime
	 *            the expiry time
	 * @param message
	 *            the message
	 * @param sender
	 *            the sender
	 */
	public Event(final long expiryTime, final JSONRequest message,
			final URI sender) {
		this.expiryTime = expiryTime;
		this.message = message;
		this.sender = sender;
	}

	/**
	 * Gets the expiry time.
	 *
	 * @return the expiry time
	 */
	public long getExpiryTime() {
		return expiryTime;
	}

	/**
	 * Sets the expiry time.
	 *
	 * @param expiryTime
	 *            the new expiry time
	 */
	public void setExpiryTime(long expiryTime) {
		this.expiryTime = expiryTime;
	}

	/**
	 * Gets the message.
	 *
	 * @return the message
	 */
	public JSONRequest getMessage() {
		return message;
	}

	/**
	 * Sets the message.
	 *
	 * @param message
	 *            the new message
	 */
	public void setMessage(JSONRequest message) {
		this.message = message;
	}

	/**
	 * Gets the sender.
	 *
	 * @return the sender
	 */
	public URI getSender() {
		return sender;
	}

	/**
	 * Sets the sender.
	 *
	 * @param sender
	 *            the new sender
	 */
	public void setSender(URI sender) {
		this.sender = sender;
	}

	/**
	 * Checks if this Event has already been triggered locally.
	 *
	 * @return true, if is triggered
	 */
	@JsonIgnore
	public boolean isTriggered() {
		return triggered;
	}

	/**
	 * Sets the flag if this Event has already been triggered locally.
	 *
	 * @param triggered
	 *            the new triggered
	 */
	@JsonIgnore
	public void setTriggered(boolean triggered) {
		this.triggered = triggered;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof Event)) {
			return false;
		}
		Event other = (Event) o;
		if (other.expiryTime != this.expiryTime) {
			return false;
		} else if (other.sender == null && this.sender != null) {
			return false;
		} else if (other.message == null && this.message != null) {
			return false;
		} else if (other.message != null && !other.message.equals(this.message)) {
			return false;
		} else if (other.sender != null && !other.sender.equals(this.sender)) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		int hashcode = 1;
		hashcode = 31 * hashcode + ((int) this.expiryTime % Integer.MAX_VALUE);
		hashcode = 31 * hashcode
				+ (this.message == null ? 0 : this.message.hashCode());
		hashcode = 31 * hashcode
				+ (this.sender == null ? 0 : this.sender.hashCode());
		return hashcode;
	}
}
