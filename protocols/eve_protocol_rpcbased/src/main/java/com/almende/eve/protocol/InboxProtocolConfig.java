/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.protocol;

import com.almende.eve.protocol.ProtocolConfig;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class JSONRpcProtocolConfig.
 */
public class InboxProtocolConfig extends ProtocolConfig {

	/**
	 * Instantiates a new JSON rpc protocol config.
	 */
	public InboxProtocolConfig() {
		super();
		setClassName(InboxProtocolBuilder.class.getName());
	}

	/**
	 * Instantiates a new JSON rpc protocol config.
	 * 
	 * @param node
	 *            the node
	 */
	public static InboxProtocolConfig decorate(final ObjectNode node) {
		final InboxProtocolConfig res = new InboxProtocolConfig();
		res.copy(node);
		return res;
	}
	
}
