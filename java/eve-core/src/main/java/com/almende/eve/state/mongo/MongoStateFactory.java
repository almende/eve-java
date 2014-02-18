package com.almende.eve.state.mongo;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jongo.Find;
import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.jongo.ResultHandler;

import com.almende.eve.state.State;
import com.almende.eve.state.StateFactory;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;

/**
 * An implementation of state factory using MongoDB & Jongo as database connection
 * 
 * @author ronny
 *
 */
public class MongoStateFactory implements StateFactory {
	
	private static final Logger		LOG			= Logger.getLogger("MongoStateFactory");
	
	/* internal attributes */
	private final Jongo jongo;
	private final String collectionName;
	
	/**
	 * default constructor which will connect to default mongodb client
	 * (localhost:27017) with "eve" as its default database and "agents"
	 * as its collection name
	 * 
	 * @throws UnknownHostException 
	 */
	public MongoStateFactory() throws UnknownHostException {
		this(new HashMap<String, Object>());
	}
	
	/**
	 * constructor with the URI & database name as its parameter
	 * 
	 * @param mongoUriHost
	 * @param mongoPort
	 * @param databaseName
	 * @param collectionName
	 * @throws UnknownHostException
	 */
	public MongoStateFactory(String mongoUriHost, int mongoPort, String databaseName, String collectionName) throws UnknownHostException {
		this(new MongoClient(mongoUriHost, mongoPort), databaseName, collectionName);
	}
	
	/**
	 * constructor which uses readily available mongo client instance and database name
	 * @param mongoClient
	 * @param databaseName
	 * @param collectionName
	 */
	public MongoStateFactory(MongoClient mongoClient, String databaseName, String collectionName) {
		this(new Jongo(mongoClient.getDB(databaseName)), collectionName);
	}
	
	/**
	 * constructor which uses jongo instantiated elsewhere
	 * @param jongo
	 * @param collectionName
	 */
	public MongoStateFactory(Jongo jongo, String collectionName) {
		this.jongo = jongo;
		this.collectionName = collectionName;
	}
	
	/**
	 * constructor using configuration mapping provided through the YAML file
	 * 
	 * @param params
	 * @throws UnknownHostException
	 */
	public MongoStateFactory(Map<String, Object> params) throws UnknownHostException {
		
		// initialization of mongo client server/port
		MongoClient client = new MongoClient(
				((params.containsKey("uriHost")) ? (String) params.get("uriHost") : "localhost"),
				((params.containsKey("port")) ? (Integer) params.get("port") : 27017)		
			);
		
		// select database
		String databaseName = (params != null && params.containsKey("database")) ? (String) params.get("database") : "eve";
		DB database = client.getDB(databaseName);
		
		// authenticate if username and password is available
		String username = (params.containsKey("username")) ? (String) params.get("username") : null;
		if (username != null) {
			if (params.containsKey("password")) {
				throw new MongoException("Password for username not configured.");
			}
			String password = (String) params.get("password");
			database.authenticate(username, password.toCharArray()); // will also throw exception if username/password incorrect
		}
		
		// setup jongo and collection
		this.jongo = new Jongo(database);
		this.collectionName = (params != null && params.containsKey("collection")) ? (String) params.get("collection") : "agents";
	}
	
	/**
	 * returns jongo connection to the underlying mongo database
	 * @return jongo
	 */
	public Jongo getJongo() {
		return jongo;
	}
	
	/**
	 * returns collection name used in this mongo state factory
	 * @return
	 */
	public String getCollectionName() {
		return collectionName;
	}
	
	/**
	 * returns jongo collection used by this state factory
	 * @return
	 */
	public MongoCollection getCollection() {
		return jongo.getCollection(collectionName);
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
			result = getCollection().findOne("{_id: #}", agentId).as(MongoState.class);
			if (result!=null) {
				result.setCollection(getCollection());
			}
		} catch (final Exception e) {
			LOG.log(Level.WARNING, "get error", e);
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
			getCollection().insert(state);
		} catch (final Exception e) {
			LOG.log(Level.WARNING, "create error", e);
		}
		state.setCollection(getCollection());
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
			getCollection().remove("{_id: #}", agentId);
		} catch (final Exception e) {
			LOG.log(Level.WARNING, "delete error", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.almende.eve.state.StateFactory#exists(java.lang.String)
	 */
	@Override
	public boolean exists(String agentId) {
		MongoState result = getCollection().
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
		try {
			Find find = getCollection().find().projection("{_id:1}");
			Iterable<String> agentIDs = find.map(
				    new ResultHandler<String>() {
						@Override
						public String map(DBObject result) {
							return (String) result.get("_id");
						}
				});
			return agentIDs.iterator();
		} catch (final Exception e) {
			LOG.log(Level.WARNING, "getAllAgentIds error", e);
		}
		return new ArrayList<String>().iterator();
	}


}
