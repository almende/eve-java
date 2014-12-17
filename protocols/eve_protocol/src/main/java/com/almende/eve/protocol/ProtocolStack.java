/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.protocol;

import java.net.URI;
import java.util.Iterator;
import java.util.LinkedList;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class ProtocolStack.
 */
public class ProtocolStack implements Protocol {
	private final LinkedList<Protocol>	stack	= new LinkedList<Protocol>();

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

	/*
	 * (non-Javadoc)
	 * @see com.almende.eve.capabilities.Capability#getParams()
	 */
	@Override
	public ObjectNode getParams() {
		return null;
	}

	@Override
	public Meta inbound(final Object msg, URI senderUrl) {
		final Iterator<Protocol> iter = stack.iterator();
		Meta res = new Meta(msg);
		while (res.doNext && iter.hasNext()) {
			final Protocol protocol = iter.next();
			res = protocol.inbound(res.valid ? res.result : msg, senderUrl);
		}
		return res;
	}

	@Override
	public Meta outbound(final Object msg, final URI recipientUrl) {
		final Iterator<Protocol> iter = stack.descendingIterator();
		Meta res = new Meta(msg);
		while (res.doNext && iter.hasNext()) {
			Protocol protocol = iter.next();
			res = protocol
					.outbound(res.valid ? res.result : msg, recipientUrl);
		}
		return res;
	}
	
	@Override
	public void delete(){
		for (Protocol protocol : stack){
			protocol.delete();
		}
		stack.clear();
	}
}
