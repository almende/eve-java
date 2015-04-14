/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.scheduling;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class PersistentSchedulerConfig.
 */
public class SyncSchedulerConfig extends SimpleSchedulerConfig {

	/**
	 * Instantiates a new simple scheduler config.
	 */
	public SyncSchedulerConfig() {
		super();
		setClassName(SyncSchedulerBuilder.class.getName());
	}

	/**
	 * Instantiates a new simple scheduler config.
	 * 
	 * @param node
	 *            the node
	 */
	public static SyncSchedulerConfig decorate(final ObjectNode node) {
		if (node != null && node instanceof SyncSchedulerConfig) {
			return (SyncSchedulerConfig) node;
		}
		final SyncSchedulerConfig res = new SyncSchedulerConfig();
		res.copy(node);
		if (!res.has("class")) {
			res.setClassName(SyncSchedulerBuilder.class.getName());
		}
		return res;
	}
}
