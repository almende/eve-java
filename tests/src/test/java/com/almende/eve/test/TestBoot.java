/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.test;

import java.util.logging.Logger;

import junit.framework.TestCase;

import org.junit.Test;

import com.almende.eve.deploy.Boot;
import com.almende.eve.instantiation.InstantiationServiceConfig;
import com.almende.eve.state.file.FileStateConfig;
import com.almende.util.jackson.JOM;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class TestWake.
 */
public class TestBoot extends TestCase {
	private static final Logger	LOG	= Logger.getLogger(TestBoot.class.getName());

	/**
	 * Test boot: requires a testWakeService state, with a list of agents.
	 */
	@Test
	public void testBoot() {

		// This configuration normally comes from eve.yaml:
		final ObjectNode config = JOM.createObjectNode();
		final InstantiationServiceConfig instantiationConfig = InstantiationServiceConfig
				.create();
		final FileStateConfig state = FileStateConfig.create();
		state.setPath(".wakeservices");
		state.setId("testWakeService");
		instantiationConfig.setState(state);
		final ArrayNode services = JOM.createArrayNode();
		services.add(instantiationConfig);
		config.set("instantiationServices", services);

		// Basic boot action:
		Boot.boot(config);

		LOG.warning("Sleep for 20 seconds, allowing external XMPP call.");
		try {
			Thread.sleep(20000);
		} catch (final InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
