/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.agent;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.joda.time.DateTime;

import com.almende.eve.capabilities.handler.Handler;
import com.almende.eve.capabilities.handler.SimpleHandler;
import com.almende.eve.instantiation.Configurable;
import com.almende.eve.instantiation.HibernationHandler;
import com.almende.eve.instantiation.InstantiationService;
import com.almende.eve.instantiation.InstantiationServiceBuilder;
import com.almende.eve.protocol.ProtocolBuilder;
import com.almende.eve.protocol.ProtocolConfig;
import com.almende.eve.protocol.ProtocolStack;
import com.almende.eve.protocol.jsonrpc.JSONRpcProtocol;
import com.almende.eve.protocol.jsonrpc.JSONRpcProtocolBuilder;
import com.almende.eve.protocol.jsonrpc.JSONRpcProtocolConfig;
import com.almende.eve.protocol.jsonrpc.annotation.Access;
import com.almende.eve.protocol.jsonrpc.annotation.AccessType;
import com.almende.eve.protocol.jsonrpc.formats.Caller;
import com.almende.eve.protocol.jsonrpc.formats.JSONRequest;
import com.almende.eve.scheduling.Scheduler;
import com.almende.eve.scheduling.SchedulerBuilder;
import com.almende.eve.scheduling.SimpleSchedulerConfig;
import com.almende.eve.state.State;
import com.almende.eve.state.StateBuilder;
import com.almende.eve.state.StateConfig;
import com.almende.eve.transport.LocalTransportBuilder;
import com.almende.eve.transport.LocalTransportConfig;
import com.almende.eve.transport.Receiver;
import com.almende.eve.transport.Router;
import com.almende.eve.transport.Transport;
import com.almende.eve.transport.TransportBuilder;
import com.almende.eve.transport.TransportConfig;
import com.almende.util.TypeUtil;
import com.almende.util.callback.AsyncCallback;
import com.almende.util.callback.SyncCallback;
import com.almende.util.jackson.JOM;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class Agent.
 */
@Access(AccessType.UNAVAILABLE)
public class Agent implements Receiver, Configurable, AgentInterface {
	private static final Logger		LOG				= Logger.getLogger(Agent.class
															.getName());
	private String					agentId			= null;
	private AgentConfig				config			= null;
	private InstantiationService	is				= null;
	private State					state			= null;
	private Router					transport		= new Router();
	private Scheduler				scheduler		= null;
	private ProtocolStack			protocolStack	= new ProtocolStack();
	private Handler<Receiver>		receiver		= new SimpleHandler<Receiver>(
															this);
	private Handler<Configurable>	handler			= new SimpleHandler<Configurable>(
															this);

	protected Caller				caller			= new DefaultCaller();

	private Handler<Caller>			sender			= new SimpleHandler<Caller>(
															caller);

	/**
	 * Instantiates a new agent.
	 */
	public Agent() {}

	/**
	 * Instantiates a new agent.
	 * 
	 * @param config
	 *            the config
	 */
	public Agent(ObjectNode config) {
		if (config == null){
			config = JOM.createObjectNode();
		}
		config.put("class", this.getClass().getName());
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
	public Agent(final String agentId, ObjectNode config) {
		if (config == null){
			config = JOM.createObjectNode();
		}
		config.put("id", agentId);
		config.put("class", this.getClass().getName());
		setConfig(config);
	}

	/**
	 * On ready, is being run after the configuration has been loaded.
	 */
	protected void onReady() {}

	/**
	 * On destroy, is being run before the destroy() method is started.
	 */
	protected void onDestroy() {}

	/**
	 * On init.
	 * 
	 * @deprecated This old event handler is no longer in use, use onReady
	 *             instead.
	 */
	@Deprecated
	protected void onInit() {}

	/**
	 * On boot.
	 * 
	 * @deprecated This old event handler is no longer in use, use onReady
	 *             instead.
	 */
	@Deprecated
	protected void onBoot() {}

	/**
	 * Destroy the agent.
	 */
	@Access(AccessType.UNAVAILABLE)
	protected void destroy() {
		onDestroy();
		if (scheduler != null) {
			scheduler.delete();
			scheduler = null;
		}
		if (transport != null) {
			transport.disconnect();
			transport.delete();
			transport = null;
		}
		if (protocolStack != null) {
			protocolStack.delete();
			protocolStack = null;
		}
		if (state != null) {
			state.delete();
			state = null;
		}
		if (is != null) {
			is.deregister(agentId);
			is = null;
		}
	}

	/**
	 * Sets the receiver.
	 *
	 * @param receiver
	 *            the receiver to set
	 */
	protected void setReceiver(Handler<Receiver> receiver) {
		this.receiver = receiver;
	}

	/**
	 * Gets the receiver.
	 *
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
	protected void setHandler(Handler<Configurable> handler) {
		this.handler = handler;
	}

	/**
	 * Gets the handler.
	 *
	 * @return the receiver
	 */
	@JsonIgnore
	public Handler<Configurable> getHandler() {
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
	public void setConfig(final ObjectNode config) {
		this.config = AgentConfig.decorate(config);
		loadConfig();
		onReady();
		try {
			// Asynchronous connect of agent's transports.
			connect();
		} catch (IOException e) {
			LOG.log(Level.WARNING, "Couldn't connect transports", e);
		}
	}

	/**
	 * Gets the id.
	 * 
	 * @return the id
	 */
	@Access(AccessType.PUBLIC)
	@Override
	public String getId() {
		return agentId;
	}

	/**
	 * Gets the type.
	 * 
	 * @return the type
	 */
	@Access(AccessType.PUBLIC)
	@Override
	public String getType() {
		return this.getClass().getName();
	}

	/**
	 * Gets the urls.
	 * 
	 * @return the urls
	 */
	@Access(AccessType.PUBLIC)
	@Override
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
	@Override
	@JsonIgnore
	public List<Object> getMethods() {
		return ((JSONRpcProtocol) protocolStack.getLast()).getMethods();
	}

	@Access(AccessType.PUBLIC)
	@Override
	public AgentConfig getConfig() {
		return config;
	}

	/**
	 * Load config.
	 */
	private void loadConfig() {
		agentId = config.getId();
		ObjectNode iscfg = config.getInstantiationService();
		if (iscfg != null) {
			is = new InstantiationServiceBuilder().withConfig(iscfg).build();
			is.register(agentId, config, this.getClass().getName());
		}
		if (is != null && config.isCanHibernate()) {
			setHandler(new HibernationHandler<Configurable>(this, agentId, is));
			setReceiver(new HibernationHandler<Receiver>(this, agentId, is));
			setSender(new HibernationHandler<Caller>(caller, agentId, is));
		}
		loadState(config.getState());
		loadProtocols(config.getProtocols());
		loadTransports(config.getTransports());
		loadScheduler(config.getScheduler());
	}

	/**
	 * Load protocol stack.
	 *
	 * @param config
	 *            the config
	 */
	private void loadProtocols(final ArrayNode config) {
		boolean found = false;
		if (config != null) {
			for (JsonNode item : config) {
				ProtocolConfig conf = ProtocolConfig
						.decorate((ObjectNode) item);
				if (agentId != null && conf.getId() == null) {
					conf.setId(agentId);
				}
				if (JSONRpcProtocolBuilder.class.getName().equals(
						conf.getClassName())) {
					found = true;
				}
				protocolStack.add(new ProtocolBuilder()
						.withConfig((ObjectNode) conf).withHandle(handler)
						.build());
			}
		}
		if (config == null || !found) {
			// each agent has at least a JSONRPC protocol handler
			final JSONRpcProtocolConfig conf = new JSONRpcProtocolConfig();
			if (agentId != null && conf.getId() == null) {
				conf.setId(agentId);
			}
			protocolStack.add(new JSONRpcProtocolBuilder().withConfig(conf)
					.withHandle(handler).build());
		}
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
	private void loadScheduler(final ObjectNode params) {
		final SimpleSchedulerConfig schedulerConfig = SimpleSchedulerConfig
				.decorate(params);
		if (schedulerConfig != null) {
			if (agentId != null && schedulerConfig.has("state")) {
				final StateConfig stateConfig = StateConfig
						.decorate((ObjectNode) schedulerConfig.get("state"));

				if (stateConfig.getId() == null) {
					stateConfig.setId("scheduler_" + agentId);
					schedulerConfig.set("state", stateConfig);
				}
			}
			if (agentId != null && schedulerConfig.getId() == null) {
				schedulerConfig.setId(agentId);
			}
			scheduler = new SchedulerBuilder().withConfig(schedulerConfig)
					.withHandle(receiver).build();
		}
	}

	/**
	 * Gets the scheduler.
	 * 
	 * @return the scheduler
	 */
	@JsonIgnore
	protected Scheduler getScheduler() {
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
			final StateConfig stateConfig = StateConfig.decorate(sc);
			if (agentId != null && stateConfig.getId() == null) {
				stateConfig.setId(agentId);
			}
			state = new StateBuilder().withConfig(stateConfig).build();
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
	public void addTransport(final Transport transport) {
		this.transport.register(transport);
		this.config.addTransport(transport.getParams());
	}

	/**
	 * Adds the transport.
	 *
	 * @param transconfig
	 *            the transconfig
	 */
	protected void addTransport(final ObjectNode transconfig) {
		// TODO: Somewhat ugly, not every transport requires an id.
		TransportConfig transconf = TransportConfig.decorate(transconfig);
		if (transconf.get("id") == null) {
			transconf.put("id", agentId);
		}
		final Transport transport = new TransportBuilder()
				.withConfig(transconf).withHandle(receiver).build();

		this.transport.register(transport);
	}

	/**
	 * Load transport.
	 *
	 * @param transportConfig
	 *            the transport config
	 */
	private void loadTransports(final ArrayNode transportConfig) {
		if (transportConfig != null) {
			final Iterator<JsonNode> iter = transportConfig.iterator();
			while (iter.hasNext()) {
				addTransport((ObjectNode) iter.next());
			}
		}
		// All agents have a local transport
		this.transport.register(new LocalTransportBuilder()
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
		final Scheduler scheduler = this.scheduler;
		if (scheduler == null)
			return "";
		return scheduler.schedule(new JSONRequest(method, params), due);
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
		final Scheduler scheduler = this.scheduler;
		if (scheduler == null)
			return "";
		return scheduler.schedule(new JSONRequest(method, params), delay);
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
			final long delay) {
		final Scheduler scheduler = this.scheduler;
		if (scheduler == null)
			return "";
		return scheduler.schedule(new JSONRequest(method, params), delay);
	}

	/**
	 * Cancel.
	 * 
	 * @param taskId
	 *            the task id
	 */
	@Access(AccessType.UNAVAILABLE)
	protected void cancel(final String taskId) {
		final Scheduler scheduler = this.scheduler;
		if (scheduler == null)
			return;
		scheduler.cancel(taskId);
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
	 * Call sync.
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
	protected <T> T callSync(final URI url, final String method,
			final ObjectNode params, final Type type) throws IOException {
		return caller.callSync(url, method, params, type);
	}

	/**
	 * Call sync.
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
	protected <T> T callSync(final URI url, final String method,
			final ObjectNode params, final JavaType type) throws IOException {
		return caller.callSync(url, method, params, type);
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
	 * @param clazz
	 *            the clazz
	 * @return response
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	protected <T> T callSync(final URI url, final String method,
			final ObjectNode params, final Class<T> clazz) throws IOException {
		return caller.callSync(url, method, params, clazz);
	}

	/**
	 * Call sync.
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
	protected <T> T callSync(final URI url, final String method,
			final ObjectNode params, final TypeUtil<T> type) throws IOException {
		return caller.callSync(url, method, params, type);
	}

	/*
	 * (non-Javadoc)
	 * @see com.almende.eve.transport.Receiver#receive(java.lang.Object,
	 * java.net.URI, java.lang.String)
	 */
	@Access(AccessType.UNAVAILABLE)
	@Override
	public void receive(final Object msg, final URI senderUrl, final String tag) {
		if (protocolStack == null) {
			// E.g. during destroy()!
			return;
		}
		final Object response = protocolStack.outbound(
				protocolStack.inbound(msg, senderUrl).getResult(), senderUrl).result;
		if ((tag != null || response != null) && transport != null) {
			try {
				transport.send(senderUrl, response, tag);
			} catch (final Exception e) {
				LOG.log(Level.WARNING, "Couldn't send message", e);
			}
		}
	}

	private class DefaultCaller implements Caller {
		@Override
		public <T> void call(final URI url, final JSONRequest message)
				throws IOException {
			transport.send(url, protocolStack.outbound(message, url).result,
					null);
		}

		@Override
		public <T> void call(final URI url, final String method,
				final ObjectNode params, final AsyncCallback<T> callback)
				throws IOException {
			final JSONRequest message = new JSONRequest(method, params,
					callback);
			call(url, message);
		}

		@Override
		public <T> void call(final URI url, final Method method,
				final Object[] params, final AsyncCallback<T> callback)
				throws IOException {
			final JSONRequest message = new JSONRequest(method, params,
					callback);
			transport.send(url, protocolStack.outbound(message, url).result,
					null);
		}

		@Override
		public <T> void call(final URI url, final String method,
				final ObjectNode params) throws IOException {
			call(url, method, params, null);
		}

		@Override
		public <T> void call(final URI url, final Method method,
				final Object[] params) throws IOException {
			call(url, method, params, null);
		}

		@Override
		public <T> T callSync(final URI url, final String method,
				final ObjectNode params, final Class<T> clazz)
				throws IOException {
			return (T) callSync(url, method, params, TypeUtil.get(clazz));
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T callSync(final URI url, final String method,
				final ObjectNode params, final JavaType type)
				throws IOException {
			return (T) callSync(url, method, params, TypeUtil.get(type));
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T callSync(final URI url, final String method,
				final ObjectNode params, final Type type) throws IOException {
			return (T) callSync(url, method, params, TypeUtil.get(type));
		}

		@Override
		public <T> T callSync(final URI url, final String method,
				final ObjectNode params, final TypeUtil<T> type)
				throws IOException {

			final SyncCallback<T> callback = new SyncCallback<T>(type) {};
			final JSONRequest message = new JSONRequest(method, params,
					callback);
			transport.send(url, protocolStack.outbound(message, url).result,
					null);
			try {
				return callback.get();
			} catch (final Exception e) {
				throw new IOException(e);
			}
		}

		@Override
		public List<URI> getSenderUrls() {
			return getUrls();
		}
	}
}
