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
 * Among management tooling, the class also contains a Scale-Free-Network
 * generator algorithm and some graph search tools. <br>
 * API:<br>
 * <br>
 * graph.addEdge({"edge":{"node":-URI-,"tag":-tag-,"weight":-object-}}); //Add
 * single Edge <br>
 * graph.addEdges({"nodes":[-URI-],"tag":-tag-}); //Add double directional
 * tagged Edges<br>
 * graph.addNode2SFN({"node":-URI-,"tag":-tag-,
 * "nofEdges":-m-,"initialWalk":-l-}) //add a node to a tagged
 * scale-free-network overlay. <br>
 * <br>
 * graph.doRandomWalk({"tag":-tag-,"steps":-steps-}) //initiate random walk,
 * returns URI.<br>
 * graph.randomWalk({"tag":-tag-,"steps":-steps-,"origin":-URI-,"runId":-id-})
 * //perform the randomwalk, report to origin<br>
 * graph.reportRandomWalk({""runId":-id-}) //Report resulting node (through the
 * @Sender)<br>
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
	 * @param edge
	 *            the edge
	 */
	@Access(AccessType.PUBLIC)
	public void addEdge(final @Name("edge") Edge edge) {
		edges = Arrays.copyOf(edges, edges.length + 1);
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
	@Access(AccessType.PUBLIC)
	public Edge getRandomEdge(final @Name("tag") String tag) {
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
			addEdge(new Edge(node, tag, null));
			final Params params = new Params();
			params.add("edge", new Edge(caller.getSenderUrls().get(0), tag,
					null));
			caller.call(node, "graph.addEdge", params);
		}
	}

	// ScaleFreeNetwork

	/**
	 * Adds this node to a tagged Scale-Free-Network overlay, starting from
	 * remote.
	 *
	 * @param start
	 *            the start
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
	public void addNode2SFN(final @Name("start") URI start,
			final @Name("tag") String tag, final @Name("nofEdges") int m,
			final @Optional @Name("initialWalk") Integer l) throws IOException {
		final Set<URI> others = new HashSet<URI>(m);
		Params params = new Params();
		params.add("tag", tag);
		params.add("steps", 7);
		URI remote = caller.callSync(start, "graph.doRandomWalk", params,
				URI.class);
		others.add(remote);
		while (others.size() < m) {
			params = new Params();
			params.add("tag", tag);
			remote = caller.callSync(remote, "graph.doRandomWalk", params,
					URI.class);
			others.add(remote);
		}
		params = new Params();
		addEdges(new ArrayList<URI>(others), tag);
	}

	// Random walk

	/**
	 * Initiate a random walk and report the resulting address.
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
		String runId = new UUID().toString();
		callbacks.put(runId, callback);
		randomWalk(tag, steps, caller.getSenderUrls().get(0), runId);
		try {
			return callback.get();
		} catch (Exception e) {
			LOG.log(Level.WARNING,
					"Failed to obtain remote node from random walk", e);
			return null;
		}
	}

	/**
	 * Random walk: take the next step, if steps==0, send message to origin.
	 *
	 * @param tag
	 *            the tag
	 * @param steps
	 *            the steps
	 * @param origin
	 *            the origin
	 * @param runId
	 *            the run id
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Access(AccessType.PUBLIC)
	public void randomWalk(@Name("tag") String tag, @Name("steps") int steps,
			@Name("origin") URI origin, @Name("runId") String runId)
			throws IOException {
		URI next = getRandomEdge(tag).getAddress();
		if (steps > 0) {
			Params params = new Params();
			params.add("steps", steps - 1);
			params.add("origin", origin);
			params.add("runId", runId);
			params.add("tag", tag);
			caller.call(next, "graph.randomWalk", params);
		} else {
			Params params = new Params();
			params.add("runId", runId);
			caller.call(origin, "graph.reportRandomWalk", params);
		}
	}

	/**
	 * Random Walk: reporting method.
	 *
	 * @param other
	 *            the other
	 * @param runId
	 *            the run id
	 */
	@Access(AccessType.PUBLIC)
	public void reportRandomWalk(@Sender URI other, @Name("runId") String runId) {
		AsyncCallback<URI> callback = callbacks.get(runId);
		callback.onSuccess(other);
	}
}
