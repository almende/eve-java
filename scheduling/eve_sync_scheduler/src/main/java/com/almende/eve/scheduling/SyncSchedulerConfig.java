/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.scheduling;

import com.almende.util.jackson.JOM;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class PersistentSchedulerConfig.
 */
public class SyncSchedulerConfig extends SimpleSchedulerConfig {
	
	/**
	 * Instantiates a new simple scheduler config.
	 */
	public SyncSchedulerConfig() {
		this(JOM.createObjectNode());
	}
	
	/**
	 * Instantiates a new simple scheduler config.
	 * 
	 * @param node
	 *            the node
	 */
	public SyncSchedulerConfig(final ObjectNode node) {
		super(node);
		if (!node.has("class")) {
			setClassName(SyncSchedulerBuilder.class.getName());
		}
	}
}
