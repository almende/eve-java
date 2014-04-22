/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.capabilities.wake;

/**
 * The Interface Wakeable. Objects implementing this interface can be woken by the WakeService.
 */
public interface Wakeable {
	
	/**
	 * Wake.
	 * 
	 * @param wakeKey
	 *            the wake key
	 */
	void wake(String wakeKey);
}
