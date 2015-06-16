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
	private static final String	BUILDER	= GlobalAddressProtocolBuilder.class
												.getName();

	protected GlobalAddressProtocolConfig() {
		super();
	}

	/**
	 * Instantiates a new GlobalAddress protocol config.
	 *
	 * @return the global address protocol config
	 */
	public static GlobalAddressProtocolConfig create() {
		final GlobalAddressProtocolConfig res = new GlobalAddressProtocolConfig();
		res.setBuilder(BUILDER);
		return res;
	}

	/**
	 * Instantiates a new GlobalAddress protocol config.
	 * 
	 * @param node
	 *            the node
	 */
	public static GlobalAddressProtocolConfig decorate(final ObjectNode node) {
		final GlobalAddressProtocolConfig res = new GlobalAddressProtocolConfig();
		res.extend(node);
		return res;
	}

}
