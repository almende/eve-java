/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.protocol;

import java.net.URI;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * The Class ProtocolStack.
 */
public class ProtocolStack {
	private final LinkedList<Protocol>	stack	= new LinkedList<Protocol>();

	/**
	 * Inbound.
	 *
	 * @param msg
	 *            the msg
	 * @param peerUrl
	 *            the peer url
	 * @param tag
	 *            the tag
	 * @return true, if the entire stack is done.
	 */
	public Meta inbound(final Object msg, final URI peerUrl, final String tag) {
		final Iterator<Protocol> iter = stack.iterator();
		final Meta wrapper = new MetaImpl(msg, peerUrl, tag, iter);
		if (iter.hasNext()) {
			final Protocol protocol = iter.next();
			if (!protocol.inbound(wrapper)){
				return null;
			}
		}
		return wrapper;
	}

	/**
	 * Outbound.
	 *
	 * @param msg
	 *            the msg
	 * @param peerUrl
	 *            the peer url
	 * @param tag
	 *            the tag
	 * @return true, if the entire stack is done.
	 */
	public Meta outbound(final Object msg, final URI peerUrl,
			final String tag) {
		final Iterator<Protocol> iter = stack.descendingIterator();
		final Meta wrapper = new MetaImpl(msg, peerUrl, tag, iter);
		if (iter.hasNext()) {
			final Protocol protocol = iter.next();
			if (!protocol.outbound(wrapper)){
				return null;
			};
		}
		return wrapper;
	}

	/**
	 * Adds the protocol at the end of the stack
	 *
	 * @param protocol
	 *            the protocol
	 */
	public void add(final Protocol protocol) {
		stack.add(protocol);
	}

	/**
	 * Pushes the protocol to the beginning of the stack
	 *
	 * @param protocol
	 *            the protocol
	 */
	public void push(final Protocol protocol) {
		stack.add(0, protocol);
	}

	/**
	 * Gets the last protocol of the stack.
	 *
	 * @return the top
	 */
	public Protocol getLast() {
		if (stack.isEmpty()) {
			return null;
		}
		return stack.getLast();
	}

	/**
	 * Gets the first protocol in the stack
	 *
	 * @return the first
	 */
	public Protocol getFirst() {
		if (stack.isEmpty()) {
			return null;
		}
		return stack.getFirst();
	}

	/**
	 * Delete.
	 */
	public void delete() {
		for (Protocol protocol : stack) {
			protocol.delete();
		}
		stack.clear();
	}
}
