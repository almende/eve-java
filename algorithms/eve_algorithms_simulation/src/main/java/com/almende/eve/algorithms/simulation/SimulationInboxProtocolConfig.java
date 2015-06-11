/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.algorithms.simulation;

import com.almende.eve.protocol.InboxProtocolConfig;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class JSONRpcProtocolConfig.
 */
public class SimulationInboxProtocolConfig extends InboxProtocolConfig {

	/**
	 * Instantiates a new JSON rpc protocol config.
	 */
	public SimulationInboxProtocolConfig() {
		super();
		setClassName(SimulationInboxProtocolBuilder.class.getName());
	}

	private SimulationInboxProtocolConfig(final boolean skipClass) {
		super();
	}

	/**
	 * Instantiates a new JSON rpc protocol config.
	 * 
	 * @param node
	 *            the node
	 */
	public static SimulationInboxProtocolConfig decorate(final ObjectNode node) {
		final SimulationInboxProtocolConfig res = new SimulationInboxProtocolConfig(
				true);
		res.extend(node);
		return res;
	}

	/**
	 * Should the network become atomic, zero-time duration transfers?
	 *
	 * @param atomicNetwork
	 *            the new atomic network
	 */
	public void setAtomicNetwork(final boolean atomicNetwork){
		this.put("atomicNetwork", atomicNetwork);
	}
	
	/**
	 * Should the network become atomic, zero-time duration transfers?
	 *
	 * @return true, if is atomic network
	 */
	public boolean isAtomicNetwork() {
		if (this.has("atomicNetwork")){
			return this.get("atomicNetwork").asBoolean();
		}
		return false;
	}
	
}
