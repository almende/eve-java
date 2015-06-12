/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.test;

import java.net.URI;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.joda.time.DateTime;
import org.junit.Test;

import com.almende.eve.capabilities.handler.SimpleHandler;
import com.almende.eve.scheduling.PersistentSchedulerConfig;
import com.almende.eve.scheduling.Scheduler;
import com.almende.eve.scheduling.SchedulerBuilder;
import com.almende.eve.state.file.FileStateBuilder;
import com.almende.eve.state.file.FileStateConfig;
import com.almende.eve.transport.Receiver;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * The Class TestScheduling.
 */
public class TestScheduling extends TestCase {
	private static final Logger	LOG	= Logger.getLogger(TestScheduling.class
											.getName());

	/**
	 * Test scheduling.
	 */
	@Test
	public void testScheduling() {
		final PersistentSchedulerConfig params = new PersistentSchedulerConfig();
		final FileStateConfig state = new FileStateConfig();
		state.put("class", FileStateBuilder.class.getName());
		state.put("path", ".eveagents_schedulingtest");
		state.put("id", "testScheduling");

		params.setState(state);
		params.setId("TestScheduler");

		final Scheduler test = new SchedulerBuilder().withConfig(params)
				.withHandle(new SimpleHandler<Receiver>(new MyReceiver()))
				.build();

		test.schedule(null, "Hi there!", DateTime.now());

		test.schedule(null, "Hi there!", DateTime.now().plusSeconds(10));

		try {
			Thread.sleep(11000);
		} catch (final InterruptedException e) {}

	}

	/**
	 * The Class MyReceiver.
	 */
	class MyReceiver implements Receiver {

		/*
		 * (non-Javadoc)
		 * @see com.almende.eve.transport.Receiver#receive(java.lang.Object,
		 * java.net.URI, java.lang.String)
		 */
		@Override
		public void receive(final Object msg, final URI senderUrl,
				final String tag) {
			if (msg instanceof JsonNode) {
				LOG.warning("Received json msg:'" + msg + "' from: "
						+ senderUrl.toASCIIString());
			} else {
				LOG.warning("Received msg:'" + msg + "' from: "
						+ senderUrl.toASCIIString());
			}
		}

	}
}
