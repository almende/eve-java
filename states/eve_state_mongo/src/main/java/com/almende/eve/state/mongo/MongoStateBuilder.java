/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.state.mongo;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.jongo.marshall.jackson.JacksonMapper;
import org.jongo.marshall.jackson.configuration.MapperModifier;
import com.almende.eve.capabilities.AbstractCapabilityBuilder;
import com.almende.eve.state.State;
import com.almende.eve.state.StateService;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

/**
 * The Class MongoStateBuilder.
 */
public class MongoStateBuilder extends AbstractCapabilityBuilder<MongoState> {

    private static final Logger LOG = Logger.getLogger(MongoStateBuilder.class.getName());
    private static Map<String, MongoStateProvider> instances = new ConcurrentHashMap<String, MongoStateProvider>();
    private static MongoClient client = null;
    
	@Override
	public MongoState build() {
		final MongoStateProvider provider = getInstanceByParams(getParams());
		if (provider != null) {
			return provider.get(getParams());
		} else {
			LOG.warning("Couldn't get MongoStateProvider instance!");
			return null;
		}
	}

        /**
         * Gets the instance by params.
         * 
         * @param params
         *            the params
         * @return the instance by params
         */
        public MongoStateProvider getInstanceByParams(final ObjectNode params) {
    
            final MongoStateConfig config = MongoStateConfig.decorate(params);
            final String key = config.getKey();
    
            if (instances.containsKey(key)) {
                return instances.get(key);
            }
            else {
                synchronized (instances) {
                    if (!instances.containsKey(key)) {
                        try {
                            // initialization of client & jongo
                            MongoClient client = getClientInstance(config.getHosts(), null);
                            final MongoStateProvider result = new MongoStateProvider(getJongo(client, 
                                config.getDatabase()), config);
                            if (result != null) {
                                instances.put(key, result);
                            }
                        }
                        catch (final UnknownHostException e) {
                            LOG.log(Level.WARNING, "Couldn't init MongoStateService", e);
                        }
                    }
                    return instances.get(key);
                }
            }
        }
        
        /**
         * Returns a single instance of the MongoClient
         * 
         * @param hosts
         *            Arraynode of "host" and corresponding "ports"
         * @return
         * @throws UnknownHostException
         */
        public static MongoClient getClientInstance(final ArrayNode hosts, final List<MongoCredential> credentials)
            throws UnknownHostException {
    
            if (client == null) {
                final MongoClientOptions options = MongoClientOptions.builder().connectionsPerHost(500)
                                                                     .threadsAllowedToBlockForConnectionMultiplier(1500)
                                                                     .build();
                client = new MongoClient(getHosts(hosts), credentials, options);
            }
            return client;
        }
        
        public static Jongo getJongo(final MongoClient client, String dataBaseName) {
    
            return new Jongo(client.getDB(dataBaseName), new JacksonMapper.Builder().addModifier(new MapperModifier() {
    
                @Override
                public void modify(ObjectMapper mapper) {
    
                    mapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
                    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                }
            }).registerModule(new JodaModule()).withView(MongoState.class).build());
        }
        
        /**
         * Get the list of all hosts in the ArrayNode
         * 
         * @param hosts
         * @return
         * @throws UnknownHostException
         */
        private static List<ServerAddress> getHosts(ArrayNode hosts) throws UnknownHostException {
    
            List<ServerAddress> addresses = new ArrayList<ServerAddress>();
            for (final JsonNode host : hosts) {
                addresses.add(new ServerAddress(host.get("host").asText(), host.get("port").asInt()));
            }
            return addresses;
        }

	/**
	 * A service for managing MemoryState objects.
	 */
	public class MongoStateProvider implements StateService {

		/* internal attributes */
		private Jongo		jongo;
		private final String	collectionName;

                /**
                 * Instantiates a new mongo state service.
                 * 
                 * @param client
                 * 
                 * @param params
                 *            the params
                 * @throws UnknownHostException
                 *             the unknown host exception
                 */
                public MongoStateProvider(final Jongo jongo, final MongoStateConfig config) throws UnknownHostException {
        
                    collectionName = config.getCollection();
                    this.jongo = jongo;
                }

		/**
		 * returns jongo collection used by this state factory.
		 * 
		 * @return the collection
		 */
                public MongoCollection getInstance() {
        
                    final MongoCollection collection = jongo.getCollection(collectionName);
                    collection.ensureIndex("{ _id: 1, timestamp:1 }");
                    return collection;
                }

		/**
		 * Gets the.
		 * 
		 * @param params
		 *            the params
		 * @return the mongo state
		 */
                public MongoState get(final ObjectNode params) {
        
                    final MongoStateConfig config = MongoStateConfig.decorate(params);
                    final String id = config.getId();
        
                    MongoState result = null;
                    try {
                        synchronized (this) {
                            result = getInstance().findOne("{_id: #}", id).as(MongoState.class);
                            if (result == null) {
                                result = new MongoState(id, this, params);
                                getInstance().insert(result);
                            }
                            else {
                                result.setService(this);
                            }
                        }
                    }
                    catch (final Exception e) {
                        LOG.log(Level.WARNING, "get error", e);
                    }
                    return result;
                }

		/*
                 * (non-Javadoc)
                 * @see
                 * com.almende.eve.state.StateService#delete(com.almende.eve.state.State)
                 */
                @Override
                public void delete(final State instance) {
                        delete(instance, false);
                }
                
                /*
                 * (non-Javadoc)
                 * @see
                 * com.almende.eve.state.StateService#delete(com.almende.eve.state.State)
                 */
                @Override
                public void delete(final State instance, final Boolean instanceOnly) {
                        if(!instanceOnly) {
                            try {
                                    getInstance().remove("{_id: #}", instance.getId());
                            } catch (final Exception e) {
                                    LOG.log(Level.WARNING, "delete error", e);
                            }
                        }
                }

		@Override
		public Set<String> getStateIds() {
			Iterator<MongoState> res = getInstance().find()
					.as(MongoState.class).iterator();
			Set<String> result = new HashSet<String>();
			while (res.hasNext()) {
				MongoState state = res.next();
				result.add(state.getId());
			}
			return result;
		}
	}
}
