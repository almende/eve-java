/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.config;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import com.almende.util.TypeUtil;
import com.almende.util.jackson.JOM;
import com.almende.util.uuid.UUID;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class Config.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS,
		include = JsonTypeInfo.As.PROPERTY,
		property = "class")
public class Config extends ObjectNode {
	private static final Logger	LOG		= Logger.getLogger(Config.class
												.getName());
	private static Config		global	= new Config();

	/**
	 * Instantiates a new config.
	 */
	protected Config() {
		super(JOM.getInstance().getNodeFactory(), new MyKids(2));
	}

	private Config(final ObjectNode node) {
		super(JOM.getInstance().getNodeFactory(), new MyKids(
				node != null ? node.size() : 2));
		if (node != null) {
			this.setAll(node);
		}
	}

	static class MyKids extends LinkedHashMap<String, JsonNode> {
		private static final long	serialVersionUID	= 7450473642684901780L;
		public List<Config>			pointers			= new LinkedList<Config>();
		private boolean				configured			= false;

		public MyKids(int capacity) {
			super(capacity);
		}

		@Override
		public int size() {
			return entrySet().size();
		}

		@SuppressWarnings("unchecked")
		public Map<String, JsonNode> getParentMap() {
			return (Map<String, JsonNode>) super.clone();
		}

		public void putAll(MyKids map) {
			super.putAll(map.getParentMap());
			pointers.addAll(map.pointers);
		}

		@Override
		public Set<Entry<String, JsonNode>> entrySet() {
			final Map<String, Entry<String, JsonNode>> map = new HashMap<String, Entry<String, JsonNode>>();
			for (Config other : pointers) {
				for (Entry<String, JsonNode> entry : other.getKids().entrySet()) {
					map.put(entry.getKey(), entry);
				}
			}
			for (Entry<String, JsonNode> entry : super.entrySet()) {
				map.put(entry.getKey(), entry);
			}
			return new HashSet<Entry<String, JsonNode>>(map.values());
		}

		@Override
		public Collection<JsonNode> values() {
			Collection<JsonNode> result = new ArrayList<JsonNode>();
			for (Entry<String, JsonNode> entry : entrySet()) {
				result.add(entry.getValue());
			}
			return result;
		}

		@Override
		public Set<String> keySet() {
			Set<String> result = new HashSet<String>();
			for (Entry<String, JsonNode> entry : entrySet()) {
				result.add(entry.getKey());
			}
			return result;
		}

		@Override
		public void clear() {
			super.clear();
			pointers.clear();
		}

		@Override
		public JsonNode get(final Object key) {
			if (!(key instanceof String)) {
				return null;
			}
			final String strKey = (String) key;
			JsonNode res = super.get(key);
			if ((res != null && !res.isObject()) || "extends".equals(key)) {
				return res;
			}
			if (!configured && this.containsKey("extends")) {
				setupExtend();
			}
			JsonNode otherres = null;
			Config result = null;
			for (Config other : pointers) {
				JsonNode val = other.get(strKey);
				if (val == null) {
					continue;
				}
				if (val.isArray()) {
					final ArrayNode array = JOM.createArrayNode();
					for (JsonNode elem : val) {
						final Config item = new Config();
						item.getKids().pointers.add(Config
								.decorate((ObjectNode) elem));
						array.add(item);
					}
					super.put(strKey, array);
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
			if (res == null && otherres != null) {
				return otherres;
			}
			if (result != null) {
				if (res != null && res.isObject()) {
					// Merge the results
					result.setAll((ObjectNode) res);
					return result;
				}
				if (res == null) {
					return result;
				}
			}
			return res;
		}

		private JsonNode lget(final String... keys) {
			if (keys == null || keys.length == 0) {
				return null;
			}
			JsonNode node = this.get(keys[0]);
			if (node == null) {
				return null;
			}
			for (int i = 1; i < keys.length; i++) {
				node = node.get(keys[i]);
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
		 * Setup extends.
		 */
		public void setupExtend() {
			configured = true;
			final JsonNode extNode = this.get("extends");
			if (extNode == null || extNode.isNull()) {
				return;
			}
			JsonNode reference = null;
			boolean found = false;
			final String path = extNode.textValue();
			if (path != null && !path.equals("")) {
				reference = this.lget(path.split("/"));
				if (reference == null || reference.isNull()) {
					reference = global.lget(path.split("/"));
					found = true;
				}
			}
			if (reference != null) {
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
				pointers.add(refConf);
			}
		}
	}

	/**
	 * Load a configuration file.
	 *
	 * @param type
	 *            the type, one of: ["yaml","json"]
	 * @param is
	 *            the inputStream from the config file
	 * @return the config
	 */
	public static Config load(final String type, final InputStream is) {
		switch (type) {
			case "yaml":
				return YamlReader.load(is);
			case "json":
				return JsonReader.load(is);
			default:
				LOG.warning("Unknown file type given for configuration:'"
						+ type + "' Trying to read Yaml format.");
				return YamlReader.load(is);
		}
	}

	/**
	 * Poor man's file extension interpreter.
	 *
	 * @param filename
	 *            the filename
	 * @return the type
	 */
	public static String getType(final String filename) {
		return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase(
				Locale.ENGLISH);
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

	private MyKids getKids() {
		return (MyKids) this._children;
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
				getKids().pointers.addAll(((Config) node).getKids().pointers);
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
		return getKids().pointers;
	}

	/**
	 * Sets the pointers.
	 *
	 * @param pointers
	 *            the new pointers
	 */
	public void setPointers(List<Config> pointers) {
		getKids().pointers = pointers;
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
	 * Sets the class path.
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
	 * Gets the classname of the builder for this type.
	 *
	 * @param className
	 *            the classname of the builder
	 */
	public void setBuilder(final String className) {
		this.put("builder", className);
	}

	/**
	 * Gets the builder className for this type.
	 *
	 * @return the classname of the builder
	 */
	public String getBuilder() {
		if (this.has("builder")) {
			return this.get("builder").asText();
		} else {
			LOG.warning("Couldn't find 'builder' field, falling back to the backwards compatibility 'class' field.");
			return getClassName();
		}
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
		final TypeUtil<T> tu = new TypeUtil<T>() {};
		return tu.inject(node);
	}

	@Override
	public ObjectNode deepCopy() {
		final ObjectNode result = JOM.createObjectNode();
		for (Config other : getKids().pointers) {
			result.setAll(other.deepCopy());
		}
		result.setAll(super.deepCopy());
		return result;
	}
}
