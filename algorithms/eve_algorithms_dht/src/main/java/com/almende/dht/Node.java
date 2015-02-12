/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.dht;

import java.io.Serializable;
import java.net.URI;

/**
 * The Class Node.
 */
public class Node implements Serializable, Comparable<Node>{
	private static final long serialVersionUID = 941011043741195069L;
	private Key key;
	private URI uri;

	/**
	 * Instantiates a new node.
	 */
	public Node() {
	};
	
	/**
	 * Instantiates a new node.
	 *
	 * @param key
	 *            the key
	 * @param uri
	 *            the uri
	 */
	public Node(final Key key, final URI uri){
		this.key=key;
		this.uri=uri;
	}

	/**
	 * Gets the key.
	 *
	 * @return the key
	 */
	public Key getKey() {
		return key;
	}

	/**
	 * Sets the key.
	 *
	 * @param key
	 *            the new key
	 */
	public void setKey(Key key) {
		this.key = key;
	}

	/**
	 * Gets the uri.
	 *
	 * @return the uri
	 */
	public URI getUri() {
		return uri;
	}

	/**
	 * Sets the uri.
	 *
	 * @param uri
	 *            the new uri
	 */
	public void setUri(URI uri) {
		this.uri = uri;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString(){
		return key+":"+uri.toASCIIString();
	}

	@Override
	public int compareTo(Node o) {
		return key.compareTo(o.getKey());
	}
	
	@Override
	public int hashCode(){
		return key.hashCode();
	}
	@Override
	public boolean equals(Object o){
		if (o == null) {
			return false;
		}
		if (!(o instanceof Node))
			return false;
		if (o == this)
			return true;
		final Node other = (Node) o;
		return key.equals(other.getKey());
	}
}
