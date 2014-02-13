/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.test;

import java.net.URI;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.junit.Test;

import com.almende.eve.agent.AgentHost;
import com.almende.eve.agent.callback.AsyncCallback;
import com.almende.eve.rpc.jsonrpc.JSONRequest;
import com.almende.eve.rpc.jsonrpc.JSONResponse;
import com.almende.eve.rpc.jsonrpc.jackson.JOM;
import com.almende.eve.scheduler.ClockSchedulerFactory;
import com.almende.eve.state.FileStateFactory;
import com.almende.test.agents.TestAgent;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class TestProxy.
 */
public class TestCall extends TestCase {
	private static final Logger	LOG	= Logger.getLogger(TestCall.class.getName());
	
	/**
	 * Test proxy.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void testCall() throws Exception {
		// Create TestAgent according to TestInterface
		final AgentHost host = AgentHost.getInstance();
		final FileStateFactory stateFactory = new FileStateFactory(".eveagents");
		host.setStateFactory(stateFactory);
		host.setSchedulerFactory(new ClockSchedulerFactory(host,
				new HashMap<String, Object>()));
		if (host.hasAgent("TestAgent")) {
			host.deleteAgent("TestAgent");
		}
		final TestAgent agent = host.createAgent(TestAgent.class, "TestAgent");
		final AsyncCallback<JSONResponse> callback = new AsyncCallback<JSONResponse>() {
			
			@Override
			public void onSuccess(JSONResponse result) {
				LOG.info("received result:" + result);
			}
			
			@Override
			public void onFailure(Exception exception) {
				LOG.log(Level.WARNING, "Failure:", exception);
				fail("Failure:" + exception.getLocalizedMessage());
			}
		};
		
		agent.send(new JSONRequest("helloWorld", (ObjectNode) JOM.getInstance()
				.readTree("{\"msg\":\"hi there!\"}")), URI
				.create("local:TestAgent"), callback, null);
		
		agent.send(
				new JSONRequest("helloWorld2", (ObjectNode) JOM.getInstance()
						.readTree("{\"msg1\":\"hi there!\",\"msg2\":\"Bye!\"}")),
				URI.create("local:TestAgent"), callback, null);
		
		agent.send(
				new JSONRequest("scheduler.getTasks", JOM.createObjectNode()),
				URI.create("local:TestAgent"), callback, null);
		
		agent.send(new JSONRequest("testVoid", JOM.createObjectNode()),
				URI.create("local:TestAgent"), callback, null);
		
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
		}
	}
	
}
