/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.algorithms.simulation;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

import com.almende.eve.capabilities.handler.Handler;
import com.almende.eve.protocol.InboxProtocol;
import com.almende.eve.protocol.Meta;
import com.almende.eve.protocol.jsonrpc.formats.JSONMessage;
import com.almende.util.jackson.JOM;
import com.almende.util.uuid.UUID;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class SimulationInboxProtocol.
 */
public class SimulationInboxProtocol extends InboxProtocol {
	private final static Integer[]	inboxCnt	= new Integer[] { 0 };
	private final static Integer[]	latch		= new Integer[] { 0 };
	private BlockingQueue<Meta>		outbox		= new PriorityBlockingQueue<Meta>();

	// private final static Integer[] msgInCnt = new Integer[] { 0 };
	// private final static Integer[] msgOutCnt = new Integer[] { 0 };

	/**
	 * Instantiates a new simulation inbox protocol.
	 *
	 * @param params
	 *            the params
	 * @param handle
	 *            the handle
	 */
	public SimulationInboxProtocol(ObjectNode params, Handler<Object> handle) {
		super(params, handle, false);
		synchronized (inboxCnt) {
			inboxCnt[0]++;
		}
		initLooper();
	}

	@Override
	public boolean inbound(Meta msg) {
		synchronized (getInbox()) {
			final JSONMessage message = JSONMessage.jsonConvert(msg.getMsg());
			if (message != null) {
				msg.setMsg(message);
				if (message.getId() == null || message.getId().isNull()) {
					message.setId(JOM.getInstance().valueToTree(
							new UUID().toString()));
				}
				final UUID id = new UUID(message.getId().textValue());
				return super.inbound(new QueueEntry(msg, id));
			}
			return super.inbound(new QueueEntry(msg));
		}
	}

	private void waitForHeartBeat() {
		synchronized (latch) {
			latch[0]++;
			if (latch[0] == inboxCnt[0]) {
				latch[0] = 0;
				latch.notifyAll();
			} else {
				try {
					latch.wait();
				} catch (InterruptedException e) {}
			}
			// Copy inbox to -currentSendBox
			synchronized (getInbox()) {
				outbox.addAll(getInbox());
				getInbox().clear();
			}
		}
	};

	public Meta getNext(BlockingQueue<Meta> inbox) throws InterruptedException {
		if (!inbox.isEmpty()) {
			return super.getNext(inbox);
		}
		return null;
	}

	private void initLooper() {
		super.chgLooper(new Runnable() {
			// Agent thread
			@Override
			public void run() {
				stop[0] = false;
				while (!stop[0]) {
					waitForHeartBeat();
					try {
						Meta next = getNext(outbox);
						while (next != null) {
							next(next);
							final Boolean[] sequencer = getSequencer();
							synchronized (sequencer) {
								while (!sequencer[0]) {
									sequencer.wait();
								}
							}
							next = getNext(outbox);
						}
					} catch (InterruptedException e) {
						// Nothing todo.
					}

				}
			}
		});
	}

}
