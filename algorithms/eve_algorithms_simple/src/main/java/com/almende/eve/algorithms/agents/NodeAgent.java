/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.algorithms.agents;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.almende.eve.agent.Agent;
import com.almende.eve.algorithms.Graph;
import com.almende.eve.protocol.jsonrpc.annotation.Access;
import com.almende.eve.protocol.jsonrpc.annotation.AccessType;
import com.almende.eve.protocol.jsonrpc.annotation.Namespace;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class NodeAgent.
 */
@Access(AccessType.PUBLIC)
public class NodeAgent extends Agent implements NodeAgentInterface {
	private static final Logger	LOG					= Logger.getLogger(NodeAgent.class
															.getName());
	private Graph				graph				= new Graph(super.caller);
	/** The initial nodes. */
	protected static URI[]		initialNodes		= new URI[10];
	protected static int[]		initialNodeCount	= new int[] { 0 };

	// Some magic values given from the SFN paper:
	// http://arxiv.org/pdf/1105.3347.pdf
	private static final int	INITNODEMAX			= 10;
	private static final int	NOFEDGESPERNODE		= 3;


	/**
	 * Instantiates a new node agent.
	 *
	 * @param id
	 *            the id
	 * @param config
	 *            the config
	 */
	public NodeAgent(String id, ObjectNode config) {
		super(id, config);
	}

	/**
	 * Instantiates a new node agent.
	 */
	public NodeAgent() {
		super();
	}

	@Namespace("graph")
	public Graph getGraph() {
		return graph;
	}
	
	public void addNode2SFN(final String tag) {
		if (initialNodeCount[0] < 10) {
			synchronized (initialNodeCount) {
				switch (initialNodeCount[0]) {
					case 0:
						initialNodeCount[0]++;
						initialNodes[0] = getUrls().get(0);
						return;
					case 1:
						initialNodeCount[0]++;
						initialNodes[1] = getUrls().get(0);
						try {
							getGraph().addEdges(Arrays.asList(initialNodes[0]),
									tag);
						} catch (IOException e) {
							LOG.log(Level.WARNING, "Failed to init SFN", e);
						}
						return;
					default:
						if (initialNodeCount[0] < INITNODEMAX) {
							int myIndex = initialNodeCount[0]++;
							initialNodes[myIndex] = getUrls().get(0);
							try {
								getGraph()
										.addEdges(
												Arrays.asList(
														initialNodes[myIndex - 2],
														initialNodes[myIndex - 1]),
												tag);
							} catch (IOException e) {
								LOG.log(Level.WARNING, "Failed to init SFN", e);
							}
							return;
						}
						// explicit fall through
				}
			}
		}
		try {
			getGraph().addNode2SFN(initialNodes[0], tag, NOFEDGESPERNODE);
		} catch (IOException e) {
			LOG.log(Level.WARNING, "Couldn't join SFN!", e);
		}
	}
}
