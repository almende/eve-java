/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.capabilities.wake;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Interface Wakeable. Objects implementing this interface can be woken by
 * the WakeService.
 */
public interface Wakeable {
	
	/**
	 * Wake.
	 * 
	 * @param wakeKey
	 *            the wake key
	 * @param params
	 *            the params
	 * @param onBoot
	 *            the onBoot flag, set to true if this wake came from
	 *            WakeService.boot(). False in all other cases.
	 */
	void wake(String wakeKey, ObjectNode params, boolean onBoot);
}
