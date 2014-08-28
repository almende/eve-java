/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.util.threads;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * The Class RunQueue. This is our own ThreadPool, with the following behavior:
 * -Unlimited queue
 * -Threadcount based on number of "Running" Threads, excluding "Blocked",
 * "Timed_waiting" and "Waiting" threads from the threadcount.
 * -Approximately nofCPU threads in Running state.
 */
public class RunQueue extends AbstractExecutorService {
	private static final Logger		LOG				= Logger.getLogger(RunQueue.class
															.getName());
	private final Object			terminationLock	= new Object();
	private final Queue<Runnable>	tasks			= new ConcurrentLinkedQueue<Runnable>();
	private final HashSet<Worker>	waiting			= new HashSet<Worker>();
	private final Scanner			scanner			= new Scanner();
	private int						nofCores;
	private HashSet<Worker>			running			= null;
	private boolean					isShutdown		= false;

	private class Scanner extends Thread {
		@Override
		public void run() {
			for (;;) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {}
				if (isShutdown) {
					return;
				}
				scan();
			}
		}
	}
	
	private class Worker extends Thread {
		final private Object	lock		= new Object();
		private Runnable		task		= null;
		private boolean			isShutdown	= false;

		public void runTask(final Runnable task) {
			if (isShutdown) {
				return;
			}
			this.task = task;
			synchronized (lock) {
				lock.notify();
			}
		}

		@Override
		public void run() {
			for (;;) {
				synchronized (lock) {
					while (task == null) {
						try {
							lock.wait();
						} catch (InterruptedException e) {
							if (isShutdown) {
								return;
							}
						}
					}
					threadContinue(this);
					task.run();
					task = null;
					threadDone(this);
				}
			}
		}
	}

	/**
	 * Instantiates a new run queue.
	 */
	public RunQueue() {
		nofCores = Runtime.getRuntime().availableProcessors();
		running = new HashSet<Worker>(nofCores);
		scanner.start();
	}

	@Override
	public void shutdown() {
		isShutdown = true;
		scanner.interrupt();
	}

	@Override
	public List<Runnable> shutdownNow() {
		isShutdown = true;
		scanner.interrupt();
		synchronized (running) {
			synchronized (waiting) {
				for (Worker worker : running) {
					worker.isShutdown = true;
					worker.interrupt();
				}
				for (Worker worker : waiting) {
					worker.isShutdown = true;
					worker.interrupt();
				}
			}
		}
		return new ArrayList<Runnable>(tasks);
	}

	@Override
	public boolean isShutdown() {
		return isShutdown;
	}

	@Override
	public boolean isTerminated() {
		return isShutdown && (running.size() == 0 && waiting.size() == 0);
	}

	@Override
	public boolean awaitTermination(final long timeout, final TimeUnit unit)
			throws InterruptedException {
		final long sleepTime = TimeUnit.MILLISECONDS.convert(timeout, unit);
		synchronized (terminationLock) {
			while (!(running.size() == 0 && waiting.size() == 0)) {
				terminationLock.wait(sleepTime);
			}
		}
		return (running.size() == 0 && waiting.size() == 0);
	}

	@Override
	public void execute(final Runnable command) {
		if (isShutdown()) {
			LOG.warning("Execute called after shutdown, dropping command");
			return;
		}
		final Worker thread = getFreeThread();
		if (thread != null) {
			thread.runTask(command);
		} else {
			tasks.add(command);
		}
	}

	private Worker getFreeThread() {
		if (running.size() >= nofCores) {
			// early out
			return null;
		}
		Worker res = null;
		synchronized (running) {
			if (running.size() < nofCores) {
				if (res == null) {
					res = new Worker();
					res.start();
				}
				running.add(res);
			}
		}
		return res;
	}

	private void threadDone(final Worker thread) {
		if (isShutdown()) {
			thread.isShutdown = true;
		}
		if (!thread.isShutdown) {
			if (running.contains(thread) && running.size() <= nofCores) {
				final Runnable task = tasks.poll();
				if (task != null) {
					thread.runTask(task);
					return;
				}
			}
		}
		threadTearDown(thread);
		synchronized (terminationLock) {
			terminationLock.notify();
		}
	}

	private void threadWaiting(final Worker thread) {
		synchronized (waiting) {
			waiting.add(thread);
		}
		synchronized (running) {
			running.remove(thread);
		}
	}

	private void threadTearDown(final Worker thread) {
		synchronized (running) {
			running.remove(thread);
		}
		synchronized (waiting) {
			waiting.remove(thread);
		}
		if (!thread.isShutdown) {
			thread.isShutdown = true;
			thread.interrupt();
		}
	}

	private void threadContinue(final Worker thread) {
		if (!running.contains(thread)) {
			synchronized (running) {
				running.add(thread);
			}
		}
		synchronized (waiting) {
			waiting.remove(thread);
		}
	}

	private void scan() {
		final Worker[] runn_arr;
		synchronized (running) {
			runn_arr = running.toArray(new Worker[0]);
		}
		for (final Worker thread : runn_arr) {
			switch (thread.getState()) {
				case TIMED_WAITING:
					// explicit no break
				case WAITING:
					// explicit no break
				case BLOCKED:
					if (thread.task != null) {
						threadWaiting(thread);
					}
					break;
				case TERMINATED:
					threadDone(thread);
					break;
				default:
					break;
			}
		}
		Worker thread = getFreeThread();
		while (thread != null) {
			final Runnable task = tasks.poll();
			if (task != null) {
				thread.runTask(task);
			} else {
				threadTearDown(thread);
				break;
			}
			thread = getFreeThread();
		}
	}
}
