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

import com.almende.eve.capabilities.Config;
import com.almende.eve.transport.http.HttpTransportConfig;
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
	 * 
	 * @throws URISyntaxException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	@Test
	public void testAgent() throws IOException, URISyntaxException,
			InterruptedException {
		
		final HttpTransportConfig transportConfig = new HttpTransportConfig();
		transportConfig.setServletUrl("http://localhost:8080/agents/");
		transportConfig.setId("example");
		
		transportConfig.setServletLauncher("JettyLauncher");
		final ObjectNode jettyParms = JOM.createObjectNode();
		jettyParms.put("port", 8080);
		transportConfig.put("jetty", jettyParms);
		
		final Config config = new Config();
		config.put("transport", transportConfig);
		
		final Agent agent = new ExampleAgent();
		agent.setConfig(config);
		
		final ObjectNode callParams = JOM.createObjectNode();
		callParams.put("message", "Hello world!");
		
		agent.send(new URI("http://localhost:8080/agents/example"),
				"helloWorld", callParams, new AsyncCallback<String>() {
					
					@Override
					public void onSuccess(final String result) {
						LOG.warning("Received:'" + result + "'");
					}
					
					@Override
					public void onFailure(final Exception exception) {
						LOG.log(Level.SEVERE, "", exception);
						fail();
					}
					
				});

		LOG.warning("Sync received:'"+agent.sendSync(new URI("http://localhost:8080/agents/example"),
				"helloWorld", callParams)+"'");
	}
	
}
