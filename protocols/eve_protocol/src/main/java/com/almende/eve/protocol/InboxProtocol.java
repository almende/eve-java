/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.protocol;

import java.util.concurrent.LinkedBlockingQueue;

import com.almende.eve.capabilities.Config;
import com.almende.eve.capabilities.handler.Handler;
import com.almende.util.threads.ThreadPool;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class InboxProtocol, provides an easy way to get a single threaded agent,
 * only one inbound message in a single thread at a time.
 */
public class InboxProtocol implements Protocol {

	private final LinkedBlockingQueue<Meta>	inbox	= new LinkedBlockingQueue<Meta>();
	private Config							params	= null;
	private final Boolean[]					stop	= new Boolean[] { false };

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
						Meta next = new Meta(inbox.take());
						next.nextIn();
						// TODO: handle next.
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
	public void inbound(Meta msg) {
		try {
			inbox.put(msg);
		} catch (InterruptedException e) {
		}		
		// explicitely not calling next on protocol stack from this point.
	}

	@Override
	public void outbound(Meta msg) {
		// just forwarding...
		msg.nextOut();
	}

}
