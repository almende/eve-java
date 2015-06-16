/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.protocol.jsonrpc;

import com.almende.eve.protocol.ProtocolConfig;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class JSONRpcProtocolConfig.
 */
public class JSONRpcProtocolConfig extends ProtocolConfig {
	private static final String	BUILDER	= JSONRpcProtocolBuilder.class
												.getName();

	/**
	 * Instantiates a new JSON rpc protocol config.
	 */
	protected JSONRpcProtocolConfig() {
		super();
	}

	/**
	 * Instantiates a new JSON rpc protocol config.
	 *
	 * @return the JSON rpc protocol config
	 */
	public static JSONRpcProtocolConfig create() {
		final JSONRpcProtocolConfig res = new JSONRpcProtocolConfig();
		res.setBuilder(BUILDER);
		return res;
	}

	/**
	 * Instantiates a new JSON rpc protocol config.
	 *
	 * @param node
	 *            the node
	 * @return the JSON rpc protocol config
	 */
	public static JSONRpcProtocolConfig decorate(final ObjectNode node) {
		final JSONRpcProtocolConfig res = new JSONRpcProtocolConfig();
		res.extend(node);
		return res;
	}

	/**
	 * Sets the callback timeout. (in seconds)
	 * 
	 * @param timeout
	 *            the new callback timeout
	 */
	public void setCallbackTimeout(final int timeout) {
		this.put("rpcTimeout", timeout);
	}

	/**
	 * Gets the callback timeout. (in seconds)
	 * 
	 * @return the callback timeout
	 */
	public int getCallbackTimeout() {
		if (this.has("rpcTimeout")) {
			return this.get("rpcTimeout").asInt();
		}
		return 30;
	}
}
