/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.algorithms.test.agents;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

import com.almende.eve.agent.Agent;
import com.almende.eve.algorithms.DAA;
import com.almende.eve.algorithms.DAAValueBean;
import com.almende.eve.algorithms.Trickle;
import com.almende.eve.protocol.jsonrpc.annotation.Access;
import com.almende.eve.protocol.jsonrpc.annotation.AccessType;
import com.almende.eve.protocol.jsonrpc.annotation.Name;
import com.almende.eve.protocol.jsonrpc.formats.Params;
import com.almende.util.jackson.JOM;
import com.fasterxml.jackson.databind.node.ObjectNode;


/**
 * The Class DAAAgent.
 */
@Access(AccessType.PUBLIC)
public class DAAAgent extends Agent {
	private DAA daa = new DAA();
	private Trickle trickle = new Trickle();
	private List<URI> neighbors = new ArrayList<URI>();
	private String intTaskId = null;
	private String sendTaskId = null;
	
	/**
	 * Sets the initial DAA value.
	 *
	 * @param value
	 *            the new initial value
	 */
	public void start(final double value){
		final ObjectNode config = JOM.createObjectNode();
		config.put("width", 1000);
		config.put("initialTTL", 10);
		config.put("evictionFactor", 3);
		
		daa.configure(config);
		daa.setNewValue(value);
		
		long[] intervals = trickle.next();
		sendTaskId = schedule("sendValue",null,DateTime.now().plus(intervals[0]));
		intTaskId = schedule("nextInterval",null,DateTime.now().plus(intervals[1]));
	}
	
	/**
	 * Send value.
	 *
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void sendValue() throws IOException{
		if (trickle.check()){
			for (final URI agent: neighbors){
				Params params = new Params();
				params.add("value", daa.getCurrentEstimate());
				call(agent,"daaReceive",params);
			}
		}
	}
	
	/**
	 * Next interval.
	 */
	public void nextInterval(){
		final long[] intervals = trickle.next();
		sendTaskId = schedule("sendValue",null,DateTime.now().plus(intervals[0]));
		intTaskId = schedule("nextInterval",null,DateTime.now().plus(intervals[1]));		
	}
	
	/**
	 * Daa receive.
	 *
	 * @param value
	 *            the value
	 */
	public void daaReceive(final @Name("value") DAAValueBean value){
		if (daa.getCurrentEstimate().computeSum() == value.computeSum()){
			trickle.incr();
			//Still receive to update TTLs:
			daa.receive(value);
		} else {
			daa.receive(value);
			getScheduler().cancel(sendTaskId);
			getScheduler().cancel(intTaskId);
			final long[] intervals = trickle.reset();
			sendTaskId = schedule("sendValue",null,DateTime.now().plus(intervals[0]));
			intTaskId = schedule("nextInterval",null,DateTime.now().plus(intervals[1]));	
		}
	}
	
	/**
	 * Change value.
	 *
	 * @param value
	 *            the value
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void changeValue(@Name("value") Double value) throws IOException{
		DAAValueBean old = daa.negateValue();
		//send old value to network to evict it.
		for (final URI agent: neighbors){
			Params params = new Params();
			params.add("value", old);
			call(agent,"daaReceive",params);
		}
		
		//set new value for next round:
		daa.setNewValue(value);
		getScheduler().cancel(sendTaskId);
		getScheduler().cancel(intTaskId);
		final long[] intervals = trickle.reset();
		sendTaskId = schedule("sendValue",null,DateTime.now().plus(intervals[0]));
		intTaskId = schedule("nextInterval",null,DateTime.now().plus(intervals[1]));	
	}
}
