package com.almende.demo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;

import com.almende.eve.agent.Agent;
import com.almende.eve.capabilities.Config;
import com.almende.eve.config.YamlReader;
import com.almende.eve.deploy.Boot;
import com.almende.eve.transform.rpc.annotation.Access;
import com.almende.eve.transform.rpc.annotation.AccessType;
import com.almende.eve.transform.rpc.annotation.Name;
import com.almende.eve.transform.rpc.annotation.Sender;
import com.almende.util.jackson.JOM;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Access(AccessType.PUBLIC)
public class MyFirstAgent extends Agent {

	public void helloWorld(@Name("message") String message,
			@Sender String senderUrl) throws IOException {
		String result = "Hello World! You said:" + message;
		ObjectNode params = JOM.createObjectNode();
		params.put("result", result);

		call(URI.create(senderUrl), "result", params);
	}

	public static void main(String[] args) throws FileNotFoundException {
		final Config configfile = YamlReader.load(
				new FileInputStream(new File(args[0]))).expand();
		Boot.boot(configfile);
	}

}
