/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.ggdemo;

import java.io.IOException;
import java.util.ArrayList;

import com.almende.eve.agent.AgentInterface;
import com.almende.eve.agent.annotation.ThreadSafe;
import com.almende.eve.rpc.annotation.Access;
import com.almende.eve.rpc.annotation.AccessType;
import com.almende.eve.rpc.annotation.Name;
import com.almende.eve.rpc.annotation.Sender;
import com.almende.eve.rpc.jsonrpc.JSONRPCException;
import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * The Interface LampAgent.
 */
@Access(AccessType.PUBLIC)
@ThreadSafe(true)
public interface LampAgent extends AgentInterface {
	
	/**
	 * Creates the.
	 * 
	 * @param neighbours
	 *            the neighbours
	 * @param stepSize
	 *            the step size
	 * @throws JSONRPCException
	 *             the jSONRPC exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void create(@Name("neighbours") ArrayList<String> neighbours,
			@Name("stepSize") Integer stepSize) throws JSONRPCException, IOException;
	
	/**
	 * Checks if is on.
	 * 
	 * @return true, if is on
	 */
	public boolean isOn();
	
	/**
	 * Checks if is on block.
	 * 
	 * @return true, if is on block
	 * @throws InterruptedException
	 *             the interrupted exception
	 */
	public boolean isOnBlock() throws InterruptedException;
	
	/**
	 * Handle goal.
	 * 
	 * @param goal
	 *            the goal
	 * @param sender
	 *            the sender
	 * @throws JSONRPCException
	 *             the jSONRPC exception
	 * @throws JsonProcessingException
	 *             the json processing exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void handleGoal(@Name("goal") Goal goal, @Sender String sender) throws 
			JSONRPCException, JsonProcessingException, IOException;
	
	/**
	 * Gets the neighbours.
	 * 
	 * @return the neighbours
	 */
	public Iterable<String> getNeighbours();
}
