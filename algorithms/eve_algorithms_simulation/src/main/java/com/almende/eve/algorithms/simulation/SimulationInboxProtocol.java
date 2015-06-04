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
import com.almende.eve.protocol.jsonrpc.formats.JSONRequest;
import com.almende.util.jackson.JOM;
import com.almende.util.uuid.UUID;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class SimulationInboxProtocol.
 */
public class SimulationInboxProtocol extends InboxProtocol {
	private final static Boolean[]			isWork			= new Boolean[] { false };

	private final static Integer[]			inboxCnt		= new Integer[] { 0 };
	private final static Integer[]			latch			= new Integer[] { 0 };
	private BlockingQueue<Meta>				outbox			= new PriorityBlockingQueue<Meta>();
	private SimulationInboxProtocolConfig	params			= null;

	private boolean							isAtomicNetwork	= false;
	private final static Integer[]			msgInCnt		= new Integer[] { 0 };
	private final static Integer[]			msgOutCnt		= new Integer[] { 0 };

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
		this.params = SimulationInboxProtocolConfig.decorate(params);
		isAtomicNetwork = this.params.isAtomicNetwork();
		synchronized (inboxCnt) {
			inboxCnt[0]++;
		}
		initLooper();
	}

	/**
	 * Scheduler out reporting.
	 *
	 * @param count
	 *            the count
	 */
	public static void schedulerOut(final int count) {
		inWork();
		synchronized (msgOutCnt) {
			msgOutCnt[0] += count;
		}
	}

	@Override
	public boolean outbound(Meta msg) {
		inWork();
		boolean res = false;
		if (isAtomicNetwork) {
			final JSONMessage message = JSONMessage.jsonConvert(msg.getMsg());
			if (message != null) {
				msg.setMsg(message);
				if (message.isRequest()) {
					JSONRequest request = (JSONRequest) message;
					if ("scheduler.receiveTracerReport".equals(request
							.getMethod())) {
						return super.outbound(msg);
					}
				}
			}
			synchronized (msgOutCnt) {
				res = super.outbound(msg);
				if (res) {
					msgOutCnt[0]++;
				}
			}
		} else {
			res = super.outbound(msg);
		}
		return res;
	}

	@Override
	public boolean inbound(Meta msg) {
		inWork();
		boolean res = false;
		final JSONMessage message = JSONMessage.jsonConvert(msg.getMsg());
		UUID id = null;
		if (message != null) {
			msg.setMsg(message);
			if (message.getId() == null || message.getId().isNull()) {
				message.setId(JOM.getInstance().valueToTree(
						new UUID().toString()));
			}
			id = new UUID(message.getId().textValue());
		}
		synchronized (getInbox()) {
			if (id != null) {
				res = super.inbound(new QueueEntry(msg, id));
			} else {
				res = super.inbound(new QueueEntry(msg));
			}
		}
		if (isAtomicNetwork) {
			// Make sure all outstanding messages have arrived.
			if (message != null && message.isRequest()) {
				JSONRequest request = (JSONRequest) message;
				if ("scheduler.receiveTracerReport".equals(request.getMethod())) {
					return res;
				}
			}
			synchronized (msgInCnt) {
				msgInCnt[0]++;
				synchronized (msgOutCnt) {
					if (msgInCnt[0] == msgOutCnt[0]) {
						msgInCnt[0] = 0;
						msgOutCnt[0] = 0;
						msgInCnt.notifyAll();
					}
				}
			}
		}
		return res;
	}

	private static void inWork() {
		if (!isWork[0]) {
			synchronized (isWork) {
				isWork[0] = true;
				isWork.notifyAll();
			}
		}
	}

	private static void outOfWork() {
		if (isWork[0]) {
			synchronized (isWork) {
				isWork[0] = false;
			}
		}
	}

	private void waitForWork() {
		synchronized (isWork) {
			while (!isWork[0]) {
				try {
					isWork.wait();
				} catch (InterruptedException e) {}
			}
		}
	}

	private void waitForNetwork() {
		synchronized (msgInCnt) {
			boolean wait = false;
			synchronized (msgOutCnt) {
				wait = msgInCnt[0] != msgOutCnt[0];
			}
			if (wait) {
				try {
					msgInCnt.wait();
				} catch (InterruptedException e) {}
			}
		}
	}

	private void waitForHeartBeat() {
		waitForWork();
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
		}

		if (isAtomicNetwork) {
			// Make sure all outstanding messages have arrived.
			waitForNetwork();
		}
		// Copy inbox to -currentSendBox
		synchronized (getInbox()) {
			outbox.addAll(getInbox());
			getInbox().clear();
		}
		if (outbox.isEmpty()) {
			outOfWork();
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
