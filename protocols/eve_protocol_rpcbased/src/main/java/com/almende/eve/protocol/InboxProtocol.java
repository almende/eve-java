/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.protocol;

import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.almende.eve.capabilities.handler.Handler;
import com.almende.eve.protocol.jsonrpc.formats.JSONMessage;
import com.almende.eve.protocol.jsonrpc.formats.JSONRequest;
import com.almende.util.callback.AsyncCallback;
import com.almende.util.callback.SyncCallback;
import com.almende.util.threads.ThreadPool;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class InboxProtocol, provides an easy way to get a single threaded agent,
 * only one inbound message in a single thread at a time.
 */
public class InboxProtocol implements Protocol {

	private BlockingQueue<Meta>	inbox		= new LinkedBlockingQueue<Meta>();
	private InboxProtocolConfig	params		= null;
	protected final Boolean[]	stop		= new Boolean[] { false };
	private Set<String>			callbackIds	= new HashSet<String>(5);
	final Boolean[]				sequencer	= new Boolean[2];
	protected Runnable			loop		= null;

	/**
	 * Instantiates a new inbox protocol.
	 *
	 * @param params
	 *            the params
	 * @param handle
	 *            the handle
	 */
	public InboxProtocol(final ObjectNode params, final Handler<Object> handle) {
		this.params = InboxProtocolConfig.decorate(params);
		initDefLoop();
	}

	protected void initDefLoop() {
		chgLooper(new Runnable() {
			// Agent thread
			@Override
			public void run() {
				stop[0] = false;
				while (!stop[0]) {
					try {
						final Meta next = getNext();
						next(next);
						synchronized (sequencer) {
							while (!sequencer[0]) {
								sequencer.wait();
							}
						}
					} catch (InterruptedException e) {
						// Nothing todo.
					}
				}
			}
		});
	}

	protected Meta getNext() throws InterruptedException {
		// sequencer[0] is the actual wait latch
		sequencer[0] = false;
		// sequencer[1] is the flag to skip triggering on the
		// latch if this is a reply to synchronous call.
		sequencer[1] = false;
		final Meta next = inbox.take();
		if (!callbackIds.isEmpty()) {
			final JSONMessage message = JSONMessage.jsonConvert(next.getMsg());
			if (message != null) {
				// No need to parse it again later.
				next.setMsg(message);
				if (message.isResponse() && callbackIds.remove(message.getId())) {
					sequencer[1] = true;
				}
			}
		}
		return next;
	}

	protected void next(final Meta next) {
		ThreadPool.getPool().execute(new Runnable() {

			@Override
			public void run() {
				next.nextIn();
				synchronized (sequencer) {
					if (!sequencer[1]) {
						sequencer[0] = true;
						sequencer.notifyAll();
					}
				}
			}
		});
	}

	/**
	 * Gets the inbox.
	 *
	 * @return the inbox
	 */
	public Queue<Meta> getInbox() {
		return inbox;
	}

	/**
	 * Sets the inbox.
	 *
	 * @param inbox
	 *            the new inbox
	 * @param chgLooper
	 *            the chg looper
	 */
	public void setInbox(BlockingQueue<Meta> inbox, boolean chgLooper) {
		this.inbox = inbox;
		if (chgLooper) {
			chgLooper(loop);
		}
	}

	/**
	 * Replaces the inbox send loop;
	 *
	 * @param loop
	 *            the new looper
	 */
	public void chgLooper(Runnable loop) {
		this.loop = loop;
		this.stop[0] = true;
		synchronized (sequencer) {
			sequencer[0] = true;
			sequencer.notifyAll();
		}
		ThreadPool.getPool().execute(loop);
	}

	/**
	 * Gets the sequencer.
	 *
	 * @return the sequencer
	 */
	public Boolean[] getSequencer() {
		return sequencer;
	}

	/*
	 * (non-Javadoc)
	 * @see com.almende.eve.capabilities.Capability#getParams()
	 */
	@Override
	public ObjectNode getParams() {
		return this.params;
	}

	/*
	 * (non-Javadoc)
	 * @see com.almende.eve.capabilities.Capability#delete()
	 */
	@Override
	public void delete() {
		// empty inbox
		stop[0] = true;
		inbox.clear();
		callbackIds.clear();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.almende.eve.protocol.Protocol#inbound(com.almende.eve.protocol.Meta)
	 */
	@Override
	public boolean inbound(Meta msg) {
		try {
			inbox.put(msg);
		} catch (InterruptedException e) {}
		// explicitely not calling next on protocol stack from this point.
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.almende.eve.protocol.Protocol#outbound(com.almende.eve.protocol.Meta)
	 */
	@Override
	public boolean outbound(Meta msg) {
		if (params.isSupportSynccalls()) {
			final JSONMessage message = JSONMessage.jsonConvert(msg.getMsg());
			if (message != null && message.isRequest()) {
				final JSONRequest request = (JSONRequest) message;
				AsyncCallback<?> callback = request.getCallback();
				if (callback != null && callback instanceof SyncCallback<?>) {
					callbackIds.add(request.getId().asText());
					synchronized (sequencer) {
						sequencer[0] = true;
						sequencer.notifyAll();
					}
				}
			}
		}
		// just forwarding...
		return msg.nextOut();
	}
}
