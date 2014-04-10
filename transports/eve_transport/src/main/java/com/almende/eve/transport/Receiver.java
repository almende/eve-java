/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.transport;

import java.net.URI;

/**
 * The Interface Receiver, implementations will be able to receive messages from Eve's transport service.
 */
public interface Receiver {
	/**
	 * This is the primary receive method of the agent. All incoming messages
	 * are delivered through this method.
	 * 
	 * @param msg
	 *            the message, mostly a string containing JSON-RPC. Can be other
	 *            types as well in various situations.
	 * @param senderUrl
	 *            the sender url
	 * @param tag
	 *            If set, this is a tagged message, meaning any replies should
	 *            also carry this tag. (see send())
	 */
	void receive(Object msg, URI senderUrl, String tag);
	
}
