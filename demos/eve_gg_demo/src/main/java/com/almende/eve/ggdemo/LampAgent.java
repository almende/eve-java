package com.almende.eve.ggdemo;

import java.io.IOException;
import java.util.List;

import org.apache.http.annotation.ThreadSafe;

import com.almende.eve.agent.AgentInterface;
import com.almende.eve.protocol.jsonrpc.annotation.Access;
import com.almende.eve.protocol.jsonrpc.annotation.AccessType;
import com.almende.eve.protocol.jsonrpc.annotation.Name;
import com.almende.eve.protocol.jsonrpc.annotation.Sender;
import com.almende.eve.protocol.jsonrpc.formats.JSONRPCException;
import com.fasterxml.jackson.core.JsonProcessingException;

@Access(AccessType.PUBLIC)
@ThreadSafe
public interface LampAgent extends AgentInterface {

	public void create(@Name("neighbours") List<String> neighbours,
			@Name("stepSize") Integer stepSize) throws JSONRPCException,
			IOException;

	public boolean isOn();

	public boolean isOnBlock() throws InterruptedException;

	public void handleGoal(@Name("goal") Goal goal, @Sender String sender)
			throws JSONRPCException, JsonProcessingException, IOException;

	public Iterable<String> getNeighbours();
}
