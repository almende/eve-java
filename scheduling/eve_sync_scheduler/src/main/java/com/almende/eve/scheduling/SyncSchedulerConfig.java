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

	private SyncSchedulerConfig(boolean local) {
		super();
	}

	/**
	 * Instantiates a new simple scheduler config.
	 * 
	 * @param node
	 *            the node
	 */
	public static SyncSchedulerConfig decorate(final ObjectNode node) {
		final SyncSchedulerConfig res = new SyncSchedulerConfig(true);
		res.extend(node);
		if (!res.has("class")) {
			res.setClassName(SyncSchedulerBuilder.class.getName());
		}
		return res;
	}
}
