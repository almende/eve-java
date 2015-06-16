/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.test;

import java.util.logging.Logger;

import junit.framework.TestCase;

import org.junit.Test;

import com.almende.eve.agent.AgentConfig;
import com.almende.eve.agent.AgentProxyFactory;
import com.almende.eve.agent.ExampleAgent;
import com.almende.eve.agent.ExampleAgentInterface;
import com.almende.eve.transport.http.HttpTransportConfig;
import com.almende.util.URIUtil;
import com.almende.util.jackson.JOM;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class TestRpc.
 */
public class TestProxy extends TestCase {
	private static final Logger	LOG	= Logger.getLogger(TestProxy.class
											.getName());

	/**
	 * Test me.
	 */
	@Test
	public void testProxy() {
		final HttpTransportConfig transportConfig = HttpTransportConfig
				.create();
		transportConfig.setServletUrl("http://localhost:8081/agents/");

		transportConfig.setServletLauncher("JettyLauncher");
		final ObjectNode jettyParms = JOM.createObjectNode();
		jettyParms.put("port", 8081);
		transportConfig.set("jetty", jettyParms);

		final AgentConfig config = AgentConfig.create("example");
		config.addTransport(transportConfig);

		final ExampleAgent agent = new ExampleAgent();
		agent.setConfig(config);

		final ExampleAgentInterface proxy = AgentProxyFactory.genProxy(agent,
				URIUtil.create("http://localhost:8081/agents/example"),
				ExampleAgentInterface.class);
		LOG.warning("Proxy got reply:" + proxy.helloWorld("Hi there"));

		assertEquals(proxy.helloWorld("Hi there"), "You said:Hi there");
		proxy.doSomething();

		assertNull(proxy.doMore());

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {

		}
	}
}
