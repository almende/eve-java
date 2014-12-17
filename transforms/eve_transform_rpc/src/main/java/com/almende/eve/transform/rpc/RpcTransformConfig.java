/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.transform.rpc;

import com.almende.eve.transform.TransformConfig;
import com.almende.util.jackson.JOM;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class RpcTransformConfig.
 */
public class RpcTransformConfig extends TransformConfig {
	
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
			setClassName(RpcTransformBuilder.class.getName());
		}
	}
	
	/**
	 * Sets the callback timeout. (in seconds)
	 * 
	 * @param timeout
	 *            the new callback timeout
	 */
	public void setCallbackTimeout(final int timeout){
		this.put("rpcTimeout", timeout);
	}
	
	/**
	 * Gets the callback timeout. (in seconds)
	 * 
	 * @return the callback timeout
	 */
	public int getCallbackTimeout(){
		if (this.has("rpcTimeout")){
			return this.get("rpcTimeout").asInt();
		}
		return 30;
	}
}
