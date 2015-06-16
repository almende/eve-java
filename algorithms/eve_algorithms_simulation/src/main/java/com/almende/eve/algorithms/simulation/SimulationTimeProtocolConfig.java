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
	private static final String	BUILDER	= SimulationTimeProtocolBuilder.class
												.getName();

	protected SimulationTimeProtocolConfig() {
		super();
	}

	/**
	 * Instantiates a new Simulation protocol config.
	 *
	 * @return the simulation time protocol config
	 */
	public static SimulationTimeProtocolConfig create() {
		final SimulationTimeProtocolConfig res = new SimulationTimeProtocolConfig();
		res.setBuilder(BUILDER);
		return res;
	}

	/**
	 * Instantiates a new Simulation protocol config.
	 *
	 * @param node
	 *            the node
	 * @return the simulation protocol config
	 */
	public static SimulationTimeProtocolConfig decorate(final ObjectNode node) {
		final SimulationTimeProtocolConfig res = new SimulationTimeProtocolConfig();
		res.extend(node);
		return res;
	}
}
