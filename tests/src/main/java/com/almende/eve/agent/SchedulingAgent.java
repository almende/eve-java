/*
 * Copyright: Almende B.V. (2015), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.agent;

import java.util.logging.Logger;

import org.joda.time.DateTime;

import com.almende.eve.protocol.jsonrpc.annotation.Access;
import com.almende.eve.protocol.jsonrpc.annotation.AccessType;

/**
 * The Class SchedulingAgent.
 */
public class SchedulingAgent extends Agent {
	private static final Logger	LOG		= Logger.getLogger(SchedulingAgent.class
												.getName());
	private static int			counter	= 0;

	/*
	 * (non-Javadoc)
	 * @see com.almende.eve.agent.AgentCore#onReady()
	 */
	public void onReady() {
		DateTime now = DateTime.now();
		super.onReady();

		LOG.warning("OnReady called!");
		schedule("repeatTest", null, now.plusMillis(400));
		schedule("repeatTest", null, now.plusMillis(400));
		schedule("repeatTest", null, now.plusMillis(400));
		schedule("repeatTest", null, now.plusMillis(400));
	}

	/**
	 * Repeat test.
	 */
	@Access(AccessType.PUBLIC)
	public void repeatTest() {
		//schedule("test", null, DateTime.now());
		test();
		schedule("repeatTest", null, DateTime.now().plusMillis(1000));
	}

	/**
	 * Test.
	 */
	@Access(AccessType.PUBLIC)
	public void test() {
		LOG.warning("test:" + counter++);
	}
}
