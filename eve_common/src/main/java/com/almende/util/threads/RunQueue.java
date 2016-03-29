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
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

/**
 * The Class RunQueue. This is our own ThreadPool, with the following behavior:
 * -Unlimited queue
 * -Threadcount based on number of "Running" Threads, excluding "Blocked",
 * "Timed_waiting" and "Waiting" threads from the threadcount.
 * -Approximately nofCPU threads in Running state.
 */
public class RunQueue extends AbstractExecutorService {
	private static final Logger		LOG					= Logger.getLogger(RunQueue.class
																.getName());

	private final Object			terminationLock		= new Object();
	private final Queue<Worker>		reserve				= new ConcurrentLinkedQueue<Worker>();
	private final Queue<Runnable>	tasks				= new ConcurrentLinkedQueue<Runnable>();
	private final HashSet<Worker>	waiting				= new HashSet<Worker>(8);
	private final Scanner			scanner				= new Scanner(
																"RunQueue_Scanner");
	private static int				maxtasks			= -1;
	private static final int		MAXTASKSPERWORKER	= 1000;
	private int						nofCores;
	private HashSet<Worker>			running				= null;
	private boolean					isShutdown			= false;
	private int						interval			= 100;
	private final int				maxinterval			= 500;
	private int[]					taskCnt				= new int[1];

	private class Scanner extends Thread {

		public Scanner(final String name) {
			super(name);
		}

		@Override
		public void run() {
			for (;;) {
				try {
					Thread.sleep(interval);
				} catch (InterruptedException e) {}
				if (isShutdown) {
					return;
				}
				scan();
			}
		}
	}

	private class Worker extends Thread {
		final private ReentrantLock	lock		= new ReentrantLock();
		final private Condition		condition	= lock.newCondition();
		private Runnable			task		= null;
		private boolean				isShutdown	= false;
		private int					taskCnt		= 0;

		public boolean runTask(final Runnable task) {
			if (this.task != null) {
				// Early out
				return false;
			}
			if (isShutdown) {
				return false;
			}
			if (task == null) {
				return true;
			}
			if (lock.tryLock()) {
				if (this.task != null) {
					lock.unlock();
					return false;
				}
				this.task = task;
				taskCnt++;
				condition.signal();
				lock.unlock();
				return true;
			} else {
				return false;
			}
		}

		@Override
		public void run() {
			while (!isShutdown) {
				try {
					lock.lockInterruptibly();
				} catch (InterruptedException e1) {
					continue;
				}
				while (task == null) {
					if (isShutdown) {
						return;
					}
					try {
						if (running.contains(this)) {
							threadDone(this);
							continue;
						}
						condition.await();
					} catch (InterruptedException e) {}
				}
				if (!running.contains(this)) {
					threadContinue(this);
				}
				task.run();
				task = null;
				taskCnt--;
				if (running.size() <= nofCores) {
					// Shortcut: Check if there are more tasks available.
					task = getTask();
				}
				if (task == null) {
					threadDone(this);
				} else {
					taskCnt++;
				}
				lock.unlock();
			}
			if (task != null) {
				putTask(task);
			}
		}
	}

	/**
	 * Instantiates a new run queue.
	 */
	public RunQueue() {
		nofCores = Runtime.getRuntime().availableProcessors();
		if (nofCores < 4) {
			// Keep a minimum number of assumed cores, to prevent thread
			// starvation.
			nofCores = 4;
		}
		running = new HashSet<Worker>(nofCores);
		taskCnt[0] = 0;
		scanner.start();
	}

	/**
	 * Sets the max tasks.
	 *
	 * @param max
	 *            the new max tasks
	 */
	public void setMaxTasks(final int max) {
		maxtasks = max;
		if (maxtasks > 0) {
			synchronized (taskCnt) {
				taskCnt[0] = tasks.size();
			}
		} else {
			synchronized (taskCnt) {
				taskCnt[0] = 0;
			}
		}
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
				for (Worker worker : reserve) {
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
		return isShutdown
				&& (running.isEmpty() && waiting.isEmpty() && reserve.isEmpty());
	}

	@Override
	public boolean awaitTermination(final long timeout, final TimeUnit unit)
			throws InterruptedException {
		final long sleepTime = TimeUnit.MILLISECONDS.convert(timeout, unit);
		synchronized (terminationLock) {
			while (!(running.isEmpty() && waiting.isEmpty())) {
				terminationLock.wait(sleepTime);
			}
		}
		return (running.isEmpty() && waiting.isEmpty() && reserve.isEmpty());
	}

	private boolean addTask(final Runnable command) {
		if (maxtasks > 0 && taskCnt[0] > maxtasks) {
			// TODO: find a way to limit this without a shared counter! For
			// example: runtime to add task > 1a2 ms?
			Thread thread = Thread.currentThread();
			if (thread instanceof Worker) {
				if (((Worker) thread).taskCnt <= MAXTASKSPERWORKER) {
					// Do this task yourself!
					return false;
				}
			}
		}
		putTask(command);
		return true;
	}

	private void putTask(final Runnable command) {
		if (maxtasks > 0) {
			synchronized (taskCnt) {
				taskCnt[0]++;
			}
		}
		tasks.add(command);
	}

	private Runnable getTask() {
		final Runnable task = tasks.poll();
		if (maxtasks > 0 && task != null) {
			synchronized (taskCnt) {
				taskCnt[0]--;
			}
		}
		return task;
	}

	@Override
	public void execute(final Runnable command) {
		if (command == null) {
			throw new NullPointerException(
					"Command to execute may never be null.");
		}
		if (isShutdown()) {
			LOG.warning("Execute called after shutdown, dropping command");
			return;
		}
		Worker thread = getFreeThread();
		while (thread != null) {
			if (thread.runTask(command)) {
				break;
			}
			thread = getFreeThread();
		}
		if (thread == null) {
			if (!addTask(command)) {
				thread = (Worker) Thread.currentThread();
				thread.taskCnt++;
				command.run();
				thread.taskCnt--;
			}
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
				synchronized (reserve) {
					res = reserve.poll();
					if (res != null) {
						running.add(res);
						return res;
					}
				}
				res = new Worker();
				res.start();
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
				final Runnable task = getTask();
				if (task != null) {
					if (!thread.runTask(task)) {
						putTask(task);
					} else {
						return;
					}
				}
			}
			synchronized (running) {
				running.remove(thread);
			}
			synchronized (waiting) {
				waiting.remove(thread);
			}
			synchronized (reserve) {
				if (reserve.size() < nofCores * 5) {
					reserve.add(thread);
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
		if (!thread.isShutdown) {
			thread.isShutdown = true;
			thread.interrupt();
		}
		synchronized (reserve) {
			reserve.remove(thread);
		}
		synchronized (running) {
			running.remove(thread);
		}
		synchronized (waiting) {
			waiting.remove(thread);
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
		int count = 0;
		for (final Worker thread : runn_arr) {
			switch (thread.getState()) {
				case TIMED_WAITING:
					// explicit no break
				case WAITING:
					// explicit no break
				case BLOCKED:
					if (thread.task != null) {
						threadWaiting(thread);
						count++;
					}
					break;
				case TERMINATED:
					threadDone(thread);
					break;
				default:
					break;
			}
		}
		if (count == 0) {
			interval = Math.min(interval * 2, maxinterval);
		} else {
			while (count-- > 0) {
				interval = interval > 1 ? interval / 2 : 1;
			}
		}
		while (!tasks.isEmpty()) {
			Worker thread = getFreeThread();
			if (thread != null) {
				threadDone(thread);
			} else {
				break;
			}
		}
	}

	public String toString() {
		return this.getClass().getName() + ": ru:" + running.size() + " wa:"
				+ waiting.size() + " rs:" + reserve.size() + " t:"
				+ tasks.size() + " nofCores:" + nofCores + " int:" + interval
				+ " ms.";
	}
}
