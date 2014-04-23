/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.util.threads;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * The Class ThreadPool.
 */
public class ThreadPool {
	private static ThreadFactory	factory	= Executors.defaultThreadFactory();
	private static ExecutorService	pool	= Executors
													.newCachedThreadPool(factory);
	
	/**
	 * Gets the pool.
	 * 
	 * @return the pool
	 */
	public static ExecutorService getPool() {
		return pool;
	}
	
	/**
	 * Gets the factory.
	 * 
	 * @return the factory
	 */
	public static ThreadFactory getFactory() {
		return factory;
	}

	/**
	 * Sets the factory.
	 * 
	 * @param factory
	 *            the new factory
	 */
	public static void setFactory(final ThreadFactory factory) {
		ThreadPool.factory = factory;
		pool.shutdownNow();
		pool = Executors.newCachedThreadPool(factory);
	}
}
