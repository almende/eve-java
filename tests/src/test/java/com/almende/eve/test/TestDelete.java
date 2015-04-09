/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.test;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URISyntaxException;

import junit.framework.TestCase;

import org.junit.Test;

import com.almende.eve.agent.Agent;
import com.almende.eve.agent.AgentConfig;
import com.almende.eve.agent.ExampleAgent;
import com.almende.eve.transport.http.HttpTransportConfig;
import com.almende.util.jackson.JOM;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class TestAgents.
 */
public class TestDelete extends TestCase {

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
	public void testDelete() throws IOException, URISyntaxException,
			InterruptedException {

		HttpTransportConfig transportConfig = new HttpTransportConfig();
		transportConfig.setServletUrl("http://localhost:8080/agents/");

		transportConfig.setServletLauncher("JettyLauncher");
		ObjectNode jettyParms = JOM.createObjectNode();
		jettyParms.put("port", 8080);
		transportConfig.set("jetty", jettyParms);

		AgentConfig config = new AgentConfig("example");
		config.addTransport(transportConfig);

		ExampleAgent agent = new ExampleAgent();
		agent.setConfig(config);
		
		transportConfig = null;
		config = null;

		WeakReference<Agent> test = new WeakReference<Agent>(agent);
		agent.destroy();
		agent = null;

		System.gc();
		System.gc();
		try {
			Thread.sleep(1000);
		} catch (final InterruptedException e) {}
		System.gc();
		System.gc();

		assertNull(test.get());

//		LOG.warning("Sleeping for profiler connection.");
//		try {
//			Thread.sleep(20000);
//		} catch (final InterruptedException e) {}
	}
}
