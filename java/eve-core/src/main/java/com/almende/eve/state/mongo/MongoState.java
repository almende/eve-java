/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.state.mongo;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jongo.MongoCollection;
import org.jongo.marshall.jackson.oid.Id;

import com.almende.eve.state.AbstractState;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;

import com.mongodb.WriteResult;

/**
 * Simple representation of Eve agents state based on MongoDB.
 * 
 * @author ronny
 */
public class MongoState extends AbstractState<JsonNode> {
	
	private static final Logger		LOG			= Logger.getLogger("MongoState");
	
	/* mapping object that contains variables used by the agent */
	private Map<String, JsonNode> properties	= Collections.synchronizedMap(new HashMap<String, JsonNode>());
	
	/* metadata for agenthost : agent type and last update for a simple update conflict avoidance */
	private Class<?> agentType;
	private Date timestamp;
	
	
	@JsonIgnore
	private MongoCollection collection;
	
	/**
	 * default constructor, used when instantiating state while fetching the
	 * appropriate agents.
	 * 
	 * @see com.almende.eve.state.AbstractState#AbstractState()
	 */
	public MongoState() {
	}
	
	/**
	 * the constructor used on creation of new state in the database.
	 * 
	 * @param agentId
	 *            the agent id
	 */
	public MongoState(final String agentId) {
		super(agentId);
		timestamp = Calendar.getInstance().getTime();
		agentType = null;
	}
	
	/**
	 * Sets the collection.
	 * 
	 * @param collection
	 *            the new collection
	 */
	@JsonIgnore
	public void setCollection(MongoCollection collection) {
		this.collection = collection;
	}
	
	/**
	 * Gets the collection.
	 * 
	 * @return the collection
	 */
	@JsonIgnore
	public MongoCollection getCollection() {
		return collection;
	}
	
	/**
	 * Gets the timestamp.
	 * 
	 * @return the timestamp
	 */
	public Date getTimestamp() {
		return timestamp;
	}
	
	/**
	 * returns agent ID and adding @Id annotation to mark it as objectID in
	 * Mongo
	 * otherwise mongo will generate new object each time.
	 * 
	 * @return the agent id
	 * @see com.almende.eve.state.State#getAgentId()
	 */
	@Override
	@Id
	public synchronized String getAgentId() {
		return super.getAgentId();
	}
	
	/**
	 * agent type is considered as a separate attribute, not a common property.
	 * 
	 * @param agentType
	 *            the new agent type
	 */
	@Override
	public synchronized void setAgentType(final Class<?> agentType) {
		this.agentType = agentType;
		// assuming this is called only once after creation, simply save the entire state
		collection.save(this);
	}
	
	/* (non-Javadoc)
	 * @see com.almende.eve.state.AbstractState#getAgentType()
	 */
	@Override
	public synchronized Class<?> getAgentType() throws ClassNotFoundException {
		return this.agentType;
	}

	/* (non-Javadoc)
	 * @see com.almende.eve.state.State#init()
	 */
	@Override
	public void init() {
	}
	
	/* (non-Javadoc)
	 * @see com.almende.eve.state.State#destroy()
	 */
	@Override
	public void destroy() {
		this.collection = null;
	}

	/* (non-Javadoc)
	 * @see com.almende.eve.state.State#remove(java.lang.String)
	 */
	@Override
	public synchronized Object remove(final String key) {
		Object result = null;
		try {
			result = properties.remove(key);
			updateProperties(false);
		} catch (final Exception e) {
			LOG.log(Level.WARNING, "remove error", e);
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see com.almende.eve.state.State#containsKey(java.lang.String)
	 */
	@Override
	public boolean containsKey(final String key) {
		boolean result = false;
		try {
			result = properties.containsKey(key);
		} catch (final Exception e) {
			LOG.log(Level.WARNING, "containsKey error", e);
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see com.almende.eve.state.State#keySet()
	 */
	@Override
	public Set<String> keySet() {
		Set<String> result = null;
		try {
			result = properties.keySet();
		} catch (final Exception e) {
			LOG.log(Level.WARNING, "keySet error", e);
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see com.almende.eve.state.State#clear()
	 */
	@Override
	public void clear() {
		try {
			properties.clear();
			updateProperties(false);
		} catch (final Exception e) {
			LOG.log(Level.WARNING, "clear error", e);
		}	
	}


	/* (non-Javadoc)
	 * @see com.almende.eve.state.State#size()
	 */
	@Override
	public int size() {
		int result = 0;
		try {
			result = properties.size();
		} catch (final Exception e) {
			LOG.log(Level.WARNING, "size error", e);
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see com.almende.eve.state.AbstractState#get(java.lang.String)
	 */
	@Override
	public JsonNode get(String key) {
		JsonNode result = null;
		try {
			result = properties.get(key);
		} catch (final Exception e) {
			LOG.log(Level.WARNING, "get error", e);
		}
		return result;
	}
	
	/* (non-Javadoc)
	 * @see com.almende.eve.state.AbstractState#locPut(java.lang.String, com.fasterxml.jackson.databind.JsonNode)
	 */
	@Override
	public synchronized JsonNode locPut(final String key, final JsonNode value) {
		JsonNode result = null;
		try {
			result = properties.put(key, value);
			updateProperties(false); // updateField(key, value);
		} catch (final Exception e) {
			LOG.log(Level.WARNING, "locPut error", e);
		}
		
		return result;
	}
	
	/* (non-Javadoc)
	 * @see com.almende.eve.state.AbstractState#locPutIfUnchanged(java.lang.String, com.fasterxml.jackson.databind.JsonNode, com.fasterxml.jackson.databind.JsonNode)
	 */
	@Override
	public synchronized boolean locPutIfUnchanged(final String key,
			final JsonNode newVal, JsonNode oldVal) {
		boolean result = false;
		try {
			JsonNode cur = NullNode.getInstance();
			if (properties.containsKey(key)) {
				cur = properties.get(key);
			}
			if (oldVal == null) {
				oldVal = NullNode.getInstance();
			}
			
			// Poor man's equality as some Numbers are compared incorrectly: e.g.
			// IntNode versus LongNode
			if (oldVal.equals(cur) || oldVal.toString().equals(cur.toString())) {
				properties.put(key, newVal);
				result = updateProperties(false); // updateField(key, newVal);
			}
		} catch (final Exception e) {
			LOG.log(Level.WARNING, "locPutIfUnchanged error", e);
		}
		
		return result;
	}
	
	/**
	 * returns agent properties as a mapped collection of JSON nodes.
	 * 
	 * @return the properties
	 */
	public Map<String, JsonNode> getProperties() {
		return properties;
	}
	
	/**
	 * set all property values from a collection.
	 * 
	 * @param properties
	 *            the properties
	 */
	public void setProperties(final Map<String, JsonNode> properties) {
		this.properties.clear();
		this.properties.putAll(properties);
		updateProperties(false);
	}
	
	/**
	 * FIXME this method still has errors on serializing/deserializing <value> parameter, probably 
	 * related with the mappers of Jongo
	 * 
	 * update a single field, by default will fail if there is a newer update from some other instance
	 * @throws JsonProcessingException 
	 */
	@SuppressWarnings("unused")
	private synchronized boolean updateField(String field, JsonNode value) throws JsonProcessingException {
		Date now = Calendar.getInstance().getTime();
		WriteResult result = collection.update("{_id: #, timestamp: #}", getAgentId(), timestamp).
				with("{$set: {properties."+field+": #, timestamp: #}}", value, now);
		Boolean updatedExisting = (Boolean) result.getField("updatedExisting");
		if (updatedExisting) {
			timestamp = now;
		} 
		return updatedExisting;
	} 
	
	/**
	 * updating the entire properties object at the same time, with force flag to allow overwriting of updates
	 * from other instances of the state
	 * 
	 * @param force
	 * 
	 */
	private synchronized boolean updateProperties(boolean force) {
		Date now = Calendar.getInstance().getTime();
		/* write to database */
		WriteResult result = (force) ?
				collection.update("{_id: #}", getAgentId()).with("{$set: {properties: #, timestamp: #}}", properties, now) :
				collection.update("{_id: #, timestamp: #}", getAgentId(), timestamp).with("{$set: {properties: #, timestamp: #}}", properties, now);
		/* check results */
		Boolean updatedExisting = (Boolean) result.getField("updatedExisting");
		if (updatedExisting) {
			timestamp = now;
		}
		return updatedExisting;
	}

}
