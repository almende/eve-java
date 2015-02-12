/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.dht;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * The Class Bucket.
 */
public class Bucket {
//	private static final Logger	LOG	= Logger.getLogger(Bucket.class.getName());

	private class Meta {
		private long	lastUpdate	= 0;
		private int		rank = 0;

		public Meta(){}
		
		/**
		 * Gets the last update.
		 *
		 * @return the last update
		 */
		@JsonIgnore
		public long getLastUpdate() {
			return lastUpdate;
		}
		
		/**
		 * Sets the last update.
		 *
		 * @param lastUpdate
		 *            the new last update
		 */
		public void setLastUpdate(long lastUpdate) {
			this.lastUpdate = lastUpdate;
		}

		/**
		 * Gets the rank.
		 *
		 * @return the rank
		 */
		public int getRank() {
			return rank;
		}

		/**
		 * Sets the rank.
		 *
		 * @param rank
		 *            the new rank
		 */
		public void setRank(int rank) {
			this.rank = rank;
		}
		
	}

	private Meta						meta	= new Meta();
	private LinkedHashMap<Key, Node>	nodes;

	/**
	 * Instantiates a new bucket.
	 */
	public Bucket(){};
	
	/**
	 * Instantiates a new bucket.
	 *
	 * @param rank
	 *            the rank
	 */
	public Bucket(final int rank) {
		this.meta.setRank(rank);
		this.nodes = new LinkedHashMap<Key, Node>(Constants.K,(float) 0.75,true);
	};

	
	/**
	 * Seen node.
	 *
	 * @param node
	 *            the node
	 */
	public void seenNode(final Node node) {
		synchronized (nodes) {
			if (nodes.containsKey(node.getKey())) {
				nodes.remove(node.getKey());
				nodes.put(node.getKey(), node);
			} else if (nodes.size() < Constants.K) {
				nodes.put(node.getKey(), node);
			} else {
				// get first Node through iterator, do Ping(), wait, on answer
				// within timeout drop key; else drop first Node, recurse
				// seenNode();
//				LOG.warning("Bucket full:"+this.meta.rank+ " dropping oldest (not yet doing ping)");
				synchronized (nodes) {
					Iterator<Entry<Key, Node>> iter = nodes.entrySet().iterator();
					iter.next();
					iter.remove();
					nodes.put(node.getKey(), node);
				}
			}
			meta.setLastUpdate(System.currentTimeMillis());
		}
	}

	/**
	 * Gets the closest nodes.
	 *
	 * @param near
	 *            the near
	 * @param limit
	 *            the limit
	 * @param filter
	 *            the filter
	 * @return the closest nodes
	 */
	public List<Node> getClosestNodes(final Key near, final int limit,
			final Collection<Key> filter) {
		synchronized (nodes) {
			final TreeMap<Key, Node> distMap = new TreeMap<Key, Node>();
			final Iterator<Entry<Key, Node>> iter = nodes.entrySet().iterator();
			while (iter.hasNext()) {
				Entry<Key, Node> entry = iter.next();
				if (filter != null && filter.contains(entry.getKey())) {
					continue;
				}
				distMap.put(near.dist(entry.getKey()), entry.getValue());
			}
			final Node[] values = distMap.values().toArray(new Node[0]);
			return Arrays.asList(Arrays.copyOf(values, Math.min(limit, distMap.size())));
		}
	}

	/**
	 * Gets the closest nodes.
	 *
	 * @param near
	 *            the near
	 * @param limit
	 *            the limit
	 * @param filter
	 *            the filter
	 * @return the closest nodes
	 */
	public List<Node> getClosestNodes(final Key near, final int limit,
			final Key[] filter) {
		final Set<Key> set = new HashSet<Key>(filter.length);
		Collections.addAll(set, filter);
		return getClosestNodes(near, limit, set);
	}

	/**
	 * Gets the closest nodes.
	 *
	 * @param near
	 *            the near
	 * @param limit
	 *            the limit
	 * @return the closest nodes
	 */
	public List<Node> getClosestNodes(final Key near, final int limit) {
		return getClosestNodes(near, limit, Collections.<Key> emptySet());
	}

	/**
	 * Gets the closest nodes.
	 *
	 * @param near
	 *            the near
	 * @return the closest nodes
	 */
	public List<Node> getClosestNodes(final Key near) {
		return getClosestNodes(near, Integer.MAX_VALUE,
				Collections.<Key> emptySet());
	}

	/**
	 * Checks if is stale.
	 *
	 * @return true, if is stale
	 */
	@JsonIgnore
	public boolean isStale(){
		final long due = System.currentTimeMillis()-Constants.REFRESH;
		return meta.getLastUpdate()<due;
	}
	
	/**
	 * Size.
	 *
	 * @return the int
	 */
	@JsonIgnore
	public int size(){
		return nodes.size();
	}
	
	/**
	 * Gets the random key.
	 *
	 * @return the random key
	 */
	@JsonIgnore
	public Key getRandomKey(){
		if (this.meta.getRank() == -1){
			return Key.random();
		} else {
			return Key.random(this.meta.getRank());
		}
	}
	
	/**
	 * Gets the meta.
	 *
	 * @return the meta
	 */
	public Meta getMeta() {
		return meta;
	}

	/**
	 * Sets the meta.
	 *
	 * @param meta
	 *            the new meta
	 */
	public void setMeta(Meta meta) {
		this.meta = meta;
	}
	
	/**
	 * Gets the nodes.
	 *
	 * @return the nodes
	 */
	public LinkedHashMap<Key, Node> getNodes() {
		return nodes;
	}

	/**
	 * Sets the nodes.
	 *
	 * @param nodes
	 *            the nodes
	 */
	public void setNodes(LinkedHashMap<Key, Node> nodes) {
		this.nodes = nodes;
	}
	
}
