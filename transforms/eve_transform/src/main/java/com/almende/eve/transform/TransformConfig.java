/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.transform;

import com.almende.eve.capabilities.Config;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class TransformConfig.
 */
public class TransformConfig extends Config {

	/**
	 * Instantiates a new transform config.
	 *
	 * @param config
	 *            the config
	 */
	public TransformConfig(final ObjectNode config){
		super(config);
	}

}
