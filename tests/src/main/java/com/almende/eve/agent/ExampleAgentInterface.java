/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.agent;

import com.almende.eve.agent.annotation.ProxyAsync;
import com.almende.eve.protocol.jsonrpc.annotation.Name;

/**
 * The Interface ExampleAgentInterface.
 */
@ProxyAsync
public interface ExampleAgentInterface {

	/**
	 * Hello world.
	 * 
	 * @param message
	 *            the message
	 * @return the string
	 */
	String helloWorld(@Name("message") String message);

	/**
	 * Do something.
	 */
	void doSomething();

	/**
	 * Do more, with an ignored return value.....
	 *
	 * @return the string
	 */
	@ProxyAsync
	String doMore();
}
