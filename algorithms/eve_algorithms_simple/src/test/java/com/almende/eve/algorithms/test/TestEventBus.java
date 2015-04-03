/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.algorithms.test;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.junit.Test;

import com.almende.eve.agent.AgentConfig;
import com.almende.eve.algorithms.Edge;
import com.almende.eve.algorithms.test.agents.EventAgent;
import com.almende.eve.algorithms.test.agents.NodeAgent;

/**
 * The Class TestEventBus.
 */
public class TestEventBus extends TestCase {
		
	/**
	 * Test events.
	 */
	@Test
	public void testEvents() {
		final AgentConfig config = new AgentConfig();
		config.setClassName(EventAgent.class.getName());
		
		final List<EventAgent> agents = new ArrayList<EventAgent>(10);
		for (int i = 0; i < 10; i++) {
			EventAgent agent = new EventAgent("" + i, config, null);
			agents.add(agent);
		}
		for (int i = 0; i < 10; i++) {
			NodeAgent agent = agents.get(i);
			for (int j = 0; j < 2; j++) {
				NodeAgent other = agents.get((i + j + 1) % 10);
				if (other.equals(agent))
					continue;
				agent.getGraph().addEdge(new Edge(other.getUrls().get(0), "SFN",null));
			}
		}
		EventAgent start = agents.get(0);
		for (int i=10; i< 100; i++ ){
			agents.add(new EventAgent("" + i, config,start.getUrls().get(0)));
		}
		start.sendEvent("Hi there!");
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
