/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.test;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.junit.Test;

import com.almende.eve.agent.AgentConfig;
import com.almende.eve.agent.ExampleAgent;
import com.almende.eve.protocol.InboxProtocolConfig;
import com.almende.util.TypeUtil;
import com.almende.util.callback.AsyncCallback;
import com.almende.util.jackson.JOM;
import com.fasterxml.jackson.databind.node.ArrayNode;

/**
 * The Class TestAgents.
 */
public class TestInboxAgent extends TestCase {
	private static final Logger	LOG	= Logger.getLogger(TestInboxAgent.class
											.getName());

	/**
	 * Test agents.
	 *
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws InterruptedException
	 *             the interrupted exception
	 */
	@Test
	public void testAgent() throws IOException, InterruptedException {

		final AgentConfig config = new AgentConfig("normal");

		ExampleAgent agent = new ExampleAgent();
		agent.setConfig(config);

		final AgentConfig config2 = new AgentConfig("inbox");
		final ArrayNode protocols = JOM.createArrayNode();
		protocols.add(new InboxProtocolConfig());
		config2.setProtocols(protocols);

		ExampleAgent agent2 = new ExampleAgent();
		agent2.setConfig(config2);

		final long[] res = new long[2];
		agent.pubSend(URI.create("local:normal"), "checkThread", null,
				new AsyncCallback<Long>() {

					@Override
					public void onSuccess(final Long result) {
						LOG.warning("Received:'" + result + "'");
						res[0] = result;
					}

					@Override
					public void onFailure(final Exception exception) {
						LOG.log(Level.SEVERE, "", exception);
						fail();
					}

				});

		res[1] = agent.pubSendSync(URI.create("local:normal"), "checkThread",
				null, new TypeUtil<Long>() {});

		final long[] res2 = new long[2];
		agent2.pubSend(URI.create("local:inbox"), "checkThread", null,
				new AsyncCallback<Long>() {

					@Override
					public void onSuccess(final Long result) {
						LOG.warning("Received:'" + result + "'");
						res2[0] = result;
					}

					@Override
					public void onFailure(final Exception exception) {
						LOG.log(Level.SEVERE, "", exception);
						fail();
					}

				});

		res2[1] = agent2.pubSendSync(URI.create("local:inbox"), "checkThread",
				null, new TypeUtil<Long>() {});

		Thread.sleep(500);
		
		LOG.warning("normal:"+res[0]+":"+res[1]+"   inbox:"+res2[0]+":"+res2[1]);
		assertEquals(res2[0],res2[1]);
		
	}

}
