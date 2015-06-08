/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.junit.Test;

import com.almende.eve.agent.AgentBuilder;
import com.almende.eve.agent.AgentConfig;
import com.almende.eve.agent.ExampleAgent;
import com.almende.eve.agent.TestScaleAgent;
import com.almende.eve.protocol.jsonrpc.formats.JSONRequest;
import com.almende.eve.protocol.jsonrpc.formats.Params;
import com.almende.eve.state.file.FileStateConfig;
import com.almende.util.TypeUtil;
import com.almende.util.callback.AsyncCallback;

/**
 * The Class TestScale.
 */
public class TestScale extends TestCase {
	private static final Logger	LOG	= Logger.getLogger(TestScale.class
											.getName());

	/**
	 * Test Scale.
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws URISyntaxException
	 *             the URI syntax exception
	 * @throws InterruptedException
	 *             the interrupted exception
	 */
	@Test
	public void testScale() throws IOException, URISyntaxException,
			InterruptedException {
		AgentConfig config = new AgentConfig();
		FileStateConfig filestateconfig = new FileStateConfig();
		filestateconfig.setPath(".scaleTest");
		config.setState(filestateconfig);
		TestScaleAgent top = new TestScaleAgent("top", config, null,
				new ArrayList<Integer>());
		List<Integer> left = new ArrayList<Integer>();
		left.add(20);
		left.add(10);
		List<Integer> right = new ArrayList<Integer>();
		right.add(3);
		right.add(3);
		right.add(3);
		right.add(30);

		final long start = System.currentTimeMillis();
		LOG.warning("Starting!" + start);
		new TestScaleAgent("left", config, top.getUrls().get(0), left);
		TestScaleAgent rightAgent = new TestScaleAgent("right", config, top
				.getUrls().get(0), right);
		final long created = System.currentTimeMillis();
		LOG.warning("Created!" + created + "(" + (created - start) + " ms)");

		List<URI> leafs = rightAgent.getAllLeafs();
		assertEquals(3 * 270, leafs.size());

		final long end = System.currentTimeMillis();
		LOG.warning("Done!" + end + "(" + (end - created) + " ms)");

	}

	private void runTest(final int duration, final int parallel,
			final String test) {
		final Thread[] threads = new Thread[parallel];
		final Long[] reports = new Long[parallel];
		final String[] strings = new String[parallel];
		final TypeUtil<String> stringType = new TypeUtil<String>() {};

		final ExampleAgent[] agents = new ExampleAgent[parallel*2];
		for (int i = 0; i< parallel; i++){
			agents[i] = genAgent();
			agents[parallel+i] = genAgent();			
		}
		final Boolean[] stop = new Boolean[] { false };
		for (int i = 0; i < parallel; i++) {
			final int j = i;
			reports[i] = -1L;
			threads[i] = new Thread(new Runnable() {

				@Override
				public void run() {
					final DateTime start = DateTime.now();
					long counter = 0;
					String res = "";
					URI sender = agents[0].getUrls().get(0);
					switch (test) {
						case "direct":
							while (!stop[0]) {
								res = agents[0]
										.helloWorld("round:" + counter++);
							}
							break;
						case "rpcdirect":
							while(!stop[0]){
								final Params params = new Params("message",
										"round:" + counter++);
								agents[0].receive(new JSONRequest("helloWorld",params), sender, null);
							}
							break;
						case "sync":
							final URI peer = agents[1].getUrls().get(0);
							while (!stop[0]) {
								final Params params = new Params("message",
										"round:" + counter++);
								try {
									res = agents[0].pubSendSync(peer,
											"helloWorld", params, stringType);
								} catch (IOException e) {
									LOG.log(Level.WARNING,
											"failed to communicate", e);
								}
							}
							break;
						case "async":
						case "multi":
							final long[] cnt = new long[] { 0L };
							final String[] ress = new String[] { res };
							
							int senderId = 0;
							int receiverId = 1;
							if ("multi".equals(test)){
								senderId = j;
								receiverId = parallel+j;
							}
							final ExampleAgent agent = agents[senderId];
							final URI peeruri = agents[receiverId].getUrls().get(0);
							
							final AsyncCallback<String> callback = new AsyncCallback<String>(
									stringType) {

								@Override
								public void onSuccess(final String result) {
									if (!stop[0]) {
										ress[0] = result;
										final Params params = new Params(
												"message", "round:" + cnt[0]++);
										try {
											agent.pubSend(peeruri,
													"helloWorld", params, this);
										} catch (IOException e) {
											LOG.log(Level.WARNING,
													"failed to communicate", e);
										}
									} else {
										final DateTime finish = DateTime.now();
										final Duration duration = new Duration(
												start, finish);

										reports[j] = (cnt[0] * 1000 / duration
												.getMillis());
										strings[j] = ress[0];
									}
								}

								@Override
								public void onFailure(Exception exception) {
									LOG.log(Level.WARNING,
											"failed to communicate", exception);
								}
							};
							callback.onSuccess("init");
							return;
					}

					final DateTime finish = DateTime.now();
					final Duration duration = new Duration(start, finish);

					reports[j] = (counter * 1000 / duration.getMillis());
					strings[j] = res;
				};
			});
		}
		for (int i = 0; i < parallel; i++) {
			threads[i].start();
		}
		try {
			Thread.sleep(duration);
		} catch (InterruptedException e) {}
		stop[0] = true;

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {}

		long total = 0;
		int count = 0;
		for (int i = 0; i < parallel; i++) {
			if (reports[i] > 0) {
				total += reports[i];
				count++;
			}
		}
		LOG.warning("Total score:" + count + " treads reported:"
				+ (total / count) + " call/s");
	}

	private int	count	= 0;

	private ExampleAgent genAgent() {
		AgentConfig config = new AgentConfig();
		config.setId(new Integer(count++).toString());
		config.setClassName(ExampleAgent.class.getName());

		ExampleAgent agent = (ExampleAgent) new AgentBuilder().withConfig(
				config).build();

		return agent;
	}

	/**
	 * Test rpc scale.
	 */
	@Test
	public void testRpcScale() {

		int runTime = 50000;
		boolean runDirect = false;

		LOG.warning("Runtime test:" + runTime + " ms");
		if (runDirect) {
			LOG.warning("Direct:");
			runTest(runTime, 8, "direct");
		}
		
		LOG.warning("RPC direct:");
		runTest(runTime, 4, "rpcdirect");

//		LOG.warning("RPC Sync:");
//		runTest(runTime, 8, "sync");
//
//		LOG.warning("RPC Async:");
//		runTest(runTime, 8, "async");

//		LOG.warning("RPC Multi-agent:");
//		runTest(runTime, 8, "multi");
	}
}
