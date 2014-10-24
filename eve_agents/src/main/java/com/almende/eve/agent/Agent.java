/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.agent;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.joda.time.DateTime;

import com.almende.eve.capabilities.handler.Handler;
import com.almende.eve.capabilities.handler.SimpleHandler;
import com.almende.eve.instantiation.HibernationHandler;
import com.almende.eve.instantiation.Initable;
import com.almende.eve.instantiation.InstantiationService;
import com.almende.eve.instantiation.InstantiationServiceBuilder;
import com.almende.eve.scheduling.Scheduler;
import com.almende.eve.scheduling.SchedulerBuilder;
import com.almende.eve.state.State;
import com.almende.eve.state.StateBuilder;
import com.almende.eve.state.StateConfig;
import com.almende.eve.transform.TransformStack;
import com.almende.eve.transform.rpc.RpcTransform;
import com.almende.eve.transform.rpc.RpcTransformBuilder;
import com.almende.eve.transform.rpc.annotation.Access;
import com.almende.eve.transform.rpc.annotation.AccessType;
import com.almende.eve.transform.rpc.annotation.Namespace;
import com.almende.eve.transport.Caller;
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
import com.almende.util.threads.ThreadPool;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class Agent.
 */
@Access(AccessType.UNAVAILABLE)
public class Agent implements Receiver, Initable {
	private static final Logger							LOG				= Logger.getLogger(Agent.class
																				.getName());
	private String										agentId			= null;
	private AgentConfig									config			= null;
	private InstantiationService						is				= null;
	private State										state			= null;
	private Router										transport		= new Router();
	private Scheduler									scheduler		= null;
	private TransformStack								transforms		= new TransformStack();
	private Handler<Receiver>							receiver		= new SimpleHandler<Receiver>(
																				this);
	private Handler<Object>								handler			= new SimpleHandler<Object>(
																				this);
	private final Map<String, List<AgentEventListener>>	eventListeners	= new HashMap<String, List<AgentEventListener>>();

	protected Caller									caller			= new DefaultCaller();
	protected DefaultEventCaller						eventCaller		= new DefaultEventCaller();

	private Handler<Caller>								sender			= new SimpleHandler<Caller>(
																				caller);

	/**
	 * Instantiates a new agent.
	 */
	public Agent() {
		registerDefaultEventListeners();
		eventCaller.on("init");
	}

	/**
	 * Instantiates a new agent.
	 * 
	 * @param config
	 *            the config
	 */
	public Agent(ObjectNode config) {
		if (config == null) {
			config = JOM.createObjectNode();
		}
		AgentConfig conf = new AgentConfig(config);
		conf.setClassName(this.getClass().getName());
		registerDefaultEventListeners();
		setConfig(conf);
		loadConfig();
		eventCaller.on("init");
	}

	/**
	 * Instantiates a new agent.
	 * 
	 * @param agentId
	 *            the agent id
	 * @param config
	 *            the config
	 */
	public Agent(final String agentId, ObjectNode config) {
		if (config == null) {
			config = JOM.createObjectNode();
		}
		AgentConfig conf = new AgentConfig(config);
		conf.setId(agentId);
		conf.setClassName(this.getClass().getName());
		registerDefaultEventListeners();
		setConfig(conf);
		loadConfig();
		eventCaller.on("init");
	}

	/**
	 * On initialisation of the agent (boot, wake, etc.)
	 */
	protected void onInit() {}

	/**
	 * On boot.
	 */
	protected void onBoot() {
		if (transport != null) {
			try {
				transport.connect();
			} catch (IOException e) {
				LOG.log(Level.WARNING, "Couldn't connect transports on boot", e);
			}
		}
	}

	@Override
	public void init(ObjectNode params, boolean onBoot) {
		setConfig(params);
		loadConfig();
		if (onBoot) {
			eventCaller.on("boot");
		}
	};

	/**
	 * Set and loads the config.
	 * 
	 * @param config
	 *            the new config
	 */
	public void loadConfig(final ObjectNode config) {
		init(config, false);
	}

	/**
	 * Destroy the agent
	 */
	@Access(AccessType.UNAVAILABLE)
	protected void destroy() {
		if (transport != null) {
			transport.disconnect();
			transport.delete();
		}
		if (transforms != null) {
			transforms.delete();
		}
		if (scheduler != null) {
			scheduler.delete();
		}
		if (state != null) {
			state.delete();
		}
		if (is != null) {
			is.deregister(agentId);
		}
	}

	class DefaultEventCaller {
		/**
		 * On.
		 *
		 * @param event
		 *            the event
		 */
		public final void on(final String event) {
			List<AgentEventListener> list = eventListeners.get(event);
			if (list != null) {
				Executor pool = ThreadPool.getPool();
				synchronized (list) {
					for (AgentEventListener listener : list) {
						pool.execute(listener);
					}
				}
			}
		}

		/**
		 * Adds the event listener.
		 *
		 * @param event
		 *            the event
		 * @param listener
		 *            the listener
		 */
		public final void addEventListener(final String event,
				final AgentEventListener listener) {
			synchronized (eventListeners) {
				List<AgentEventListener> list = eventListeners.get(event);
				if (list == null) {
					list = new ArrayList<AgentEventListener>();
					eventListeners.put(event, list);
				}
				synchronized (list) {
					list.add(listener);
				}
			}
		}

	}

	private final void registerDefaultEventListeners() {
		eventCaller.addEventListener("boot", new AgentEventListener() {
			@Override
			public void run() {
				onBoot();
			}
		});
		eventCaller.addEventListener("init", new AgentEventListener() {
			@Override
			public void run() {
				onInit();
			}
		});
	}

	/**
	 * Gets the event caller.
	 *
	 * @return the event caller
	 */
	@JsonIgnore
	protected DefaultEventCaller getEventCaller() {
		return this.eventCaller;
	}

	/**
	 * @param receiver
	 *            the receiver to set
	 */
	protected void setReceiver(Handler<Receiver> receiver) {
		this.receiver = receiver;
	}

	/**
	 * @return the receiver
	 */
	@JsonIgnore
	protected Handler<Receiver> getReceiver() {
		return receiver;
	}

	/**
	 * Sets the handler.
	 *
	 * @param handler
	 *            the new handler
	 */
	protected void setHandler(Handler<Object> handler) {
		this.handler = handler;
	}

	/**
	 * Gets the handler.
	 *
	 * @return the receiver
	 */
	@JsonIgnore
	protected Handler<Object> getHandler() {
		return handler;
	}

	/**
	 * Sets the sender.
	 *
	 * @param sender
	 *            the new sender
	 */
	protected void setSender(Handler<Caller> sender) {
		this.sender = sender;
	}

	/**
	 * Gets the sender.
	 *
	 * @return the sender
	 */
	@JsonIgnore
	protected Handler<Caller> getSender() {
		return sender;
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
	protected void setConfig(final ObjectNode config) {
		this.config = new AgentConfig(config);
	}

	/**
	 * Gets the rpc.
	 *
	 * @return the rpc
	 */
	protected RpcTransform getRpc() {
		return (RpcTransform) transforms.getLast();
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
		return ((RpcTransform) transforms.getLast()).getMethods();
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

	/**
	 * Load config.
	 *
	 * @param onBoot
	 *            the on boot
	 */
	protected void loadConfig() {
		agentId = config.getId();
		ObjectNode iscfg = config.getInstantiationService();
		if (iscfg != null) {
			is = new InstantiationServiceBuilder().withConfig(iscfg).build();
			is.register(agentId, config, this.getClass().getName());
		}
		if (is != null && config.isCanHibernate()) {
			setHandler(new HibernationHandler<Object>(this, agentId, is));
			setReceiver(new HibernationHandler<Receiver>(this, agentId, is));
			setSender(new HibernationHandler<Caller>(caller, agentId, is));
		}
		loadScheduler(config.getScheduler());
		loadState(config.getState());
		loadTransports(config.getTransport());
		loadTransforms(config.getTransforms());
	}

	/**
	 * Load transforms.
	 *
	 * @param config
	 *            the config
	 */
	private void loadTransforms(final ArrayNode config) {
		// TODO: load transforms from config

		// each agent has at least a RPC transform
		transforms.add(new RpcTransformBuilder().withHandle(handler).build());
	}

	/**
	 * Sets the scheduler.
	 * 
	 * @param scheduler
	 *            the new scheduler
	 */
	@JsonIgnore
	protected void setScheduler(final Scheduler scheduler) {
		if (this.scheduler != null) {
			this.scheduler.clear();
		}
		this.scheduler = scheduler;
		config.set("scheduler", scheduler.getParams());
	}

	/**
	 * Load scheduler.
	 * 
	 * @param schedulerConfig
	 *            the scheduler config
	 */
	private void loadScheduler(final ObjectNode schedulerConfig) {
		if (schedulerConfig != null) {
			if (agentId != null && schedulerConfig.has("state")) {
				final StateConfig stateConfig = new StateConfig(
						(ObjectNode) schedulerConfig.get("state"));

				if (stateConfig.getId() == null) {
					stateConfig.setId("scheduler_" + agentId);
					schedulerConfig.set("state", stateConfig);
				}
			}
			scheduler = new SchedulerBuilder().withConfig(schedulerConfig)
					.withHandle(receiver).build();

			config.set("scheduler", schedulerConfig);
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
	protected void setState(final State state) {
		this.state = state;
		config.set("state", state.getParams());
	}

	/**
	 * Load state.
	 * 
	 * @param sc
	 *            the sc
	 */
	private void loadState(final ObjectNode sc) {
		if (sc != null) {
			final StateConfig stateConfig = new StateConfig(sc);
			if (agentId != null && stateConfig.getId() == null) {
				stateConfig.setId(agentId);
			}
			state = new StateBuilder().withConfig(stateConfig).build();
			config.set("state", stateConfig);
		}
	}

	/**
	 * Gets the state.
	 * 
	 * @return the state
	 */
	@Access(AccessType.UNAVAILABLE)
	@JsonIgnore
	protected State getState() {
		return state;
	}

	/**
	 * Adds the transport.
	 * 
	 * @param transport
	 *            the transport
	 */
	protected void addTransport(final Transport transport) {
		this.transport.register(transport);

		JsonNode transportConfig = config.get("transport");
		if (transportConfig == null) {
			transportConfig = JOM.createArrayNode();
		}
		if (transportConfig.isArray()) {
			((ArrayNode) transportConfig).add(transport.getParams());
		} else {
			final ArrayNode transports = JOM.createArrayNode();
			transports.add(transportConfig);
			transports.add(transport.getParams());
			transportConfig = transports;
		}
		config.set("transport", transportConfig);
	}

	protected void addTransport(final ObjectNode transconfig) {
		// TODO: Somewhat ugly, not every transport requires an id.
		TransportConfig transconf = new TransportConfig(transconfig);
		if (transconf.get("id") == null) {
			transconf.put("id", agentId);
		}
		final Transport transport = new TransportBuilder()
				.withConfig(transconf).withHandle(receiver).build();

		addTransport(transport);
	}

	/**
	 * Load transport.
	 *
	 * @param transportConfig
	 *            the transport config
	 */
	private void loadTransports(final JsonNode transportConfig) {
		// Cleanout old config
		config.remove("transport");
		if (transportConfig != null) {
			if (transportConfig.isArray()) {
				final Iterator<JsonNode> iter = transportConfig.iterator();
				while (iter.hasNext()) {
					addTransport((ObjectNode) iter.next());
				}
			} else {
				addTransport((ObjectNode) transportConfig);
			}
		}
		// All agents have a local transport
		addTransport(new LocalTransportBuilder()
				.withConfig(new LocalTransportConfig(agentId))
				.withHandle(receiver).build());
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
		return getScheduler().schedule(
				((RpcTransform) transforms.getLast()).buildMsg(method, params),
				due);
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
	protected String schedule(final String method, final ObjectNode params,
			final int delay) {
		return getScheduler().schedule(
				((RpcTransform) transforms.getLast()).buildMsg(method, params),
				delay);
	}

	/**
	 * Cancel.
	 * 
	 * @param taskId
	 *            the task id
	 */
	@Access(AccessType.UNAVAILABLE)
	protected void cancel(final String taskId) {
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
	protected <T> void call(final URI url, final String method,
			final ObjectNode params, final AsyncCallback<T> callback)
			throws IOException {
		caller.call(url, method, params, callback);
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
	protected <T> void call(final URI url, final Method method,
			final Object[] params, final AsyncCallback<T> callback)
			throws IOException {
		caller.call(url, method, params, callback);
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
	protected <T> void call(final URI url, final String method,
			final ObjectNode params) throws IOException {
		caller.call(url, method, params);
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
	protected <T> T callSync(final URI url, final String method,
			final ObjectNode params) throws IOException {
		return caller.callSync(url, method, params);
	}

	/*
	 * (non-Javadoc)
	 * @see com.almende.eve.transport.Receiver#receive(java.lang.Object,
	 * java.net.URI, java.lang.String)
	 */
	@Access(AccessType.UNAVAILABLE)
	@Override
	public void receive(final Object msg, final URI senderUrl, final String tag) {
		final Object response = transforms.outbound(
				transforms.inbound(msg, senderUrl).getResult(), senderUrl).result;
		if (response != null) {
			try {
				transport.send(senderUrl, response, tag);
			} catch (final IOException e) {
				LOG.log(Level.WARNING, "Couldn't send message", e);
			}
		}
	}

	private class DefaultCaller implements Caller {
		public <T> void call(final URI url, final String method,
				final ObjectNode params, final AsyncCallback<T> callback)
				throws IOException {
			final Object message = ((RpcTransform) transforms.getLast())
					.buildMsg(method, params, callback);
			transport.send(url, transforms.outbound(message, url).result, null);
		}

		public <T> void call(final URI url, final Method method,
				final Object[] params, final AsyncCallback<T> callback)
				throws IOException {
			final Object message = ((RpcTransform) transforms.getLast())
					.buildMsg(method, params, callback);
			transport.send(url, transforms.outbound(message, url).result, null);
		}

		public <T> void call(final URI url, final String method,
				final ObjectNode params) throws IOException {
			final Object message = ((RpcTransform) transforms.getLast())
					.buildMsg(method, params, null);
			transport.send(url, transforms.outbound(message, url).result, null);
		}

		public <T> T callSync(final URI url, final String method,
				final ObjectNode params) throws IOException {
			final SyncCallback<T> callback = new SyncCallback<T>() {};
			final Object message = ((RpcTransform) transforms.getLast())
					.buildMsg(method, params, callback);
			transport.send(url, transforms.outbound(message, url).result, null);
			try {
				return callback.get();
			} catch (final Exception e) {
				throw new IOException(e);
			}
		}
	}
}
