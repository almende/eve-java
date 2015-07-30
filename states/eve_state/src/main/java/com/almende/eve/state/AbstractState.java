/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.state;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.logging.Logger;

import com.almende.util.TypeUtil;
import com.almende.util.jackson.JOM;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class AbstractState.
 * 
 * @author Almende
 * @param <V>
 *            the value type
 */
public abstract class AbstractState<V> implements State {
	private static final Logger	LOG			= Logger.getLogger(AbstractState.class
													.getCanonicalName());
	private String				id			= null;
	private StateService		service		= null;
	private ObjectNode			myParams	= null;
	
	/**
	 * The implemented classes must have a public constructor.
	 */
	public AbstractState() {
	}
	
	/**
	 * The implemented classes must have this public constructor with
	 * parameters AgentHost, and agentId.
	 * 
	 * @param id
	 *            the agent id
	 * @param service
	 *            the service in which this instance is created
	 * @param params
	 *            the params
	 */
	public AbstractState(final String id, final StateService service,
			final ObjectNode params) {
		this.id = id;
		this.service = service;
		this.myParams = params;
	}
	
	/**
	 * Get the id.
	 * 
	 * @return id
	 */
	@Override
	public synchronized String getId() {
		return id;
	}
	
	/**
	 * Sets the id.
	 * 
	 * @param id
	 *            the new agent id
	 */
	public void setId(final String id) {
		this.id = id;
	}
	
	/**
	 * Gets the service.
	 * 
	 * @return the service
	 */
	@JsonIgnore
	public StateService getService() {
		return service;
	}
	
	/**
	 * Sets the service.
	 * 
	 * @param service
	 *            the new service
	 */
	public void setService(final StateService service) {
		this.service = service;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.almende.eve.state.State#put(java.lang.String, java.lang.Object)
	 */
	@Override
	public synchronized Object put(final String key, final Object value) {
		if (value != null && JsonNode.class.isAssignableFrom(value.getClass())) {
			return locPut(key, (JsonNode) value);
		} else if (Serializable.class.isAssignableFrom(value.getClass())) {
			return locPut(key, (Serializable) value);
		} else {
			LOG.severe("Can't handle input that is not Serializable nor JsonNode.");
			throw new IllegalArgumentException("Can't handle input that is not Serializable nor JsonNode.");
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.almende.eve.state.State#putIfUnchanged(java.lang.String,
	 * java.lang.Object, java.lang.Object)
	 */
	@Override
	public synchronized boolean putIfUnchanged(final String key,
			final Object newVal, final Object oldVal) {
		if (newVal == null
				|| Serializable.class.isAssignableFrom(newVal.getClass())) {
			return locPutIfUnchanged(key, (Serializable) newVal,
					(Serializable) oldVal);
		} else if (JsonNode.class.isAssignableFrom(newVal.getClass())) {
			return locPutIfUnchanged(key, (JsonNode) newVal, (JsonNode) oldVal);
		} else {
			LOG.severe("Can't handle input that is not Serializable nor JsonNode.");
			throw new IllegalArgumentException();
		}
	}
	
	/**
	 * Gets the.
	 * 
	 * @param key
	 *            the key
	 * @return the v
	 */
	@JsonIgnore
	public abstract V get(String key);
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.almende.eve.state.State#get(java.lang.String, java.lang.Class)
	 */
	@Override
	@JsonIgnore
	public <T> T get(final String key, final Class<T> type) {
		return TypeUtil.inject(get(key), type);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.almende.eve.state.State#get(java.lang.String,
	 * java.lang.reflect.Type)
	 */
	@Override
	@JsonIgnore
	public <T> T get(final String key, final Type type) {
		return TypeUtil.inject(get(key), type);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.almende.eve.state.State#get(java.lang.String,
	 * com.fasterxml.jackson.databind.JavaType)
	 */
	@Override
	@JsonIgnore
	public <T> T get(final String key, final JavaType type) {
		return TypeUtil.inject(get(key), type);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.almende.eve.state.State#get(java.lang.String,
	 * com.almende.util.TypeUtil)
	 */
	@Override
	@JsonIgnore
	public <T> T get(final String key, final TypeUtil<T> type) {
		return type.inject(get(key));
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.almende.eve.state.State#get(com.almende.eve.state.TypedKey)
	 */
	@Override
	@JsonIgnore
	public <T> T get(final TypedKey<T> typedKey) {
		return get(typedKey.getKey(), typedKey.getType());
	}
	
	/**
	 * Loc put.
	 * 
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * @return the json node
	 */
	public JsonNode locPut(final String key, final JsonNode value) {
		LOG.warning("Warning, this type of State can't store JsonNodes, only Serializable objects. This JsonNode is stored as string.");
		locPut(key, value.toString());
		return value;
	}
	
	// Default cross type input acceptance, specific States are expected to
	// override their own typed version.
	/**
	 * Loc put if unchanged.
	 * 
	 * @param key
	 *            the key
	 * @param newVal
	 *            the new val
	 * @param oldVal
	 *            the old val
	 * @return true, if successful
	 */
	public boolean locPutIfUnchanged(final String key, final JsonNode newVal,
			final JsonNode oldVal) {
		LOG.warning("Warning, this type of State can't store JsonNodes, only Serializable objects. This JsonNode is stored as string.");
		return locPutIfUnchanged(key, newVal.toString(), oldVal.toString());
	}
	
	/**
	 * Loc put.
	 * 
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * @return the serializable
	 */
	public synchronized Serializable locPut(final String key,
			final Serializable value) {
		final ObjectMapper om = JOM.getInstance();
		locPut(key, om.valueToTree(value));
		return value;
	}
	
	/**
	 * Loc put if unchanged.
	 * 
	 * @param key
	 *            the key
	 * @param newVal
	 *            the new val
	 * @param oldVal
	 *            the old val
	 * @return true, if successful
	 */
	public boolean locPutIfUnchanged(final String key,
			final Serializable newVal, final Serializable oldVal) {
		final ObjectMapper om = JOM.getInstance();
		return locPutIfUnchanged(key, om.valueToTree(newVal),
				om.valueToTree(oldVal));
	}
	
	/*
         * (non-Javadoc)
         * 
         * @see com.almende.eve.state.State#delete()
         */
	@Override
        public void delete() {
	    this.delete(false);
	}
	
	/*
         * (non-Javadoc)
         * 
         * @see com.almende.eve.state.State#delete()
         */
	public void delete(Boolean instanceOnly) {
		if (service != null) {
		        if(!instanceOnly) {
		            clear();
		        }
			service.delete(this, instanceOnly);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.almende.eve.capabilities.Capability#getParams()
	 */
	@Override
	@JsonIgnore
	public ObjectNode getParams() {
		return myParams;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		for (final String key : keySet()) {
			try {
				result.append("'"
						+ key
						+ "': "
						+ JOM.getInstance().writeValueAsString(
								get(key, JsonNode.class)));
			} catch (final JsonProcessingException e) {
				result.append("'" + key + "': [unprintable]");
			}
			result.append("\n");
		}
		return result.toString();
	}
}
