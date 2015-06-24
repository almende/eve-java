/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.algorithms.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.junit.Test;

import com.almende.eve.agent.AgentConfig;
import com.almende.eve.algorithms.Edge;
import com.almende.eve.algorithms.agents.NodeAgent;
import com.almende.eve.algorithms.test.agents.TestLinkedListAgent;
import com.almende.eve.protocol.jsonrpc.formats.Params;
import com.almende.util.jackson.JOM;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author ludo
 *         The Class TestNetwork.
 */
public class TestGraph extends TestCase {
	private static final Logger	LOG	= Logger.getLogger(TestGraph.class
											.getName());

	/**
	 * Write visGraph.
	 *
	 * @param agents
	 *            the agents
	 * @return the string
	 */
	private String writeVisGraph(List<NodeAgent> agents) {
		final ObjectNode result = JOM.createObjectNode();
		final ArrayNode nodes = JOM.createArrayNode();
		final ArrayNode edges = JOM.createArrayNode();

		for (NodeAgent agent : agents) {
			final ObjectNode node = JOM.createObjectNode();
			node.put("id", agent.getId());
			node.put("label", agent.getId());
			nodes.add(node);
			for (Edge edge : agent.getGraph().getEdges()) {
				final ObjectNode edgeNode = JOM.createObjectNode();
				edgeNode.put("from", agent.getId());
				edgeNode.put("to",
						edge.getAddress().toASCIIString().replace("local:", ""));
				edges.add(edgeNode);
			}
		}

		result.set("nodes", nodes);
		result.set("edges", edges);
		return result.toString();
	}

	/**
	 * Test keys.
	 *
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void testSFNGraph() throws IOException {
		int nofNodes = 100;

		// Generate X agent scalefree network:
		// add M connections per step
		// start at random node, do the algo
		AgentConfig config = AgentConfig.create();
		config.setClassName(NodeAgent.class.getName());

		List<NodeAgent> agents = new ArrayList<NodeAgent>(nofNodes);
		for (int i = 0; i < nofNodes; i++) {
			NodeAgent agent = new NodeAgent("" + i, config);
			agents.add(agent);
			agent.addNode2SFN("SFN");
		}

		LOG.warning(writeVisGraph(agents));
	}

	/**
	 * Test linked list.
	 *
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void testLinkedList() throws IOException {
		final TestLinkedListAgent A = new TestLinkedListAgent("A", 3);
		final TestLinkedListAgent X = new TestLinkedListAgent("X", 2);

		final TestLinkedListAgent B = new TestLinkedListAgent("B", 5);
		final TestLinkedListAgent C = new TestLinkedListAgent("C", 8);

		Params params = new Params();
		params.add("value", X.getValue());
		X.getGraph().llinsert("X", params, B.getUrls().get(0));

		params.add("value", C.getValue());
		C.getGraph().llinsert("X", params, B.getUrls().get(0));

		params.add("value", A.getValue());
		A.getGraph().llinsert("X", params, C.getUrls().get(0));

		LOG.warning("X:" + JOM.getInstance().writeValueAsString(X.getGraph()));
		LOG.warning("A:" + JOM.getInstance().writeValueAsString(A.getGraph()));
		LOG.warning("B:" + JOM.getInstance().writeValueAsString(B.getGraph()));
		LOG.warning("C:" + JOM.getInstance().writeValueAsString(C.getGraph()));

		A.getGraph().llswap("X", false, params);
		A.getGraph().llswap("X", false, params);
		// A.getGraph().llswap("X", false, params);

		LOG.warning("X:" + JOM.getInstance().writeValueAsString(X.getGraph()));
		LOG.warning("A:" + JOM.getInstance().writeValueAsString(A.getGraph()));
		LOG.warning("B:" + JOM.getInstance().writeValueAsString(B.getGraph()));
		LOG.warning("C:" + JOM.getInstance().writeValueAsString(C.getGraph()));

	}
}
