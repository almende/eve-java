/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.state.couch;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.ektorp.http.HttpClient;
import org.ektorp.http.StdHttpClient;
import org.ektorp.http.StdHttpClient.Builder;
import org.ektorp.impl.StdCouchDbInstance;

import com.almende.eve.capabilities.Capability;
import com.almende.eve.capabilities.handler.Handler;
import com.almende.eve.state.State;
import com.almende.eve.state.StateService;
import com.almende.util.TypeUtil;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class CouchStateService.
 */
public class CouchStateService implements StateService {
	private static final Logger						LOG			= Logger.getLogger(CouchStateService.class
																		.getName());
	private static Map<String, CouchStateService>	instances	= new ConcurrentHashMap<String, CouchStateService>();
	
	private CouchDbConnector						db			= null;
	
	/**
	 * Instantiates a new couch state service.
	 * 
	 * @param params
	 *            the params
	 */
	public CouchStateService(final ObjectNode params) {
		try {
			final CouchStateConfig config = new CouchStateConfig(params);
			final String url = config.getUrl();
			final String username = config.getUsername();
			final String password = config.getPassword();
			final String database = config.getDatabase();
			
			final Builder builder = new StdHttpClient.Builder().url(url);
			if (username != null && !username.isEmpty()) {
				builder.username(username);
			}
			
			if (password != null && !password.isEmpty()) {
				builder.password(password);
			}
			
			final HttpClient httpClient = builder.build();
			final CouchDbInstance dbInstance = new StdCouchDbInstance(
					httpClient);
			// if the second parameter is true, the database will be created if
			// it doesn't exists
			db = dbInstance.createConnector(database, true);
			
		} catch (final Exception e) {
			LOG.log(Level.SEVERE, "Failed to connect to couch db", e);
		}
	}
	
	/**
	 * Gets the instance by params.
	 * 
	 * @param params
	 *            the params
	 * @return the instance by params
	 */
	public static CouchStateService getInstanceByParams(final ObjectNode params) {
		final CouchStateConfig config = new CouchStateConfig(params);
		final String key = config.getKey();
		if (instances.containsKey(key)) {
			return instances.get(key);
		} else {
			final CouchStateService result = new CouchStateService(params);
			instances.put(key, result);
			return result;
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.almende.eve.capabilities.CapabilityService#get(com.fasterxml.jackson
	 * .databind.node.ObjectNode, com.almende.eve.capabilities.handler.Handler,
	 * java.lang.Class)
	 */
	@Override
	public <T extends Capability, V> T get(final ObjectNode params,
			final Handler<V> handle, final Class<T> type) {
		final CouchStateConfig config = new CouchStateConfig(params);
		final String id = couchify(config.getId());
		
		CouchState state = null;
		try {
			if (db.contains(id)) {
				state = db.get(CouchState.class, id);
				state.setDb(db);
				state.setService(this);
			} else {
				state = new CouchState(id, db, this, config);
				db.create(state);
			}
		} catch (final Exception e) {
			LOG.log(Level.WARNING, "Failed to load agent", e);
		}
		
		return TypeUtil.inject(state, type);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.almende.eve.state.StateService#delete(com.almende.eve.state.State)
	 */
	@Override
	public void delete(final State instance) {
		db.delete(instance);
	}
	
	/**
	 * Check the key if it starts with a _
	 * Add a prefix if this is the case, because _ properties are reserved.
	 * 
	 * @param key
	 *            the key
	 * @return prefixed key (if necessary)
	 */
	private String couchify(final String key) {
		if (key.startsWith("_")) {
			return "cdb" + key;
		}
		
		return key;
	}
}
