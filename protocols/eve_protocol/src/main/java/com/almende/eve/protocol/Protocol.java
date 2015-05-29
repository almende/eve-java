/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.protocol;

import com.almende.eve.capabilities.Capability;

/**
 * The Interface Protocol.
 */
public interface Protocol extends Capability {

	/**
	 * Handle inbound messages, converting them from the right protocols.
	 *
	 * @param msg
	 *            the msg
	 */
	void inbound(final Meta msg);

	/**
	 * Handle outbound messages, converting them into the right protocols.
	 *
	 * @param msg
	 *            the msg or null if this protocol creates the content (is a
	 *            source)
	 */
	void outbound(final Meta msg);
	
}
