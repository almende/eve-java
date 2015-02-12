/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.dht;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The Class RoutingTable.
 */
public class RoutingTable {
	private Key					myKey;
	private Bucket[]			table	= new Bucket[Constants.BITLENGTH];

	/**
	 * Instantiates a new routing table.
	 */
	public RoutingTable() {}

	/**
	 * Instantiates a new routing table.
	 *
	 * @param key
	 *            the key
	 */
	public RoutingTable(final Key key) {
		this.myKey = key;
		for (int i = 0; i < Constants.BITLENGTH; i++) {
			table[i] = new Bucket(i + 1);
		}
	}

	/**
	 * Gets the bucket.
	 *
	 * @param key
	 *            the key
	 * @param offset
	 *            the offset
	 * @return the bucket
	 */
	public Bucket getBucket(final Key key, final int offset) {
		final Key dist = myKey.dist(key);
		final int rank = dist.rank()-1;
		final int index = rank + offset;
		if (index >= 0 && index < Constants.BITLENGTH) {
			return table[index];
		} else {
			if (rank < -1 || rank >= Constants.BITLENGTH){
				throw new IllegalArgumentException("Incorrect bucket index requested:"+rank);
			}
			return null;
		}
	}

	/**
	 * Gets the bucket.
	 *
	 * @param key
	 *            the key
	 * @return the bucket
	 */
	public Bucket getBucket(final Key key) {
		return getBucket(key, 0);
	}

	/**
	 * Seen node.
	 *
	 * @param node
	 *            the node
	 */
	public void seenNode(final Node node) {
		if (node == null) {
			return;
		}
		final Bucket bucket = getBucket(node.getKey());
		bucket.seenNode(node);
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
		Node[] result = new Node[0];
		int offset = 0;
		boolean[] edges = new boolean[2];
		edges[0] = false;
		edges[1] = false;
		Bucket bucket = null;
		while (result.length < limit && !(edges[0] && edges[1])) {
			bucket = getBucket(near, offset);
			if (bucket != null) {
				Node[] res = bucket.getClosestNodes(near,
						limit - result.length, filter).toArray(new Node[0]);
				if (res.length > 0) {
					final Node[] oldres = result;
					result = new Node[oldres.length + res.length];
					for (int i = 0; i < oldres.length; i++) {
						result[i] = oldres[i];
					}
					for (int i = 0; i < res.length; i++) {
						result[i + oldres.length] = res[i];
					}
				}
			} else {
				if (offset <= 0) {
					edges[0] = true;
				} else {
					edges[1] = true;
				}
			}
			offset = offset <= 0 ? -offset + 1 : -offset;
		}
		return Arrays.asList(result);
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
	 * Gets the stale buckets, needed to be refreshed.
	 *
	 * @return the stale buckets
	 */
	public List<Bucket> getStaleBuckets() {
		final ArrayList<Bucket> result = new ArrayList<Bucket>();
		for (Bucket bucket : table) {
			if (bucket.isStale()) {
				result.add(bucket);
			}
		}
		return result;
	}
	
	/**
	 * Gets the filled buckets (at least 1 node), useful for statistics and debugging.
	 *
	 * @return the filled buckets
	 */
	public List<Bucket> getFilledBuckets(){
		final ArrayList<Bucket> result = new ArrayList<Bucket>();
		for (Bucket bucket : table) {
			if (bucket.size()>0) {
				result.add(bucket);
			}
		}
		return result;		
	}

	/**
	 * Gets the table.
	 *
	 * @return the table
	 */
	public Bucket[] getTable() {
		return table;
	}

	/**
	 * Sets the table.
	 *
	 * @param table
	 *            the new table
	 */
	public void setTable(Bucket[] table) {
		this.table = table;
	}

	/**
	 * Gets the my key.
	 *
	 * @return the my key
	 */
	public Key getMyKey() {
		return myKey;
	}

	/**
	 * Sets the my key.
	 *
	 * @param myKey
	 *            the new my key
	 */
	public void setMyKey(final Key myKey) {
		this.myKey = myKey;
	}
	
	@Override
	public String toString(){
		return "key:"+getMyKey()+" : "+getFilledBuckets().size()+ " buckets filled.";
	}
}
