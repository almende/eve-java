/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.demo;

import java.util.logging.Logger;

import org.joda.time.DateTime;

import com.almende.eve.agent.Agent;
import com.almende.eve.protocol.jsonrpc.annotation.Access;
import com.almende.eve.protocol.jsonrpc.annotation.AccessType;


/**
 * The Class TipsNTricksAgent.
 */
@Access(AccessType.PUBLIC)
public class TipsNTricksAgent extends Agent {
	private static final Logger LOG = Logger.getLogger(TipsNTricksAgent.class
			.getName());
	private int protectedCounter = 0;
	private int counter			 = 0;
	
	
	/* (non-Javadoc)
	 * @see com.almende.eve.agent.Agent#onInit()
	 */
	public void onInit(){
				
		/* Example scheduling: */
		
		//run something every second, never parallel
		scheduleSequential();
		
		//run something every second, potentially parallel
		scheduleParallel();
		
		//run something at specific seconds, potentially parallel
		scheduleSpecific();
				
		//Print the two counters, which potentially differ as there is a small race condition. (two scheduled threads doing the counter++ at the same time)
		scheduleStats();
	}
	
	
	/**
	 * On interval.
	 */
	public void onInterval(){
		counter++;
		synchronized(this) {
			protectedCounter++;
		}
	}
	
	/**
	 * Schedule sequential.
	 */
	public void scheduleSequential(){
		onInterval();
		schedule("scheduleSequential",null,1000);
	}

	/**
	 * Schedule parallel.
	 */
	public void scheduleParallel(){
		schedule("scheduleParallel",null,1000);
		onInterval();
	}
	
	/**
	 * Schedule parallel.
	 */
	public void scheduleSpecific(){
		schedule("scheduleSpecific",null,DateTime.now().plus(1000-DateTime.now().millisOfSecond().get()));
		onInterval();
	}
	
	/**
	 * Print some stats
	 */
	public void scheduleStats(){
		LOG.info("Two counters:"+counter+"/"+protectedCounter);
		schedule("scheduleStats",null,2000);
	}
	
}

