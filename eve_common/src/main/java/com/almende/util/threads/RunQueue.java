/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.util.threads;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
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
	private static final Logger	LOG				= Logger.getLogger(RunQueue.class
														.getName());
	private static final double	RESERVEFACTOR	= 1.5;
	private int					nofCores;
	private Object				terminationLock	= new Object();
	private Queue<Runnable>		tasks			= new ConcurrentLinkedQueue<Runnable>();
	private HashSet<Worker>		running			= null;
	private HashSet<Worker>		reserves		= null;
	private HashSet<Worker>		waiting			= new HashSet<Worker>();
	private boolean				isShutdown		= false;
	private Thread				scanner			= new Thread(new Runnable() {
													@Override
													public void run() {
														for (;;) {
															if (isShutdown) {
																break;
															}
															scan();
															try {
																Thread.sleep(100);
															} catch (InterruptedException e) {}
														}
													}

												});

	class Worker extends Thread {
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

		public void run() {
			for (;;) {
				synchronized (lock) {
					while (task == null) {
						try {
							lock.wait();
						} catch (InterruptedException e) {
							if (isShutdown) {
								break;
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

		public boolean isShutdown() {
			return isShutdown;
		}

	}

	/**
	 * Instantiates a new run queue.
	 */
	public RunQueue() {
		nofCores = Runtime.getRuntime().availableProcessors();
		running = new HashSet<Worker>(nofCores);
		reserves = new HashSet<Worker>((int) (nofCores * RESERVEFACTOR));
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
				synchronized (reserves) {
					for (Worker worker : running) {
						worker.isShutdown = true;
						worker.interrupt();
					}
					for (Worker worker : waiting) {
						worker.isShutdown = true;
						worker.interrupt();
					}
					for (Worker worker : reserves) {
						worker.isShutdown = true;
						worker.interrupt();
					}
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
		Worker res = null;
		if (running.size() >= nofCores) {
			// early out
			return null;
		}
		// Try to obtain reserve thread
		synchronized (reserves) {
			if (reserves.size() > 0) {
				final Iterator<Worker> iter = reserves.iterator();
				if (iter.hasNext()) {
					res = iter.next();
					iter.remove();
				}
			}
		}
		synchronized (running) {
			// Double check if there is still room for more running threads
			if (running.size() < nofCores) {
				if (res == null) {
					// No reserve found, create new thread
					res = new Worker();
					res.start();
				}
				running.add(res);
			} else {
				// Too bad, revert thread back to reserves
				if (res != null) {
					synchronized (reserves) {
						reserves.add(res);
						res = null;
					}
				}
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
		threadReserve(thread);
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

	private void threadReserve(final Worker thread) {
		synchronized (reserves) {
			if (!thread.isShutdown
					&& reserves.size() < nofCores * RESERVEFACTOR) {
				reserves.add(thread);
			}
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
				threadReserve(thread);
				break;
			}
			thread = getFreeThread();
		}
	}
}
