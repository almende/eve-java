/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.transform;

import java.net.URI;

import com.almende.eve.capabilities.Capability;

/**
 * The Interface Transform.
 */
public interface Transform extends Capability {
	
	/**
	 * Transform.
	 *
	 * @param msg the msg
	 * @param senderUrl the sender url
	 * @return the modified msg or null if no chaining is allowed
	 */
	Object inbound(final Object msg, final URI senderUrl);

	/**
	 * Transform.
	 *
	 * @param msg the msg or null if this transforms creates the content
	 * @param recipientUrl the recipient url
	 * @return the modified msg
	 */
	Object outbound(final Object msg, final URI recipientUrl);

}
