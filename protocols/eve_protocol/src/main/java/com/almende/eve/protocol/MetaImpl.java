/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.protocol;

import java.net.URI;
import java.util.Iterator;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * The Class Meta.
 */
public class MetaImpl implements Meta {
	private Object				msg		= null;
	private URI					peer	= null;
	private String				tag		= null;
	private Iterator<Protocol>	iter	= null;

	/**
	 * Instantiates a new meta.
	 */
	public MetaImpl() {}

	/**
	 * Instantiates a new meta.
	 *
	 * @param clone
	 *            the clone
	 */
	public MetaImpl(Meta clone) {
		this.msg = clone.getMsg();
		this.peer = clone.getPeer();
		this.tag = clone.getTag();
		this.iter = clone.getIter();
	}

	/**
	 * Instantiates a new meta.
	 *
	 * @param msg
	 *            the result
	 * @param peer
	 *            the peer
	 * @param tag
	 *            the tag
	 * @param iter
	 *            the iter
	 */
	public MetaImpl(final Object msg, final URI peer, final String tag,
			final Iterator<Protocol> iter) {
		this.msg = msg;
		this.peer = peer;
		this.tag = tag;
		this.iter = iter;
	}

	public String toString() {
		return msg.toString();
	}

	/**
	 * Gets the result.
	 *
	 * @return the result
	 */
	public Object getMsg() {
		return msg;
	}

	/**
	 * Sets the result.
	 *
	 * @param msg
	 *            the new result
	 */
	public void setMsg(final Object msg) {
		this.msg = msg;
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

	/**
	 * Gets the iter.
	 *
	 * @return the iter
	 */
	@JsonIgnore
	public Iterator<Protocol> getIter() {
		return iter;
	}

	/**
	 * Sets the iter.
	 *
	 * @param iter
	 *            the new iter
	 */
	@JsonIgnore
	public void setIter(final Iterator<Protocol> iter) {
		this.iter = iter;
	}

	/**
	 * Next in.
	 *
	 * @return true, if successful
	 */
	@JsonIgnore
	public boolean nextIn() {
		if (iter.hasNext()) {
			final Protocol protocol = iter.next();
			return protocol.inbound(this);
		}
		return true;
	}

	/**
	 * Next out.
	 *
	 * @return true, if successful
	 */
	@JsonIgnore
	public boolean nextOut() {
		if (iter.hasNext()) {
			final Protocol protocol = iter.next();
			return protocol.outbound(this);
		}
		return true;
	}
}
