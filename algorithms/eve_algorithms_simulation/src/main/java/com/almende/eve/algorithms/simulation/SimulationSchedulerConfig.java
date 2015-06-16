/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.algorithms.simulation;

import com.almende.eve.scheduling.SimpleSchedulerConfig;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class PersistentSchedulerConfig.
 */
public class SimulationSchedulerConfig extends SimpleSchedulerConfig {
	private static final String	BUILDER	= SimulationSchedulerBuilder.class
												.getName();

	/**
	 * Instantiates a new simple scheduler config.
	 */
	protected SimulationSchedulerConfig() {
		super();
	}

	public static SimulationSchedulerConfig create(){
		final SimulationSchedulerConfig res = new SimulationSchedulerConfig();
		res.setBuilder(BUILDER);
		return res;
	}
	/**
	 * Instantiates a new simple scheduler config.
	 * 
	 * @param node
	 *            the node
	 */
	public static SimulationSchedulerConfig decorate(final ObjectNode node) {
		final SimulationSchedulerConfig res = new SimulationSchedulerConfig();
		res.extend(node);
		return res;
	}
	
	/**
	 * Sets the strong consistency.
	 *
	 * @param strongConsistency
	 *            the new strong consistency
	 */
	public void setStrongConsistency(final boolean strongConsistency) {
		this.put("strongConsistency", strongConsistency);
	}

	/**
	 * Checks if is strong consistency.
	 *
	 * @return true, if is strong consistency
	 */
	public boolean isStrongConsistency() {
		if (has("strongConsistency")) {
			return get("strongConsistency").asBoolean();
		}
		return false;
	}
}
