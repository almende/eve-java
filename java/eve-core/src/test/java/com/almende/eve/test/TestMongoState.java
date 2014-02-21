package com.almende.eve.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.almende.eve.agent.AgentHost;
import com.almende.eve.scheduler.RunnableSchedulerFactory;
import com.almende.eve.state.State;
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
	
	private static final String AGENT_ID = TestMongoState.class.getCanonicalName();
	private static final Logger	LOG	= Logger.getLogger("TestMongoState");
	
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
		host.createAgent(TestStateAgent.class, AGENT_ID);
	}
	
	@Before
	public void before() throws Exception {
		TestStateAgent agent = (TestStateAgent) AgentHost.getInstance().getAgent(AGENT_ID);
		agent.getState().clear();
	}
	 
	@Test
	public void test16UpdateThreads() throws Exception {
	    test(16);
	}
	
	@Test
	public void test32UpdateThreads() throws Exception {
	    test(32);
	}

	@Test
	public void test64UpdateThreads() throws Exception {
	    test(64);
	}
	 
	private void test(final int threadCount) throws Exception {
		
		// define the update task
	    Callable<Long> updateTask = new Callable<Long>() {
	        @Override
	        public Long call() throws Exception {
	        	TestStateAgent agent = (TestStateAgent) AgentHost.getInstance().getAgent(AGENT_ID);
	        	Long timestamp = System.nanoTime();
	            agent.push(timestamp);
	            return timestamp;
	        }
	    };
	    
	    List<Callable<Long>> tasks = Collections.nCopies(threadCount, updateTask);
	    
	    ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
	    List<Future<Long>> futures = executorService.invokeAll(tasks);
	    List<Long> results = new ArrayList<Long>(futures.size());
	    
	    for (Future<Long> future : futures) {
	        // will forward exception to the unit test when any of the task throws one.
	    	Long version = future.get();
	    	results.add(version);
	    }
	    
	    // Validate the futures list
	    Assert.assertEquals(threadCount, futures.size());
	    
	    TestStateAgent agent = (TestStateAgent) AgentHost.getInstance().getAgent(AGENT_ID);
	    Map<String, Object> state = new HashMap<String, Object>();
	    for (String key : agent.getState().keySet()) {
			state.put(key, agent.getState().get(key, Object.class));
		}
	    
	    // validate state contents
	    Assert.assertEquals(threadCount, agent.getState().size()); 
	    Assert.assertEquals(results.size(), state.size()); 
	    
	    LOG.log(Level.INFO, "results: " + results);
	    LOG.log(Level.INFO, "agent state:" + state);
	    
	    for (Long timestamp : results) {
			Assert.assertTrue(state.containsValue(timestamp));
		}
	}
	
	@AfterClass
	public static void wrapUp() {
		
		MongoStateFactory mongo = (MongoStateFactory) AgentHost.getInstance().getStateFactory();
		// print the contents of the agents' state
		Iterator<String> iterator = mongo.getAllAgentIds();
		while(iterator.hasNext()) {
			String agentId = iterator.next();
			State agentState = mongo.get(agentId);
			try {
				LOG.log(Level.INFO, "> " + agentState.getAgentType().getCanonicalName()+" ("+ agentId+")");
			} catch (ClassNotFoundException e) {
				e.printStackTrace(); // should not happen
			}
			for (String key : agentState.keySet()) {
				LOG.log(Level.INFO, "    > " + key +" : "+ agentState.get(key, String.class));
			}
		}
		// clean up database after operation
		DB database = mongo.getJongo().getDatabase();
		Mongo client = database.getMongo();
		database.dropDatabase();
		client.close(); 
		
		// stop the mongo daemon & executables
		_mongod.stop();
		_mongodExe.stop();
	}


}
