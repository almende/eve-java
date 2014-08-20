/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.transform;

import java.net.URI;
import java.util.List;
import java.util.ArrayList;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class TransformStack.
 */
public class TransformStack implements Transform {
	private final List<Transform> stack = new ArrayList<Transform>();

	/**
	 * Adds the transform at the end of the stack
	 *
	 * @param transform
	 *            the transform
	 */
	public void add(final Transform transform) {
		stack.add(transform);
	}

	/**
	 * Pushes the transform to the beginning of the stack
	 *
	 * @param transform
	 *            the transform
	 */
	public void push(final Transform transform) {
		stack.add(0, transform);
	}

	/**
	 * Gets the last transform of the stack.
	 *
	 * @return the top
	 */
	public Transform getLast() {
		if (stack.size() == 0) {
			return null;
		}
		return stack.get(stack.size() - 1);
	}

	/**
	 * Gets the first transform in the stack
	 *
	 * @return the first
	 */
	public Transform getFirst() {
		if (stack.size() == 0) {
			return null;
		}
		return stack.get(0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.almende.eve.capabilities.Capability#getParams()
	 */
	@Override
	public ObjectNode getParams() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.almende.eve.transform.Transform#inbound(java.lang.Object,
	 * java.net.URI)
	 */
	@Override
	public Object inbound(Object msg, URI senderUrl) {
		int i = 0;
		while (msg != null && i< stack.size()){
			Transform transform = stack.get(i++);
			msg = transform.inbound(msg, senderUrl);
		}
		return msg;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.almende.eve.transform.Transform#outbound(java.lang.Object,
	 * java.net.URI)
	 */
	@Override
	public Object outbound(Object msg, URI recipientUrl) {
		int i = stack.size()-1;
		while (msg != null && i>= 0){
			Transform transform = stack.get(i--);
			msg = transform.outbound(msg, recipientUrl);
		}
		return msg;
	}
}
