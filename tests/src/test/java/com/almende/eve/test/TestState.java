/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.test;

import junit.framework.TestCase;

import org.junit.Test;

import com.almende.eve.capabilities.CapabilityBuilder;
import com.almende.eve.state.State;
import com.almende.eve.state.StateBuilder;
import com.almende.eve.state.couch.CouchState;
import com.almende.eve.state.couch.CouchStateBuilder;
import com.almende.eve.state.couch.CouchStateConfig;
import com.almende.eve.state.file.FileStateBuilder;
import com.almende.eve.state.memory.MemoryStateConfig;
import com.almende.eve.state.mongo.MongoState;
import com.almende.eve.state.mongo.MongoStateBuilder;
import com.almende.eve.state.mongo.MongoStateConfig;
import com.almende.eve.state.redis.RedisStateConfig;
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
		assertNotNull(myState2);
		assertNotNull(myState);
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
		final MemoryStateConfig params = new MemoryStateConfig();
		params.setId("TestAgent");
		
		State myState = new CapabilityBuilder<State>().withConfig(params).build();
		State myState2 = new StateBuilder().withConfig(params).build();
		runTest(myState, myState2);
		
		myState = new CapabilityBuilder<State>().withConfig(params).build();
		myState2 = new StateBuilder().withConfig(params).build();
		runTest(myState, myState2);
	}
	
	/**
	 * Test file state.
	 */
	@Test
	public void testFileState() {
		ObjectNode params = JOM.createObjectNode();
		params.put("class", FileStateBuilder.class.getName());
		params.put("json", false);
		params.put("id", "TestAgent");
		
		State myState = new CapabilityBuilder<State>().withConfig(params).build();
		State myState2 = new StateBuilder().withConfig(params).build();		
		runTest(myState, myState2);
		myState = new CapabilityBuilder<State>().withConfig(params).build();
		myState2 = new StateBuilder().withConfig(params).build();
		runTest(myState, myState2);
		
		params = params.deepCopy();
		params.put("json", true);
		myState = new CapabilityBuilder<State>().withConfig(params).build();
		myState2 = new StateBuilder().withConfig(params).build();
		runTest(myState, myState2);
		
	}
	
	
	/**
	 * Test file state.
	 */
	@Test
	public void testRedisState() {
		RedisStateConfig params = new RedisStateConfig();
		params.setDbId(2);
		params.setId("TestAgent");
		
		State myState = new CapabilityBuilder<State>().withConfig(params).build();
		State myState2 = new StateBuilder().withConfig(params).build();		
		runTest(myState, myState2);
		
		myState = new CapabilityBuilder<State>().withConfig(params).build();
		myState2 = new StateBuilder().withConfig(params).build();
		runTest(myState, myState2);
	}
	
	/**
	 * Test me.
	 */
	@Test
	public void testMongoState() {
		final MongoStateConfig params = new MongoStateConfig();
		params.setId("TestAgent");
		
		State myState = new CapabilityBuilder<State>().withConfig(params).build();
		MongoState myState2 = new MongoStateBuilder().withConfig(params).build();
		runTest(myState, myState2);
		
		myState = new CapabilityBuilder<State>().withConfig(params).build();
		myState2 = new MongoStateBuilder().withConfig(params).build();
		runTest(myState, myState2);
	}
	
	/**
	 * Test me.
	 */
	@Test
	public void testCouchState() {
		final CouchStateConfig params = new CouchStateConfig();
		params.setId("TestAgent");
		params.setUrl("http://localhost:5984");
		
		State myState = new CapabilityBuilder<State>().withConfig(params).build();
		CouchState myState2 = new CouchStateBuilder().withConfig(params).build();
		runTest(myState, myState2);
		
		myState = new CapabilityBuilder<State>().withConfig(params).build();
		myState2 = new CouchStateBuilder().withConfig(params).build();
		runTest(myState, myState2);
	}
}
