/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.util.callback;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Store to hold a map with callbacks in progress.
 * The Store handles timeouts on the callbacks.
 * 
 * @param <T>
 *            the generic type
 */
public class AsyncCallbackStore<T> {
	private final Map<Object, CallbackHandler>	store		= new ConcurrentHashMap<Object, CallbackHandler>(
																	5);
	private final TimeoutHandler				head		= new TimeoutHandler(
																	null);
	private final Thread						scanner;
	private boolean								started		= false;
	private TimeoutHandler						tail		= head;
	private int									growsize	= 10;
	private final static int					MAXGROWSIZE	= 20000;

	/** timeout in milliseconds */
	private long								timeout		= 30000;

	/**
	 * Instantiates a new async callback store.
	 *
	 * @param id
	 *            the id
	 */
	public AsyncCallbackStore(String id) {
		scanner = new Thread(new Runnable() {
			public void run() {
				while (true) {
					TimeoutHandler handler = tail;
					while (handler != null) {
						handler.checkTimeout();
						handler = handler.prev;
					}
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {}
				}

			}
		}, "AsyncCBScanner:" + id);

	}

	private void startScanner() {
		if (!started) {
			synchronized (scanner) {
				if (!started) {
					scanner.start();
					started = true;
				}
			}
		}
	}

	private void grow() {
		synchronized (head) {
			if (growsize <= MAXGROWSIZE / 2) {
				growsize *= 2;
			}
			for (int i = 0; i < growsize; i++) {
				tail = new TimeoutHandler(tail);
			}
		}
	}

	private void put(final CallbackHandler handler) {
		TimeoutHandler spot = head;
		while (!spot.put(handler)) {
			if (spot == tail) {
				grow();
			}
			spot = spot.next;
		}
	}

	class TimeoutHandler {
		private TimeoutHandler	next	= null;
		private TimeoutHandler	prev	= null;
		private CallbackHandler	handler;
		private ReentrantLock	lock	= new ReentrantLock();

		public TimeoutHandler(final TimeoutHandler prev) {
			if (prev != null) {
				this.prev = prev;
				prev.next = this;
			}
		}

		public boolean put(final CallbackHandler handler) {
			if (this.handler == null) {
				if (lock.tryLock()) {
					if (this.handler == null) {
						this.handler = handler;
						handler.parent = this;
						lock.unlock();
						return true;
					}
					lock.unlock();
				}
			}
			return false;
		}

		public void checkTimeout() {
			if (this.handler != null) {
				lock.lock();
				if (this.handler != null && this.handler.callback != null
						&& this.handler.timeout <= System.currentTimeMillis()) {
					this.handler.callback.onFailure(new TimeoutException(
							"Timeout occurred for callback with id '"
									+ this.handler.id + "': "
									+ this.handler.description));
					this.handler = null;
				}
				lock.unlock();
			}
		}

		public void forget() {
			lock.lock();
			this.handler = null;
			lock.unlock();
		}

		public CallbackHandler get() {
			return this.handler;
		}
	}

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
		startScanner();
		if (store.containsKey(id)) {
			throw new IllegalStateException("Callback with id '" + id
					+ "' already in queue");
		}
		final CallbackHandler handler = new CallbackHandler();
		handler.callback = callback;
		handler.id = id;
		handler.description = description;
		handler.timeout = System.currentTimeMillis() + timeout;
		put(handler);
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
			handler.parent.forget();
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
		private Object				id;
		private String				description;
		private AsyncCallback<T>	callback;
		private long				timeout;
		private TimeoutHandler		parent;
	}

	/**
	 * Gets the default callback timeout.
	 * 
	 * @return the default timeout
	 */
	public int getTimeout() {
		return (int) (timeout / 1000);
	}

	/**
	 * Sets the default callback timeout.
	 *
	 * @param timeout
	 *            the new timeout
	 */
	public void setTimeout(int timeout) {
		this.timeout = timeout * 1000;
	}

}
