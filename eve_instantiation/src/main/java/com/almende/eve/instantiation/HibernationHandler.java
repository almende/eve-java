/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.instantiation;

import java.lang.ref.WeakReference;

import com.almende.eve.capabilities.handler.Handler;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * The Class WakeHandler.
 * 
 * @param <T>
 *            the generic type
 */
public class HibernationHandler<T> implements Handler<T> {
	private WeakReference<T>		referent	= null;
	private final Object			wakeLock	= new Object();
	private String					wakeKey		= null;
	private InstantiationService	service		= null;

	/**
	 * Instantiates a new wake handler.
	 */
	public HibernationHandler() {}

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
	public HibernationHandler(final T referent, final String wakeKey,
			final InstantiationService service) {
		this.referent = new WeakReference<T>(referent);
		this.setWakeKey(wakeKey);
		this.service = service;
	}

	/*
	 * (non-Javadoc)
	 * @see com.almende.eve.capabilities.handler.Handler#get()
	 */
	@Override
	@JsonIgnore
	public T get() {
		if (referent.get() == null) {
			service.init(getWakeKey());
		}
		while (referent.get() == null) {
			synchronized (wakeLock) {
				try {
					wakeLock.wait();
				} catch (final InterruptedException e) {}
			}
		}
		return referent.get();
	}

	/**
	 * Gets the no wait.
	 *
	 * @return the no wait
	 */
	@JsonIgnore
	public T getNoWait() {
		return this.referent.get();
	}

	/*
	 * (non-Javadoc)
	 * @see com.almende.eve.capabilities.handler.Handler#update(com.almende.eve.
	 * capabilities.handler.Handler)
	 */
	@Override
	public void update(final Handler<T> newHandler) {
		this.referent = new WeakReference<T>(newHandler.get());

		// Can this be done in a cleaner way?
		if (newHandler instanceof HibernationHandler) {
			final HibernationHandler<T> other = (HibernationHandler<T>) newHandler;
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

	/*
	 * (non-Javadoc)
	 * @see com.almende.eve.capabilities.handler.Handler#getKey()
	 */
	@Override
	public String getKey() {
		return wakeKey;
	}

}
