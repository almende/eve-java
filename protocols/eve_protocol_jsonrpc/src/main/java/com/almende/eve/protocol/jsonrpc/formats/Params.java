/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.protocol.jsonrpc.formats;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.almende.util.jackson.JOM;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class Params.
 */
public class Params extends ObjectNode {
	private static final ObjectMapper	MAPPER	= JOM.getInstance();

	static class String2JsonNodeArrayMap implements Map<String, JsonNode> {
		private int			size		= 0;
		private int			deleted		= 0;
		private int			capacity	= 2;
		private String[]	keys		= new String[capacity];
		private JsonNode[]	values		= new JsonNode[capacity];

		@Override
		public int size() {
			return size - deleted;
		}

		@Override
		public boolean isEmpty() {
			return size == 0;
		}

		@Override
		public boolean containsKey(Object key) {
			for (int i = 0; i < size; i++) {
				if (keys[i].equals(key)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public boolean containsValue(Object value) {
			for (int i = 0; i < size; i++) {
				if (values[i].equals(value)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public JsonNode get(Object key) {
			for (int i = 0; i < size; i++) {
				if (keys[i].equals(key)) {
					return values[i];
				}
			}
			return null;
		}

		private void growCapacity() {
			capacity = 2 * capacity;
			keys = Arrays.copyOf(keys, capacity);
			values = Arrays.copyOf(values, capacity);
		}

		@Override
		public JsonNode put(String key, JsonNode value) {
			for (int i = 0; i < size; i++) {
				if (keys[i].equals(key)) {
					final JsonNode oldval = values[i];
					values[i] = value;
					return oldval;
				}
			}
			if (size == capacity) {
				growCapacity();
			}
			keys[size] = key;
			values[size] = value;
			size++;
			return null;
		}

		@Override
		public JsonNode remove(Object key) {
			for (int i = 0; i < size; i++) {
				if (keys[i].equals(key)) {
					final JsonNode oldval = values[i];
					keys[i] = null;
					values[i] = null;
					deleted++;
					return oldval;
				}
			}
			return null;
		}

		@Override
		public void putAll(Map<? extends String, ? extends JsonNode> m) {
			for (Entry<? extends String, ? extends JsonNode> entry : m
					.entrySet()) {
				put(entry.getKey(), entry.getValue());
			}

		}

		@Override
		public void clear() {
			size = 0;
			deleted = 0;
			capacity = 2;
			keys = new String[capacity];
			values = new JsonNode[capacity];
		}

		@Override
		public Set<String> keySet() {
			final Set<String> res = new HashSet<String>(size());
			int count = 0;
			for (String key : keys) {
				if (key != null) {
					res.add(key);
				}
				if (count++ == size){
					break;
				}
			}
			return res;
		}

		@Override
		public Collection<JsonNode> values() {
			final Collection<JsonNode> res = new ArrayList<JsonNode>(size());
			int count = 0;
			for (JsonNode value : values) {
				if (value != null) {
					res.add(value);
				}
				if (count++ == size){
					break;
				}
			}
			return res;
		}

		@Override
		public Set<Entry<String, JsonNode>> entrySet() {
			final Set<Entry<String,JsonNode>> res = new HashSet<Entry<String,JsonNode>>(size());
			for (int i = 0; i< size; i++){
				final int j = i;
				final String key = keys[i];
				if (key != null){
					res.add(new Entry<String,JsonNode>() {
						@Override
						public JsonNode setValue(JsonNode newval) {
							final JsonNode value = values[j];
							values[j]=newval;
							return value;
						}
						
						@Override
						public JsonNode getValue() {
							return values[j];
						}
						
						@Override
						public String getKey() {
							return keys[j];
						}
					});
				}
			}
			return res;
		}

	}

	/**
	 * Instantiates a new config.
	 */
	public Params() {
		super(MAPPER.getNodeFactory(), new String2JsonNodeArrayMap());
	}

	/**
	 * Instantiates a new params.
	 *
	 * @param node
	 *            the node
	 */
	public Params(final ObjectNode node) {
		super(MAPPER.getNodeFactory(), new String2JsonNodeArrayMap());
		if (node != null) {
			this.setAll(node);
		}
	}

	/**
	 * Instantiates a new params, with given text fields (Convenience method)
	 *
	 * @param strings
	 *            the strings
	 */
	public Params(String... strings) {
		this();
		String last = null;
		for (String item : strings) {
			if (last == null) {
				last = item;
			} else {
				put(last, item);
				last = null;
			}
		}

	}

	/**
	 * Adds a parameter
	 *
	 * @param name
	 *            the name
	 * @param value
	 *            the value
	 */
	public void add(final String name, final Object value) {
		if (value instanceof JsonNode) {
			super.set(name, (JsonNode) value);
		} else {
			super.set(name, MAPPER.valueToTree(value));
		}
	}

	/**
	 * Extend these params with the other tree, overwriting existing
	 * fields, adding new ones.
	 * 
	 * @param other
	 *            the other
	 * @return the params
	 */
	public Params extend(final ObjectNode other) {
		this.setAll(other);
		return this;
	}
}
