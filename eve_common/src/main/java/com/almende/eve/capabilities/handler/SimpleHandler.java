/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.capabilities.handler;

/**
 * The Class SimpleHandler.
 * 
 * @param <T>
 *            the generic type
 */
public class SimpleHandler<T> implements Handler<T> {
	private T	referent	= null;
	
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
	 * 
	 * @see com.almende.eve.capabilities.handler.Handler#get()
	 */
	@Override
	public T get() {
		return referent;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.almende.eve.capabilities.handler.Handler#update(com.almende.eve.
	 * capabilities.handler.Handler)
	 */
	@Override
	public void update(final Handler<T> newHandler) {
		this.referent = newHandler.get();
	}
	
	/* (non-Javadoc)
	 * @see com.almende.eve.capabilities.handler.Handler#getKey()
	 */
	@Override
	public String getKey() {
		return null;
	}
	
}
