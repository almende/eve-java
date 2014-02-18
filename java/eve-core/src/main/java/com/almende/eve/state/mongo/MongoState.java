package com.almende.eve.state.mongo;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.mongodb.WriteResult;

/**
 * Simple representation of Eve agents state based on MongoDB
 * 
 * @author ronny
 *
 */
public class MongoState extends AbstractState<JsonNode> {
	
	/**
	 * internal exception signifying update conflict
	 * @author ronny
	 *
	 */
	class UpdateConflictException extends Exception {

		/**
		 * 
		 */
		private static final long serialVersionUID = -8714877645567521282L;
		
		/**
		 * timestamp of 
		 */
		private Date timestamp;
		
		/**
		 * constant for print formatting the timestamp
		 */
		private final DateFormat format = new SimpleDateFormat("dd/MM/yy hh:mm:ss:SSS");
		
		/**
		 * default constructor for class specific exception
		 * @param timestamp
		 */
		public UpdateConflictException(Date timestamp) {
			this.timestamp = timestamp;
		}
		
		@Override
		public String getMessage() {
			return "Document updated on ["+format.format(timestamp)+"] is no longer the latest version.";
		}
		
	}
	
	/* logger object */
	private static final Logger		LOG			= Logger.getLogger("MongoState");
	
	/* mapping object that contains variables used by the agent */
	private Map<String, JsonNode> properties	= Collections.synchronizedMap(new HashMap<String, JsonNode>());
	
	/* metadata for agenthost : agent type and last update for a simple update conflict avoidance */
	private Class<?> agentType;
	private Date timestamp;
	
	
	@JsonIgnore
	private MongoCollection collection;
	
	/**
	 * default constructor, used when instantiating state while fetching the appropriate agents
	 * @see com.almende.eve.state.AbstractState#AbstractState()
	 */
	public MongoState() {
	}
	
	/**
	 * the constructor used on creation of new state in the database
	 * @param agentId
	 */
	public MongoState(final String agentId) {
		super(agentId);
		timestamp = Calendar.getInstance().getTime();
		agentType = null;
	}
	
	@JsonIgnore
	public void setCollection(MongoCollection collection) {
		this.collection = collection;
	}
	
	@JsonIgnore
	public MongoCollection getCollection() {
		return collection;
	}
	
	public Date getTimestamp() {
		return timestamp;
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
	
	/**
	 * agent type is considered as a separate attribute, not a common property
	 */
	@Override
	public synchronized void setAgentType(final Class<?> agentType) {
		this.agentType = agentType;
		// assuming this is called only once after creation, simply save the entire state
		collection.save(this);
	}
	
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

	@Override
	public void clear() {
		try {
			properties.clear();
			updateProperties(false);
		} catch (final Exception e) {
			LOG.log(Level.WARNING, "clear error", e);
		}	
	}


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
		} catch (final UpdateConflictException e) {
			LOG.log(Level.WARNING, e.getMessage() +" Adding ["+key+"="+value+"]");
			reloadProperties();
			// go recursive if update conflict occurs
			result = locPut(key, value);
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
		} catch (final UpdateConflictException e) {
			LOG.log(Level.WARNING, e.getMessage());
			reloadProperties();
			// recur if update conflict occurs
			locPutIfUnchanged(key, newVal, oldVal);
		} catch (final Exception e) {
			LOG.log(Level.WARNING, "locPutIfUnchanged error", e);
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
		this.properties.clear();
		this.properties.putAll(properties);
		try {
			updateProperties(true);
		} catch (UpdateConflictException e) {
			// should never happen
			LOG.log(Level.WARNING, "setProperties error", e); 
		}
	} 
	
	/**
	 * Refreshes the state according to the latest version as a preceding step before recursive call.
	 * With this mechanism, changes on different State property will be safely merged. However, with
	 * interwoven execution order among multiple threads, multiple updates on the same State property 
	 * is not guaranteed to be executed properly.
	 * 
	 */
	private synchronized void reloadProperties() {
		final MongoState updatedState = collection.findOne("{_id: #}", getAgentId()).as(MongoState.class);
		this.timestamp = updatedState.timestamp;
		this.properties = updatedState.properties;
	}
	
	/**
	 * updating the entire properties object at the same time, with force flag to allow overwriting of updates
	 * from other instances of the state
	 * 
	 * @param force
	 * @throws UpdateConflictException | will not throw anything when $force flag is true
	 */
	private synchronized boolean updateProperties(boolean force) throws UpdateConflictException {
		Date now = Calendar.getInstance().getTime();
		/* write to database */
		WriteResult result = (force) ?
				collection.update("{_id: #}", getAgentId()).with("{$set: {properties: #, timestamp: #}}", properties, now) :
				collection.update("{_id: #, timestamp: #}", getAgentId(), timestamp).with("{$set: {properties: #, timestamp: #}}", properties, now);
		/* check results */
		Boolean updatedExisting = (Boolean) result.getField("updatedExisting");
		if (!updatedExisting) {
			throw new UpdateConflictException(timestamp);
		} 
		timestamp = now;
		return updatedExisting;
	}

}
