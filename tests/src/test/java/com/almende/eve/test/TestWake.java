/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.test;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.net.URISyntaxException;

import junit.framework.TestCase;

import org.junit.Test;

import com.almende.eve.agent.Agent;
import com.almende.eve.agent.AgentConfig;
import com.almende.eve.agent.MyAgent;
import com.almende.eve.instantiation.InstantiationServiceConfig;
import com.almende.eve.state.file.FileStateConfig;
import com.almende.eve.transport.http.DebugServlet;
import com.almende.eve.transport.http.HttpTransportConfig;
import com.almende.util.callback.AsyncCallback;
import com.almende.util.jackson.JOM;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class TestWake.
 */
public class TestWake extends TestCase {
	final Ret called = new Ret();
	
	class Ret{
		boolean value = false;
	}
	
	/**
	 * Test wake example.
	 */
	@Test
	public void testWake(){
		
		final AgentConfig config = new AgentConfig("testWakeAgent");
		
		//First we need to setup the WakeService: (Either keep a global pointer to the wake service, or obtain it again through the same configuration)
		final InstantiationServiceConfig isConfig = new InstantiationServiceConfig();
		final FileStateConfig stateconfig = new FileStateConfig();
		stateconfig.setPath(".wakeservices");
		stateconfig.setId("testWakeService");
		isConfig.setState(stateconfig);
		
		config.setInstantiationService(isConfig);
		
		
		final HttpTransportConfig transConfig = new HttpTransportConfig();
		transConfig.setServletUrl("http://localhost:8080/agents/");
		transConfig.setServletLauncher("JettyLauncher");
		transConfig.setServletClass(DebugServlet.class.getName());
		final ObjectNode jettyParms = JOM.createObjectNode();
		jettyParms.put("port", 8080);
		transConfig.set("jetty", jettyParms);
		config.addTransport(transConfig);
		
		// Now create a WakeAble Agent
		WeakReference<Agent> test = new WeakReference<Agent>(new MyAgent(config));
		
		//after a while the agent is unloaded:
		System.gc();
		System.gc();
		try {
			Thread.sleep(1000);
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}
		System.gc();
		System.gc();
		
		// By now the agent should be unloaded:
		assertNull(test.get());
		
		//Now some other agent calls the agent:
		new Agent("other",null){
			public void test(){
				try {
					call(new URI("local:testWakeAgent"),"helloWorld",null,new AsyncCallback<String>(){

						@Override
						public void onSuccess(String result) {
							called.value=true;
						}

						@Override
						public void onFailure(Exception exception) {
							fail();
						}
						
					});
				} catch (IOException e) {
					e.printStackTrace();
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
			}
		}.test();
		
		//Give the async call time to reach the agent (and wake the agent in the proces);
		try {
			Thread.sleep(1000);
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}
		assertTrue(called.value);
	}
	
}
