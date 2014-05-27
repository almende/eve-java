/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.test;

import java.io.IOException;
import java.net.URISyntaxException;

import junit.framework.TestCase;

import org.junit.Test;

import com.almende.eve.agent.AgentConfig;
import com.almende.eve.agent.CalcAgent;
import com.almende.eve.agent.ExampleAgent;
import com.almende.eve.transport.http.HttpTransportConfig;
import com.almende.util.jackson.JOM;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class TestAgents.
 */
public class TestDebug extends TestCase {
	
	/**
	 * Test agents.
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws URISyntaxException
	 *             the URI syntax exception
	 * @throws InterruptedException
	 *             the interrupted exception
	 */
	@Test
	public void testDebug() throws IOException, URISyntaxException,
			InterruptedException {
		
		final HttpTransportConfig transportConfig = new HttpTransportConfig();
		transportConfig.setServletUrl("http://localhost:8080/agents/");
		transportConfig
				.setServletClass("com.almende.eve.transport.http.debug.DebugServlet");
		transportConfig.setDoAuthentication(false);
		
		transportConfig.setServletLauncher("JettyLauncher");
		final ObjectNode jettyParms = JOM.createObjectNode();
		jettyParms.put("port", 8080);
		transportConfig.put("jetty", jettyParms);
		
		final AgentConfig config = new AgentConfig("example");
		config.setTransport(transportConfig);
		
		final ExampleAgent agent = new ExampleAgent();
		agent.setConfig(config);
		
		final AgentConfig config2 = new AgentConfig("calc");
		config2.setTransport(transportConfig);
		
		final CalcAgent agent2 = new CalcAgent();
		agent2.setConfig(config2);
		
		synchronized (this) {
			this.wait();
		}
	}
	
}
