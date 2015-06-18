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

/**
 * The Class MyFirstAgent.  This class extends Agent to obtain agent capabilities. 
 */
@Access(AccessType.PUBLIC)
public class MyFirstAgent extends Agent {
	
	/**
	 * Returns with "Hello world!".
	 *
	 * @return the string
	 */
	public String helloWorld(){
		return "Hello World!";
	}
	
	/**
	 * Echo, this method answers the echo request directly.
	 *
	 * @param message 
	 *            the message
	 * @return the string
	 */
	public String echo(@Name("message") String message){
		return "You said:"+message;
	}
	
	/**
	 * Async echo, this method answers the echo request through a separate, asynchronous call.
	 *
	 * @param message
	 *            the message
	 * @param senderUrl
	 *            the sender url
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void asyncEcho(@Name("message") String message,
			@Sender URI senderUrl) throws IOException {
		
		String returnMessage = "You said:" + message;
		final Params params = new Params();
		params.put("returnMessage", returnMessage);

		call(senderUrl, "result", params);
	}

}
