/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.agent;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.List;

import org.joda.time.DateTime;

import com.almende.eve.protocol.jsonrpc.JSONRpcProtocol;
import com.almende.eve.protocol.jsonrpc.annotation.Access;
import com.almende.eve.protocol.jsonrpc.annotation.AccessType;
import com.almende.eve.protocol.jsonrpc.formats.JSONRequest;
import com.almende.util.TypeUtil;
import com.almende.util.callback.AsyncCallback;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class Agent, wraps various convenience methods around AgentCore.
 */
@Access(AccessType.UNAVAILABLE)
public class Agent extends AgentCore implements AgentInterface {

	/**
	 * Instantiates a new agent.
	 */
	public Agent() {
		super();
	}

	/**
	 * Instantiates a new agent with given configuration. For configuration
	 * options check AgentConfig.
	 * 
	 * @see AgentConfig
	 * @param config
	 *            A JSON tree containing the configuration for this agent
	 */
	public Agent(final ObjectNode config) {
		super(config);
	}

	/**
	 * Instantiates a new agent, with given id and configuration.
	 * 
	 * @param agentId
	 *            the new agent id
	 * @param config
	 *            A JSON tree containing the configuration for this agent
	 */
	public Agent(final String agentId, ObjectNode config) {
		super(agentId, config);
	}

	@Access(AccessType.PUBLIC)
	@Override
	public String getType() {
		return this.getClass().getName();
	}

	@Access(AccessType.PUBLIC)
	@Override
	@JsonIgnore
	public List<URI> getUrls() {
		return caller.getSenderUrls();
	}

	@Access(AccessType.PUBLIC)
	@Override
	@JsonIgnore
	public ObjectNode getMethods() {
		// TODO: find a different way to get this list. (maybe loop over all
		// protocols, let each JSONRpcProtocol add methods.
		return ((JSONRpcProtocol) getProtocolStack().getLast()).getMethods();
	}

	/**
	 * Creates a proxy for given URL and interface, with this agent as sender.
	 * 
	 * @param <T>
	 *            the generic type
	 * @param url
	 *            the url
	 * @param agentInterface
	 *            the agent interface
	 * @return the t
	 */
	@Access(AccessType.UNAVAILABLE)
	protected final <T> T createAgentProxy(final URI url,
			final Class<T> agentInterface) {
		return AgentProxyFactory.genProxy(this, url, agentInterface);
	}

	/**
	 * Schedule an RPC call at a specified due time.
	 * 
	 * @param method
	 *            the method
	 * @param params
	 *            the params
	 * @param due
	 *            the due
	 * @return the string
	 */
	@Access(AccessType.UNAVAILABLE)
	protected String schedule(final String method, final ObjectNode params,
			final DateTime due) {
		return schedule(new JSONRequest(method, params), due);
	}

	/**
	 * Schedule an RPC call after the specified delay in milliseconds.
	 * 
	 * @param method
	 *            the method
	 * @param params
	 *            the params
	 * @param delay
	 *            the delay
	 * @return the triggerId of this scheduled task (for cancelling)
	 */
	@Access(AccessType.UNAVAILABLE)
	protected String schedule(final String method, final ObjectNode params,
			final int delay) {
		return schedule(new JSONRequest(method, params), getScheduler()
				.nowDateTime().plus(delay));
	}

	/**
	 * Schedule an RPC call after the specified delay in milliseconds.
	 * 
	 * @param method
	 *            the method
	 * @param params
	 *            the params
	 * @param delay
	 *            the delay
	 * @return the triggerId of this scheduled task (for cancelling)
	 */
	@Access(AccessType.UNAVAILABLE)
	protected String schedule(final String method, final ObjectNode params,
			final long delay) {
		return schedule(new JSONRequest(method, params), getScheduler()
				.nowDateTime().plus(delay));
	}

	/**
	 * Send async, expecting a response through the given callback.
	 * 
	 * @param <T>
	 *            the generic type of the result, controlled by the TypeUtil
	 *            injector.
	 * @param url
	 *            the address of the other agent
	 * @param method
	 *            the remote RPC method
	 * @param params
	 *            the remote RPC method's params
	 * @param callback
	 *            A callback with the expected result type.
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	protected <T> void call(final URI url, final String method,
			final ObjectNode params, final AsyncCallback<T> callback)
			throws IOException {
		caller.call(url, method, params, callback);
	}

	/**
	 * Send async, expecting a response through the given callback.
	 * 
	 * @param <T>
	 *            the generic type of the result, controlled by the TypeUtil injector.
	 * @param url
	 *            the address of the other agent
	 * @param method
	 *            the remote RPC method
	 * @param params
	 *            the remote RPC method's params
	 * @param callback
	 *            A callback with the expected result type.
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	protected <T> void call(final URI url, final Method method,
			final Object[] params, final AsyncCallback<T> callback)
			throws IOException {
		caller.call(url, method, params, callback);
	}

	/**
	 * Send JSON-RPC notification, expecting no response.
	 *
	 * @param url
	 *            the address of the other agent
	 * @param method
	 *            the remote RPC method
	 * @param params
	 *            the remote RPC method's params
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	protected void call(final URI url, final String method,
			final ObjectNode params) throws IOException {
		caller.call(url, method, params);
	}

	/**
	 * Send synchronous request, waiting for a response.
	 *
	 * @param <T>
	 *            the generic type of the result, controlled by the TypeUtil
	 *            injector.
	 * @param url
	 *            the address of the other agent
	 * @param method
	 *            the remote RPC method
	 * @param params
	 *            the remote RPC method's params
	 * @param type
	 *            the expected result type, in the form of a Java Type.
	 * @return the result, cast/converted to the given type.
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	protected <T> T callSync(final URI url, final String method,
			final ObjectNode params, final Type type) throws IOException {
		return caller.callSync(url, method, params, type);
	}

	/**
	 * Send synchronous request, waiting for a response.
	 *
	 * @param <T>
	 *            the generic type of the result, controlled by the TypeUtil
	 *            injector.
	 * @param url
	 *            the address of the other agent
	 * @param method
	 *            the remote RPC method
	 * @param params
	 *            the remote RPC method's params
	 * @param type
	 *            the expected result type, in the form of a Jackson JavaType.
	 * @return the result, cast/converted to the given type.
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	protected <T> T callSync(final URI url, final String method,
			final ObjectNode params, final JavaType type) throws IOException {
		return caller.callSync(url, method, params, type);
	}

	/**
	 * Send synchronous request, waiting for a response.
	 *
	 * @param <T>
	 *            the generic type of the result, controlled by the TypeUtil
	 *            injector.
	 * @param url
	 *            the address of the other agent
	 * @param method
	 *            the remote RPC method
	 * @param params
	 *            the remote RPC method's params
	 * @param clazz
	 *            the expected result type, in the form of a class.
	 * @return the result, cast/converted to the given type.
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	protected <T> T callSync(final URI url, final String method,
			final ObjectNode params, final Class<T> clazz) throws IOException {
		return caller.callSync(url, method, params, clazz);
	}

	/**
	 * Send synchronous request, waiting for a response.
	 *
	 * @param <T>
	 *            the generic type of the result, controlled by the TypeUtil
	 *            injector.
	 * @param url
	 *            the address of the other agent
	 * @param method
	 *            the remote RPC method
	 * @param params
	 *            the remote RPC method's params
	 * @param type
	 *            the expected result type, in the form of a TypeUtil injector.
	 * @return the result, cast/converted to the given type.
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	protected <T> T callSync(final URI url, final String method,
			final ObjectNode params, final TypeUtil<T> type) throws IOException {
		return caller.callSync(url, method, params, type);
	}
}
