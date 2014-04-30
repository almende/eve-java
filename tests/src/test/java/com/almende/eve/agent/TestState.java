/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.agent;

import junit.framework.TestCase;

import org.junit.Test;

import com.almende.eve.capabilities.CapabilityFactory;
import com.almende.eve.state.State;
import com.almende.eve.state.StateFactory;
import com.almende.eve.state.couch.CouchStateConfig;
import com.almende.eve.state.mongo.MongoStateConfig;
import com.almende.util.jackson.JOM;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class TestState.
 */
public class TestState extends TestCase {
	
	/**
	 * Run test.
	 * 
	 * @param myState
	 *            the my state
	 * @param myState2
	 *            the my state2
	 */
	public void runTest(final State myState, final State myState2) {
		myState.put("msg", "Hi There!");
		assertEquals("Hi There!", myState.get("msg", String.class));
		assertEquals("Hi There!", myState2.get("msg", String.class));
		
		myState.delete();
		assertNull(myState.get("msg", String.class));
	}
	
	/**
	 * Test me.
	 */
	@Test
	public void testState() {
		final ObjectNode params = JOM.createObjectNode();
		params.put("class", "com.almende.eve.state.memory.MemoryStateService");
		params.put("id", "TestAgent");
		
		State myState = CapabilityFactory.get(params, null, State.class);
		State myState2 = StateFactory.getState(params);
		runTest(myState, myState2);
		
		myState = CapabilityFactory.get(params, null, State.class);
		myState2 = StateFactory.getState(params);
		runTest(myState, myState2);
	}
	
	/**
	 * Test file state.
	 */
	@Test
	public void testFileState() {
		ObjectNode params = JOM.createObjectNode();
		params.put("class", "com.almende.eve.state.file.FileStateService");
		params.put("json", false);
		params.put("id", "TestAgent");
		
		State myState = CapabilityFactory.get(params, null, State.class);
		State myState2 = StateFactory.getState(params);
		runTest(myState, myState2);
		myState = CapabilityFactory.get(params, null, State.class);
		myState2 = StateFactory.getState(params);
		runTest(myState, myState2);
		
		params = params.deepCopy();
		params.put("json", true);
		myState = CapabilityFactory.get(params, null, State.class);
		myState2 = StateFactory.getState(params);
		runTest(myState, myState2);
		
	}
	
	/**
	 * Test me.
	 */
	@Test
	public void testMongoState() {
		final MongoStateConfig config = new MongoStateConfig();
		config.setId("TestAgent");
		
		State myState = CapabilityFactory.get(config, null, State.class);
		State myState2 = StateFactory.getState(config);
		runTest(myState, myState2);
		
		myState = CapabilityFactory.get(config, null, State.class);
		myState2 = StateFactory.getState(config);
		runTest(myState, myState2);
	}
	
	/**
	 * Test me.
	 */
	@Test
	public void testCouchState() {
		final CouchStateConfig config = new CouchStateConfig();
		config.setId("TestAgent");
		config.setUrl("http://localhost:5984");
		
		State myState = CapabilityFactory.get(config, null, State.class);
		State myState2 = StateFactory.getState(config);
		runTest(myState, myState2);
		
		myState = CapabilityFactory.get(config, null, State.class);
		myState2 = StateFactory.getState(config);
		runTest(myState, myState2);
	}
}
