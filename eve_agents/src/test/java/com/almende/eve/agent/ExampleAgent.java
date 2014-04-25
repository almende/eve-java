/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.agent;

import com.almende.eve.transform.rpc.annotation.Access;
import com.almende.eve.transform.rpc.annotation.AccessType;
import com.almende.eve.transform.rpc.annotation.Name;

/**
 * The Class ExampleAgent.
 */
@Access(AccessType.PUBLIC)
public class ExampleAgent extends Agent {
	
	/**
	 * Hello world.
	 * 
	 * @param message
	 *            the message
	 * @return the string
	 */
	public String helloWorld(@Name("message") final String message) {
		return "You said:" + message;
	}
}
