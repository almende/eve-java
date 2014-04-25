/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.transform.rpc;

import com.almende.eve.capabilities.Config;
import com.almende.util.jackson.JOM;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class RpcTransformConfig.
 */
public class RpcTransformConfig extends Config {
	
	/**
	 * Instantiates a new rpc transform config.
	 */
	public RpcTransformConfig() {
		this(JOM.createObjectNode());
	}
	
	/**
	 * Instantiates a new rpc transform config.
	 * 
	 * @param node
	 *            the node
	 */
	public RpcTransformConfig(final ObjectNode node) {
		super(node);
		if (!this.has("class")) {
			setClassName(RpcTransform.class.getName());
		}
	}
}
