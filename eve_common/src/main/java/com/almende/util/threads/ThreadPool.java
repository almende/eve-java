/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.util.threads;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * The Class ThreadPool.
 */
public class ThreadPool {
	private static int							nofCores		= 8;
	private static ThreadFactory				factory			= Executors
																		.defaultThreadFactory();
	private static ScheduledThreadPoolExecutor	scheduledPool	= null;
	private static RunQueue						queue			= null;
	
	static {
		initPools();
	}

	private static void initPools() {
		List<Runnable> openTasks  = new ArrayList<Runnable>();
		if (queue != null){
			 openTasks.addAll(queue.shutdownNow());
		}
		if (scheduledPool != null) {
			scheduledPool.purge();
			openTasks.addAll(scheduledPool.shutdownNow());
		}
		scheduledPool = new ScheduledThreadPoolExecutor(nofCores, factory,
				new ThreadPoolExecutor.CallerRunsPolicy());
		queue = new RunQueue();
		
		for (Runnable task : openTasks){
			if (task instanceof RunnableScheduledFuture){
				final RunnableScheduledFuture<?> futureTask = (RunnableScheduledFuture<?>) task;
				scheduledPool.schedule(futureTask, futureTask.getDelay(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS);
			} else {
				queue.execute(task);
			}
		}
	}

	/**
	 * Sets the nof CPU cores, for efficient resource usage.
	 * 
	 * @param nofCores
	 *            the new nof cores
	 */
	public static void setNofCores(int nofCores) {
		ThreadPool.nofCores = nofCores;
		initPools();
	}

	/**
	 * Gets the pool.
	 * 
	 * @return the pool
	 */
	public static ScheduledThreadPoolExecutor getScheduledPool() {
		return scheduledPool;
	}

	/**
	 * Gets the pool.
	 * 
	 * @return the pool
	 */
	public static Executor getPool() {
		return queue;
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
		initPools();
	}
}
