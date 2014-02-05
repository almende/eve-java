package com.almende.eve.state.mongo;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jongo.Find;
import org.jongo.Jongo;

import com.almende.eve.state.State;
import com.almende.eve.state.StateFactory;
import com.mongodb.MongoClient;

/**
 * An implementation of state factory using MongoDB & Jongo as database connection
 * 
 * @author ronny
 *
 */
public class MongoStateFactory implements StateFactory {
	
	private static final Logger	LOG			= Logger.getLogger("MongoStateFactory");
	
	private final Jongo jongo;
	
	/**
	 * default constructor which will connect to default mongodb client
	 * (localhost:27017) with "eve" as its default database
	 * 
	 * @throws UnknownHostException 
	 */
	public MongoStateFactory() throws UnknownHostException {
		this(new MongoClient(), "eve");
	}
	
	/**
	 * constructor with the URI & database name as its parameter
	 * 
	 * @param mongoUriHost
	 * @param mongoPort
	 * @param databaseName
	 * @throws UnknownHostException
	 */
	public MongoStateFactory(String mongoUriHost, int mongoPort, String databaseName) throws UnknownHostException {
		this(new MongoClient(mongoUriHost, mongoPort), databaseName);
	}
	
	/**
	 * constructor which uses readily available mongo client instance and database name
	 * @param mongoClient
	 * @param databaseName
	 */
	public MongoStateFactory(MongoClient mongoClient, String databaseName) {
		this(new Jongo(mongoClient.getDB(databaseName)));
	}
	
	/**
	 * constructor which uses jongo instantiated elsewhere
	 * @param jongo
	 */
	public MongoStateFactory(Jongo jongo) {
		this.jongo = jongo;
	}
	
	/**
	 * returns jongo connection to the underlying mongo database
	 * @return jongo
	 */
	public Jongo getJongo() {
		return jongo;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.almende.eve.state.StateFactory#get(java.lang.String)
	 */
	@Override
	public State get(String agentId) {
		MongoState result = null;
		try {
			result = jongo.getCollection(MongoState.COLLECTION_NAME).
							findOne("{_id: #}", agentId).as(MongoState.class);
			if (result!=null) {
				result.setConnection(jongo);
			}
		} catch (final Exception e) {
			LOG.log(Level.WARNING, "get error {}", e);
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.almende.eve.state.StateFactory#create(java.lang.String)
	 */
	@Override
	public synchronized State create(String agentId) throws IOException {
		if (exists(agentId)) {
			throw new IllegalStateException("Cannot create state, "
					+ "state with id '" + agentId + "' already exists.");
		}
		
		MongoState state = new MongoState(agentId);
		try {
			jongo.getCollection(MongoState.COLLECTION_NAME).insert(state);
		} catch (final Exception e) {
			LOG.log(Level.SEVERE, "create error {}", e);
		}
		state.setConnection(jongo);
		return state;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.almende.eve.state.StateFactory#delete(java.lang.String)
	 */
	@Override
	public void delete(String agentId) {
		try {
			jongo.getCollection(MongoState.COLLECTION_NAME).remove("{_id: #}", agentId);
		} catch (final Exception e) {
			LOG.log(Level.SEVERE, "get error {}", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.almende.eve.state.StateFactory#exists(java.lang.String)
	 */
	@Override
	public boolean exists(String agentId) {
		MongoState result = jongo.getCollection(MongoState.COLLECTION_NAME).
								findOne("{_id: #}", agentId).as(MongoState.class);
		return (result != null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.almende.eve.state.StateFactory#getAllAgentIds(java.lang.String)
	 */
	@Override
	public Iterator<String> getAllAgentIds() {
		List<String> agentIDs = new ArrayList<String>();
		try {
			Find find = jongo.getCollection(MongoState.COLLECTION_NAME).find().projection("{_id:1}");
			// :: there's probably a faster way to iterate over id fields
	        for (Object map : find.as(Object.class)) {
				String agentId = (String) ((LinkedHashMap) map).get("_id");
	        	agentIDs.add(agentId);
	        }
		} catch (final Exception e) {
			LOG.log(Level.SEVERE, "getAllAgentIds error {}", e);
		}
		return agentIDs.iterator();
	}

}

