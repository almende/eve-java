/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.scheduling;

import java.io.IOException;
import java.net.URI;
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
import com.almende.eve.transport.Caller;
import com.almende.eve.transport.Receiver;
import com.almende.util.callback.AsyncCallback;
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
		peers.add(peer);
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
		return schedule(msg,new DateTime(now()).plus(delay));
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

	/**
	 * Sync.
	 */
	@Access(AccessType.PUBLIC)
	public void sync() {
		if (caller == null){
			LOG.warning("Sync requested, but caller is still null, invalid!");
			return;
		}
		LOG.warning("Starting sync! oldOffset:" + offset);
		final Long[] offsets = new Long[peers.size()];
		int count = 0;
		for (final URI peer : peers) {
			final long start = now();
			final int myCount = count++;
			try {
				caller.call(peer, "syncScheduler.ping", JOM.createObjectNode(),
						new AsyncCallback<Long>() {

							@Override
							public void onSuccess(Long result) {
								final long now = now();
								final long roundtrip = now - start;
								long res = result - (start + (roundtrip / 2)) ;
								offsets[myCount] = res;
								LOG.log(Level.WARNING, "Received syncPing:("+myCount+") "+result+ " in a roundTrip of:"+roundtrip+" ms :"+(start+(roundtrip/2)));
							}

							@Override
							public void onFailure(Exception exception) {
								LOG.log(Level.WARNING, "peer ping failed:",
										exception);
							}

						});
			} catch (IOException e) {
				LOG.log(Level.WARNING, "failed to send ping", e);
			}
		}
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			LOG.log(Level.WARNING, "interrupted!", e);
		}

		long sum = 0;
		count = 0;
		for (final Long offset : offsets) {
			if (offset != null) {
				count++;
				sum += offset;
			}
		}
		if (count > 0){
		offset += sum / count;
		} else {
			LOG.warning("Couldn't sync with any peer? "+peers);
		}

		LOG.warning("Done sync! new offset:" + offset);

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
