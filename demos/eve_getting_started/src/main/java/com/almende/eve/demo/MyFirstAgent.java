/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.demo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;

import com.almende.eve.agent.Agent;
import com.almende.eve.capabilities.Config;
import com.almende.eve.config.YamlReader;
import com.almende.eve.deploy.Boot;
import com.almende.eve.transform.rpc.annotation.Access;
import com.almende.eve.transform.rpc.annotation.AccessType;
import com.almende.eve.transform.rpc.annotation.Name;
import com.almende.eve.transform.rpc.annotation.Sender;
import com.almende.util.jackson.JOM;
import com.fasterxml.jackson.databind.node.ObjectNode;

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
			@Sender String senderUrl) throws IOException {
		
		String returnMessage = "You said:" + message;
		ObjectNode params = JOM.createObjectNode();
		params.put("returnMessage", returnMessage);

		call(URI.create(senderUrl), "result", params);
		
	}

	/**
	 * The main method, this method starts all agents from the configuration file.
	 * Usage:  java -jar executable.jar  eve.yaml
	 *
	 * @param args
	 *            the arguments
	 * @throws FileNotFoundException
	 *             the file not found exception
	 */
	public static void main(String[] args) throws FileNotFoundException {
		final Config configfile = YamlReader.load(
				new FileInputStream(new File(args[0]))).expand();
		Boot.boot(configfile);
	}

}
