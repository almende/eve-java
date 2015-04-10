/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.test;

import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.junit.Test;

import com.almende.eve.capabilities.handler.SimpleHandler;
import com.almende.eve.protocol.jsonrpc.JSONRpcProtocol;
import com.almende.eve.protocol.jsonrpc.JSONRpcProtocolBuilder;
import com.almende.eve.protocol.jsonrpc.JSONRpcProtocolConfig;
import com.almende.eve.protocol.jsonrpc.annotation.Access;
import com.almende.eve.protocol.jsonrpc.annotation.AccessType;
import com.almende.eve.protocol.jsonrpc.annotation.Name;
import com.almende.eve.protocol.jsonrpc.annotation.Namespace;
import com.almende.eve.protocol.jsonrpc.formats.JSONRequest;
import com.almende.eve.protocol.jsonrpc.formats.Params;
import com.almende.util.URIUtil;
import com.almende.util.callback.AsyncCallback;
import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * The Class TestRpc.
 */
public class TestRpc extends TestCase {
	private static final Logger	LOG	= Logger.getLogger(TestRpc.class.getName());

	/**
	 * Test me.
	 *
	 * @throws JsonProcessingException
	 *             the json processing exception
	 */
	@Test
	public void testRpc() throws JsonProcessingException {
		final JSONRpcProtocolConfig params = new JSONRpcProtocolConfig();
		params.setId("me");

		final JSONRpcProtocol protocol = new JSONRpcProtocolBuilder()
				.withConfig(params)
				.withHandle(new SimpleHandler<Object>(new MyClass())).build();

		final AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {

			@Override
			public void onSuccess(final Boolean result) {
				LOG.warning("Success!");
				assertTrue(result);
			}

			@Override
			public void onFailure(final Exception exception) {
				LOG.log(Level.WARNING, "Expected exception:", exception);
			}

		};

		final Params parms = new Params();
		parms.add("parm", true);
		Object request = new JSONRequest("testMe", parms, callback);

		final URI myUri = URIUtil.create("local://me");
		// prepare message for transport
		Object message = protocol.outbound(request, myUri)
				.getResult();
		// transport
		Object response = protocol
				.outbound(
						protocol.inbound(message, myUri)
								.getResult(), myUri)
				.getResult();
		// transport back
		protocol.inbound(response, myUri);

		request = new JSONRequest("test.testMe", parms);

		// prepare message for transport
		message = protocol.outbound(request, myUri)
				.getResult();
		// transport
		response = protocol
				.outbound(
						protocol.inbound(message, myUri)
								.getResult(), myUri)
				.getResult();
		assertNull(response);

		request = new JSONRequest("failMe", parms, callback);

		// prepare message for transport
		message = protocol.outbound(request, myUri)
				.getResult();
		// transport
		response = protocol
				.outbound(
						protocol.inbound(message, myUri)
								.getResult(), myUri)
				.getResult();
		protocol.inbound(response, myUri);

		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {}

	}

	/**
	 * The Class MyClass.
	 */
	@Access(AccessType.PUBLIC)
	public class MyClass {

		/**
		 * Test me.
		 * 
		 * @param test
		 *            the test
		 * @return the boolean
		 */
		public Boolean testMe(@Name("parm") final Boolean test) {
			return test;
		}

		/**
		 * Fail me.
		 *
		 * @return the boolean
		 */
		public Boolean failMe() {
			return true;
		}

		/**
		 * Fail me.
		 *
		 * @param test
		 *            the test
		 * @return the boolean
		 */
		public Boolean failMe(@Name("parm") final Boolean test) {
			return test;
		}

		/**
		 * The Class MyClass.
		 */
		@Access(AccessType.PUBLIC)
		class MySubClass {

			/**
			 * Test me.
			 * 
			 * @param test
			 *            the test
			 * @return the boolean
			 */
			public Boolean testMe(@Name("parm") final Boolean test) {
				return test;
			}
		}

		/**
		 * Gets the sub.
		 *
		 * @return the sub
		 */
		@Namespace("test")
		public MySubClass getSub() {
			return new MySubClass();
		}

	}

}
