/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.algorithms.simulation;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import com.almende.eve.protocol.Meta;
import com.almende.util.threads.ThreadPool;
import com.almende.util.uuid.UUID;

/**
 * The Class SimulationInbox.
 */
public class SimulationInbox {
	private static final Logger							LOG				= Logger.getLogger(SimulationInbox.class
																				.getName());
	private static final Map<String, Queue<QueueEntry>>	inbox			= new HashMap<String, Queue<QueueEntry>>();
	private static final Integer[]						inboxSize		= new Integer[1];

	private static Map<String, Integer>					seenHeartBeats	= new HashMap<String, Integer>();
	private String										id				= null;
	private static final ReentrantLock					duringSendRound	= new ReentrantLock();

	static {
		inboxSize[0] = 0;
	}

	// Multiple threads may wait
	// for count threads, wait for "ok"
	// when all ok, kick waiting threads

	/**
	 * Instantiates a new simulation inbox.
	 *
	 * @param id
	 *            the id
	 */
	public SimulationInbox(final String id) {
		this.id = id;
		if (id.startsWith("scheduler_")) {
			seenHeartBeats.put(id, -1);
		}
	}

	/**
	 * Adds in inbound message.
	 *
	 * @param meta
	 *            the meta
	 * @param msgId
	 *            the msg id
	 */
	public void add(final Meta meta, final UUID msgId) {
		LOG.warning(id + ": adding inbound message:" + meta.getMsg());
		Queue<QueueEntry> queue = null;
		synchronized (inbox) {
			if (inbox.containsKey(id)) {
				queue = inbox.get(id);
			}
			if (queue == null) {
				queue = new PriorityBlockingQueue<QueueEntry>();
				inbox.put(id, queue);
			}
			queue.add(new QueueEntry(meta, msgId));
			synchronized (inboxSize) {
				inboxSize[0]++;
			}
		}
		checkTotalHeartBeat();
	}

	/**
	 * Heart beat.
	 *
	 * @param msgCount
	 *            the msg count
	 * @return true, if successful
	 */
	public boolean heartBeat(final int msgCount) {
		if (!duringSendRound.tryLock()) {
			return false;
		}

		synchronized (seenHeartBeats) {
			Integer count = seenHeartBeats.get(id);
			if (count == null || count == -1) {
				count = 0;
			}
			LOG.warning(id + ": heartBeat:" + msgCount + " from "
					+ (count + msgCount));
			seenHeartBeats.put(id, count + msgCount);
		}
		duringSendRound.unlock();
		checkTotalHeartBeat();
		return true;
	}

	private void nextRound() {
		HashMap<String, Queue<QueueEntry>> copy = null;
		final Integer[] counter = new Integer[1];
		synchronized (seenHeartBeats) {
			synchronized (inbox) {
				synchronized (inboxSize) {
					LOG.warning(id + ": doing next round:" + inboxSize[0]);
					copy = new HashMap<String, Queue<QueueEntry>>(inbox);
					inbox.clear();
					seenHeartBeats.clear();
					counter[0] = inboxSize[0];
					inboxSize[0] = 0;
				}
			}
		}
		if (copy != null) {
			// Send each agent it's messages in order, single threaded per agent
			for (final Queue<QueueEntry> queue : copy.values()) {
				ThreadPool.getPool().execute(new Runnable() {
					@Override
					public void run() {
						final Iterator<QueueEntry> iter = queue.iterator();
						while (iter.hasNext()) {
							QueueEntry entry = iter.next();
							synchronized (counter) {
								counter[0]--;
								LOG.warning("Sending next message onwards: "
										+ counter[0] + " : "
										+ entry.meta.getMsg());
								counter.notify();
							}
							entry.meta.nextIn();
						}
					}

				});
			}
		}
		synchronized (counter) {
			while (counter[0] > 0) {
				try {
					counter.wait();
				} catch (InterruptedException e) {}
			}
		}
	}

	private void checkTotalHeartBeat() {
		Integer counter = 0;
		synchronized (seenHeartBeats) {
			for (Integer entry : seenHeartBeats.values()) {
				if (entry == -1) {
					// Not done yet!
					return;
				}
				counter += entry;
			}
			synchronized (inbox) {
				synchronized (inboxSize) {
					if (counter == inboxSize[0]) {
						duringSendRound.lock();
						if (counter > 0 && counter == inboxSize[0]) {
							nextRound();
						}
						duringSendRound.unlock();
					}
				}
			}
		}
	}

	/**
	 * The Class QueueEntry.
	 */
	class QueueEntry implements Comparable<QueueEntry> {
		private Meta	meta	= null;
		private UUID	id		= null;

		/**
		 * Instantiates a new queue entry.
		 *
		 * @param meta
		 *            the meta
		 * @param id
		 *            the id
		 */
		public QueueEntry(final Meta meta, final UUID id) {
			this.meta = meta;
			this.id = id;
		}

		@Override
		public int compareTo(QueueEntry o) {
			if (this.equals(o))
				return 0;
			if (meta == null || meta.getPeer() == null || id == null)
				return -1;
			if (o == null || o.meta == null || o.meta.getPeer() == null
					|| o.id == null)
				return 1;
			int res = meta.getPeer().compareTo(o.meta.getPeer());
			if (res == 0) {
				res = id.compareTo(o.id);
			}
			return res;
		}

	}

}
