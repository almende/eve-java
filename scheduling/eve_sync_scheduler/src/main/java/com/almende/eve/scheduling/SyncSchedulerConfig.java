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
	private static final String	BUILDER	= SyncSchedulerBuilder.class.getName();

	/**
	 * Instantiates a new simple scheduler config.
	 */
	protected SyncSchedulerConfig() {
		super();
	}

	/**
	 * Instantiates a new synchronized clock scheduler config.
	 *
	 * @return the sync scheduler config
	 */
	public static SyncSchedulerConfig create() {
		final SyncSchedulerConfig res = new SyncSchedulerConfig();
		res.setBuilder(BUILDER);
		return res;
	}

	/**
	 * Instantiates a new synchronized clock scheduler config.
	 *
	 * @param node
	 *            the node
	 * @return the sync scheduler config
	 */
	public static SyncSchedulerConfig decorate(final ObjectNode node) {
		final SyncSchedulerConfig res = new SyncSchedulerConfig();
		res.extend(node);
		return res;
	}

}
