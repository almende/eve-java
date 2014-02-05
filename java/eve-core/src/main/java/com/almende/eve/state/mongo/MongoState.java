package com.almende.eve.state.mongo;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jongo.Jongo;
import org.jongo.marshall.jackson.oid.Id;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.almende.eve.rpc.jsonrpc.jackson.JOM;
import com.almende.eve.state.AbstractState;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.mongodb.WriteResult;

/**
 * Representation of Eve agents state based on MongoDB
 * 
 * @author ronny
 *
 */
public class MongoState extends AbstractState<JsonNode> {
	
	private static final Logger	LOG			= Logger.getLogger("MongoState");
	
	private Map<String, JsonNode>	properties	= Collections.synchronizedMap(new HashMap<String, JsonNode>());
	
	@JsonIgnore
	private Jongo connection;
	
	/**
	 * @see com.almende.eve.state.AbstractState#AbstractState()
	 */
	public MongoState() {
	}
	
	/**
	 * main constructor: with agentId as input parameter
	 * @param agentId
	 */
	public MongoState(final String agentId) {
		super(agentId);
	}
	
	@JsonIgnore
	public void setConnection(Jongo connection) {
		this.connection = connection;
	}
	
	@JsonIgnore
	public Jongo getConnection() {
		return connection;
	}
	
	/**
	 * returns agent ID and adding @Id annotation to mark it as objectID in Mongo
	 * otherwise mongo will generate new object each time
	 * 
	 * @see com.almende.eve.state.State#getAgentId()
	 */
	@Override
	@Id
	public synchronized String getAgentId() {
		return super.getAgentId();
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
	}

	/* (non-Javadoc)
	 * @see com.almende.eve.state.State#remove(java.lang.String)
	 */
	@Override
	public synchronized Object remove(final String key) {
		Object result = null;
		try {
			result = properties.remove(key);
			updateObject();
		} catch (final Exception e) {
			LOG.log(Level.SEVERE, "remove error {}", e);
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
			LOG.log(Level.WARNING, "containsKey error {}", e);
		}
		return result;
	}

	@Override
	public Set<String> keySet() {
		Set<String> result = null;
		try {
			result = properties.keySet();
		} catch (final Exception e) {
			LOG.log(Level.WARNING, "keySet error {}", e);
		}
		return result;
	}

	@Override
	public void clear() {
		try {
			final String agentType = properties.get(KEY_AGENT_TYPE).textValue();
			properties.clear();
			properties.put(KEY_AGENT_TYPE, JOM.getInstance().valueToTree(agentType));
			updateObject();
		} catch (final Exception e) {
			LOG.log(Level.SEVERE, "clear error {}", e);
		}	
	}


	@Override
	public int size() {
		int result = 0;
		try {
			result = properties.size();
		} catch (final Exception e) {
			LOG.log(Level.WARNING, "size error {}", e);
		}
		return result;
	}

	@Override
	public JsonNode get(String key) {
		JsonNode result = null;
		try {
			result = properties.get(key);
		} catch (final Exception e) {
			LOG.log(Level.WARNING, "get error {}", e);
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
			updateObject();
		} catch (final Exception e) {
			LOG.log(Level.SEVERE, "locPut error {}", e);
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
				result = updateObject();
			}
		} catch (final Exception e) {
			LOG.log(Level.SEVERE, "locPutIfUnchanged error {}", e);
		}
		
		return result;
	}
	
	/**
	 * returns agent properties as a mapped collection of JSON nodes
	 *
	 * @return the properties
	 */
	public Map<String, JsonNode> getProperties() {
		return properties;
	}
	
	/**
	 * set all property values from a collection
	 *
	 * @param properties the properties
	 */
	public void setProperties(final Map<String, JsonNode> properties) {
		final String agentType = properties.get(KEY_AGENT_TYPE).textValue();
		properties.clear();
		this.properties.putAll(properties);
		this.properties.put(KEY_AGENT_TYPE, JOM.getInstance().valueToTree(agentType));
		updateObject();
	}
	
	/**
	 * Finer granularity update command, not working yet
	 */
	private synchronized boolean updateField(String field, JsonNode value) {
		WriteResult result = connection.getCollection(MongoStateFactory.COLLECTION_NAME).
				update("{id: #}", getAgentId()).
				with("{$set: {#, #}}", field, value);
		if (!result.getLastError().ok()) { 
			LOG.log(Level.SEVERE, "update error", result.getError());
			return false;
		}
		return true;
	}
	
	/**
	 * in its current implementation, the whole object is being updated all at the same time.
	 * probably will perform better on a finer granularity updates 
	 * 
	 */
	private synchronized boolean updateObject() {
		 WriteResult result = connection.getCollection(MongoStateFactory.COLLECTION_NAME).save(this);
		 if (!result.getLastError().ok()) { 
			 LOG.log(Level.SEVERE, "update error", result.getError());
			 return false;
		 }
		 return true;
	}

}
