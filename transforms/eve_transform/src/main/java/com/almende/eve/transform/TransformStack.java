/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.transform;

import java.net.URI;
import java.util.Iterator;
import java.util.LinkedList;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class TransformStack.
 */
public class TransformStack implements Transform {
	private final LinkedList<Transform>	stack	= new LinkedList<Transform>();

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
		if (stack.isEmpty()) {
			return null;
		}
		return stack.getLast();
	}

	/**
	 * Gets the first transform in the stack
	 *
	 * @return the first
	 */
	public Transform getFirst() {
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

	/*
	 * (non-Javadoc)
	 * @see com.almende.eve.transform.Transform#inbound(java.lang.Object,
	 * java.net.URI)
	 */
	@Override
	public Meta inbound(final Object msg, URI senderUrl) {
		final Iterator<Transform> iter = stack.iterator();
		Meta res = new Meta(msg);
		while (res.doNext && iter.hasNext()) {
			final Transform transform = iter.next();
			res = transform.inbound(res.valid ? res.result : msg, senderUrl);
/*			if (result.isValid()){
				res.result=result.result;
				res.valid=true;
			}
			res.doNext=result.doNext;
*/
		}
		return res;
	}

	/*
	 * (non-Javadoc)
	 * @see com.almende.eve.transform.Transform#outbound(java.lang.Object,
	 * java.net.URI)
	 */
	@Override
	public Meta outbound(final Object msg, final URI recipientUrl) {
		final Iterator<Transform> iter = stack.descendingIterator();
		Meta res = new Meta(msg);
		while (res.doNext && iter.hasNext()) {
			Transform transform = iter.next();
			res = transform
					.outbound(res.valid ? res.result : msg, recipientUrl);
/*			if (result.isValid()){
				res.result=result.result;
				res.valid=true;
			}
			res.doNext=result.doNext;
*/
		}
		return res;
	}
	
	@Override
	public void delete(){
		for (Transform transform : stack){
			transform.delete();
		}
		stack.clear();
	}
}
