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

import com.almende.eve.capabilities.AbstractCapabilityBuilder;
import com.almende.eve.state.State;
import com.almende.eve.state.StateService;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class CouchStateBuilder.
 */
public class CouchStateBuilder extends AbstractCapabilityBuilder<CouchState> {
	private static final Logger						LOG			= Logger.getLogger(CouchStateBuilder.class
																		.getName());
	private static Map<String, CouchStateProvider>	instances	= new ConcurrentHashMap<String, CouchStateProvider>();
	
	@Override
	public CouchState build() {
		final CouchStateProvider provider = getInstanceByParams(getParams());
		if (provider != null) {
			return provider.get(getParams());
		} else {
			LOG.warning("Couldn't get CouchStateProvider instance!");
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
	public CouchStateProvider getInstanceByParams(final ObjectNode params) {
		final CouchStateConfig config = new CouchStateConfig(params);
		final String key = config.getKey();
		
		if (instances.containsKey(key)) {
			return instances.get(key);
		} else {
			synchronized (instances) {
				if (!instances.containsKey(key)) {
					final CouchStateProvider result = new CouchStateProvider(
							params);
					if (result != null) {
						instances.put(key, result);
					}
				}
				return instances.get(key);
			}
			
		}
	}
	
	/**
	 * The Class CouchStateService.
	 */
	class CouchStateProvider implements StateService {
		
		private CouchDbConnector	db	= null;
		
		/**
		 * Instantiates a new couch state service.
		 * 
		 * @param params
		 *            the params
		 */
		public CouchStateProvider(final ObjectNode params) {
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
				// if the second parameter is true, the database will be created
				// if
				// it doesn't exists
				db = dbInstance.createConnector(database, true);
				
			} catch (final Exception e) {
				LOG.log(Level.SEVERE, "Failed to connect to couch db", e);
			}
		}
		
		public CouchState get(final ObjectNode params) {
			final CouchStateConfig config = new CouchStateConfig(params);
			final String id = couchify(config.getId());
			
			CouchState state = null;
			try {
				synchronized (this) {
					if (db.contains(id)) {
						state = db.get(CouchState.class, id);
						state.setDb(db);
						state.setService(this);
					} else {
						state = new CouchState(id, db, this, config);
						db.create(state);
					}
				}
			} catch (final Exception e) {
				LOG.log(Level.WARNING, "Failed to load agent", e);
			}
			return state;
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
}