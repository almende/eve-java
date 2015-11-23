/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.state.mongo;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jongo.MongoCollection;
import org.jongo.marshall.jackson.oid.Id;
import com.almende.eve.state.AbstractState;
import com.almende.eve.state.State;
import com.almende.eve.state.StateService;
import com.almende.eve.state.mongo.MongoStateBuilder.MongoStateProvider;
import com.almende.util.jackson.JOM;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.MongoException;
import com.mongodb.WriteResult;

/**
 * The Class MongoState.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MongoState extends AbstractState<JsonNode> implements State {
	
	/**
	 * internal exception signifying update conflict.
	 * 
	 * @author ronny
	 */
	class UpdateConflictException extends Exception {
		
		/**
		 * 
		 */
		private static final long	serialVersionUID	= -8714877645567521282L;
		
		/**
		 * timestamp of last update
		 */
		private final Long			timestamp;
		
		/**
		 * default constructor for class specific exception.
		 * 
		 * @param timestamp
		 *            the timestamp
		 */
		public UpdateConflictException(final Long timestamp) {
			this.timestamp = timestamp;
		}
		
		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Throwable#getMessage()
		 */
		@Override
		public String getMessage() {
			return "Document updated on [" + timestamp
					+ "] is no longer the latest version.";
		}
		
	}
	
        private static final Logger LOG = Logger.getLogger("MongoState");
    
        /* mapping object that contains variables used by the agent */
        @JsonIgnore
        private Map<String, JsonNode> properties = Collections.synchronizedMap(new HashMap<String, JsonNode>());
        @JsonProperty("properties")
        private String propertiesJson = null;
        private Long timestamp;
        @JsonIgnore
        private MongoStateProvider provider = null;
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.almende.eve.state.AbstractState#getId()
	 */
	@Id
	@Override
	public String getId() {
		return super.getId();
	}
	
	/**
	 * Default constructor
	 */
        public MongoState() {
            timestamp = System.nanoTime();
        }
        
        /**
         * Instantiates a new mongo state.
         * 
         * @param id
         *            the state's id
         * @param mongoStateProvider
         *            the service
         * @param params
         *            the params
         */
        public MongoState(final String id, final MongoStateProvider mongoStateProvider, final ObjectNode params) {
            super(id, mongoStateProvider, params);
            provider = mongoStateProvider;
            timestamp = System.nanoTime();
        }
	
	/**
	 * Gets the timestamp.
	 * 
	 * @return the timestamp
	 */
	public Long getTimestamp() {
		return timestamp;
	}
	
	@Override
	public StateService getService() {
	
	    return provider;
	}
	
	@Override
	public void setService(StateService service) {
	
	    if(service instanceof MongoStateProvider) {
	           provider = (MongoStateProvider) service;
	    }
	}
	
	/*
	 * (non-Javadoc)
	 * 
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
	
	/*
	 * (non-Javadoc)
	 * 
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
	
	/*
	 * (non-Javadoc)
	 * 
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
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.almende.eve.state.State#clear()
	 */
	@Override
	public void clear() {
		try {
			properties.clear();
			updateProperties(true);
		} catch (final Exception e) {
			LOG.log(Level.WARNING, "clear error", e);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
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
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.almende.eve.state.AbstractState#get(java.lang.String)
	 */
	@Override
	@JsonIgnore
        public JsonNode get(final String key) {
    
            JsonNode result = null;
            try {
                result = properties.get(key);
                if (result == null) {
                    reloadProperties();
                    result = properties.get(key);
                }
            }
            catch (final Exception e) {
                LOG.log(Level.WARNING, "get error", e);
            }
            return result;
        }
	
        /*
         * (non-Javadoc)
         * 
         * @see com.almende.eve.state.AbstractState#locPut(java.lang.String,
         * com.fasterxml.jackson.databind.JsonNode)
         */
        @Override
        public synchronized JsonNode locPut(final String key, final JsonNode value) {
    
            JsonNode result = null;
            try {
                result = properties.put(key, value);
                updateProperties(false);
            }
            catch (final UpdateConflictException e) {
                LOG.log(Level.WARNING, e.getMessage() + " Adding [" + key + "=" + value + "]");
                reloadProperties();
                // go recursive if update conflict occurs
                result = locPut(key, value);
            }
            catch (final Exception e) {
                LOG.log(Level.WARNING, "locPut error: Adding [" + key + "=" + value + "] " + properties, e);
            }
            return result;
        }
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.almende.eve.state.AbstractState#locPutIfUnchanged(java.lang.String,
	 * com.fasterxml.jackson.databind.JsonNode,
	 * com.fasterxml.jackson.databind.JsonNode)
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
			
			// Poor man's equality as some Numbers are compared incorrectly:
			// e.g.
			// IntNode versus LongNode
			if (oldVal.equals(cur) || oldVal.toString().equals(cur.toString())) {
				properties.put(key, newVal);
				result = updateProperties(false);
			}
		} catch (final UpdateConflictException e) {
			LOG.log(Level.WARNING, e.getMessage());
			reloadProperties();
			// retry if update conflict occurs
			locPutIfUnchanged(key, newVal, oldVal);
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
	@JsonIgnore
	public Map<String, JsonNode> getProperties() {
		return properties;
	}
	
	/**
	 * Gets the serialized version of {@link MongoState#properties}
	 * @return
	 */
	@JsonProperty("properties")
        public String getPropertiesJSON() {
    
            if(properties != null) {
                try {
                    propertiesJson = JOM.getInstance().writeValueAsString(properties);
                    return propertiesJson;
                }
                catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }
            return "{}";
        }
	
        @JsonProperty("properties")
        public void setProperties(String propertiesJson) {

            try {
                if (propertiesJson != null) {
                    this.propertiesJson = propertiesJson;
                    properties = JOM.getInstance().readValue(propertiesJson, new TypeReference<Map<String, JsonNode>>() {
                    });
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
	
	/**
	 * set all property values from a collection. (Warning, keys in the properties map should be MongoDB safe: no '$', nor '.' in the keys!)
	 * 
	 * @param properties
	 *            the properties
	 * @throws JsonProcessingException 
	 */
	@JsonIgnore
	public void setProperties(final Map<String, JsonNode> properties) throws JsonProcessingException {
		this.properties.clear();
		this.properties.putAll(properties);
		try {
			updateProperties(true);
		} catch (final UpdateConflictException e) {
			// should never happen
			LOG.log(Level.WARNING, "setProperties error", e);
		}
	}
	
	/**
	 * Refreshes the state according to the latest version as a preceding step
	 * before recursive call.
	 * With this mechanism, changes on different State property will be safely
	 * merged. However, with
	 * interwoven execution order among multiple threads, multiple updates on
	 * the same State property
	 * is not guaranteed to be executed properly.
	 * 
	 */
        private synchronized void reloadProperties() {
    
            final MongoCollection collection = provider.getInstance();
            final MongoState updatedState = collection.findOne("{_id: #}", getId()).as(MongoState.class);
            if (updatedState != null) {
                timestamp = updatedState.timestamp;
                properties = updatedState.properties;
            }
            else {
                properties = Collections.synchronizedMap(new HashMap<String, JsonNode>());
                timestamp = System.nanoTime();
            }
        }
	
	/**
	 * updating the entire properties object at the same time, with force flag
	 * to allow overwriting of updates
	 * from other instances of the state
	 * 
	 * @param force
	 * @throws UpdateConflictException
	 *             | will not throw anything when $force flag is true
	 */
	private synchronized boolean updateProperties(final boolean force)
			throws UpdateConflictException {
		final Long now = System.nanoTime();
		propertiesJson = getPropertiesJSON();
		final MongoCollection collection = provider.getInstance();
		/* write to database */
                final WriteResult result = (force)
                    ? collection.update("{_id: #}", getId()).with("{$set: {properties: #, timestamp: #}}", propertiesJson, now)
                    : collection.update("{_id: #, timestamp: #}", getId(), timestamp)
                                .with("{$set: {properties: #, timestamp: #}}", propertiesJson, now);
		/* check results */
		final Boolean updatedExisting = (Boolean) result
				.getField("updatedExisting");
		if (result.getN() == 0 && result.getError() == null) {
			throw new UpdateConflictException(timestamp);
		} else if (result.getN() != 1) {
			throw new MongoException(result.getError()+ " <--- "+propertiesJson);
		}
		timestamp = now;
		return updatedExisting;
	}
}
