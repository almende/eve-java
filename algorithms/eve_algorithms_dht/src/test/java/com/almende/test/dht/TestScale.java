/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.test.dht;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.BitSet;

import junit.framework.TestCase;

import org.junit.Test;

import com.almende.dht.Key;
import com.almende.util.jackson.JOM;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class TestAgent.
 */
public class TestScale extends TestCase {

	private static final int	NOFNODES	= 1000;

	/**
	 * Test a large nof nodes.
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws URISyntaxException
	 *             the URI syntax exception
	 * @throws InterruptedException
	 *             the interrupted exception
	 */
	@Test
	public void testScale() throws IOException, URISyntaxException,
			InterruptedException {
		DHTAgent[] agents = new DHTAgent[NOFNODES];
		
		DHTAgent agent = new DHTAgent("agent_0");
		agents[0] = agent;
		for (int i = 1; i < NOFNODES; i++) {
			System.out.print("Created node:agent_"+i+"\r");
			DHTAgent next = new DHTAgent("agent_" + i);
			try {
				next.getDht().join(agent.asNode());
			} catch (NullPointerException e) {
				System.err.println("NPE at:" + i);
				throw e;
			}
			agent = next;
			agents[i] = agent;
		}
		final Key key = Key.fromString("Hello world");
		final ObjectNode value = JOM.createObjectNode();
		value.put("Hello:","world!");
		agents[0].getDht().iterative_store_value(key,value);
		
		JsonNode result = agents[(int) Math.floor(Math.random()*NOFNODES)].getDht().iterative_find_value(Key.fromString("Hello world"), false);
		
		assertEquals(result,value);
		
		final int otherIdx =(int) Math.floor(Math.random()*NOFNODES); 
		JsonNode result2 = agents[otherIdx].getDht().iterative_find_value(Key.fromString("Hello world"), false);
		assertEquals(result2,value);

		final Key key2 = Key.fromString("Some other key");
		final ObjectNode value2 = JOM.createObjectNode();
		value2.put("Hello:","world2!");
		agents[0].getDht().iterative_store_value(key2,value2);
		
		JsonNode result3 = agents[(int) Math.floor(Math.random()*NOFNODES)].getDht().iterative_find_value(Key.fromString("Some other key"), false);
		
		assertEquals(result3,value2);
		
		JsonNode result4 = agents[otherIdx].getDht().iterative_find_value(Key.fromString("Hello world"), false);
		assertNotSame(result4,value2);
		assertEquals(result4,value);

		JsonNode result5 = agents[otherIdx].getDht().iterative_find_value(Key.fromString("Hello world"), false);
		assertEquals(result5,value);
		JsonNode result6 = agents[otherIdx].getDht().iterative_find_value(Key.fromString("Hello world!"), false);
		assertNotSame(result6,value);
		assertEquals(result6,JOM.createNullNode());
		
		final BitSet set = key2.getVal();
		set.set(10,!set.get(10));
		final Key key3 = new Key(set);
		final ObjectNode value3 = JOM.createObjectNode();
		value3.put("Hello:","world3!");
		agents[0].getDht().iterative_store_value(key3,value3);
		
		JsonNode result7 = agents[otherIdx+2].getDht().iterative_find_value(key3, false);
		assertEquals(result7,value3);
		
		int count=0;
		for (final DHTAgent a: agents){
			if (a.getDht().hasValues()){
				count++;
			}
		}
		System.out.println(count + " agents have some value stored.");
	}
}
