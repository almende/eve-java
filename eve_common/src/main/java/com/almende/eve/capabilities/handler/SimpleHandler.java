/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.capabilities.handler;

import java.util.logging.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * The Class SimpleHandler.
 * 
 * @param <T>
 *            the generic type
 */
public class SimpleHandler<T> implements Handler<T> {
	private static final Logger	LOG			= Logger.getLogger(SimpleHandler.class
													.getName());
	private T					referent	= null;

	/**
	 * Instantiates a new simple handler.
	 * 
	 * @param referent
	 *            the referent
	 */
	public SimpleHandler(final T referent) {
		this.referent = referent;
	}

	/*
	 * (non-Javadoc)
	 * @see com.almende.eve.capabilities.handler.Handler#get()
	 */
	@Override
	@JsonIgnore
	public T get() {
		return referent;
	}

	/*
	 * (non-Javadoc)
	 * @see com.almende.eve.capabilities.handler.Handler#getNoWait()
	 */
	@Override
	@JsonIgnore
	public T getNoWait() {
		return referent;
	}

	/*
	 * (non-Javadoc)
	 * @see com.almende.eve.capabilities.handler.Handler#update(com.almende.eve.
	 * capabilities.handler.Handler)
	 */
	@Override
	public void update(final Handler<T> newHandler) {
		if (!this.referent.equals(newHandler.get())) {
			LOG.warning("Updating a Simplehandler with another referent, which is unlikely to be correct, please check your config for cross-agent reuse of capabilities.");
		}
		this.referent = newHandler.get();
	}

	/*
	 * (non-Javadoc)
	 * @see com.almende.eve.capabilities.handler.Handler#getKey()
	 */
	@Override
	public String getKey() {
		return null;
	}

}
