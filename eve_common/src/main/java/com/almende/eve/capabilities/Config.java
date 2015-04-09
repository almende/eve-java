/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.capabilities;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Logger;

import com.almende.util.TypeUtil;
import com.almende.util.jackson.JOM;
import com.almende.util.uuid.UUID;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class Config.
 */
public class Config extends ObjectNode {
	private static final Logger	LOG		= Logger.getLogger(Config.class
												.getName());
	private static Config		global	= new Config();

	/**
	 * Instantiates a new config.
	 */
	public Config() {
		super(JOM.getInstance().getNodeFactory(),
				new LinkedHashMap<String, JsonNode>(5));
	}

	/**
	 * Instantiates a new config.
	 * 
	 * @param node
	 *            the node
	 */
	public Config(final ObjectNode node) {
		super(JOM.getInstance().getNodeFactory(),
				new LinkedHashMap<String, JsonNode>(node != null ? node.size()
						: 2));
		if (node != null) {
			this.setAll(node);
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

	@Override
	public JsonNode get(final String key) {
		JsonNode res = super.get(key);
		JsonNode other = null;
		if (!"extends".equals(key) && this.has("extends")) {
			other = get("extends").get(key);
		}
		if (other != null && res == null){
			return other;
		}
		if (other != null && other instanceof ObjectNode) {
			JsonNode clone = res.deepCopy();
			merge(clone, other);
			return clone;
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

	/**
	 * Expand this config (replace all occurences of 'extends' with referenced
	 * part of the tree)
	 * 
	 * @return the config
	 */
	public Config expand() {
		final ObjectNode config = this.deepCopy();
		int count = 0;
		while (lexpand(config)) {
			count++;
			if (count >= 100) {
				LOG.warning("Too deep 'extends' nesting in configuration!");
				break;
			}
		}
		return new Config(config);
	}

	private boolean lexpand(ObjectNode target) {
		List<JsonNode> extendNodes = target.findParents("extends");
		if (extendNodes.size() == 0) {
			return false;
		}
		for (final JsonNode node : extendNodes) {
			final ObjectNode parent = (ObjectNode) node;
			final JsonNode extNode = parent.remove("extends");
			ObjectNode reference = null;
			if (extNode.isObject()) {
				reference = (ObjectNode) extNode;
			} else {
				final String path = extNode.textValue();
				if (path != null && !path.equals("")) {
					reference = (ObjectNode) this.lget(path.split("/"));
					if (reference == null || reference.isNull()) {
						reference = (ObjectNode) global.lget(path.split("/"));
					}
				}
			}
			if (reference != null && !reference.isNull()) {
				final ObjectNode clone = reference.deepCopy();
				merge(clone, parent);
				parent.setAll(clone);
			}
		}
		return true;
	}

	/**
	 * Merge.
	 *
	 * @param target
	 *            the target
	 * @param reference
	 *            the reference
	 */
	private void merge(JsonNode target, JsonNode reference) {
		if (reference.isObject()) {
			Iterator<Entry<String, JsonNode>> iter = reference.fields();
			while (iter.hasNext()) {
				final Entry<String, JsonNode> item = iter.next();
				final String key = item.getKey();
				if (target.has(key)) {
					merge(target.get(key), item.getValue());
				} else {
					((ObjectNode) target).set(key, item.getValue());
				}
			}
		} else {
			target = reference;
		}
	}

	private boolean removeMatches(JsonNode target, JsonNode reference) {
		boolean found = false;
		if (target.equals(reference)) {
			return false;
		}
		if (target.isArray() && !reference.isArray()) {
			return true;
		}
		if (target.isObject() && !reference.isObject()) {
			return true;
		}
		if (target.isObject()) {
			Iterator<Entry<String, JsonNode>> iter = target.fields();
			while (iter.hasNext()) {
				final Entry<String, JsonNode> item = iter.next();
				final String key = item.getKey();
				if (!reference.has(key)) {
					found = true;
				} else {
					if (!removeMatches(item.getValue(), reference.get(key))) {
						iter.remove();
					} else {
						if (reference.get(key).isArray()) {
							((ObjectNode) reference).remove(key);
						}
						found = true;
					}
				}
			}
		} else {
			final Iterator<JsonNode> iter = target.elements();
			while (iter.hasNext()) {
				JsonNode node = iter.next();
				if (node.isObject()) {
					compressTree((ObjectNode) node);
				}
			}
			found = true;
		}
		return found;
	}

	private int countMatches(JsonNode target, JsonNode reference) {
		if (target.isArray() && !reference.isArray()) {
			return 0;
		}
		if (target.isObject() && !reference.isObject()) {
			return 0;
		}
		if (!target.isObject() && !target.isArray()) {
			return target.equals(reference) ? 1 : 0;
		}
		if (target.isObject()) {
			int res = 0;
			Iterator<Entry<String, JsonNode>> iter = target.fields();
			while (iter.hasNext()) {
				final Entry<String, JsonNode> item = iter.next();
				final String key = item.getKey();
				if (reference.has(key)) {
					res += countMatches(item.getValue(), reference.get(key));
				}
			}
			return res;
		} else {
			return 0;
		}
	}

	private String findRef(ObjectNode target) {
		final Iterator<Entry<String, JsonNode>> iter = global.fields();
		String bestMatch = "";
		int size = 0;
		while (iter.hasNext()) {
			final Entry<String, JsonNode> item = iter.next();
			int count = countMatches(target, item.getValue());
			if (count > size) {
				size = count;
				bestMatch = item.getKey();
			}
		}
		return bestMatch;
	}

	private void compressTree(ObjectNode target) {
		List<JsonNode> idNodes = target.findParents("id");
		ObjectNode clone = null;
		if (idNodes.size() > 0) {
			clone = target.deepCopy();
			idNodes = clone.findParents("id");
			for (final JsonNode node : idNodes) {
				((ObjectNode) node).remove("id");
			}
		} else {
			clone = target;
		}
		if (clone.size() > 0) {
			removeMatches(target, clone);

			String ref = findRef(clone);
			if (ref == null || ref.equals("")) {
				ref = new UUID().toString();
				global.set(ref, clone);
			} else {
				clone = (ObjectNode) global.get(ref);
			}
			target.set("extends", clone);
		}

	}

	/**
	 * Setup extends.
	 */
	public void setupExtends() {
		List<JsonNode> extendNodes = this.findParents("extends");
		if (extendNodes.size() == 0) {
			return;
		}
		for (final JsonNode node : extendNodes) {
			final ObjectNode parent = (ObjectNode) node;
			final JsonNode extNode = parent.remove("extends");
			if (extNode.isObject()) {
				continue;
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
				if (!found) {
					String ref = new UUID().toString();
					global.set(ref, reference);
				}
				parent.set("extends", reference);
			}
		}
	}

	/**
	 * Compress this config (Search for subtrees to extend templates)
	 *
	 * @return the config
	 */
	public Config compress() {
		setupExtends();
		if (!this.isArray()) {
			compressTree(this);
		} else {
			Iterator<JsonNode> iter = this.elements();
			while (iter.hasNext()) {
				final JsonNode node = iter.next();
				if (node.isObject()) {
					compressTree(new Config((ObjectNode) node));
				}
			}
		}
		return this;
	}
}
