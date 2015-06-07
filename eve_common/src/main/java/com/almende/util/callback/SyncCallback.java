/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.util.callback;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import com.almende.util.TypeUtil;

/**
 * The Class SyncCallback.
 * 
 * @param <T>
 *            the generic type
 */
public class SyncCallback<T> extends AsyncCallback<T> {

	private ReentrantLock	lock		= new ReentrantLock();
	private Condition		condition	= lock.newCondition();

	private T				response	= null;
	private Exception		exception	= null;
	private boolean			done		= false;
	private boolean			waiting		= false;

	/**
	 * Instantiates a new sync callback.
	 *
	 * @param type
	 *            the type
	 */
	public SyncCallback(TypeUtil<T> type) {
		super(type);
	}

	/**
	 * Instantiates a new sync callback.
	 */
	public SyncCallback() {
		super();
	}

	/**
	 * Checks if is waiting.
	 *
	 * @return true, if is someone is waiting for this callback.
	 */
	public boolean isWaiting() {
		return waiting;
	}

	/**
	 * Checks if is done.
	 *
	 * @return true, if is done
	 */
	public boolean isDone() {
		return done;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.almende.eve.agent.callback.AsyncCallback#onSuccess(java.lang.Object)
	 */
	@Override
	public void onSuccess(final T response) {
		this.response = response;
		lock.lock();
		done = true;
		condition.signalAll();
		lock.unlock();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.almende.eve.agent.callback.AsyncCallback#onFailure(java.lang.Exception
	 * )
	 */
	@Override
	public void onFailure(final Exception exception) {
		this.exception = exception;
		lock.lock();
		done = true;
		condition.signalAll();
		lock.unlock();
	}

	/**
	 * Get will wait for the request to finish and then return the
	 * response. If an exception is returned, the exception will be
	 * thrown.
	 * 
	 * @return response
	 * @throws Exception
	 *             the exception
	 */
	public T get() throws Exception {
		lock.lock();
		waiting = true;
		while (!done) {
			condition.await();
		}
		lock.unlock();
		waiting = false;
		if (exception != null) {
			throw exception;
		}
		return type.inject(response);
	}

};
