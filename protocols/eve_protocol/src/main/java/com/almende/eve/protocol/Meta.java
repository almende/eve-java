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
public class Meta {
	private Object				result	= null;
	private URI					peer	= null;
	private String				tag		= null;
	private Iterator<Protocol>	iter	= null;

	/**
	 * Instantiates a new meta.
	 */
	public Meta() {}

	/**
	 * Instantiates a new meta.
	 *
	 * @param clone
	 *            the clone
	 */
	public Meta(Meta clone) {
		this.result = clone.result;
		this.peer = clone.peer;
		this.tag = clone.tag;
		this.iter = clone.iter;
	}

	/**
	 * Instantiates a new meta.
	 *
	 * @param result
	 *            the result
	 * @param peer
	 *            the peer
	 * @param tag
	 *            the tag
	 * @param iter
	 *            the iter
	 */
	public Meta(final Object result, final URI peer, final String tag, final Iterator<Protocol> iter) {
		this.result = result;
		this.peer = peer;
		this.tag = tag;
		this.iter = iter;
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
	public boolean nextIn(){
		if (iter.hasNext()){
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
	public boolean nextOut(){
		if (iter.hasNext()){
			final Protocol protocol = iter.next();
			return protocol.outbound(this);
		}
		return true;
	}
}
