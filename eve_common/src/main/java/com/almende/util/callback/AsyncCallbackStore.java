/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.util.callback;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.almende.util.threads.ThreadPool;

/**
 * Store to hold a map with callbacks in progress.
 * The Store handles timeouts on the callbacks.
 * 
 * @param <T>
 *            the generic type
 */
public class AsyncCallbackStore<T> {
	private final Map<Object, CallbackHandler>	store			= new ConcurrentHashMap<Object, CallbackHandler>(
																		5);

	/** timeout in seconds */
	private int									defTimeout		= 30;

	/**
	 * Place a callback in the store..
	 * The callback must be pulled from the store again within the
	 * timeout. If not, the callback.onFailure will be called with a
	 * TimeoutException as argument, and the callback will be deleted from the
	 * store.
	 * The method will throw an exception when a callback with the same id
	 * is already in the store.
	 * 
	 * @param id
	 *            the id
	 * @param description
	 *            the description
	 * @param callback
	 *            the callback
	 */
	public void put(final Object id, final String description,
			final AsyncCallback<T> callback) {
		if (store.containsKey(id)) {
			throw new IllegalStateException("Callback with id '" + id
					+ "' already in queue");
		}
		final CallbackHandler handler = new CallbackHandler();
		handler.callback = callback;

		ThreadPool.getPool().execute(new Runnable() {
			public void run() {
				if (handler.done) {
					return;
				} else {
					ScheduledFuture<?> timeout = ThreadPool.getScheduledPool()
							.schedule(new Runnable() {
								@Override
								public void run() {
									final AsyncCallback<T> callback = handler.callback;
									if (callback != null && !handler.done) {
										callback.onFailure(new TimeoutException(
												"Timeout occurred for callback with id '"
														+ id + "': "
														+ description));

									}
								}
							}, defTimeout, TimeUnit.SECONDS);
					if (!handler.done) {
						handler.timeout = timeout;
					} else {
						timeout.cancel(true);
					}
				}

			};
		});
		store.put(id, handler);
	}

	/**
	 * Get a callback from the Store. The callback can be pulled from the
	 * store only once. If no callback is found with given id, null will
	 * be returned.
	 * 
	 * @param id
	 *            the id
	 * @return the async callback
	 */
	public AsyncCallback<T> get(final Object id) {
		final CallbackHandler handler = store.remove(id);
		if (handler != null) {
			handler.done = true;
			// stop the timeout
			if (handler.timeout != null) {
				handler.timeout.cancel(true);
				handler.timeout = null;
			}
			return handler.callback;
		}
		return null;
	}

	/**
	 * Remove all callbacks from the queue.
	 */
	public synchronized void clear() {
		store.clear();
	}

	/**
	 * Helper class to store a callback and its timeout task.
	 */
	private class CallbackHandler {
		private boolean				done	= false;
		private AsyncCallback<T>	callback;
		private ScheduledFuture<?>	timeout	= null;
	}

	/**
	 * Gets the default callback timeout.
	 * 
	 * @return the default timeout
	 */
	public int getDefTimeout() {
		return defTimeout;
	}

	/**
	 * Sets the default callback timeout.
	 * 
	 * @param defTimeout
	 *            the new default timeout
	 */
	public void setDefTimeout(int defTimeout) {
		this.defTimeout = defTimeout;
	}

}
