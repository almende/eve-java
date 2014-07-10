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

import org.joda.time.DateTime;

import com.almende.eve.capabilities.handler.Handler;
import com.almende.eve.capabilities.handler.SimpleHandler;
import com.almende.eve.scheduling.Scheduler;
import com.almende.eve.scheduling.SchedulerBuilder;
import com.almende.eve.state.State;
import com.almende.eve.state.StateBuilder;
import com.almende.eve.state.StateConfig;
import com.almende.eve.transform.rpc.RpcTransform;
import com.almende.eve.transform.rpc.RpcTransformBuilder;
import com.almende.eve.transform.rpc.annotation.Access;
import com.almende.eve.transform.rpc.annotation.AccessType;
import com.almende.eve.transform.rpc.annotation.Namespace;
import com.almende.eve.transform.rpc.formats.JSONResponse;
import com.almende.eve.transport.LocalTransportBuilder;
import com.almende.eve.transport.LocalTransportConfig;
import com.almende.eve.transport.Receiver;
import com.almende.eve.transport.Router;
import com.almende.eve.transport.Transport;
import com.almende.eve.transport.TransportBuilder;
import com.almende.eve.transport.TransportConfig;
import com.almende.util.callback.AsyncCallback;
import com.almende.util.callback.SyncCallback;
import com.almende.util.jackson.JOM;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class Agent.
 */
@Access(AccessType.UNAVAILABLE)
public class Agent implements Receiver {
	private static final Logger	LOG			= Logger.getLogger(Agent.class
													.getName());
	private String				agentId		= null;
	private AgentConfig			config		= null;
	private State				state		= null;
	private Router				transport	= new Router();
	private Scheduler			scheduler	= null;
	private RpcTransform		rpc			= new RpcTransformBuilder()
													.withHandle(
															new SimpleHandler<Object>(
																	this))
													.build();
	private Handler<Receiver>	receiver	= new SimpleHandler<Receiver>(this);
	
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
	 * Instantiates a new agent.
	 * 
	 * @param agentId
	 *            the agent id
	 * @param config
	 *            the config
	 * @param onBoot
	 *            the on boot
	 */
	public Agent(final String agentId, final ObjectNode config,
			final boolean onBoot) {
		this.config = new AgentConfig(agentId, config);
		loadConfig(onBoot);
	}
	
	/**
	 * @return the rpc
	 */
	@JsonIgnore
	protected RpcTransform getRpc() {
		return rpc;
	}
	
	/**
	 * @param rpc
	 *            the rpc to set
	 */
	protected void setRpc(RpcTransform rpc) {
		this.rpc = rpc;
	}
	
	/**
	 * @return the receiver
	 */
	@JsonIgnore
	protected Handler<Receiver> getReceiver() {
		return receiver;
	}
	
	/**
	 * @param receiver
	 *            the receiver to set
	 */
	protected void setReceiver(Handler<Receiver> receiver) {
		this.receiver = receiver;
	}
	
	/**
	 * Gets the transport.
	 * 
	 * @return the transport
	 */
	protected Router getTransport() {
		return transport;
	}

	/**
	 * Sets the transport.
	 * 
	 * @param transport
	 *            the new transport
	 */
	protected void setTransport(Router transport) {
		this.transport = transport;
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
	@Access(AccessType.PUBLIC)
	public String getId() {
		return agentId;
	}
	
	/**
	 * Gets the type.
	 * 
	 * @return the type
	 */
	@Access(AccessType.PUBLIC)
	public String getType() {
		return this.getClass().getName();
	}
	
	/**
	 * Gets the urls.
	 * 
	 * @return the urls
	 */
	@Access(AccessType.PUBLIC)
	@JsonIgnore
	public List<URI> getUrls() {
		return transport.getAddresses();
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
	@Access(AccessType.PUBLIC)
	public AgentConfig getConfig() {
		return config;
	}
	
	protected void loadConfig(final boolean onBoot) {
		agentId = config.getId();
		loadScheduler(config.getScheduler());
		loadState(config.getState());
		loadTransports(config.getTransport(), onBoot);
		// All agents have a local transport
		transport.register(new LocalTransportBuilder()
				.withConfig(new LocalTransportConfig(agentId))
				.withHandle(receiver).build());
	}
	
	/**
	 * Sets the scheduler.
	 * 
	 * @param scheduler
	 *            the new scheduler
	 */
	@JsonIgnore
	public void setScheduler(final Scheduler scheduler) {
		this.scheduler = scheduler;
		config.put("scheduler", scheduler.getParams());
	}
	
	/**
	 * Load scheduler.
	 * 
	 * @param schedulerConfig
	 *            the scheduler config
	 */
	public void loadScheduler(final ObjectNode schedulerConfig) {
		if (schedulerConfig != null) {
			if (agentId != null && schedulerConfig.has("state")) {
				final StateConfig stateConfig = new StateConfig(
						(ObjectNode) schedulerConfig.get("state"));
				
				if (stateConfig.getId() == null) {
					stateConfig.setId("scheduler_" + agentId);
				}
			}
			scheduler = new SchedulerBuilder().withConfig(schedulerConfig)
					.withHandle(receiver).build();
			
			config.put("scheduler", schedulerConfig);
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
	 * Sets the state.
	 * 
	 * @param state
	 *            the new state
	 */
	@JsonIgnore
	public void setState(final State state) {
		this.state = state;
		config.put("state", state.getParams());
	}
	
	/**
	 * Load state.
	 * 
	 * @param sc
	 *            the sc
	 */
	public void loadState(final ObjectNode sc) {
		if (sc != null) {
			final StateConfig stateConfig = new StateConfig(sc);
			if (agentId != null && stateConfig.getId() == null) {
				stateConfig.setId(agentId);
			}
			state = new StateBuilder().withConfig(stateConfig).build();
			config.put("state", stateConfig);
		}
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
	 * Adds the transport.
	 * 
	 * @param transport
	 *            the transport
	 */
	public void addTransport(final Transport transport) {
		this.transport.register(transport);
		
		final JsonNode transportConfig = config.get("transport");
		if (transportConfig.isArray()) {
			((ArrayNode) transportConfig).add(transport.getParams());
		} else {
			final ArrayNode transports = JOM.createArrayNode();
			transports.add(transportConfig);
			transports.add(transport.getParams());
			config.put("transport", transports);
		}
	}
	
	/**
	 * Load transport.
	 * 
	 * @param transportConfig
	 *            the transport config
	 * @param onBoot
	 *            the on boot
	 */
	public void loadTransports(final JsonNode transportConfig,
			final boolean onBoot) {
		if (transportConfig != null) {
			if (transportConfig.isArray()) {
				final Iterator<JsonNode> iter = transportConfig.iterator();
				while (iter.hasNext()) {
					final TransportConfig transconfig = new TransportConfig(
							(ObjectNode) iter.next());
					// TODO: Somewhat ugly, not every transport requires an id.
					if (transconfig.get("id") == null) {
						transconfig.put("id", agentId);
					}
					transport.register(new TransportBuilder()
							.withConfig(transconfig).withHandle(receiver)
							.build());
				}
			} else {
				final TransportConfig transconfig = new TransportConfig(
						(ObjectNode) transportConfig);
				if (transconfig.get("id") == null) {
					transconfig.put("id", agentId);
				}
				transport.register(new TransportBuilder()
						.withConfig(transconfig).withHandle(receiver).build());
			}
			
			if (onBoot) {
				try {
					transport.connect();
				} catch (final IOException e) {
					LOG.log(Level.WARNING,
							"Couldn't connect transports on boot", e);
				}
			}
			config.put("transport", transportConfig);
		}
	}
	
	/**
	 * Connect all transports.
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Access(AccessType.UNAVAILABLE)
	public void connect() throws IOException {
		transport.connect();
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
	public String schedule(final String method, final ObjectNode params,
			final DateTime due) {
		return getScheduler().schedule(rpc.buildMsg(method, params), due);
	}
	
	/**
	 * Schedule an RPC call at a specified due time.
	 * 
	 * @param method
	 *            the method
	 * @param params
	 *            the params
	 * @param delay
	 *            the delay
	 * @return the string
	 */
	@Access(AccessType.UNAVAILABLE)
	public String schedule(final String method, final ObjectNode params,
			final int delay) {
		return getScheduler().schedule(rpc.buildMsg(method, params), delay);
	}
	
	/**
	 * Cancel.
	 * 
	 * @param taskId
	 *            the task id
	 */
	@Access(AccessType.UNAVAILABLE)
	public void cancel(final String taskId) {
		getScheduler().cancel(taskId);
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
	protected <T> void call(final URI url, final String method,
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
	protected <T> void call(final URI url, final Method method,
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
	protected <T> void call(final URI url, final String method,
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
	protected <T> T callSync(final URI url, final String method,
			final ObjectNode params) throws IOException {
		final SyncCallback<T> callback = new SyncCallback<T>() {
		};
		transport.send(url, rpc.buildMsg(method, params, callback).toString(),
				null);
		try {
			return callback.get();
		} catch (final Exception e) {
			throw new IOException(e);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.almende.eve.transport.Receiver#receive(java.lang.Object,
	 * java.net.URI, java.lang.String)
	 */
	@Access(AccessType.UNAVAILABLE)
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
}
