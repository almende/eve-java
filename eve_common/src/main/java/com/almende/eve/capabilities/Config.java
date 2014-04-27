/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.capabilities;

import com.almende.util.TypeUtil;
import com.almende.util.jackson.JOM;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class Config.
 */
public class Config extends ObjectNode {
	
	/**
	 * Instantiates a new config.
	 */
	public Config() {
		super(JOM.getInstance().getNodeFactory());
	}
	
	/**
	 * Instantiates a new config.
	 * 
	 * @param node
	 *            the node
	 */
	public Config(final ObjectNode node) {
		super(JOM.getInstance().getNodeFactory());
		if (node != null) {
			this.setAll(node);
		}
	}
	
	/**
	 * Sets the class path. (Required)
	 * 
	 * @param className
	 *            the new class
	 */
	public void setClassName(final String className) {
		this.put("class", className);
	}
	
	/**
	 * Gets the class path.
	 * 
	 * @return the class path
	 */
	public String getClassName() {
		if (this.has("class")) {
			return this.get("class").asText();
		}
		return null;
	}
	
	/**
	 * Gets the.
	 * 
	 * @param <T>
	 *            the generic type
	 * @param keys
	 *            the keys
	 * @return the json node
	 */
	public <T> T get(String... keys){
		if (keys == null || keys.length == 0){
			return null;
		}
		JsonNode node = this;
		for (String key : keys){
			node = node.get(key);
			if (node == null){
				break;
			}
		}
		if (node == null){
			return null;
		}
		TypeUtil<T> tu = new TypeUtil<T>(){};
		return tu.inject(node);
	}
}
