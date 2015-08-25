/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.transport;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import com.almende.eve.capabilities.Capability;
import com.almende.eve.capabilities.handler.Handler;
import com.almende.util.TypeUtil;
import com.almende.util.callback.AsyncCallback;

/**
 * The Interface Transport.
 */
public interface Transport extends Capability {

	/**
	 * The Constant TYPEUTIL.
	 */
	TypeUtil<Handler<Receiver>>	TYPEUTIL	= new TypeUtil<Handler<Receiver>>() {};

	/**
	 * Send a message to an other agent.
	 * 
	 * @param receiverUri
	 *            the receiver url
	 * @param message
	 *            the message
	 * @param tag
	 *            the tag
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @oaran receiverUrl
	 */
	<T> void send(final URI receiverUri, final String message, final String tag, final AsyncCallback<T> callback)
			throws IOException;

	/**
	 * Send bytes to an other agent. String based transports
	 * may need to encode these bytes to base64. (e.g. through
	 * org.apache.commons.codec.binary.Base64)
	 * 
	 * @param receiverUri
	 *            the receiver url
	 * @param message
	 *            the message
	 * @param tag
	 *            the tag
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @oaran receiverUrl
	 */
	<T> void send(final URI receiverUri, final byte[] message, final String tag, final AsyncCallback<T> callback)
			throws IOException;

	/**
	 * Send bytes to an other agent. String based transports
	 * may need to encode these bytes to base64. (e.g. through
	 * org.apache.commons.codec.binary.Base64)
	 *
	 * @param receiverUri
	 *            the receiver uri
	 * @param message
	 *            the message
	 * @param tag
	 *            the tag
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	<T> void send(final URI receiverUri, final Object message, final String tag, final AsyncCallback<T> callback)
			throws IOException;

	/**
	 * (re)Connect this url (if applicable for this transport type).
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	void connect() throws IOException;

	/**
	 * Disconnect transport.
	 */
	void disconnect();

	/**
	 * Gets the receive handler.
	 * 
	 * @return the handler
	 */
	Handler<Receiver> getHandle();

	/**
	 * Gets the address of this transport instance.
	 * 
	 * @return the address
	 */
	URI getAddress();

	/**
	 * Get the outbound protocols supported by this transport.
	 * 
	 * @return protocols
	 */
	List<String> getProtocols();

}
