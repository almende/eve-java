/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.algorithms.simulation;

import com.almende.eve.protocol.ProtocolConfig;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class JSONRpcProtocolConfig.
 */
public class SimulationTimeProtocolConfig extends ProtocolConfig {

	/**
	 * Instantiates a new JSON rpc protocol config.
	 */
	public SimulationTimeProtocolConfig() {
		super();
		setClassName(SimulationTimeProtocolBuilder.class.getName());
	}

	/**
	 * Instantiates a new simulation protocol config.
	 *
	 * @param skipClass
	 *            the skip class
	 */
	public SimulationTimeProtocolConfig(final boolean skipClass) {
		super();
	}

	/**
	 * Instantiates a new JSON rpc protocol config.
	 *
	 * @param node
	 *            the node
	 * @return the simulation protocol config
	 */
	public static SimulationTimeProtocolConfig decorate(final ObjectNode node) {
		final SimulationTimeProtocolConfig res = new SimulationTimeProtocolConfig(
				true);
		res.copy(node);
		return res;
	}

}
