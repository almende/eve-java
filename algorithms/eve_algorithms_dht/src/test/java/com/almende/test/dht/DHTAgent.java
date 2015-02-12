/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.test.dht;

import com.almende.dht.Node;
import com.almende.dht.rpc.DHT;
import com.almende.eve.agent.Agent;
import com.almende.eve.agent.AgentConfig;
import com.almende.eve.protocol.jsonrpc.annotation.Namespace;

/**
 * The Class DHTAgent.
 */
public class DHTAgent extends Agent {
	private DHT dht = new DHT(caller);
	
	/**
	 * Instantiates a new DHT agent.
	 */
	public DHTAgent(){
		super();
	}
	
	/**
	 * Instantiates a new DHT agent.
	 *
	 * @param id
	 *            the id
	 */
	public DHTAgent(final String id){
		super(new AgentConfig(id));
	}
	
	/**
	 * Gets the DHT endpoint.
	 *
	 * @return the dht
	 */
	@Namespace("*")
	public DHT getDht(){
		return dht;
	}
	
	/**
	 * return the node description of this DHT node.
	 *
	 * @return the node
	 */
	public Node asNode(){
		final Node result = new Node();
		result.setKey(dht.getKey());
		result.setUri(this.getUrls().get(this.getUrls().size()-1));
		return result;
	}
}
