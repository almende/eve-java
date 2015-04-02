/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.algorithms;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.almende.eve.protocol.jsonrpc.annotation.Access;
import com.almende.eve.protocol.jsonrpc.annotation.AccessType;
import com.almende.eve.protocol.jsonrpc.annotation.Name;
import com.almende.eve.protocol.jsonrpc.annotation.Namespace;
import com.almende.eve.protocol.jsonrpc.annotation.Optional;
import com.almende.eve.protocol.jsonrpc.annotation.Sender;
import com.almende.eve.protocol.jsonrpc.formats.Caller;
import com.almende.eve.protocol.jsonrpc.formats.Params;
import com.almende.util.callback.AsyncCallback;
import com.almende.util.callback.SyncCallback;
import com.almende.util.uuid.UUID;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * The Class Graph, contains storage, logic and communication for maintaining a
 * graph, potentially existing of tagged subgraphs, with edges that can carry a
 * Comparable Weight.
 * 
 * Among management tooling, the class also contains a Scale-Free-Network
 * generator algorithm and some graph search tools.
 */
@Namespace("graph")
public class Graph {
	private static final Logger								LOG			= Logger.getLogger(Graph.class
																				.getName());
	private Caller											caller		= null;
	private double											probSFN		= Math.random();
	private static final Map<String, AsyncCallback<URI>>	callbacks	= new HashMap<String, AsyncCallback<URI>>();

	private Edge[]											edges		= new Edge[0];
	transient private TreeSet<Edge>							set			= null;

	/**
	 * Instantiates a new network rpc.
	 *
	 * @param caller
	 *            the caller
	 */
	public Graph(Caller caller) {
		this.caller = caller;
	}

	/**
	 * Gets the edges.
	 *
	 * @return the edges
	 */
	public synchronized Edge[] getEdges() {
		return edges;
	}

	/**
	 * Sets the edges.
	 *
	 * @param edges
	 *            the new edges
	 */
	public synchronized void setEdges(final Edge[] edges) {
		this.edges = edges;
	}

	/**
	 * Adds a new edge.
	 *
	 * @param node
	 *            the node
	 */
	@Access(AccessType.PUBLIC)
	public void addEdge(@Name("node") URI node) {
		addFullEdge(node, null, null);
	}

	/**
	 * Adds a single direction tagged edge to the other node.
	 *
	 * @param node
	 *            the node
	 * @param tag
	 *            the tag
	 */
	@Access(AccessType.PUBLIC)
	public void addTaggedEdge(@Name("node") URI node, @Name("tag") String tag) {
		addFullEdge(node, tag, null);
	}

	/**
	 * Adds a new edge.
	 *
	 * @param node
	 *            the node
	 * @param weight
	 *            the weight
	 */
	public void addWeightedEdge(URI node, Comparable<Object> weight) {
		addFullEdge(node, null, weight);
	}

	/**
	 * Adds a new edge.
	 *
	 * @param address
	 *            the address
	 * @param tag
	 *            the tag
	 * @param weight
	 *            the weight
	 */
	public synchronized void addFullEdge(final URI address, final Object tag,
			final Comparable<Object> weight) {
		edges = Arrays.copyOf(edges, edges.length + 1);
		final Edge edge = new Edge(address, tag, weight);
		edges[edges.length - 1] = edge;
		if (set != null) {
			set.add(edge);
		}
	}

	/**
	 * Get a treeSet view of the network, sorted and navigational.
	 *
	 * @return the tree set
	 */
	@JsonIgnore
	public synchronized TreeSet<Edge> getTreeSet() {
		if (set == null) {
			set = new TreeSet<Edge>();
			set.addAll(Arrays.asList(edges));
		}
		return set;
	}

	/**
	 * Gets the top x of the TreeSet view.
	 *
	 * @param x
	 *            the x
	 * @return the top x
	 */
	@JsonIgnore
	public synchronized Edge[] getTopX(final int x) {
		Edge[] tree = getTreeSet().toArray(new Edge[0]);
		List<Edge> res = new ArrayList<Edge>(x);
		for (int i = 0; i < x && i < tree.length; i++) {
			res.add(tree[i]);
		}
		return res.toArray(new Edge[0]);
	}

	/**
	 * Gets the edges that match the given tag.
	 *
	 * @param tag
	 *            the tag
	 * @return the by tag
	 */
	@JsonIgnore
	public synchronized Edge[] getByTag(final Object tag) {
		List<Edge> res = new ArrayList<Edge>(edges.length);
		for (Edge edge : edges) {
			if (edge.getTag() != null && edge.getTag().equals(tag)) {
				res.add(edge);
			}
		}
		return res.toArray(new Edge[0]);
	}

	/**
	 * Gets the random edge.
	 *
	 * @param tag
	 *            the tag
	 * @return the random edge
	 */
	@JsonIgnore
	public Edge getRandomEdge(final String tag) {
		final Edge[] taggedEdges = getByTag(tag);
		if (taggedEdges.length > 0) {
			return taggedEdges[(int) Math.floor(Math.random()
					* taggedEdges.length)];
		} else {
			return null;
		}
	}

	/**
	 * Adds a list of double directional edges.
	 *
	 * @param nodes
	 *            the nodes
	 * @param tag
	 *            the tag
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Access(AccessType.PUBLIC)
	public void addEdges(@Name("nodes") List<URI> nodes, @Name("tag") String tag)
			throws IOException {
		for (URI node : nodes) {
			addTaggedEdge(node, tag);
			final Params params = new Params();
			params.add("node", caller.getSenderUrls().get(0));
			params.add("tag", tag);
			caller.call(node, "graph.addTaggedEdge", params);
		}
	}
	
	// ScaleFreeNetwork

	/**
	 * Adds the node to a tagged Scale-Free-Network overlay.
	 *
	 * @param node
	 *            the new nodes address
	 * @param tag
	 *            the tag
	 * @param m
	 *            the number of new edges to attach
	 * @param l
	 *            the length of the initial randomWalk.
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Access(AccessType.PUBLIC)
	public void addNode2SFN(final @Name("address") URI node,
			final @Name("tag") String tag, final @Name("nofEdges") int m,
			final @Optional @Name("initialWalk") Integer l) throws IOException {
		final Set<URI> others = new HashSet<URI>(m);
		URI remote = doRandomWalk(tag, l != null ? l : 7);
		others.add(remote);
		while (others.size() < m) {
			final Params params = new Params();
			params.add("tag", tag);
			remote = caller.callSync(remote, "graph.doRandomWalk", params,
					URI.class);
			others.add(remote);
		}
		final Params params = new Params();
		params.add("nodes", others.toArray(new URI[0]));
		params.add("tag", tag);
		caller.call(node, "graph.addEdges", params);
	}

	// Random walk

	/**
	 * Do random walk.
	 *
	 * @param steps
	 *            the steps
	 * @param tag
	 *            the tag
	 * @return the uri
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Access(AccessType.PUBLIC)
	public URI doRandomWalk(@Name("tag") String tag,
			@Optional @Name("steps") Integer steps) throws IOException {
		if (steps == null) {
			steps = Math.random() > probSFN ? 2 : 1;
		}
		SyncCallback<URI> callback = new SyncCallback<URI>();
		String id = new UUID().toString();
		callbacks.put(id, callback);
		randomWalk(steps, caller.getSenderUrls().get(0), id, tag);
		try {
			return callback.get();
		} catch (Exception e) {
			LOG.log(Level.WARNING,
					"Failed to obtain remote node from random walk", e);
			return null;
		}
	}

	/**
	 * Random walk.
	 *
	 * @param steps
	 *            the steps
	 * @param origin
	 *            the origin
	 * @param id
	 *            the id
	 * @param tag
	 *            the tag
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Access(AccessType.PUBLIC)
	public void randomWalk(@Name("steps") int steps,
			@Name("origin") URI origin, @Name("id") String id,
			@Name("tag") String tag) throws IOException {
		URI next = getRandomEdge(tag).getAddress();
		if (steps > 0) {
			Params params = new Params();
			params.add("steps", steps - 1);
			params.add("origin", origin);
			params.add("id", id);
			params.add("tag", tag);
			caller.call(next, "graph.randomWalk", params);
		} else {
			Params params = new Params();
			params.add("id", id);
			caller.call(origin, "graph.reportRandomWalk", params);
		}
	}

	/**
	 * Report random walk.
	 *
	 * @param other
	 *            the other
	 * @param id
	 *            the id
	 */
	@Access(AccessType.PUBLIC)
	public void reportRandomWalk(@Sender URI other, @Name("id") String id) {
		AsyncCallback<URI> callback = callbacks.get(id);
		callback.onSuccess(other);
	}
}
