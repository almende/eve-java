/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.protocol.jsonrpc;

import com.almende.eve.protocol.ProtocolConfig;
import com.almende.util.jackson.JOM;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class JSONRpcProtocolConfig.
 */
public class JSONRpcProtocolConfig extends ProtocolConfig {

	/**
	 * Instantiates a new JSON rpc protocol config.
	 */
	public JSONRpcProtocolConfig() {
		this(JOM.createObjectNode());
	}

	/**
	 * Instantiates a new JSON rpc protocol config.
	 * 
	 * @param node
	 *            the node
	 */
	public JSONRpcProtocolConfig(final ObjectNode node) {
		super(node);
		if (!this.has("class")) {
			setClassName(JSONRpcProtocolBuilder.class.getName());
		}
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
