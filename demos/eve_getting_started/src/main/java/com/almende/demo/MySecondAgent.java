package com.almende.demo;

import java.io.IOException;
import java.net.URI;

import com.almende.eve.agent.Agent;
import com.almende.eve.transform.rpc.annotation.Access;
import com.almende.eve.transform.rpc.annotation.AccessType;
import com.almende.eve.transform.rpc.annotation.Name;
import com.almende.util.jackson.JOM;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Access(AccessType.PUBLIC)
public class MySecondAgent extends Agent {

	public void result(@Name("result") String result) {
		System.out.println(result);
	}

	public void getHelloWorld() throws IOException {

		ObjectNode params = JOM.createObjectNode();
		params.put("message", "Hi there!");

		// call the other agent
		call(URI.create("http://localhost:8081/agents/helloWorld/"),
				"helloWorld", params);
	}
}
