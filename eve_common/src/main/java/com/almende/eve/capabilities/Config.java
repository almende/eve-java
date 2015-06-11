/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.capabilities;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import com.almende.util.TypeUtil;
import com.almende.util.jackson.JOM;
import com.almende.util.uuid.UUID;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class Config.
 */
public class Config extends ObjectNode {
	private static Config	global		= new Config();

	private List<Config>	pointers	= new LinkedList<Config>();
	private boolean			configured	= false;

	/**
	 * Instantiates a new config.
	 */
	public Config() {
		super(JOM.getInstance().getNodeFactory(),
				new LinkedHashMap<String, JsonNode>(2));
	}

	private Config(final ObjectNode node) {
		super(JOM.getInstance().getNodeFactory(),
				new LinkedHashMap<String, JsonNode>(node != null ? node.size()
						: 2));
		if (node != null) {
			this.setAll(node);
		}
	}

	/**
	 * Decorate.
	 *
	 * @param node
	 *            the node
	 * @return the config
	 */
	public static Config decorate(final ObjectNode node) {
		Config res = null;
		if (node instanceof Config) {
			res = (Config) node;
		} else {
			res = new Config(node);
		}
		return res;
	}

	/**
	 * Extend this configuration with the other tree, overwriting existing
	 * fields, adding new ones.
	 *
	 * @param node
	 *            the node
	 */
	public void extend(final ObjectNode node) {
		if (node != null) {
			this.setAll(node);
			if (node instanceof Config) {
				this.pointers.addAll(((Config) node).pointers);
			}
		}
	}

	/**
	 * Gets the global.
	 *
	 * @return the global
	 */
	public static ObjectNode getGlobal() {
		return global;
	}

	/**
	 * Gets the pointers.
	 *
	 * @return the pointers
	 */
	public List<Config> getPointers() {
		return pointers;
	}

	/**
	 * Sets the pointers.
	 *
	 * @param pointers
	 *            the new pointers
	 */
	public void setPointers(List<Config> pointers) {
		this.pointers = pointers;
	}

	/**
	 * Load templates.
	 *
	 * @param fieldName
	 *            the field name
	 */
	public void loadTemplates(final String fieldName) {
		if (this.has(fieldName)) {
			global.set(fieldName, this.get(fieldName));
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
	
	@Override
	public Iterator<Entry<String,JsonNode>> fields(){
		final LinkedHashMap<String,JsonNode> fields = new LinkedHashMap<String,JsonNode>();
		final Iterator<Entry<String,JsonNode>> supr = super.fields();
		while (supr.hasNext()){
			Entry<String,JsonNode> item = supr.next();
			fields.put(item.getKey(), item.getValue());
		}
		for (Config other : getPointers()) {
			final Iterator<Entry<String,JsonNode>> othr = other.fields();
			while (othr.hasNext()){
				Entry<String,JsonNode> item = othr.next();
				fields.put(item.getKey(), item.getValue());
			}			
		}
		return fields.entrySet().iterator();
	}
	
	@Override
	public JsonNode get(final String key) {
		JsonNode res = super.get(key);
		if ((res != null && !res.isObject()) || "extends".equals(key)) {
			return res;
		}
		if (!configured && this.has("extends")) {
			setupExtend();
		}
		JsonNode otherres = null;
		Config result = null;
		for (Config other : getPointers()) {
			JsonNode val = other.get(key);
			if (val == null) {
				continue;
			}
			if (val.isArray()) {
				final ArrayNode array = JOM.createArrayNode();
				for (JsonNode elem : val) {
					final Config item = new Config();
					item.pointers.add(Config.decorate((ObjectNode) elem));
					array.add(item);
				}
				this.set(key, array);
				otherres = array;
			} else if (!val.isObject()) {
				otherres = val;
			} else {
				if (result == null) {
					result = Config.decorate((ObjectNode) res);
				}
				result.getPointers().add(Config.decorate((ObjectNode) val));
			}
		}
		if (otherres != null) {
			return otherres;
		}
		if (result != null) {
			return result;
		}
		return res;
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
		final TypeUtil<T> tu = new TypeUtil<T>() {};
		return tu.inject(node);
	}

	@Override
	public ObjectNode deepCopy() {
		final ObjectNode result = JOM.createObjectNode();
		for (Config other : pointers) {
			result.setAll(other.deepCopy());
		}
		result.setAll(super.deepCopy());
		return result;
	}

	/**
	 * Setup extends.
	 */
	public void setupExtend() {
		configured = true;
		final JsonNode extNode = this.get("extends");
		if (extNode == null || extNode.isNull()) {
			return;
		}
		ObjectNode reference = null;
		boolean found = false;
		final String path = extNode.textValue();
		if (path != null && !path.equals("")) {
			reference = (ObjectNode) this.lget(path.split("/"));
			if (reference == null || reference.isNull()) {
				reference = (ObjectNode) global.lget(path.split("/"));
				found = true;
			}
		}
		if (reference != null && !reference.isNull()) {
			Config refConf = null;
			if (reference instanceof Config) {
				refConf = (Config) reference;
			} else {
				refConf = new Config((ObjectNode) reference);
			}

			if (!found) {
				String ref = new UUID().toString();
				global.set(ref, refConf);
			}
			getPointers().add(refConf);
		}
	}
}
