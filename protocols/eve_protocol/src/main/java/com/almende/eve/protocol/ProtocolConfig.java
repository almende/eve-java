/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.protocol;

import com.almende.eve.capabilities.Config;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class TransformConfig.
 */
public class ProtocolConfig extends Config {

	/**
	 * Instantiates a new protocol config.
	 *
	 * @param config
	 *            the config
	 */
	public ProtocolConfig(final ObjectNode config) {
		super(config);
	}

}
