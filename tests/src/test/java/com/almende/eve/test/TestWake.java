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
import com.almende.eve.agent.MyAgent;
import com.almende.eve.capabilities.wake.WakeService;
import com.almende.eve.capabilities.wake.WakeServiceBuilder;
import com.almende.eve.capabilities.wake.WakeServiceConfig;
import com.almende.eve.state.file.FileStateConfig;
import com.almende.util.callback.AsyncCallback;

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
		
		//First we need to setup the WakeService: (Either keep a global pointer to the wake service, or obtain it again through the same configuration)
		final WakeServiceConfig config = new WakeServiceConfig();
		final FileStateConfig stateconfig = new FileStateConfig();
		stateconfig.setPath(".wakeservices");
		stateconfig.setId("testWakeService");
		config.setState(stateconfig);
				
		final WakeService ws = 
			new WakeServiceBuilder()
			.withConfig(config)
			.build();

		// Now create a WakeAble Agent
		WeakReference<Agent> test = new WeakReference<Agent>(new MyAgent("testWakeAgent", ws));
		
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
