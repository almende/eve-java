/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.state.redis;

import com.almende.eve.state.StateConfig;
import com.almende.util.jackson.JOM;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class MemoryStateConfig.
 */
public class RedisStateConfig extends StateConfig {
	
	/**
	 * Instantiates a new memory state config.
	 */
	public RedisStateConfig() {
		this(JOM.createObjectNode());
	}
	
	/**
	 * Instantiates a new memory state config.
	 * 
	 * @param node
	 *            the node
	 */
	public RedisStateConfig(final ObjectNode node) {
		super(node);
		if (!node.has("class")) {
			this.put("class", RedisStateBuilder.class.getName());
		}
	}
	/**
	 * Gets the host.
	 *
	 * @return the host
	 */
	public String getHost(){
		if (this.has("host")){
			return this.get("host").asText();
		}
		return "localhost";
	}
	
	/**
	 * Sets the host.
	 *
	 * @param host
	 *            the new host
	 */
	public void setHost(final String host){
		this.put("host", host);
	}

	/**
	 * Gets the db id.
	 *
	 * @return the db id
	 */
	public int getDbId() {
		if (this.has("dbId")){
			return this.get("dbId").asInt();
		}
		return 0;
	}
	
	/**
	 * Sets the db id.
	 *
	 * @param id
	 *            the new db id
	 */
	public void setDbId(final int id){
		this.put("dbId",id);
	}
}
