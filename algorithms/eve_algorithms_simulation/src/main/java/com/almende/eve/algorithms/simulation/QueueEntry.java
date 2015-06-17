/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.algorithms.simulation;

import java.net.URI;
import java.util.Iterator;

import com.almende.eve.protocol.Meta;
import com.almende.eve.protocol.MetaImpl;
import com.almende.eve.protocol.Protocol;
import com.almende.util.uuid.UUID;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * The Class QueueEntry.
 */
public class QueueEntry implements Comparable<QueueEntry>, Meta {
	private Meta	meta	= null;
	private UUID	id		= null;

	/**
	 * Instantiates a new meta.
	 */
	public QueueEntry() {
		super();
		this.meta = new MetaImpl();
	}

	/**
	 * Instantiates a new meta.
	 *
	 * @param clone
	 *            the clone
	 */
	public QueueEntry(Meta clone) {
		this.meta = new MetaImpl(clone);
	}

	/**
	 * Instantiates a new queue entry.
	 *
	 * @param meta
	 *            the meta
	 * @param id
	 *            the id
	 */
	public QueueEntry(final Meta meta, final UUID id) {
		this.meta = meta;
		this.id = id;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(QueueEntry o) {
		if (this.equals(o))
			return 0;
		if (meta == null || meta.getPeer() == null || id == null) {
			return -1;
		}
		if (o == null || o.meta == null || o.meta.getPeer() == null
				|| o.id == null) {
			return 1;
		}
		int res = meta.getPeer().compareTo(o.meta.getPeer());
		if (res == 0) {
			res = id.compareTo(o.id);
		}
		return res;
	}

	/*
	 * (non-Javadoc)
	 * @see com.almende.eve.protocol.Meta#getMsg()
	 */
	@Override
	public Object getMsg() {
		return this.meta.getMsg();
	}

	/*
	 * (non-Javadoc)
	 * @see com.almende.eve.protocol.Meta#setMsg(java.lang.Object)
	 */
	@Override
	public void setMsg(final Object msg) {
		this.meta.setMsg(msg);
	}

	/*
	 * (non-Javadoc)
	 * @see com.almende.eve.protocol.Meta#setTag(java.lang.String)
	 */
	@Override
	public void setTag(final String tag) {
		this.meta.setTag(tag);
	}

	/*
	 * (non-Javadoc)
	 * @see com.almende.eve.protocol.Meta#getTag()
	 */
	@Override
	public String getTag() {
		return this.meta.getTag();
	}

	/*
	 * (non-Javadoc)
	 * @see com.almende.eve.protocol.Meta#getPeer()
	 */
	@Override
	public URI getPeer() {
		return this.meta.getPeer();
	}

	/*
	 * (non-Javadoc)
	 * @see com.almende.eve.protocol.Meta#setPeer(java.net.URI)
	 */
	@Override
	public void setPeer(final URI peer) {
		this.meta.setPeer(peer);
	}

	/*
	 * (non-Javadoc)
	 * @see com.almende.eve.protocol.Meta#getIter()
	 */
	@Override
	@JsonIgnore
	public Iterator<Protocol> getIter() {
		return this.meta.getIter();
	}

	/*
	 * (non-Javadoc)
	 * @see com.almende.eve.protocol.Meta#setIter(java.util.Iterator)
	 */
	@Override
	@JsonIgnore
	public void setIter(final Iterator<Protocol> iter) {
		this.meta.setIter(iter);
	}

	/*
	 * (non-Javadoc)
	 * @see com.almende.eve.protocol.Meta#nextIn()
	 */
	@Override
	@JsonIgnore
	public boolean nextIn() {
		return this.meta.nextIn();
	}

	/*
	 * (non-Javadoc)
	 * @see com.almende.eve.protocol.Meta#nextOut()
	 */
	@Override
	@JsonIgnore
	public boolean nextOut() {
		return this.meta.nextOut();
	}

	/**
	 * Gets the meta.
	 *
	 * @return the meta
	 */
	public Meta getMeta() {
		return this.meta;
	}

	/**
	 * Sets the meta.
	 *
	 * @param meta
	 *            the new meta
	 */
	public void setMeta(final Meta meta) {
		this.meta = meta;
	}
}