package com.almende.test;

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
import com.almende.test.agents.TestStateAgent;
import com.mongodb.DB;
import com.mongodb.Mongo;

public class TestMongoState {
	
	private static final String AGENT_ID = TestMongoState.class.getCanonicalName();
	private static final Logger	LOG	= Logger.getLogger("TestMongoState");
	
	@BeforeClass
	public static void setupEve() throws Exception {
		
		// another precondition here: mongodb daemon must be running
		
		// setup separate database for unit testing
		Map<String, Object> config = new HashMap<String, Object>();
		config.put("database", "junit"); 
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
		
	    Callable<Long> updateTask = new Callable<Long>() {
	        @Override
	        public Long call() throws Exception {
	        	TestStateAgent agent = (TestStateAgent) AgentHost.getInstance().getAgent(AGENT_ID);
	            agent.push((Long) System.nanoTime());
	            return (long) agent.getState().size();
	        }
	    };
	    
	    List<Callable<Long>> tasks = Collections.nCopies(threadCount, updateTask);
	    
	    ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
	    List<Future<Long>> futures = executorService.invokeAll(tasks);
	    List<Long> resultList = new ArrayList<Long>(futures.size());
	    
	    // Check for exceptions
	    for (Future<Long> future : futures) {
	        // Throws an exception if an exception was thrown by the task.
	    	Long version = future.get();
	    	resultList.add(version);
	    }
	    // Validate the IDs
	    Assert.assertEquals(threadCount, futures.size());
	    
	    TestStateAgent agent = (TestStateAgent) AgentHost.getInstance().getAgent(AGENT_ID);
	    Assert.assertEquals(threadCount, agent.getState().size()); 
	}
	
	@AfterClass
	public static void wrapUp() {
		
		MongoStateFactory mongo = (MongoStateFactory) AgentHost.getInstance().getStateFactory();
		// print all result
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
		
	}


}
