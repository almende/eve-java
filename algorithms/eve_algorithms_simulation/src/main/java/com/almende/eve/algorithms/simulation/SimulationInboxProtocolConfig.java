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
		res.copy(node);
		return res;
	}

	/**
	 * Checks if is support synccalls.
	 *
	 * @return true, if is support synccalls
	 */
	public boolean isSupportSynccalls() {
		if (this.has("supportSynccalls")) {
			return this.get("supportSynccalls").asBoolean();
		}
		return false;
	}

	/**
	 * Sets the support synccalls.
	 *
	 * @param supportSynccalls
	 *            the new support synccalls
	 */
	public void setSupportSynccalls(final boolean supportSynccalls) {
		this.put("supportSynccalls", supportSynccalls);
	}

}
