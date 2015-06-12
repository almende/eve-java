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
import com.almende.eve.protocol.jsonrpc.annotation.Name;
import com.almende.eve.protocol.jsonrpc.annotation.Optional;
import com.almende.eve.protocol.jsonrpc.annotation.RequestId;
import com.almende.eve.protocol.jsonrpc.formats.JSONRequest;
import com.almende.eve.protocol.jsonrpc.formats.Params;
import com.almende.eve.scheduling.Scheduler;
import com.almende.util.TypeUtil;
import com.almende.util.callback.AsyncCallback;
import com.almende.util.jackson.JOM;
import com.almende.util.uuid.UUID;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
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
	 *            the type represented by the given interface, of which the
	 *            generated proxy is an instance.
	 * @param url
	 *            the address of the remote agent
	 * @param agentInterface
	 *            the interface that the remote agent implements
	 * @return the t
	 */
	@Access(AccessType.UNAVAILABLE)
	protected final <T> T createAgentProxy(final URI url,
			final Class<T> agentInterface) {
		return AgentProxyFactory.genProxy(this, url, agentInterface);
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

	/**
	 * Schedule a local RPC call after the specified delay in milliseconds.
	 *
	 * @param request
	 *            the RPC request
	 * @param delay
	 *            the delay
	 * @return the triggerId of this scheduled task (for cancelling)
	 */
	@Access(AccessType.UNAVAILABLE)
	protected String schedule(final JSONRequest request, final long delay) {
		return schedule(request, getScheduler().nowDateTime().plus(delay));
	}

	/**
	 * Schedule a local RPC call after the specified delay in milliseconds.
	 *
	 * @param request
	 *            the RPC request
	 * @param delay
	 *            the delay
	 * @return the triggerId of this scheduled task (for cancelling)
	 */
	@Access(AccessType.UNAVAILABLE)
	protected String schedule(final JSONRequest request, final int delay) {
		return schedule(request, getScheduler().nowDateTime().plus(delay));
	}

	/**
	 * Schedule an local RPC call at a specified due time.
	 * 
	 * @param method
	 *            the local RPC method
	 * @param params
	 *            the local RPC method's params
	 * @param due
	 *            the due time
	 * @return the string
	 */
	@Access(AccessType.UNAVAILABLE)
	protected String schedule(final String method, final ObjectNode params,
			final DateTime due) {
		return schedule(new JSONRequest(method, params), due);
	}

	/**
	 * Schedule a local RPC call after the specified delay in milliseconds.
	 * 
	 * @param method
	 *            the local RPC method
	 * @param params
	 *            the local RPC method's params
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
	 * Schedule a local RPC call after the specified delay in milliseconds.
	 * 
	 * @param method
	 *            the local RPC method
	 * @param params
	 *            the local RPC method's params
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
	 * _schedule next.
	 *
	 * @param request
	 *            the request
	 * @param interval
	 *            the interval
	 * @param type
	 *            the type
	 * @param timestamp
	 *            the timestamp
	 * @param id
	 *            the id
	 */
	@Access(AccessType.SELF)
	public void _scheduleNext(final @Name("request") JSONRequest request,
			final @Name("interval") long interval,
			final @Name("type") String type,
			final @Optional @Name("timestamp") DateTime timestamp,
			@RequestId JsonNode id) {
		final Scheduler scheduler = getScheduler();
		if (scheduler == null) {
			return;
		}
		DateTime nextDue = scheduler.nowDateTime().plus(interval);
		final Params params = new Params();
		params.add("request", request);
		params.add("interval", interval);
		params.add("type", type);
		if (timestamp != null) {
			nextDue = timestamp.plus(interval);
			params.add("timestamp", nextDue);
		}
		switch (type) {
			case "sequential":
				receive(request, scheduler.getSchedulerUrl(), null);
				schedule(new JSONRequest(id, "_scheduleNext", params, null),
						nextDue);
				break;
			default:
				schedule(new JSONRequest(id, "_scheduleNext", params, null),
						nextDue);
				receive(request, scheduler.getSchedulerUrl(), null);
		}
	}

	private String scheduleInt(final String method, final ObjectNode params,
			final long interval, final String type) {
		final Params parms = new Params();
		parms.add("request", new JSONRequest(method, params));
		parms.add("interval", interval);
		parms.add("type", type);
		return schedule(
				new JSONRequest(JOM.createObjectNode().textNode(
						new UUID().toString()), "_scheduleNext", parms, null),
				getScheduler().nowDateTime().plus(interval));
	}

	/**
	 * Repetitive schedule a local RPC call at the specified interval in
	 * milliseconds.
	 *
	 * @param method
	 *            the local RPC method
	 * @param params
	 *            the local RPC method's params
	 * @param interval
	 *            the interval in milliseconds
	 * @return the triggerID for cancellation
	 */
	@Access(AccessType.UNAVAILABLE)
	protected String scheduleInterval(final String method,
			final ObjectNode params, final long interval) {
		return scheduleInt(method, params, interval, "parallel");
	}

	/**
	 * Repetitive schedule a local RPC call at the specified interval in
	 * milliseconds. This version only schedules the next interval after the
	 * earlier run has
	 * finished, preventing overlap between calls.
	 * 
	 * @param method
	 *            the method
	 * @param params
	 *            the params
	 * @param interval
	 *            the interval
	 * @return the triggerID for cancellation
	 */
	@Access(AccessType.UNAVAILABLE)
	protected String scheduleIntervalSequential(final String method,
			final ObjectNode params, final long interval) {
		return scheduleInt(method, params, interval, "sequential");
	}

	/**
	 * Repetitive schedule a local RPC call at the specified interval in
	 * milliseconds.
	 * This version schedules the next interval without allowing drift, the next
	 * scheduled due time is an exact interval after the former.
	 * <b>If the start timestamp is further in the past than one interval, this
	 * will run for each interval, quickly after each other to catch up!</b>
	 *
	 * @param method
	 *            the method
	 * @param params
	 *            the params
	 * @param interval
	 *            the interval
	 * @param start
	 *            the start timestamp, on which the first interval is based.
	 * @return the triggerID for cancellation
	 */
	@Access(AccessType.UNAVAILABLE)
	protected String scheduleIntervalPrecize(final String method,
			final ObjectNode params, final long interval, final DateTime start) {
		final Params parms = new Params();
		parms.add("request", new JSONRequest(method, params));
		parms.add("interval", interval);
		parms.add("timestamp", start);
		parms.add("type", "precize");
		return schedule(
				new JSONRequest(JOM.createObjectNode().textNode(
						new UUID().toString()), "_scheduleNext", parms, null),
				getScheduler().nowDateTime().plus(interval));
	}

}
