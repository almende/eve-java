/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.algorithms.clustering;

import com.almende.eve.protocol.ProtocolConfig;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class JSONRpcProtocolConfig.
 */
public class GlobalAddressProtocolConfig extends ProtocolConfig {

	/**
	 * Instantiates a new JSON rpc protocol config.
	 */
	public GlobalAddressProtocolConfig() {
		super();
		setClassName(GlobalAddressProtocolBuilder.class.getName());
	}

	/**
	 * Instantiates a new global address protocol config.
	 *
	 * @param setClass
	 *            the set class
	 */
	public GlobalAddressProtocolConfig(boolean setClass){
		super();
	}
	
	/**
	 * Instantiates a new JSON rpc protocol config.
	 * 
	 * @param node
	 *            the node
	 */
	public static GlobalAddressProtocolConfig decorate(final ObjectNode node) {
		final GlobalAddressProtocolConfig res = new GlobalAddressProtocolConfig(true);
		res.copy(node);
		return res;
	}

}
