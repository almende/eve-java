/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.protocol;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

import com.almende.eve.capabilities.Config;
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

	private final LinkedBlockingQueue<Meta>	inbox		= new LinkedBlockingQueue<Meta>();
	private Config							params		= null;
	private final Boolean[]					stop		= new Boolean[] { false };
	private Set<String>						callbackIds	= new HashSet<String>(5);
	final Boolean[]							sequencer	= new Boolean[2];

	/**
	 * Instantiates a new inbox protocol.
	 *
	 * @param params
	 *            the params
	 * @param handle
	 *            the handle
	 */
	public InboxProtocol(final ObjectNode params, final Handler<Object> handle) {
		this.params = Config.decorate(params);
		ThreadPool.getPool().execute(new Runnable() {
			// Agent thread
			@Override
			public void run() {
				while (!stop[0]) {
					try {
						// sequencer[0] is the actual wait latch
						sequencer[0] = false;
						// sequencer[1] is the flag to skip triggering on the
						// latch if this is a reply to synchronous call.
						sequencer[1] = false;
						final Meta next = new Meta(inbox.take());
						final JSONMessage message = JSONMessage
								.jsonConvert(next.getMsg());
						if (message != null) {
							// No need to parse it again later.
							next.setMsg(message);
							if (message.isResponse()
									&& callbackIds.remove(message.getId())) {
								sequencer[1] = true;
							}
						}
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

	@Override
	public ObjectNode getParams() {
		return this.params;
	}

	@Override
	public void delete() {
		// empty inbox
		stop[0] = true;
		inbox.clear();
	}

	@Override
	public boolean inbound(Meta msg) {
		try {
			inbox.put(msg);
		} catch (InterruptedException e) {}
		// explicitely not calling next on protocol stack from this point.
		return false;
	}

	@Override
	public boolean outbound(Meta msg) {
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
		// just forwarding...
		return msg.nextOut();
	}

}
