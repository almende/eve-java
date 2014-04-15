/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.capabilities.handler;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * The Class WakeHandler.
 * 
 * @param <T>
 *            the generic type
 */
public class WakeHandler<T> implements Handler<T> {
	private T				referent	= null;
	private final Object	wakeLock	= new Object();
	private String			wakeKey		= null;
	
	/**
	 * Instantiates a new wake handler.
	 */
	public WakeHandler() {
	}
	
	/**
	 * Instantiates a new wake handler.
	 * 
	 * @param referent
	 *            the referent
	 * @param wakeKey
	 *            the wake key
	 */
	public WakeHandler(final T referent, final String wakeKey) {
		this.referent = referent;
		this.setWakeKey(wakeKey);
	}
	
	@Override
	@JsonIgnore
	public synchronized T get() {
		while (referent == null) {
			// TODO: call Wake service with wakeKey. Currently it will
			// deadlock:)
			
			try {
				wakeLock.wait();
			} catch (final InterruptedException e) {
			}
		}
		return referent;
	}
	
	@Override
	public void update(final Handler<T> newHandler) {
		this.referent = newHandler.get();
		
		// Can this be done in a cleaner way?
		if (newHandler instanceof WakeHandler) {
			final WakeHandler<T> other = (WakeHandler<T>) newHandler;
			this.wakeKey = other.getWakeKey();
			wakeLock.notifyAll();
		}
	}
	
	/**
	 * Gets the wake key.
	 * 
	 * @return the wake key
	 */
	public String getWakeKey() {
		return wakeKey;
	}
	
	/**
	 * Sets the wake key.
	 * 
	 * @param wakeKey
	 *            the new wake key
	 */
	public void setWakeKey(final String wakeKey) {
		this.wakeKey = wakeKey;
	}
	
}
