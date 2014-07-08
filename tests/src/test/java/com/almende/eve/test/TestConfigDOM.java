/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.junit.Test;

import com.almende.eve.agent.AgentBuilder;
import com.almende.eve.agent.AgentConfig;
import com.almende.eve.agent.ExampleAgent;
import com.almende.eve.capabilities.Config;
import com.almende.eve.config.YamlReader;
import com.almende.util.callback.AsyncCallback;
import com.almende.util.jackson.JOM;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class TestConfigDOM.
 */
public class TestConfigDOM extends TestCase {
	private static final Logger	LOG	= Logger.getLogger(TestConfigDOM.class
											.getName());
	
	/**
	 * Test agents from DOM.
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void testDOM() throws IOException {
		// First obtain the configuration:
		final Config config = YamlReader.load(
				new FileInputStream(new File("target/test-classes/test.yaml")))
				.expand();
		
		final ArrayNode agents = (ArrayNode) config.get("agents");
		ExampleAgent newAgent = null;
		for (final JsonNode agent : agents) {
			final AgentConfig agentConfig = new AgentConfig((ObjectNode) agent);
			newAgent = (ExampleAgent) new AgentBuilder().with(agentConfig)
					.build();
			LOG.info("Created agent:" + newAgent.getId());
		}
		final ObjectNode params = JOM.createObjectNode();
		params.put("message", "Hi There!");
		newAgent.pubSend(URI.create("local:example"), "helloWorld", params,
				new AsyncCallback<String>() {
					
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
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}
	}
	
}
