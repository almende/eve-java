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
public interface Meta {

	/**
	 * Gets the result.
	 *
	 * @return the result
	 */
	Object getMsg();

	/**
	 * Sets the result.
	 *
	 * @param msg
	 *            the new result
	 */
	void setMsg(final Object msg);

	/**
	 * Sets the tag.
	 *
	 * @param tag
	 *            the new tag
	 */
	void setTag(final String tag);

	/**
	 * Gets the tag.
	 *
	 * @return the tag
	 */
	String getTag();

	/**
	 * Gets the peer.
	 *
	 * @return the peer
	 */
	URI getPeer();

	/**
	 * Sets the peer.
	 *
	 * @param peer
	 *            the new peer
	 */
	void setPeer(final URI peer);

	/**
	 * Gets the iter.
	 *
	 * @return the iter
	 */
	@JsonIgnore
	Iterator<Protocol> getIter();

	/**
	 * Sets the iter.
	 *
	 * @param iter
	 *            the new iter
	 */
	@JsonIgnore
	void setIter(final Iterator<Protocol> iter);

	/**
	 * Next in.
	 *
	 * @return true, if successful
	 */
	@JsonIgnore
	boolean nextIn();

	/**
	 * Next out.
	 *
	 * @return true, if successful
	 */
	@JsonIgnore
	boolean nextOut();
}
