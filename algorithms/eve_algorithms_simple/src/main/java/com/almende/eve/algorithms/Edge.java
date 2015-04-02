package com.almende.eve.algorithms;

import java.net.URI;

/**
 * The Class Edge.
 */
public class Edge implements Comparable<Edge> {
	private URI					address	= null;
	private Object				tag		= null;
	private Comparable<Object>	weight	= null;

	/**
	 * Instantiates a new edge.
	 */
	public Edge() {}

	/**
	 * Instantiates a new edge.
	 *
	 * @param address
	 *            the address
	 * @param tag
	 *            the tag
	 * @param weight
	 *            the weight
	 */
	public Edge(URI address, Object tag, Comparable<Object> weight) {
		this.address = address;
		this.tag = tag;
		this.weight = weight;
	}

	/**
	 * Gets the address.
	 *
	 * @return the address
	 */
	public URI getAddress() {
		return address;
	}

	/**
	 * Sets the address.
	 *
	 * @param address
	 *            the new address
	 */
	public void setAddress(URI address) {
		this.address = address;
	}

	/**
	 * Gets the tag.
	 *
	 * @return the tag
	 */
	public Object getTag() {
		return tag;
	}

	/**
	 * Sets the tag.
	 *
	 * @param tag
	 *            the new tag
	 */
	public void setTag(Object tag) {
		this.tag = tag;
	}

	/**
	 * Gets the weight.
	 *
	 * @return the weight
	 */
	public Comparable<?> getWeight() {
		return weight;
	}

	/**
	 * Sets the weight.
	 *
	 * @param weight
	 *            the new weight
	 */
	public void setWeight(Comparable<Object> weight) {
		this.weight = weight;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Edge o) {
		if (this.equals(o)) {
			return 0;
		}
		if (this.weight != null && o.weight != null) {
			final int res = this.weight.compareTo(o.weight);
			if (res == 0) {
				return 1;
			} else {
				return res;
			}
		}
		return 1;
	}
	
	@Override
	public boolean equals(Object o){
		if (o == null){
			return false;
		}
		if (o == this){
			return true;
		}
		if (!(o instanceof Edge)){
			return false;
		}
		Edge other = (Edge)o;
		if ( address == null && other.address != null) return false;
		if ( address != null && !address.equals(other.address)) return false;
		if ( tag == null && other.tag != null) return false;
		if ( tag != null && !tag.equals(other.tag)) return false;
		if ( weight == null && other.weight != null) return false;
		if ( weight != null && !weight.equals(other.weight)) return false;
		return true;
	}
	@Override
	public int hashCode(){
		int res = Integer.MAX_VALUE;
		if (address != null ) res ^= address.hashCode();
		if (tag != null) res ^= tag.hashCode();
		if (weight != null) res ^= weight.hashCode();
		return res;
	}
}
