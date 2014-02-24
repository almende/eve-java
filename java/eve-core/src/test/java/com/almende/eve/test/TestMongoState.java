package com.almende.eve.test;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.text.DateFormatter;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.almende.eve.agent.AgentHost;
import com.almende.eve.scheduler.RunnableSchedulerFactory;
import com.almende.eve.state.State;
import com.almende.eve.state.mongo.MongoState;
import com.almende.eve.state.mongo.MongoStateFactory;
import com.almende.eve.test.agents.TestStateAgent;
import com.mongodb.DB;
import com.mongodb.Mongo;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;

/**
 * Unit testing State integrity in multiple instance of a single agent.
 * 
 * These test cases will spawn 16, 32 and 64 concurrent update threads. The Agent
 * will then save the update into different document field, making it behave like
 * a Vector/ArrayList.
 * 
 * @author ronny
 *
 */
public class TestMongoState {
	
	private static final Logger	LOG	= Logger.getLogger("TestMongoState");
	
	private static final String CONCURRENT_AGENT_ID = "TestConcurrent";
	private static final String SERIALIZER_AGENT_ID = "TestSerializer";
	
	
	
	/* mongo daemon representations for unit testing */
	private static MongodExecutable _mongodExe;
	private static MongodProcess _mongod;
	
	@BeforeClass
	public static void setupEve() throws Exception {
		
		// another precondition here: mongodb daemon must be running
		Integer portNumber = 12345;
		MongodStarter runtime = MongodStarter.getDefaultInstance();
        _mongodExe = runtime.prepare(new MongodConfigBuilder()
            .version(Version.Main.PRODUCTION)
            .net(new Net(portNumber, Network.localhostIsIPv6()))
            .build());
        _mongod = _mongodExe.start();
		
		// setup separate database for unit testing
		Map<String, Object> config = new HashMap<String, Object>();
		config.put("uriHost", "localhost");
		config.put("port", portNumber);
		config.put("database", "junit");
		config.put("collection", "agents");
		
		MongoStateFactory mongo = new MongoStateFactory(config);
		
		// 
		AgentHost host = AgentHost.getInstance();
		host.setStateFactory(mongo);
		host.setSchedulerFactory(new RunnableSchedulerFactory(host,
				"_myRunnableScheduler"));
		
		// create new agent
		host.createAgent(TestStateAgent.class, CONCURRENT_AGENT_ID);
		host.createAgent(TestStateAgent.class, SERIALIZER_AGENT_ID);
	}
	 
	@Test
	public void testSerializing() throws Exception {
		MongoStateFactory mongo = (MongoStateFactory) AgentHost.getInstance().getStateFactory();
		 
		SerializedObject object = new SerializedObject();
		object.setTimestamp(Calendar.getInstance().getTime());
		object.setMessage("Serialized object creation.");
		 
		mongo.get(SERIALIZER_AGENT_ID).put("key", object);
		
		SerializedObject objectAcc = mongo.get(SERIALIZER_AGENT_ID)
				.get("key", SerializedObject.class);
		 
		Assert.assertNotNull(objectAcc);
		 
		Assert.assertEquals(object.getTimestamp(), objectAcc.getTimestamp());
		Assert.assertEquals(object.getMessage(), objectAcc.getMessage());
	}
	 
	@Test
	public void testConcurrentUpdate() throws Exception {
		final int threadCount = 64;
		final DateFormat format = new SimpleDateFormat("dd/MM/yyyy-hh:mm:ss:SSS");
		// define the update task
	    Callable<String> updateTask = new Callable<String>() {
	        @Override
	        public String call() throws Exception {
	        	TestStateAgent agent = (TestStateAgent) AgentHost.getInstance().getAgent(CONCURRENT_AGENT_ID);
	        	String timestamp = format.format(Calendar.getInstance().getTime());
	            return (String) agent.push(timestamp);
	        }
	    };
	    
	    List<Callable<String>> tasks = Collections.nCopies(threadCount, updateTask);
	    
	    ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
	    List<Future<String>> futures = executorService.invokeAll(tasks);
	    List<String> results = new ArrayList<String>(futures.size());
	    
	    for (Future<String> future : futures) {
	        // will forward exception to the unit test when any of the task throws one.
	    	String futureDummy = future.get();
	    	results.add(futureDummy); // just ensuring new object being added into the list
	    }
	    
	    // Validate the futures list
	    Assert.assertEquals(threadCount, futures.size());
	    
	    TestStateAgent agent = (TestStateAgent) AgentHost.getInstance().getAgent(CONCURRENT_AGENT_ID);
	    Map<String, String> state = new HashMap<String, String>();
	    for (String key : agent.getState().keySet()) {
			state.put(key, agent.getState().get(key, String.class));
		}
	    
	    for (String timestamp : results) {
	    	if (!state.containsValue(timestamp)) {
	    		LOG.log(Level.SEVERE, "timestamp missing: " + timestamp);
	    	}
			Assert.assertTrue(state.containsValue(timestamp));
		}
	    
	    for (Entry<String, String> entry : state.entrySet()) {
	    	if (!results.contains(entry.getValue())) {
	    		LOG.log(Level.SEVERE, "entry missing: " + entry);
	    	}
			Assert.assertTrue(results.contains(entry.getValue()));
	    }
	    
	    // validate state contents
	    if (results.size() != state.keySet().size()) {
	    	LOG.log(Level.WARNING, "results: " + results);
		    LOG.log(Level.WARNING, "agent state:" + state);
	    }
	    Assert.assertEquals(threadCount, state.size()); 
	    Assert.assertEquals(results.size(), state.size()); 
	}
	
	@After
	public void after() throws Exception {
		MongoStateFactory mongo = (MongoStateFactory) AgentHost.getInstance().getStateFactory();
		// print the contents of the agents' state
		Iterator<String> iterator = mongo.getAllAgentIds();
		StringBuilder builder = new StringBuilder();
		while(iterator.hasNext()) {
			String agentId = iterator.next();
			State agentState = mongo.get(agentId);
			try {
				builder.append("\n$ ");
				builder.append(agentState.getAgentType().getCanonicalName());
				builder.append(" ("+agentId+")");
			} catch (ClassNotFoundException e) {
				e.printStackTrace(); // should not happen
			}
			int i = 0;
			for (String key : agentState.keySet()) {
				builder.append("\n  "+ (i++) +" > ");
				builder.append(key);
				builder.append(" : ");
				builder.append(agentState.get(key, Object.class));
			}
		}
		LOG.log(Level.INFO, builder.toString());
	}
	
	@AfterClass
	public static void wrapUp() {
		// clean up database after operation
		MongoStateFactory mongo = (MongoStateFactory) AgentHost.getInstance().getStateFactory();
		
		DB database = mongo.getJongo().getDatabase();
		Mongo client = database.getMongo();
		database.dropDatabase();
		client.close(); 
		
		// stop the mongo daemon & executables
		_mongod.stop();
		_mongodExe.stop();
	}


}

/**
 * simple class to test serializing/deserializing in Mongo
 * @author ronnydealservices
 *
 */
class SerializedObject implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5948607234036024288L;
	
	private Date timestamp;
	private String message;
	
	public SerializedObject() {
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
}

