/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.dht.rpc;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.almende.dht.Bucket;
import com.almende.dht.Constants;
import com.almende.dht.Key;
import com.almende.dht.Node;
import com.almende.dht.RoutingTable;
import com.almende.dht.TimedValue;
import com.almende.eve.protocol.jsonrpc.annotation.Access;
import com.almende.eve.protocol.jsonrpc.annotation.AccessType;
import com.almende.eve.protocol.jsonrpc.annotation.Name;
import com.almende.eve.protocol.jsonrpc.annotation.Namespace;
import com.almende.eve.protocol.jsonrpc.annotation.Sender;
import com.almende.eve.protocol.jsonrpc.formats.Caller;
import com.almende.util.TypeUtil;
import com.almende.util.callback.AsyncCallback;
import com.almende.util.jackson.JOM;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class DHT Adapter.
 */
@Namespace("dht")
public class DHT {
	private static final Logger					LOG			= Logger.getLogger(DHT.class
																	.getName());
	// TODO: Make this persistent
	private RoutingTable						rt			= new RoutingTable(
																	Key.random());
	private Map<Key, Set<TimedValue>>			values		= new HashMap<Key, Set<TimedValue>>();
	private Caller								caller		= null;
	private static final TypeUtil<List<Node>>	NODELIST	= new TypeUtil<List<Node>>() {};
	
	/**
	 * Instantiates a new dht.
	 *
	 * @param caller
	 *            the caller
	 */
	public DHT(Caller caller) {
		this.caller = caller;
	}

	/**
	 * Gets the key of my own DHT node
	 *
	 * @return the key
	 */
	public Key getKey() {
		return rt.getMyKey();
	}

	/**
	 * Gets the table.
	 *
	 * @return the table
	 */
	public RoutingTable getTable() {
		return rt;
	}

	/**
	 * Ping. (Kademlia's PING)
	 *
	 * @return the boolean
	 */
	@Access(AccessType.PUBLIC)
	public Boolean ping() {
		return true;
	}

	/**
	 * Store the value at the key. (Kademlia's STORE)
	 *
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * @param remote
	 *            the remote
	 * @param sender
	 *            the sender
	 */
	@Access(AccessType.PUBLIC)
	public void store(@Name("key") Key key, @Name("value") ObjectNode value,
			@Name("me") Key remote, @Sender URI sender) {
		rt.seenNode(new Node(remote, sender));
		Set<TimedValue> current = new TreeSet<TimedValue>();
		TimedValue tv = new TimedValue(value);

		if (values.containsKey(key)) {
			current = values.get(key);
		}
		current.remove(tv);
		current.add(tv);
		values.put(key, current);
	}

	/**
	 * Delete a specific value from this key.
	 *
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * @param remote
	 *            the remote
	 * @param sender
	 *            the sender
	 */
	@Access(AccessType.PUBLIC)
	public void delete(@Name("key") Key key, @Name("value") ObjectNode value,
			@Name("me") Key remote, @Sender URI sender) {
		rt.seenNode(new Node(remote, sender));
		if (values.containsKey(key)) {
			Set<TimedValue> current = values.remove(key);
			TimedValue tv = new TimedValue(value);

			current.remove(tv);
			if (!current.isEmpty()) {
				values.put(key, current);
			}
		}

	}

	/**
	 * Delete a specific value from this key.
	 *
	 * @param key
	 *            the key
	 * @param remote
	 *            the remote
	 * @param sender
	 *            the sender
	 */
	@Access(AccessType.PUBLIC)
	public void deleteAll(@Name("key") Key key, @Name("me") Key remote,
			@Sender URI sender) {
		rt.seenNode(new Node(remote, sender));
		values.remove(key);
	}

	private ObjectNode loc_find_close_nodes(Key near) {
		final ObjectNode result = JOM.createObjectNode();
		result.set(
				"nodes",
				JOM.getInstance().valueToTree(
						rt.getClosestNodes(near, Constants.K)));
		return result;
	}

	/**
	 * Find_close_nodes. (Kademlia's FIND_NODE)
	 *
	 * @param near
	 *            the near
	 * @param remote
	 *            the remote
	 * @param sender
	 *            the sender
	 * @return An objectNode containing {"nodes":[.....]}
	 */
	@Access(AccessType.PUBLIC)
	public ObjectNode find_close_nodes(@Name("near") Key near,
			@Name("me") Key remote, @Sender URI sender) {
		rt.seenNode(new Node(remote, sender));
		return loc_find_close_nodes(near);
	}

	/**
	 * Find_value, optionally get all values for key.
	 *
	 * @param key
	 *            the key
	 * @param remote
	 *            the remote
	 * @param multiple
	 *            Request all values or just the newest?
	 * @param sender
	 *            the sender
	 * @return And objectNode containing either: {"value":....} or
	 *         {"nodes":[....]}
	 */
	@Access(AccessType.PUBLIC)
	public ObjectNode find_value(@Name("key") Key key, @Name("me") Key remote, @Name("multiple") Boolean multiple,
			@Sender URI sender) {
		rt.seenNode(new Node(remote, sender));
		if (values.containsKey(key)) {
			final ObjectNode result = JOM.createObjectNode();
			final ArrayNode arr = JOM.createArrayNode();
			final Set<TimedValue> current = values.get(key);
			
			final int size = current.size();
			final Iterator<TimedValue> iter = values.get(key).iterator();
			while (iter.hasNext()) {
				final TimedValue item = iter.next();
				if (item.getTtl() > 0) {
					arr.add(item.getValue());
				} else {
					iter.remove();
				}
			}
			if (current.size() < size) {
				values.put(key, current);
			}
			if (multiple){
				result.set("values", arr);
			} else {
				result.set("value", arr.get(0));
			}
			return result;
		} else {
			return loc_find_close_nodes(key);
		}
	}

	/**
	 * Insert.
	 *
	 * @param map
	 *            the map
	 * @param near
	 *            the near
	 * @param node
	 *            the node
	 */
	private void insert(final Map<Key, Node> map, final Key near,
			final List<Node> list) {
		if (list == null) {
			return;
		}
		for (Node node : list) {
			if (node.getKey().equals(rt.getMyKey())) {
				// Skip myself!
				continue;
			}
			map.put(near.dist(node.getKey()), node);
		}
		if (map.size() > Constants.K) {
			int count = 0;
			Iterator<Entry<Key, Node>> iter = map.entrySet().iterator();
			while (iter.hasNext()) {
				iter.next();
				if (count >= Constants.K) {
					iter.remove();
				}
				count++;
			}
		}
	}

	/**
	 * Iterative_node_lookup.
	 *
	 * @param near
	 *            the near
	 * @return the list
	 */
	public List<Node> iterative_node_lookup(final Key near) {
		final TreeMap<Key, Node> shortList = new TreeMap<Key, Node>();
		final Set<Node> tried = new HashSet<Node>();

		insert(shortList, near, rt.getClosestNodes(near, Constants.A));

		final int[] noInFlight = new int[1];
		noInFlight[0] = 0;
		boolean keepGoing = true;
		while (keepGoing) {
			while (noInFlight[0] > 0) {
				synchronized (shortList) {
					try {
						shortList.wait(100);
					} catch (InterruptedException e) {}
				}
			}

			List<Node> copy = Arrays.asList(shortList.values().toArray(
					new Node[0]));
			Collections.sort(copy);
			final Iterator<Node> iter = copy.iterator();
			int count = 0;
			while (iter.hasNext() && count < Constants.A) {
				final Node next = iter.next();
				if (tried.contains(next)) {
					continue;
				}
				count++;
				tried.add(next);
				try {
					ObjectNode params = JOM.createObjectNode();
					params.set("me",
							JOM.getInstance().valueToTree(rt.getMyKey()));
					params.set("near", JOM.getInstance().valueToTree(near));

					AsyncCallback<ObjectNode> callback = new AsyncCallback<ObjectNode>() {

						@Override
						public void onSuccess(ObjectNode res) {
							List<Node> result = NODELIST.inject(res
									.get("nodes"));
							rt.seenNode(next);
							synchronized (shortList) {
								insert(shortList, near, result);
								noInFlight[0]--;
								shortList.notifyAll();
							}
						}

						@Override
						public void onFailure(Exception exception) {
							synchronized (shortList) {
								shortList.remove(near.dist(next.getKey()));
								noInFlight[0]--;
								shortList.notifyAll();
								LOG.log(Level.WARNING, noInFlight[0]
										+ ":OnFailure called:" + next.getUri(),
										exception);
							}
						}

					};
					caller.call(next.getUri(), "dht.find_close_nodes", params,
							callback);
					synchronized (shortList) {
						noInFlight[0]++;
					}
				} catch (IOException e) {
					synchronized (shortList) {
						shortList.remove(near.dist(next.getKey()));
					}
					continue;
				}
			}
			if (count == 0) {
				keepGoing = false;
			}
		}
		synchronized (shortList) {
			return new ArrayList<Node>(shortList.values());
		}
	}

	/**
	 * Iterative_find_value.
	 *
	 * @param key
	 *            the key
	 * @param multiple
	 *            the multiple
	 * @return the object node
	 */
	public JsonNode iterative_find_value(final Key key, final boolean multiple) {
		final JsonNode[] result = new JsonNode[1];
		
		final TreeMap<Key, Node> shortList = new TreeMap<Key, Node>();
		final Set<Node> tried = new HashSet<Node>();

		insert(shortList, key, rt.getClosestNodes(key, Constants.A));
		final int[] noInFlight = new int[1];
		noInFlight[0] = 0;
		boolean keepGoing = true;
		while (keepGoing) {
			while (noInFlight[0] > 0 && result[0] == null) {
				synchronized (shortList) {
					try {
						shortList.wait(100);
					} catch (InterruptedException e) {}
				}
			}

			List<Node> copy = Arrays.asList(shortList.values().toArray(
					new Node[0]));
			Collections.sort(copy);
			final Iterator<Node> iter = copy.iterator();
			int count = 0;
			while (iter.hasNext() && count < Constants.A) {
				final Node next = iter.next();
				if (tried.contains(next)) {
					continue;
				}
				count++;
				tried.add(next);
				try {
					ObjectNode params = JOM.createObjectNode();
					params.set("me",
							JOM.getInstance().valueToTree(rt.getMyKey()));
					params.set("key", JOM.getInstance().valueToTree(key));
					params.put("multiple", multiple);

					AsyncCallback<ObjectNode> callback = new AsyncCallback<ObjectNode>() {

						@Override
						public void onSuccess(ObjectNode res) {
							if (res.has("value")){
								result[0]=(ObjectNode) res.get("value");
							} else if (res.has("values")){
								result[0]=(ArrayNode) res.get("values");
							} else {
								List<Node> nodes = NODELIST.inject(res
									.get("nodes"));
								synchronized (shortList) {
									insert(shortList, key, nodes);
								}				
							}
							rt.seenNode(next);
							synchronized(shortList){
								noInFlight[0]--;
								shortList.notifyAll();
							}
						}

						@Override
						public void onFailure(Exception exception) {
							synchronized (shortList) {
								shortList.remove(key.dist(next.getKey()));
								noInFlight[0]--;
								shortList.notifyAll();
								LOG.log(Level.WARNING, noInFlight[0]
										+ ":OnFailure called:" + next.getUri(),
										exception);
							}
						}

					};
					caller.call(next.getUri(), "dht.find_value", params,
							callback);
					synchronized (shortList) {
						noInFlight[0]++;
					}
				} catch (IOException e) {
					synchronized (shortList) {
						shortList.remove(key.dist(next.getKey()));
					}
					continue;
				}
			}
			if (count == 0) {
				keepGoing = false;
			}
		}
		if (result[0] == null){
			return JOM.createNullNode();
		} else {
			return result[0];
		}
	}

	/**
	 * Iterative_store_value.
	 *
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void iterative_store_value(final Key key, final ObjectNode value) throws IOException{
		List<Node> nodes = iterative_node_lookup(key);
		for (final Node node: nodes){
			final ObjectNode params = JOM.createObjectNode();
			params.set("me",
					JOM.getInstance().valueToTree(rt.getMyKey()));
			params.set("key", JOM.getInstance().valueToTree(key));
			params.set("value",value);
			caller.call(node.getUri(), "dht.store", params);
		}
	}
	
	/**
	 * Join the network
	 *
	 * @param remote
	 *            the pre-known remote start node
	 */
	@Access(AccessType.PUBLIC)
	public void join(@Name("remote") Node remote) {
		rt.seenNode(remote);
		iterative_node_lookup(rt.getMyKey());

		final Bucket[] table = rt.getTable();
		boolean found = false;
		for (int i = 0; i < table.length; i++) {
			Bucket bucket = table[i];
			if (bucket.size() > 0) {
				found = true;
			}
			if (found) {
				iterative_node_lookup(bucket.getRandomKey());
			}
		}
	}

	// TODO: Schedule Node expiry
	// TODO: Schedule Value expiry
	// TODO: Schedule expiry
	/**
	 * Refresh buckets (scheduled regularly)
	 */
	@Access(AccessType.PUBLIC)
	public void refresh() {
		List<Bucket> buckets = rt.getStaleBuckets();
		for (Bucket bucket : buckets) {
			iterative_node_lookup(bucket.getRandomKey());
		}
	}

	@Override
	public String toString() {
		return rt.toString();
	}

	/**
	 * Checks for values.
	 *
	 * @return true, if successful
	 */
	public boolean hasValues(){
		return !values.isEmpty();
	}

}
