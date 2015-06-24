/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.algorithms;

import java.net.URI;

/**
 * The Class Links.
 */
public// LinkedList
class Links {
	private Boolean	done	= false;
	private URI		prev	= null;
	private URI		next	= null;

	/**
	 * Instantiates a new links.
	 */
	public Links() {}

	/**
	 * Gets the done.
	 *
	 * @return the done
	 */
	public Boolean isDone() {
		return done;
	}

	/**
	 * Sets the done.
	 *
	 * @param done
	 *            the new done
	 */
	public void setDone(Boolean done) {
		this.done = done;
	}

	/**
	 * Gets the prev.
	 *
	 * @return the prev
	 */
	public URI getPrev() {
		return prev;
	}

	/**
	 * Sets the prev.
	 *
	 * @param prev
	 *            the new prev
	 */
	public void setPrev(URI prev) {
		this.prev = prev;
	}

	/**
	 * Gets the next.
	 *
	 * @return the next
	 */
	public URI getNext() {
		return next;
	}

	/**
	 * Sets the next.
	 *
	 * @param next
	 *            the new next
	 */
	public void setNext(URI next) {
		this.next = next;
	}
}
