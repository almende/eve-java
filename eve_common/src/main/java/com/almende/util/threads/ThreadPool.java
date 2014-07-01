/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.util.threads;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * The Class ThreadPool.
 */
public class ThreadPool {
	private static int					nofCores = 8;
	private static ThreadFactory		factory	= Executors
														.defaultThreadFactory();
	private static ThreadPoolExecutor	pool	= new ThreadPoolExecutor(
														nofCores,
														nofCores,
														60,
														TimeUnit.SECONDS,
														new LinkedBlockingQueue<Runnable>(),
														factory,
														new ThreadPoolExecutor.CallerRunsPolicy());
	
	static {
	//	pool.allowCoreThreadTimeOut(true);
	}
	
	/**
	 * Sets the nof CPU cores, for efficient resource usage.
	 * 
	 * @param nofCores
	 *            the new nof cores
	 */
	public static void setNofCores(int nofCores){
		ThreadPool.nofCores=nofCores;
		pool.purge();
		pool.shutdownNow();
		pool = new ThreadPoolExecutor(nofCores, nofCores, 60, TimeUnit.SECONDS,
				new LinkedBlockingQueue<Runnable>(), factory,
				new ThreadPoolExecutor.CallerRunsPolicy());
		pool.allowCoreThreadTimeOut(true);
	}
	
	/**
	 * Gets the pool.
	 * 
	 * @return the pool
	 */
	public static ThreadPoolExecutor getPool() {
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
		pool.purge();
		pool.shutdownNow();
		pool = new ThreadPoolExecutor(nofCores, nofCores, 60, TimeUnit.SECONDS,
				new LinkedBlockingQueue<Runnable>(), factory,
				new ThreadPoolExecutor.CallerRunsPolicy());
		pool.allowCoreThreadTimeOut(true);
	}
}
