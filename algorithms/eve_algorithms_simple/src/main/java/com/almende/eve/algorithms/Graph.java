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
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.locks.ReentrantLock;
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
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class Graph, contains storage, logic and communication for maintaining a
 * graph, potentially existing of tagged subgraphs, with edges that can carry a
 * Comparable Weight.
 * Among management tooling, the class also contains a Scale-Free-Network
 * generator algorithm (based on: http://arxiv.org/pdf/1105.3347.pdf) and some
 * graph search tools. <br>
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
 * 
 * @Sender)<br>
 */
@Namespace("graph")
public class Graph {
	private static final Logger								LOG			= Logger.getLogger(Graph.class
																				.getName());
	private static final Map<String, AsyncCallback<URI>>	CALLBACKS	= new HashMap<String, AsyncCallback<URI>>();
	private Caller											caller		= null;
	private double											probSFN		= Math.random();

	private Edge[]											edges		= new Edge[0];
	private transient SortedSet<Edge>						set			= null;

	// LinkedList
	private Map<String, Comparable<ObjectNode>>				comparators	= new HashMap<String, Comparable<ObjectNode>>();
	private ReentrantLock									lllock		= new ReentrantLock();

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
	public synchronized SortedSet<Edge> getSortedSet() {
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
		Edge[] tree = getSortedSet().toArray(new Edge[0]);
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

	/**
	 * Sets the comparator, an object that implements this agents side of
	 * comparable<ObjectNode>
	 *
	 * @param tag
	 *            the tag
	 * @param comparator
	 *            the comparator used to order the LinkedList.
	 */
	public void llcomparator(final String tag,
			final Comparable<ObjectNode> comparator) {
		this.comparators.put(tag, comparator);
	}

	@JsonIgnore
	private URI getPeer(final String fullTag) {
		Edge[] edge = getByTag(fullTag);
		if (edge != null && edge.length == 1) {
			return edge[0].getAddress();
		}
		return null;
	}

	private boolean setPeer(final String fullTag, final URI peer) {
		Edge[] edge = getByTag(fullTag);
		if (edge.length == 0) {
			addEdge(new Edge(peer, fullTag, null));
			return true;
		} else if (edge.length != 1) {
			LOG.warning(caller.getSenderUrlByScheme("local")
					+ ": Node edge is non-singular:" + fullTag);
			return false;
		} else {
			edge[0].setAddress(peer);
			return true;
		}
	}

	private boolean sendRequest(final URI peer, final Links request,
			final String tag) {
		Links res = new Links();
		final Params params = new Params();
		params.add("tag", tag);
		params.add("request", request);
		while (!res.isDone()) {
			try {
				res = caller.callSync(peer, "graph.llrequest", params,
						Links.class);
				if (res == null) {
					res = new Links();
				}
			} catch (IOException e) {
				LOG.log(Level.WARNING, caller.getSenderUrlByScheme("local")
						+ ": Failed to contact prev:" + peer, e);
				break;
			} catch (Exception e) {
				LOG.log(Level.WARNING, caller.getSenderUrlByScheme("local")
						+ ": Prev threw an exception:" + peer, e);
				break;
			}
		}
		return res.isDone();
	}

	/**
	 * Llrequest.
	 *
	 * @param tag
	 *            the tag
	 * @param request
	 *            the request
	 * @param sender
	 *            the sender
	 * @return the links
	 */
	@Access(AccessType.PUBLIC)
	public Links llrequest(@Name("tag") String tag,
			@Name("request") Links request, @Sender URI sender) {
		// TODO: revert behavior?
		final Links res = new Links();
		if (!lllock.tryLock()) {
			return res;
		}
		if (request.getPrev() != null) {
			if (request.getPrev().equals(sender)) {
				if (setPeer(tag + "_prev", request.getPrev())) {
					res.setPrev(request.getPrev());
					res.setDone(true);
				}
			} else {
				// Contact request.prev and request change, on report update
				// locally.
				final Links prevReq = new Links();
				prevReq.setNext(caller.getSenderUrlByScheme(sender.getScheme()));
				if (sendRequest(request.getPrev(), prevReq, tag)) {
					if (setPeer(tag + "_prev", request.getPrev())) {
						res.setPrev(request.getPrev());
						res.setDone(true);
					}
				}
			}
		}
		if (request.getNext() != null) {
			if (request.getNext().equals(sender)) {
				if (setPeer(tag + "_next", request.getNext())) {
					res.setNext(request.getNext());
					res.setDone(true);
				}
			} else {
				// Contact request.next and request change, on report update
				// locally.
				final Links nextReq = new Links();
				nextReq.setPrev(caller.getSenderUrlByScheme(sender.getScheme()));
				if (sendRequest(request.getNext(), nextReq, tag)) {
					if (setPeer(tag + "_next", request.getNext())) {
						res.setNext(request.getNext());
						res.setDone(true);
					}
				}
			}
		}
		lllock.unlock();
		return res;
	}

	/**
	 * Do the actual insert of a node in the linked list.
	 *
	 * @param tag
	 *            the tag
	 * @param compare
	 *            the comparator
	 * @param sender
	 *            the sender
	 * @return true, if successful
	 */
	@Access(AccessType.PUBLIC)
	public Boolean lldoinsert(@Name("tag") final String tag,
			@Name("compare") final ObjectNode compare, @Sender URI sender) {
		boolean res = false;
		final Comparable<ObjectNode> me = this.comparators.get(tag);
		if (me != null) {
			final Links request = new Links();
			final int comp = me.compareTo(compare);
			switch (comp) {
				case 0:
					break;
				case 1:
					// New node is prev
					request.setPrev(getPeer(tag + "_prev"));
					request.setNext(caller.getSenderUrlByScheme(sender
							.getScheme()));
					if (sendRequest(sender, request, tag)) {
						setPeer(tag + "_prev", sender);
						res = true;
					}
					break;
				case -1:
					// New node is next
					request.setNext(getPeer(tag + "_next"));
					request.setPrev(caller.getSenderUrlByScheme(sender
							.getScheme()));
					if (sendRequest(sender, request, tag)) {
						setPeer(tag + "_next", sender);
						res = true;
					}
					break;
				default:
					LOG.warning("Strange comparison result:" + comp);
			}
		} else {
			LOG.warning("Comparable not set for:" + tag);
		}
		return res;
	}

	/**
	 * Do the actual swap if needed.
	 *
	 * @param tag
	 *            the tag
	 * @param request
	 *            the request
	 * @param compare
	 *            the compare
	 * @param sender
	 *            the sender
	 * @return true, if successful
	 */
	@Access(AccessType.PUBLIC)
	public boolean lldoswap(@Name("tag") final String tag,
			@Name("request") Links request,
			@Name("compare") final ObjectNode compare, @Sender URI sender) {
		boolean res = false;
		final boolean up = sender.equals(request.getNext());
		final Comparable<ObjectNode> me = this.comparators.get(tag);
		if (me != null) {
			final Links prev_request = new Links();
			final Links next_request = new Links();
			final int comp = me.compareTo(compare);
			switch (comp) {
				case 0:
					break;
				case 1:
					// I'm bigger, if up don't do anything, else swap
					if (!up) {
						prev_request.setPrev(getPeer(tag + "_prev"));
						prev_request.setNext(caller.getSenderUrlByScheme(sender
								.getScheme()));
						if (sendRequest(sender, prev_request, tag)) {
							setPeer(tag + "_prev", sender);
							next_request.setPrev(caller
									.getSenderUrlByScheme(sender.getScheme()));
							if (sendRequest(request.getNext(), next_request,
									tag)) {
								setPeer(tag + "_next", request.getNext());
								res = true;
							}
						}
					}
					break;
				case -1:
					// I'm smaller, if !up don't do anything, else swap
					if (up) {
						next_request.setNext(getPeer(tag + "_next"));
						next_request.setPrev(caller.getSenderUrlByScheme(sender
								.getScheme()));
						if (sendRequest(sender, next_request, tag)) {
							setPeer(tag + "_next", sender);
							prev_request.setNext(caller
									.getSenderUrlByScheme(sender.getScheme()));
							if (sendRequest(request.getPrev(), prev_request,
									tag)) {
								setPeer(tag + "_prev", request.getPrev());
								res = true;
							}
						}
					}
					break;
				default:
					LOG.warning("Strange comparison result:" + comp);
			}
		}
		return res;
	}

	/**
	 * Check if this node need to swap with neighbor.
	 *
	 * @param tag
	 *            the tag
	 * @param up
	 *            the up
	 * @param compare
	 *            the compare
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Access(AccessType.PUBLIC)
	public void llswap(@Name("tag") final String tag, @Name("up") Boolean up,
			@Name("compare") final ObjectNode compare) throws IOException {
		final Links request = new Links();
		final Params params = new Params();
		params.add("tag", tag);
		params.add("compare", compare);
		URI peer = getPeer(tag + "_prev");
		if (!up) {
			request.setPrev(caller.getSenderUrlByScheme(peer.getScheme()));
			request.setNext(getPeer(tag + "_next"));
		} else {
			peer = getPeer(tag + "_next");
			request.setNext(caller.getSenderUrlByScheme(peer.getScheme()));
			request.setPrev(getPeer(tag + "_prev"));
		}
		params.add("request", request);
		if (peer != null) {
			caller.callSync(peer, "graph.lldoswap", params, Boolean.class);
		}
	}

	/**
	 * Insert of a node in the linked list.
	 * 
	 * @param tag
	 *            the tag
	 * @param compare
	 *            the compare
	 * @param peer
	 *            the peer
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Access(AccessType.PUBLIC)
	public void llinsert(@Name("tag") final String tag,
			@Name("compare") final ObjectNode compare, @Name("peer") URI peer)
			throws IOException {
		final Params params = new Params();
		params.add("tag", tag);
		params.add("compare", compare);
		caller.callSync(peer, "graph.lldoinsert", params, Boolean.class);
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
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Access(AccessType.PUBLIC)
	public void addNode2SFN(final @Name("start") URI start,
			final @Name("tag") String tag, final @Name("nofEdges") int m)
			throws IOException {
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
	public URI doRandomWalk(final @Name("tag") String tag,
			@Optional @Name("steps") Integer steps) throws IOException {
		if (steps == null) {
			steps = Math.random() > probSFN ? 2 : 1;
		}
		SyncCallback<URI> callback = new SyncCallback<URI>();
		String runId = new UUID().toString();
		CALLBACKS.put(runId, callback);
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
		Edge edge = getRandomEdge(tag);
		if (edge == null) {
			LOG.warning("Failed to obtain a correctly tagged edge:" + tag);
			return;
		}
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
		AsyncCallback<URI> callback = CALLBACKS.remove(runId);
		callback.onSuccess(other);
	}
}
