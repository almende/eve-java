package com.almende.eve.protocol;

import java.net.URI;

/**
 * The Class Meta.
 */
public class Meta {
	private Object	result	= null;
	private URI		peer	= null;
	private String	tag		= null;
	private boolean	doNext	= true;

	/**
	 * Instantiates a new meta.
	 */
	public Meta() {}

	/**
	 * Instantiates a new meta.
	 *
	 * @param result
	 *            the result
	 * @param peer
	 *            the peer
	 * @param tag
	 *            the tag
	 */
	public Meta(final Object result, final URI peer, final String tag) {
		this.result = result;
		this.peer = peer;
		this.tag = tag;
	}

	public String toString() {
		return result.toString();
	}

	/**
	 * Gets the result.
	 *
	 * @return the result
	 */
	public Object getResult() {
		return result;
	}

	/**
	 * Sets the result.
	 *
	 * @param result
	 *            the new result
	 */
	public void setResult(final Object result) {
		this.result = result;
	}

	/**
	 * Checks if is do next.
	 *
	 * @return true, if is do next
	 */
	public boolean isDoNext() {
		return doNext;
	}

	/**
	 * Sets the do next.
	 *
	 * @param doNext
	 *            the new do next
	 */
	public void setDoNext(final boolean doNext) {
		this.doNext = doNext;
	}

	/**
	 * Sets the tag.
	 *
	 * @param tag
	 *            the new tag
	 */
	public void setTag(final String tag) {
		this.tag = tag;
	}

	/**
	 * Gets the tag.
	 *
	 * @return the tag
	 */
	public String getTag() {
		return this.tag;
	}

	/**
	 * Gets the peer.
	 *
	 * @return the peer
	 */
	public URI getPeer() {
		return peer;
	}

	/**
	 * Sets the peer.
	 *
	 * @param peer
	 *            the new peer
	 */
	public void setPeer(final URI peer) {
		this.peer = peer;
	}
}
