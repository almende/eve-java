/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.demo;

import java.io.IOException;
import java.net.URI;

import com.almende.eve.agent.Agent;
import com.almende.eve.protocol.jsonrpc.annotation.Access;
import com.almende.eve.protocol.jsonrpc.annotation.AccessType;
import com.almende.eve.protocol.jsonrpc.annotation.Name;
import com.almende.eve.protocol.jsonrpc.annotation.Sender;
import com.almende.eve.protocol.jsonrpc.formats.Params;
import com.almende.util.callback.AsyncCallback;

/**
 * The Class MySecondAgent.
 */
@Access(AccessType.PUBLIC)
public class MySecondAgent extends Agent {

	/**
	 * A message to allow the other agent to return data to me.
	 *
	 * @param returnMessage
	 *            the return message
	 * @param senderUrl
	 *            the sender url
	 */
	public void result(@Name("returnMessage") String returnMessage,
			@Sender String senderUrl) {
		System.out.println("Received a message from " + senderUrl + ": "
				+ returnMessage);
	}

	/**
	 * Run the HelloWorld examples!
	 *
	 * @return The text results of the first two calls.
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public String getHelloWorld() throws IOException {

		// First obtain the helloWorld result synchronous, without parameters.
		String result = callSync(
				URI.create("http://localhost:8081/agents/helloWorld/"),
				"helloWorld", null, String.class);

		// Secondly obtain the echo result synchronous, with a message
		// parameter.
		Params params = new Params();
		params.add("message", "Hi there!");
		result += callSync(
				URI.create("http://localhost:8081/agents/helloWorld/"), "echo",
				params, String.class);

		// Third example: obtain the echo result asynchronously through a
		// callback.
		call(URI.create("http://localhost:8081/agents/helloWorld/"), "echo",
				params, new AsyncCallback<String>() {

					@Override
					public void onSuccess(String returnMessage) {
						System.out.println("Received a message on callback: "
								+ returnMessage);
					}

					@Override
					public void onFailure(Exception exception) {
						System.err.println("Failed to receive result");
						exception.printStackTrace();
					}
				});

		// last example: obtain the echo result asynchronously by having the
		// other agent call one of my methods.
		call(URI.create("http://localhost:8081/agents/helloWorld/"),
				"asyncEcho", params);

		return result;
	}
}
