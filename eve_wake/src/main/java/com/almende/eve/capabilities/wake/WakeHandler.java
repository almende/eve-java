/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.capabilities.wake;

import java.lang.ref.WeakReference;

import com.almende.eve.capabilities.handler.Handler;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * The Class WakeHandler.
 * 
 * @param <T>
 *            the generic type
 */
public class WakeHandler<T> implements Handler<T> {
	private WeakReference<T>	referent	= null;
	private final Object		wakeLock	= new Object();
	private String				wakeKey		= null;
	private WakeService			service		= null;
	
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
	 * @param service
	 *            the wake service where this referent is registered.
	 */
	public WakeHandler(final T referent, final String wakeKey,
			final WakeService service) {
		this.referent = new WeakReference<T>(referent);
		this.setWakeKey(wakeKey);
		this.service = service;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.almende.eve.capabilities.handler.Handler#get()
	 */
	@Override
	@JsonIgnore
	public T get() {
		if (referent.get() == null) {
			service.wake(getWakeKey());
		}
		while (referent.get() == null) {
			synchronized (wakeLock) {
				try {
					wakeLock.wait();
				} catch (final InterruptedException e) {
				}
			}
		}
		return referent.get();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.almende.eve.capabilities.handler.Handler#update(com.almende.eve.
	 * capabilities.handler.Handler)
	 */
	@Override
	public void update(final Handler<T> newHandler) {
		this.referent = new WeakReference<T>(newHandler.get());
		
		// Can this be done in a cleaner way?
		if (newHandler instanceof WakeHandler) {
			final WakeHandler<T> other = (WakeHandler<T>) newHandler;
			this.wakeKey = other.getWakeKey();
			synchronized (wakeLock) {
				wakeLock.notifyAll();
			}
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
	
	/* (non-Javadoc)
	 * @see com.almende.eve.capabilities.handler.Handler#getKey()
	 */
	@Override
	public String getKey() {
		return wakeKey;
	}
	
}
