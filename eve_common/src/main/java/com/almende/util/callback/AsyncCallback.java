/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.util.callback;

import com.almende.util.TypeUtil;

/**
 * The Class AsyncCallback.
 *
 * @param <T>
 *            the generic type
 */
public abstract class AsyncCallback<T> {
	protected TypeUtil<T>	type	= null;

	/**
	 * Instantiates a new async callback.
	 *
	 * @param type
	 *            the type
	 */
	public AsyncCallback(TypeUtil<T> type) {
		this.type = type;
	}

	/**
	 * Instantiates a new async callback.
	 */
	public AsyncCallback() {
		this.type = TypeUtil.resolve(this);
	}

	/**
	 * Gets the type.
	 *
	 * @return the type
	 */
	public TypeUtil<T> getType() {
		return type;
	}

	/**
	 * On success.
	 * 
	 * @param result
	 *            the result
	 */
	public abstract void onSuccess(T result);

	/**
	 * On failure.
	 * 
	 * @param exception
	 *            the exception
	 */
	public abstract void onFailure(Exception exception);
}
