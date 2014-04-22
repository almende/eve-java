package com.almende.eve.capabilities.wake;

/**
 * The Class WakeEntry.
 */
public class WakeEntry {
	private String	wakeKey		= null;
	private String	className	= null;
	
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
	 * @param className
	 *            the class name
	 */
	public WakeEntry(String wakeKey, String className) {
		this.wakeKey = wakeKey;
		this.className = className;
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
}