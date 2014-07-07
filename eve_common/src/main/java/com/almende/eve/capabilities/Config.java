/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.capabilities;

import java.util.List;
import java.util.logging.Logger;

import com.almende.util.TypeUtil;
import com.almende.util.jackson.JOM;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class Config.
 */
public class Config extends ObjectNode {
	private static final Logger LOG = Logger.getLogger(Config.class.getName());
	
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
		expand();
		
	}
	
	/**
	 * Extend this configuration with the other tree, overwriting existing
	 * fields, adding new ones.
	 * 
	 * @param other
	 *            the other
	 * @return the config
	 */
	public Config extend(final ObjectNode other) {
		this.setAll(other);

		return this;
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
	
	private JsonNode lget(final String... keys) {
		if (keys == null || keys.length == 0) {
			return null;
		}
		JsonNode node = this;
		for (final String key : keys) {
			node = node.get(key);
			if (node == null) {
				break;
			}
		}
		if (node == null) {
			return null;
		}
		return node;
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
	public <T> T get(final String... keys) {
		JsonNode node = lget(keys);
		final TypeUtil<T> tu = new TypeUtil<T>() {
		};
		return tu.inject(node);
	}
	
	/**
	 * Expand.
	 */
	public void expand(){
		int count=0;
		while(lexpand()){
			count++;
			if (count >= 100){
				LOG.warning("Too deep 'extends' nesting in configuration!");
				break;
			}
		};
		
	}
	
	private boolean lexpand() {
		List<JsonNode> extendNodes = this.findParents("extends");
		if (extendNodes.size() == 0) {
			return false;
		}
		for (final JsonNode node : extendNodes) {
			final String path = node.get("extends").textValue();
			final ObjectNode parent = (ObjectNode) node;
			final ObjectNode clone = ((ObjectNode) this.lget(path.split("/")))
					.deepCopy();
			parent.remove("extends");
			clone.setAll(parent);
			parent.setAll(clone);
		}
		return true;
	}
	
}
