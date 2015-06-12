/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.agent;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.almende.eve.instantiation.CanHibernate;
import com.almende.eve.protocol.jsonrpc.annotation.Access;
import com.almende.eve.protocol.jsonrpc.annotation.AccessType;
import com.almende.eve.protocol.jsonrpc.annotation.Name;
import com.almende.eve.protocol.jsonrpc.annotation.Optional;
import com.almende.eve.protocol.jsonrpc.formats.Params;
import com.almende.util.TypeUtil;
import com.almende.util.URIUtil;
import com.almende.util.callback.AsyncCallback;
import com.almende.util.callback.SyncCallback;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class ExampleAgent.
 */
@Access(AccessType.PUBLIC)
@CanHibernate
public class ExampleAgent extends Agent implements ExampleAgentInterface {
	private static final Logger	LOG	= Logger.getLogger(ExampleAgent.class
											.getName());

	/**
	 * Hello world.
	 * 
	 * @param message
	 *            the message
	 * @return the string
	 */
	@Override
	@Access(AccessType.PUBLIC)
	public String helloWorld(@Name("message") final String message) {
		return "You said:" + message;
	}

	/**
	 * Test optional.
	 *
	 * @param optional
	 *            the optional
	 * @return the string
	 */
	public String testOptional(@Optional @Name("optional") String optional) {
		return optional;
	}

	/**
	 * Named method.
	 *
	 * @return the string
	 */
	@Name("testNamedMethod")
	public String namedMethod() {
		return "ok, worked correctly!";
	}

	/**
	 * Check thread.
	 *
	 * @return the string
	 */
	public long checkThread() {
		return Thread.currentThread().getId();
	}

	/**
	 * Gets the message.
	 *
	 * @param test
	 *            the test
	 * @return the person
	 */
	public List<MessageContainer> getMessage(
			@Name("message") final MessageContainer test) {
		return Arrays.asList(test);
	}

	/**
	 * Gets the messages.
	 *
	 * @param test
	 *            the test
	 * @return the messages
	 */
	public List<MessageContainer> getMessages(
			@Name("message") final List<MessageContainer> test) {
		return test;
	}

	/**
	 * Public version of send.
	 * 
	 * @param <T>
	 *            the generic type
	 * @param url
	 *            the url
	 * @param method
	 *            the method
	 * @param params
	 *            the params
	 * @param callback
	 *            the callback
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public <T> void pubSend(final URI url, final String method,
			final ObjectNode params, final AsyncCallback<T> callback)
			throws IOException {
		super.call(url, method, params, callback);
	}

	/**
	 * Public version of sendSync.
	 *
	 * @param <T>
	 *            the generic type
	 * @param url
	 *            the url
	 * @param method
	 *            the method
	 * @param params
	 *            the params
	 * @param type
	 *            the type
	 * @return the t
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public <T> T pubSendSync(final URI url, final String method,
			final ObjectNode params, final TypeUtil<T> type) throws IOException {
		return super.callSync(url, method, params, type);
	}

	/**
	 * Call other agent.
	 *
	 * @param url
	 *            the url
	 * @param method
	 *            the method
	 * @param params
	 *            the params
	 * @return the string
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws URISyntaxException
	 *             the URI syntax exception
	 */

	public String callOtherAgent(@Name("url") final String url,
			@Name("method") final String method,
			@Name("params") final ObjectNode params) throws IOException,
			URISyntaxException {
		return pubSendSync(URIUtil.parse(url), method, params,
				new TypeUtil<String>() {});
	}

	/* Making destroy public */
	/*
	 * (non-Javadoc)
	 * @see com.almende.eve.agent.Agent#destroy()
	 */
	@Override
	public void destroy() {
		super.destroy();
	}

	/**
	 * Run complex type test.
	 *
	 * @param uri
	 *            the uri
	 */
	public void runComplexTypeTest(URI uri) {
		final MessageContainer test = new MessageContainer();
		test.setMessage("Hi there!");

		final Params params = new Params();
		params.add("message", test);
		try {
			call(uri, "getMessage", params,
					new AsyncCallback<List<MessageContainer>>() {

						@Override
						public void onSuccess(List<MessageContainer> message) {
							LOG.info("received message:"
									+ message.get(0).getMessage());
						}

						@Override
						public void onFailure(Exception exception) {
							LOG.log(Level.WARNING, "Oops:", exception);

						}
					});

		} catch (IOException e) {
			LOG.log(Level.WARNING, "failed to send message", e);
		}
		try {
			final SyncCallback<List<MessageContainer>> callback = new SyncCallback<List<MessageContainer>>() {};
			call(uri, "getMessage", params, callback);
			LOG.info("received message:" + callback.get().get(0).getMessage());

		} catch (Exception e) {
			LOG.log(Level.WARNING, "failed to send message", e);
		}
		try {
			final List<MessageContainer> message = callSync(uri, "getMessage",
					params, new TypeUtil<List<MessageContainer>>() {});
			LOG.info("received message:" + message.get(0).getMessage());

		} catch (IOException e) {
			LOG.log(Level.WARNING, "failed to send message", e);
		}
		try {
			params.put("message", "Hi There!");
			final String message = callSync(uri, "helloWorld", params,
					String.class);
			LOG.info("received message:" + message);

		} catch (IOException e) {
			LOG.log(Level.WARNING, "failed to send message", e);
		}

	}

	@Override
	public void doSomething() {
		LOG.info("Doing something!");
	}

	@Override
	public String doMore() {
		LOG.info("Doing more!");
		return "Some string!";
	}
	
	/**
	 * Test scheduling.
	 *
	 * @throws InterruptedException
	 *             the interrupted exception
	 */
	public void testScheduling() throws InterruptedException{
		String id = scheduleInterval("doSomething",null,1000);
		
		Thread.sleep(4500);
		
		cancel(id);
		
		Thread.sleep(1000);
		
	}
}
