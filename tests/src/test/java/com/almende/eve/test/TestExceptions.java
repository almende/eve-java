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

import com.almende.eve.agent.Agent;
import com.almende.eve.agent.MyAgent;
import com.almende.eve.protocol.jsonrpc.formats.JSONRPCException;
import com.almende.util.callback.AsyncCallback;
import com.almende.util.jackson.JOM;

/**
 * The Class TestWake.
 */
public class TestExceptions extends TestCase {
	private static final Logger	LOG	= Logger.getLogger(TestExceptions.class
											.getName());

	/**
	 * Test boot: requires a testWakeService state, with a list of agents.
	 */
	@Test
	public void testException() {

		new MyAgent("Other");

		new Agent() {
			@Override
			public void onReady() {
				LOG.warning("Starting run");
				try {
					call(URI.create("local:Other"), "throwException", null,
							new AsyncCallback<Void>() {

								@Override
								public void onSuccess(Void result) {
									fail();
								}

								@Override
								public void onFailure(Exception exception) {
									if (exception instanceof JSONRPCException) {
										JSONRPCException e = (JSONRPCException) exception;
										try {
											e.throwRootCause();
										} catch (IllegalStateException e1) {
											LOG.log(Level.WARNING,
													"Good, got the illegalStateException:",
													e1);
										} catch (Throwable e1) {
											LOG.log(Level.WARNING,
													"Didn't get the expected rootCause:",
													e1);
										}
									} else {
										LOG.log(Level.WARNING,
												"Didn't get a JSONRPCException?",
												exception);
									}
								}
							});
				} catch (IOException e) {
					LOG.log(Level.WARNING, "Didn't want an IOException", e);
				}
				LOG.warning("Done");
			}
		}.setConfig(JOM.createObjectNode());
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			LOG.log(Level.WARNING, "Interrupted?", e);
		}

	}
}