/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.agent;

import java.io.IOException;
import java.net.URI;

import com.almende.eve.transform.rpc.annotation.Access;
import com.almende.eve.transform.rpc.annotation.AccessType;
import com.almende.eve.transform.rpc.annotation.Name;
import com.almende.util.callback.AsyncCallback;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class ExampleAgent.
 */
@Access(AccessType.PUBLIC)
public class ExampleAgent extends WakeableAgent implements
		ExampleAgentInterface {
	
	/**
	 * Hello world.
	 * 
	 * @param message
	 *            the message
	 * @return the string
	 */
	@Override
	public String helloWorld(@Name("message") final String message) {
		return "You said:" + message;
	}
	
	/**
	 * Public version of send.
	 * 
	 * @param <T>
	 *            the generic type
	 * @param url
	 *            the url
	 * @param method
	 *            the method
	 * @param params
	 *            the params
	 * @param callback
	 *            the callback
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public <T> void pubSend(final URI url, final String method,
			final ObjectNode params, final AsyncCallback<T> callback)
			throws IOException {
		super.call(url, method, params, callback);
	}
	
	/**
	 * Public version of sendSync.
	 * 
	 * @param <T>
	 *            the generic type
	 * @param url
	 *            the url
	 * @param method
	 *            the method
	 * @param params
	 *            the params
	 * @return the t
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public <T> T pubSendSync(final URI url, final String method,
			final ObjectNode params) throws IOException {
		return super.callSync(url, method, params);
	}
}
