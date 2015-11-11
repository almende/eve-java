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

import com.almende.eve.capabilities.AbstractCapabilityBuilder;
import com.almende.eve.state.State;
import com.almende.eve.state.StateService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;

/**
 * The Class MongoStateBuilder.
 */
public class MongoStateBuilder extends AbstractCapabilityBuilder<MongoState> {
	private static final Logger						LOG			= Logger.getLogger(MongoStateBuilder.class
																		.getName());
	private static Map<String, MongoStateProvider>	instances	= new ConcurrentHashMap<String, MongoStateProvider>();
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
		} else {
			synchronized (instances) {
				if (!instances.containsKey(key)) {
					try {

						final MongoStateProvider result = new MongoStateProvider(
								params);
						if (result != null) {
							instances.put(key, result);
						}
					} catch (final UnknownHostException e) {
						LOG.log(Level.WARNING,
								"Couldn't init MongoStateService", e);
					}
				}
				return instances.get(key);
			}
		}
	}

	/**
	 * A service for managing MemoryState objects.
	 */
	public class MongoStateProvider implements StateService {

		/* internal attributes */
		private final Jongo		jongo;
		private final String	collectionName;

		/**
		 * Instantiates a new mongo state service.
		 * 
		 * @param params
		 *            the params
		 * @throws UnknownHostException
		 *             the unknown host exception
		 */
		public MongoStateProvider(final ObjectNode params)
				throws UnknownHostException {

			final MongoStateConfig config = MongoStateConfig.decorate(params);

			// initialization of client & jongo
			final MongoClient client = createClient(config.getHosts());
			jongo = new Jongo(client.getDB(config.getDatabase()));
			collectionName = config.getCollection();

			jongo.runCommand("{collMod: '" + collectionName
					+ "', usePowerOf2Sizes : true }");

		}

		private MongoClient createClient(final ArrayNode hosts) throws UnknownHostException {
			if(client == null) {
    		   final MongoClientOptions options = MongoClientOptions.builder()
    					.connectionsPerHost(100)
    					.threadsAllowedToBlockForConnectionMultiplier(1500).build();
    			List<ServerAddress> addresses = new ArrayList<ServerAddress>();
    			for(final JsonNode host : hosts) {
    			    addresses.add( new ServerAddress(host.get("host").asText(), host.get("port").asInt()) );
    			}
    			client = new MongoClient(addresses, options);
			}
			return client;
		}

		/**
		 * returns jongo collection used by this state factory.
		 * 
		 * @return the collection
		 */
		public MongoCollection getCollection() {
			return jongo.getCollection(collectionName);
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
					result = getCollection().findOne("{_id: #}", id).as(
							MongoState.class);
					if (result == null) {
						result = new MongoState(id, this, params);
						getCollection().insert(result);
					} else {
						result.setService(this);
					}
				}
				result.setCollection(getCollection());
			} catch (final Exception e) {
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
                                    getCollection().remove("{_id: #}", instance.getId());
                            } catch (final Exception e) {
                                    LOG.log(Level.WARNING, "delete error", e);
                            }
                        }
                }

		@Override
		public Set<String> getStateIds() {
			Iterator<MongoState> res = getCollection().find()
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
