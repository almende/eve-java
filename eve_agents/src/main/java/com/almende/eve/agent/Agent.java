/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.agent;

import java.io.IOException;
import java.net.URI;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.almende.eve.capabilities.handler.Handler;
import com.almende.eve.capabilities.handler.SimpleHandler;
import com.almende.eve.scheduling.Scheduler;
import com.almende.eve.scheduling.SchedulerFactory;
import com.almende.eve.state.State;
import com.almende.eve.state.StateFactory;
import com.almende.eve.transform.rpc.RpcTransform;
import com.almende.eve.transform.rpc.RpcTransformFactory;
import com.almende.eve.transform.rpc.annotation.Access;
import com.almende.eve.transform.rpc.annotation.AccessType;
import com.almende.eve.transform.rpc.annotation.Namespace;
import com.almende.eve.transform.rpc.jsonrpc.JSONRequest;
import com.almende.eve.transform.rpc.jsonrpc.JSONResponse;
import com.almende.eve.transport.Receiver;
import com.almende.eve.transport.Router;
import com.almende.eve.transport.Transport;
import com.almende.eve.transport.TransportFactory;
import com.almende.util.TypeUtil;
import com.almende.util.callback.AsyncCallback;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class Agent.
 */
public class Agent implements Receiver {
	private static final Logger	LOG			= Logger.getLogger(Agent.class
													.getName());
	private String				agentId		= null;
	private ObjectNode			config		= null;
	private State				state		= null;
	private Transport			transport	= null;
	private Scheduler			scheduler	= null;
	private final RpcTransform		rpc			= RpcTransformFactory
													.get(new SimpleHandler<Object>(
															this));
	
	/**
	 * Instantiates a new agent.
	 */
	public Agent() {
	}
	
	/**
	 * Instantiates a new agent.
	 * 
	 * @param config
	 *            the config
	 */
	public Agent(final ObjectNode config) {
		this.config = config.deepCopy();
		loadConfig();
	}
	
	/**
	 * Sets the config.
	 * 
	 * @param config
	 *            the new config
	 */
	public void setConfig(final JsonNode config) {
		this.config = config.deepCopy();
		loadConfig();
	}
	
	/**
	 * Gets the config.
	 * 
	 * @return the config
	 */
	public JsonNode getConfig() {
		return config;
	}
	
	private void loadConfig() {
		final Handler<Receiver> handle = new SimpleHandler<Receiver>(this);
		if (config.has("id")) {
			agentId = config.get("id").asText();
		}
		if (config.has("scheduler")) {
			final ObjectNode schedulerConfig = (ObjectNode) config.get("scheduler");
			if (agentId != null && schedulerConfig.has("state")) {
				final ObjectNode stateConfig = (ObjectNode) schedulerConfig
						.get("state");
				if (!stateConfig.has("id")) {
					stateConfig.put("id", "scheduler_" + agentId);
				}
			}
			scheduler = SchedulerFactory.getScheduler(schedulerConfig,
					handle);
		}
		if (config.has("state")) {
			final ObjectNode stateConfig = (ObjectNode) config.get("state");
			if (agentId != null && !stateConfig.has("id")) {
				stateConfig.put("id", agentId);
			}
			state = StateFactory.getState(stateConfig);
		}
		if (config.has("transport")) {
			if (config.get("transport").isArray()) {
				final Router router = new Router();
				final Iterator<JsonNode> iter = config.get("transport").iterator();
				while (iter.hasNext()) {
					router.register(TransportFactory.getTransport(
							(ObjectNode) iter.next(), handle));
				}
				transport = router;
			} else {
				transport = TransportFactory.getTransport(
						(ObjectNode) config.get("transport"), handle);
			}
		}
	}
	
	@Override
	public void receive(final Object msg, final URI senderUrl, final String tag) {
		final JSONResponse response = rpc.invoke(msg, senderUrl);
		if (response != null) {
			try {
				transport.send(senderUrl, response.toString(), tag);
			} catch (final IOException e) {
				LOG.log(Level.WARNING, "Couldn't send message", e);
			}
		}
	}
	
	/**
	 * Gets the scheduler.
	 * 
	 * @return the scheduler
	 */
	@Namespace("scheduler")
	@JsonIgnore
	public Scheduler getScheduler() {
		return scheduler;
	}
	
	/**
	 * Gets the state.
	 * 
	 * @return the state
	 */
	@Access(AccessType.UNAVAILABLE)
	@JsonIgnore
	public State getState() {
		return state;
	}
	
	/**
	 * Send async.
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
	 * @param type
	 *            the type
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Access(AccessType.UNAVAILABLE)
	public final <T> void sendAsync(final URI url, final String method,
			final ObjectNode params, final AsyncCallback<T> callback,
			final TypeUtil<T> type) throws IOException {
		final JSONRequest request = rpc.buildMsg(method, params, callback, type);
		transport.send(url, request.toString(), null);
	}
	
}
