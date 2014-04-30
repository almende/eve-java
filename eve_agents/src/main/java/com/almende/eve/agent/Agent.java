/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.agent;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.almende.eve.capabilities.handler.Handler;
import com.almende.eve.capabilities.handler.SimpleHandler;
import com.almende.eve.scheduling.Scheduler;
import com.almende.eve.scheduling.SchedulerFactory;
import com.almende.eve.state.State;
import com.almende.eve.state.StateConfig;
import com.almende.eve.state.StateFactory;
import com.almende.eve.transform.rpc.RpcTransform;
import com.almende.eve.transform.rpc.RpcTransformFactory;
import com.almende.eve.transform.rpc.annotation.Access;
import com.almende.eve.transform.rpc.annotation.AccessType;
import com.almende.eve.transform.rpc.annotation.Namespace;
import com.almende.eve.transform.rpc.formats.JSONResponse;
import com.almende.eve.transport.LocalTransportConfig;
import com.almende.eve.transport.LocalTransportFactory;
import com.almende.eve.transport.Receiver;
import com.almende.eve.transport.Router;
import com.almende.eve.transport.TransportConfig;
import com.almende.eve.transport.TransportFactory;
import com.almende.util.callback.AsyncCallback;
import com.almende.util.callback.SyncCallback;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class Agent.
 */
@Access(AccessType.PUBLIC)
public class Agent implements Receiver {
	private static final Logger	LOG			= Logger.getLogger(Agent.class
													.getName());
	private String				agentId		= null;
	private AgentConfig			config		= null;
	private State				state		= null;
	private Router				transport	= null;
	private Scheduler			scheduler	= null;
	protected RpcTransform		rpc			= RpcTransformFactory
													.get(new SimpleHandler<Object>(
															this));
	protected Handler<Receiver>	receiver	= new SimpleHandler<Receiver>(this);
	
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
		setConfig(config);
	}
	
	/**
	 * Instantiates a new agent.
	 * 
	 * @param agentId
	 *            the agent id
	 * @param config
	 *            the config
	 */
	public Agent(final String agentId, final ObjectNode config) {
		this.config = new AgentConfig(agentId, config);
		loadConfig(false);
	}
	
	/**
	 * Sets the config.
	 * 
	 * @param config
	 *            the new config
	 */
	public void setConfig(final ObjectNode config) {
		this.config = new AgentConfig(config);
		loadConfig(false);
	}
	
	/**
	 * Sets the config.
	 * 
	 * @param config
	 *            the new config
	 * @param onBoot
	 *            the on boot flag
	 */
	public void setConfig(final ObjectNode config, final boolean onBoot) {
		this.config = new AgentConfig(config);
		loadConfig(onBoot);
	}
	
	/**
	 * Gets the id.
	 * 
	 * @return the id
	 */
	public String getId() {
		return agentId;
	}
	
	/**
	 * Gets the methods.
	 * 
	 * @return the methods
	 */
	@Access(AccessType.PUBLIC)
	@JsonIgnore
	public List<Object> getMethods() {
		return rpc.getMethods();
	}
	
	/**
	 * Gets the config.
	 * 
	 * @return the config
	 */
	public AgentConfig getConfig() {
		return config;
	}
	
	protected void loadConfig(final boolean onBoot) {
		agentId = config.getId();
		
		final ObjectNode schedulerConfig = config.getScheduler();
		if (schedulerConfig != null) {
			if (agentId != null && schedulerConfig.has("state")) {
				final StateConfig stateConfig = new StateConfig(
						(ObjectNode) schedulerConfig.get("state"));
				
				if (stateConfig.getId() == null) {
					stateConfig.setId("scheduler_" + agentId);
				}
			}
			scheduler = SchedulerFactory
					.getScheduler(schedulerConfig, receiver);
		}
		final ObjectNode sc = config.getState();
		if (sc != null) {
			final StateConfig stateConfig = new StateConfig(sc);
			if (agentId != null && stateConfig.getId() != null) {
				stateConfig.setId(agentId);
			}
			state = StateFactory.getState(stateConfig);
		}
		transport = new Router();
		// All agents have a local transport
		transport.register(LocalTransportFactory.get(new LocalTransportConfig(
				agentId), receiver));
		
		JsonNode transportConfig = config.getTransport();
		if (transportConfig != null) {
			if (transportConfig.isArray()) {
				final Iterator<JsonNode> iter = transportConfig.iterator();
				while (iter.hasNext()) {
					TransportConfig transconfig = new TransportConfig(
							(ObjectNode) iter.next());
					if (transconfig.get("id") == null) {
						transconfig.put("id", agentId);
					}
					transport.register(TransportFactory.getTransport(
							transconfig, receiver));
				}
			} else {
				TransportConfig transconfig = new TransportConfig(
						(ObjectNode) transportConfig);
				if (transconfig.get("id") == null) {
					transconfig.put("id", agentId);
				}
				transport.register(TransportFactory.getTransport(transconfig,
						receiver));
			}
			if (onBoot) {
				try {
					transport.connect();
				} catch (IOException e) {
					LOG.log(Level.WARNING,
							"Couldn't connect transports on boot", e);
				}
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
	 * Sets the scheduler.
	 * 
	 * @param scheduler
	 *            the new scheduler
	 */
	@JsonIgnore
	public void setScheduler(Scheduler scheduler) {
		this.scheduler = scheduler;
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
	 * Sets the state.
	 * 
	 * @param state
	 *            the new state
	 */
	@JsonIgnore
	public void setState(State state) {
		this.state = state;
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
	 * Connect all transports.
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Access(AccessType.UNAVAILABLE)
	public void connect() throws IOException {
		this.transport.connect();
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
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Access(AccessType.UNAVAILABLE)
	protected final <T> void send(final URI url, final String method,
			final ObjectNode params, final AsyncCallback<T> callback)
			throws IOException {
		transport.send(url, rpc.buildMsg(method, params, callback).toString(),
				null);
	}
	
	/**
	 * Send async for usage in proxies.
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
	@Access(AccessType.UNAVAILABLE)
	protected final <T> void send(final URI url, final Method method,
			final Object[] params, final AsyncCallback<T> callback)
			throws IOException {
		transport.send(url, rpc.buildMsg(method, params, callback).toString(),
				null);
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
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Access(AccessType.UNAVAILABLE)
	protected final <T> void send(final URI url, final String method,
			final ObjectNode params) throws IOException {
		transport
				.send(url, rpc.buildMsg(method, params, null).toString(), null);
	}
	
	/**
	 * Send sync, expecting a response.
	 * 
	 * @param <T>
	 *            the generic type
	 * @param url
	 *            the url
	 * @param method
	 *            the method
	 * @param params
	 *            the params
	 * @return response
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Access(AccessType.UNAVAILABLE)
	protected final <T> T sendSync(final URI url, final String method,
			final ObjectNode params) throws IOException {
		SyncCallback<T> callback = new SyncCallback<T>();
		transport.send(url, rpc.buildMsg(method, params, callback).toString(),
				null);
		try {
			return callback.get();
		} catch (Exception e) {
			throw new IOException(e);
		}
	}
	
}
