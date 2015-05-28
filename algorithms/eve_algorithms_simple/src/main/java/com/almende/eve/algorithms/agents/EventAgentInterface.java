/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.algorithms.agents;

import com.almende.eve.algorithms.EventBus;
import com.almende.eve.protocol.jsonrpc.annotation.Access;
import com.almende.eve.protocol.jsonrpc.annotation.AccessType;
import com.almende.eve.protocol.jsonrpc.annotation.Name;
import com.almende.eve.protocol.jsonrpc.annotation.Namespace;

/**
 * The Class EventAgent.
 */
interface EventAgentInterface extends NodeAgentInterface {

	/**
	 * Send event.
	 *
	 * @param message
	 *            the message
	 * @param expiryAge
	 *            the expiry age
	 */
	void sendEvent(String message, long expiryAge);

	/**
	 * Receive event.
	 *
	 * @param message
	 *            the message
	 */
	@Access(AccessType.PUBLIC)
	void receiveEvent(@Name("message") String message);

	/**
	 * Gets the event bus.
	 *
	 * @return the event bus
	 */
	@Namespace("event")
	EventBus getEventBus();
}
