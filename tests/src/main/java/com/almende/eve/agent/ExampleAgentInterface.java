/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.agent;

import com.almende.eve.transform.rpc.annotation.Name;

/**
 * The Interface ExampleAgentInterface.
 */
public interface ExampleAgentInterface {
	
	/**
	 * Hello world.
	 * 
	 * @param message
	 *            the message
	 * @return the string
	 */
	String helloWorld(@Name("message") String message);
}
