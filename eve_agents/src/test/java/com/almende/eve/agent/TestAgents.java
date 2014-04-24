/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.agent;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.junit.Test;

import com.almende.util.callback.AsyncCallback;
import com.almende.util.jackson.JOM;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class TestAgents.
 */
public class TestAgents extends TestCase {
	private static final Logger	LOG	= Logger.getLogger(TestAgents.class
											.getName());
	
	/**
	 * Test agents.
	 * @throws URISyntaxException 
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	@Test
	public void testAgent() throws IOException, URISyntaxException, InterruptedException {
		ObjectNode config = JOM.createObjectNode();
		final ObjectNode transportConfig = JOM.createObjectNode();
		transportConfig.put("class", "com.almende.eve.transport.http.HttpService");
		transportConfig.put("url", "http://localhost:8080/agents/");
		transportConfig.put("servlet_launcher", "JettyLauncher");
		final ObjectNode jettyParms = JOM.createObjectNode();
		jettyParms.put("port", 8080);
		transportConfig.put("jetty", jettyParms);
		
		transportConfig.put("id", "example");
		transportConfig.put("authentication", true);
		config.put("transport", transportConfig);
		
		Agent agent = new ExampleAgent();
		agent.setConfig(config);
		
		ObjectNode callParams = JOM.createObjectNode();
		callParams.put("message", "Hello world!");
		
		agent.sendAsync(new URI("http://localhost:8080/agents/example"), "helloWorld",
				callParams, new AsyncCallback<String>() {
					
					@Override
					public void onSuccess(String result) {
						LOG.warning("Received:'" + result + "'");
					}
					
					@Override
					public void onFailure(Exception exception) {
						LOG.log(Level.SEVERE, "", exception);
						fail();
					}
					
				}, JOM
				.getTypeFactory().constructType(String.class));
		
		//Give connection time to complete:
		Thread.sleep(1000);
	}
	
}
