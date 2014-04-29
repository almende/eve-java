/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.state.mongo;

import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jongo.Jongo;
import org.jongo.MongoCollection;

import com.almende.eve.capabilities.Capability;
import com.almende.eve.capabilities.handler.Handler;
import com.almende.eve.state.State;
import com.almende.eve.state.StateConfig;
import com.almende.eve.state.StateService;
import com.almende.util.TypeUtil;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;

/**
 * A service for managing MemoryState objects.
 */
public class MongoStateService implements StateService {
	private static final Logger			LOG			= Logger.getLogger(MongoStateService.class
															.getName());
	
	/* internal attributes */
	private final Jongo					jongo;
	private final String				collectionName;
	
	/**
	 * Instantiates a new mongo state service.
	 * 
	 * @param params
	 *            the params
	 * @throws UnknownHostException 
	 */
	public MongoStateService(final ObjectNode params) throws UnknownHostException {
		
		MongoStateConfig config = new MongoStateConfig(params);
		
		// initialization of client & jongo
		MongoClient client = createClient(config.getHost(), config.getPort());
		this.jongo = new Jongo(client.getDB(config.getDatabase()));
		this.collectionName = config.getCollection();
		
		this.jongo.runCommand("{collMod: '" + this.collectionName
				+ "', usePowerOf2Sizes : true }");
	}
	
	private static MongoClient createClient(String databaseUri, int port)
			throws UnknownHostException {
		MongoClientOptions options = MongoClientOptions.builder()
				.connectionsPerHost(100)
				.threadsAllowedToBlockForConnectionMultiplier(1500).build();
		MongoClient client = new MongoClient(new ServerAddress(databaseUri,
				port), options);
		return client;
	}
	/**
	 * Gets the instance by params.
	 * 
	 * @param params
	 *            the params
	 * @return the instance by params
	 */
	public static MongoStateService getInstanceByParams(final ObjectNode params) {
		//TODO: fix lookup to multiple params
		try {
			return new MongoStateService(params);
		} catch (UnknownHostException e) {
			LOG.log(Level.WARNING,"Couldn't init MongoStateService",e);
		}
		return null;
	}
	
	/**
	 * returns jongo collection used by this state factory.
	 * 
	 * @return the collection
	 */
	public MongoCollection getCollection() {
		return jongo.getCollection(collectionName);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.almende.eve.capabilities.CapabilityService#get(com.fasterxml.jackson.
	 * databind
	 * .JsonNode, com.almende.eve.capabilities.handler.Handler, java.lang.Class)
	 */
	@Override
	public <T extends Capability, V> T get(final ObjectNode params,
			final Handler<V> handle, final Class<T> type) {
		final StateConfig config = new StateConfig(params);
		final String id = config.getId();
		
		MongoState result = null;
		try {
			result = getCollection().findOne("{_id: #}", id).as(
					MongoState.class);
			if (result != null) {
				result.setCollection(getCollection());
			}
		} catch (final Exception e) {
			LOG.log(Level.WARNING, "get error", e);
		}
		return TypeUtil.inject(result, type);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.almende.eve.state.StateService#delete(com.almende.eve.state.State)
	 */
	@Override
	public void delete(final State instance) {
		try {
			getCollection().remove("{_id: #}", instance.getId());
		} catch (final Exception e) {
			LOG.log(Level.WARNING, "delete error", e);
		}
	}
	
}
