/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.capabilities.wake;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class WakeEntry.
 */
public class WakeEntry {
	private String		wakeKey		= null;
	private String		className	= null;
	private ObjectNode	params		= null;
	
	/**
	 * Instantiates a new entry.
	 */
	public WakeEntry() {
	}
	
	/**
	 * Instantiates a new entry.
	 * 
	 * @param wakeKey
	 *            the wake key
	 * @param params
	 *            the params
	 * @param className
	 *            the class name
	 */
	public WakeEntry(final String wakeKey, final ObjectNode params,
			final String className) {
		this.wakeKey = wakeKey;
		this.className = className;
		setParams(params);
	}
	
	/**
	 * Gets the wake key.
	 * 
	 * @return the wake key
	 */
	public String getWakeKey() {
		return wakeKey;
	}
	
	/**
	 * Sets the wake key.
	 * 
	 * @param wakeKey
	 *            the new wake key
	 */
	public void setWakeKey(final String wakeKey) {
		this.wakeKey = wakeKey;
	}
	
	/**
	 * Gets the className.
	 * 
	 * @return the className
	 */
	public String getClassName() {
		return className;
	}
	
	/**
	 * Sets the className.
	 * 
	 * @param className
	 *            the new className
	 */
	public void setClassName(final String className) {
		this.className = className;
	}
	
	/**
	 * Gets the params.
	 * 
	 * @return the params
	 */
	public ObjectNode getParams() {
		return params;
	}
	
	/**
	 * Sets the params.
	 * 
	 * @param params
	 *            the new params
	 */
	public void setParams(final ObjectNode params) {
		this.params = params;
	}
}