/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.test;

import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.junit.Test;

import com.almende.eve.agent.AgentHost;
import com.almende.eve.agent.callback.AsyncCallback;
import com.almende.eve.rpc.jsonrpc.JSONRequest;
import com.almende.eve.rpc.jsonrpc.JSONResponse;
import com.almende.test.agents.Test2Agent;
import com.almende.test.agents.Test2AgentInterface;

/**
 * The Class TestAgentHost.
 */
public class TestAgentHost extends TestCase {
	private static final Logger	LOG	= Logger.getLogger("testAgentHost");
	
	/**
	 * Test agent call.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void testAgentCall() throws Exception {
		final String TESTAGENT = "hostTestAgent";
		
		final AgentHost host = AgentHost.getInstance();
		
		if (host.hasAgent(TESTAGENT)) {
			host.deleteAgent(TESTAGENT);
		}
		host.createAgent(Test2Agent.class, TESTAGENT);
		
		LOG.warning("Creating agentProxy");
		Test2AgentInterface agent = host.createAgentProxy(null,
				URI.create("local:" + TESTAGENT), Test2AgentInterface.class);
		LOG.warning("Starting agentProxy");
		Double res = agent.add(3.1, 4.2);
		// result not exact due to intermediate binary representation
		assertEquals(new Double(7.300000000000001), res);
		res = agent.multiply(3.1, 4.2);
		assertEquals(new Double(13.020000000000001), res);
		
		agent = host.createAgentProxy(null,
				URI.create("https://localhost:8443/agents/" + TESTAGENT + "/"),
				Test2AgentInterface.class);
		
		LOG.warning("checking local https call 1:");
		res = agent.add(3.1, 4.2);
		// result not exact due to intermediate binary representation
		assertEquals(new Double(7.300000000000001), res);
		
		LOG.warning("checking local https call 2:");
		res = agent.multiply(3.1, 4.2);
		assertEquals(new Double(13.020000000000001), res);
		
		agent = (Test2AgentInterface) host.getAgent(TESTAGENT);
		JSONRequest request = new JSONRequest("someMethod", null);
		AsyncCallback<JSONResponse> callback = new AsyncCallback<JSONResponse>() {
			
			@Override
			public void onSuccess(JSONResponse result) {
				LOG.log(Level.WARNING, "onSuccess called as expected:"+result);
				if (result.getError() != null){
					LOG.log(Level.WARNING, "contained error:",result.getError());
				}
				
			}
			
			@Override
			public void onFailure(Exception e) {
				LOG.log(Level.WARNING, "onFailure called", e);
			}
		};
		agent.send(request,
				URI.create("https://localhost:8443/agents/nonExisting"),
				callback, null);
		
		Thread.sleep(1000);
		host.deleteAgent(TESTAGENT);
	}
	/**
	 * @throws Exception
	 */
	@Test
	public void testAgentMissing() throws Exception {
		final AgentHost host = AgentHost.getInstance();
		
		final String TESTAGENT = "missingAgentTestAgent";

		if (host.hasAgent(TESTAGENT)) {
			host.deleteAgent(TESTAGENT);
		}
		Test2AgentInterface agent = host.createAgent(Test2Agent.class, TESTAGENT);

		JSONRequest request = new JSONRequest("someMethod", null);
		AsyncCallback<JSONResponse> callback = new AsyncCallback<JSONResponse>() {
			
			@Override
			public void onSuccess(JSONResponse result) {
				LOG.log(Level.WARNING, "onSuccess called as expected:"+result);
				if (result.getError() != null){
					LOG.log(Level.WARNING, "contained error:",result.getError());
				}
				
			}
			
			@Override
			public void onFailure(Exception e) {
				LOG.log(Level.WARNING, "onFailure called", e);
			}
		};
		agent.send(request,
				URI.create("http://localhost:8080/agents/nonExisting"),
				callback, null);
		
		Thread.sleep(1000);
		host.deleteAgent(TESTAGENT);
	}
}
