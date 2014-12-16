/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.scheduling;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.joda.time.DateTime;

import com.almende.eve.capabilities.handler.Handler;
import com.almende.eve.scheduling.clock.RunnableClock;
import com.almende.eve.transform.rpc.annotation.Access;
import com.almende.eve.transform.rpc.annotation.AccessType;
import com.almende.eve.transform.rpc.annotation.Namespace;
import com.almende.eve.transform.rpc.formats.Caller;
import com.almende.eve.transport.Receiver;
import com.almende.util.jackson.JOM;
import com.almende.util.uuid.UUID;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class SyncScheduler.
 */

@Namespace("syncScheduler")
public class SyncScheduler extends SimpleScheduler {
	private static final Logger	LOG				= Logger.getLogger(SyncScheduler.class
														.getName());
	private long				offset			= 0;
	private long				syncInterval	= 50000;
	private Caller				caller			= null;
	private Set<URI>			peers			= new HashSet<URI>();
	private Boolean				active			= false;

	@Override
	public long now() {
		return super.now() + offset;
	}

	/**
	 * Sets the caller.
	 *
	 * @param caller
	 *            the new caller
	 */
	public void setCaller(final Caller caller) {
		this.caller = caller;
	}

	/**
	 * Adds the peer.
	 *
	 * @param peer
	 *            the peer
	 */
	public void addPeer(final URI peer) {
		if (!peers.contains(peer)) {
			peers.add(peer);
		}
		sync();
	}

	@Override
	public String schedule(final Object msg, final DateTime due) {
		final String uuid = new UUID().toString();
		getClock().requestTrigger(uuid, due.minus(offset), new Runnable() {

			@Override
			public void run() {
				getHandle().get().receive(msg, getSchedulerUrl(), null);
			}

		});
		return uuid;
	}

	@Override
	public String schedule(Object msg, int delay) {
		return schedule(msg, new DateTime(now()).plus(delay));
	}

	/**
	 * Instantiates a new persistent scheduler.
	 * 
	 * @param params
	 *            the params
	 * @param handle
	 *            the handle
	 */
	public SyncScheduler(final ObjectNode params, final Handler<Receiver> handle) {
		super(params, handle);
		if (getClock() == null) {
			setClock(new RunnableClock());
		}
	}

	/**
	 * Ping.
	 *
	 * @return the long
	 */
	@Access(AccessType.PUBLIC)
	public Long ping() {
		return now();
	}

	class SyncTupple implements Comparable<SyncTupple> {
		long	offset;
		long	roundtrip;

		public SyncTupple(long offset, long roundtrip) {
			this.offset = offset;
			this.roundtrip = roundtrip;
		}

		@Override
		public int compareTo(SyncTupple o) {
			return roundtrip == o.roundtrip ? 0 : (roundtrip > o.roundtrip ? 1
					: -1);
		}

		@Override
		public String toString() {
			return "{\"offset\":" + offset + ",\"roundtrip\":" + roundtrip
					+ "}";
		}
	}

	/**
	 * Sync with peer.
	 *
	 * @param peer
	 *            the peer
	 * @return the long
	 */
	@Access(AccessType.PUBLIC)
	public SyncTupple syncWithPeer(final URI peer) {
		if (caller == null) {
			LOG.warning("Sync requested, but caller is still null, invalid!");
			return null;
		}
		LOG.info("Starting sync with: " + peer + "!");
		final long start = now();
		try {
			final Long result = caller.callSync(peer, "syncScheduler.ping",
					JOM.createObjectNode());
			final long now = now();
			final long roundtrip = now - start;
			final long offset = (result - (start + (roundtrip / 2)));
			LOG.info("Sync resulted in offset:" + offset + " ( " + roundtrip
					+ ":" + start + ":" + result + ")");
			return new SyncTupple(offset, roundtrip);
		} catch (IOException e) {
			LOG.log(Level.WARNING, "failed to send ping", e);
		}
		return null;
	}

	/**
	 * Sync.
	 */
	@Access(AccessType.PUBLIC)
	public void sync() {
		synchronized (active) {
			if (active) {
				return;
			}
			active = true;
		}
		try {
			for (final URI peer : peers) {
				LOG.info("Doing sync with " + peer + "!");

				final ArrayList<SyncTupple> results = new ArrayList<SyncTupple>(
						5);
				final int[] fail = new int[1];
				fail[0] = 0;
				getClock().requestTrigger(new UUID().toString(),
						DateTime.now(), new Runnable() {
							@Override
							public void run() {
								final SyncTupple res = syncWithPeer(peer);
								if (res != null) {
									results.add(res);
								} else {
									fail[0]++;
								}
								if (fail[0] < 5 && results.size() < 5) {
									getClock().requestTrigger(
											new UUID().toString(),
											DateTime.now().plus(
													(long) (4000 * Math
															.random())), this);
								}
							}
						});
				while (fail[0] < 5 && results.size() < 5) {
					try {
						Thread.sleep(4000);
					} catch (InterruptedException e) {}
				}
				long sum = 0;
				for (int i = 0; i < results.size(); i++) {
					sum += results.get(i).roundtrip;
				}
				long mean = sum / results.size();

				sum = 0;
				for (int i = 0; i < results.size(); i++) {
					sum += Math.pow(results.get(i).roundtrip - mean, 2);
				}

				double stdDev = Math.sqrt(sum / results.size());
				double limit = stdDev + mean;
				LOG.warning("Mean:" + mean + " stdDev:" + stdDev + " limit:"
						+ limit);

				sum = 0;
				int count = 0;
				for (SyncTupple tupple : results) {
					if (tupple.roundtrip > limit) {
						LOG.warning("Skipping tupple:" + tupple);
						continue;
					}
					LOG.warning("Adding tupple:" + tupple);
					count++;
					sum += tupple.offset;
				}

				offset += sum / count;
				LOG.info("Done sync with " + peer + "! new offset:" + offset
						+ "(" + sum / count + ")");
			}
		} catch (Exception e) {
			LOG.log(Level.WARNING, "TimeSync failed", e);
		}
		synchronized (active) {
			active = false;
		}
		getClock()
				.requestTrigger(
						new UUID().toString(),
						new DateTime(now()).plus((long) (syncInterval * Math
								.random())), new Runnable() {
							@Override
							public void run() {
								try {
									sync();
								} catch (Exception e) {
									LOG.log(Level.WARNING, "sync failed", e);
								}
							}
						});

	}
}
