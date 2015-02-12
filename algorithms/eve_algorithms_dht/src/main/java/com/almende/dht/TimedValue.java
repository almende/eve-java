/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.dht;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class TimedValue.
 */
public class TimedValue implements Comparable<TimedValue> {

	private ObjectNode	value;
	private long		storedTime;
	private long		expirationDuration;

	/**
	 * Instantiates a new timed value.
	 */
	public TimedValue() {}

	/**
	 * Instantiates a new timed value.
	 *
	 * @param value
	 *            the value
	 */
	public TimedValue(ObjectNode value){
		this.value=value;
		this.storedTime = System.currentTimeMillis();
		this.expirationDuration = Constants.EXPIRE;
	}
	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	public ObjectNode getValue() {
		return value;
	}

	/**
	 * Sets the value. (Only for de-serialization, use store() instead!)
	 *
	 * @param value
	 *            the new value
	 */
	public void setValue(ObjectNode value) {
		this.value = value;
	}

	/**
	 * Gets the stored time.
	 *
	 * @return the stored time
	 */
	public long getStoredTime() {
		return storedTime;
	}

	/**
	 * Sets the stored time.
	 *
	 * @param storedTime
	 *            the new stored time
	 */
	public void setStoredTime(long storedTime) {
		this.storedTime = storedTime;
	}

	/**
	 * Gets the expiration duration.
	 *
	 * @return the expiration duration
	 */
	public long getExpirationDuration() {
		return expirationDuration;
	}

	/**
	 * Sets the expiration duration.
	 *
	 * @param expirationDuration
	 *            the new expiration duration
	 */
	public void setExpirationDuration(final long expirationDuration) {
		this.expirationDuration = expirationDuration;
	}

	/**
	 * Sets the value and updates the storedTime.
	 *
	 * @param value
	 *            the new value
	 * @param storedTime
	 *            the stored time (needed to be able to use Hypertime)
	 */
	public void store(final ObjectNode value, final long storedTime) {
		this.storedTime = storedTime;
		this.value = value;
	}
	
	/**
	 * Store.
	 *
	 * @param value
	 *            the value
	 */
	public void store(final ObjectNode value){
		store(value,System.currentTimeMillis());
	}
	
	/**
	 * Get remaining expire duration.
	 *
	 * @return the amount of ms until this value is expired.
	 */
	public long getTtl(){
		return this.storedTime + this.expirationDuration - System.currentTimeMillis();
	}

	@Override
	public boolean equals(final Object o){
		if (o == null) return false;
		if (!(o instanceof TimedValue)){
			return false;
		}
		TimedValue other = (TimedValue)o;
		if (this.value.equals(other.value)){
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public int hashCode(){
		return this.value.hashCode();
	}
	
	@Override
	//Newest first 
	public int compareTo(TimedValue o) {
		return ((storedTime == o.storedTime) ? 0
				: (storedTime < o.storedTime ? 1 : -1));
	}

}
