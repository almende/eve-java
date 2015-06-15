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
import com.almende.eve.instantiation.InstantiationServiceConfig;
import com.almende.eve.protocol.Meta;
import com.almende.eve.protocol.Protocol;
import com.almende.eve.protocol.ProtocolBuilder;
import com.almende.eve.protocol.ProtocolConfig;
import com.almende.eve.protocol.ProtocolStack;
import com.almende.eve.protocol.jsonrpc.JSONRpcProtocol;
import com.almende.eve.protocol.jsonrpc.JSONRpcProtocolBuilder;
import com.almende.eve.protocol.jsonrpc.JSONRpcProtocolConfig;
import com.almende.eve.protocol.jsonrpc.RpcBasedProtocol;
import com.almende.eve.protocol.jsonrpc.annotation.Access;
import com.almende.eve.protocol.jsonrpc.annotation.AccessType;
import com.almende.eve.protocol.jsonrpc.formats.Caller;
import com.almende.eve.protocol.jsonrpc.formats.JSONMessage;
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
 * The Class AgentCore, contains the basic parts of an Eve agent: Single State,
 * Single Scheduler, Transports, ProtocolStack and Configuration management.
 */
@Access(AccessType.UNAVAILABLE)
public class AgentCore implements Receiver, Configurable {
	private static final Logger		LOG				= Logger.getLogger(AgentCore.class
															.getName());
	private String					agentId			= null;
	private ObjectNode				literalConfig	= null;
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
	public AgentCore() {
		config = new AgentConfig();
	}

	/**
	 * Instantiates a new agent.
	 * 
	 * @param config
	 *            the config
	 */
	public AgentCore(ObjectNode config) {
		if (config == null) {
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
	public AgentCore(final String agentId, ObjectNode config) {
		if (config == null) {
			config = JOM.createObjectNode();
		}
		config.put("id", agentId);
		config.put("class", this.getClass().getName());
		setConfig(config);
	}

	/**
	 * On ready, is being run after the configuration has been loaded.
	 */
	protected void onReady() {
		// Running old methods for backwards compatibility.
		onInit();
		onBoot();
	}

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
	 * Gets the id.
	 * 
	 * @return the id
	 */
	@Access(AccessType.PUBLIC)
	public String getId() {
		return agentId;
	}

	/**
	 * Sets the config.
	 * 
	 * @param config
	 *            the new config
	 */
	public void setConfig(final ObjectNode config) {
		this.literalConfig = config;
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
	 * Gets the runtime configuration of this agent
	 *
	 * @return the config
	 */
	@Access(AccessType.PUBLIC)
	public ObjectNode getConfig() {
		return config;
	}

	/**
	 * Gets the literal, unextended, unexpanded config, as given during
	 * initiation.
	 *
	 * @return the literal config
	 */
	@JsonIgnore
	@Access(AccessType.PUBLIC)
	public ObjectNode getLiteralConfig() {
		return literalConfig;
	}

	private void loadConfig() {
		agentId = config.getId();
		loadInstantiationService(config.getInstantiationService());
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
	 * Override default configuration and set the provided inbound message
	 * receiver.
	 * <b>This is meant to extend the agent with new low level behavior, handle
	 * with care</b>
	 *
	 * @param receiver
	 *            the receiver to set
	 */
	protected void setReceiver(Handler<Receiver> receiver) {
		this.receiver = receiver;
	}

	/**
	 * Get the current message receiver.
	 *
	 * @return the receiver
	 */
	@JsonIgnore
	protected Handler<Receiver> getReceiver() {
		return receiver;
	}

	/**
	 * Override configuration and set the provided inbound RPC target handler
	 * <b>This is meant to extend the agent with new low level behavior, handle
	 * with care</b>
	 * 
	 * @param handler
	 *            the new RPC target handler
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
	 * Disconnect all transports.
	 */
	@Access(AccessType.UNAVAILABLE)
	public void disconnect() {
		transport.disconnect();
	}

	/**
	 * Gets the caller.
	 *
	 * @return the caller
	 */
	@JsonIgnore
	protected Caller getCaller() {
		return caller;
	}

	/**
	 * Override default configuration and set the send handler.
	 * <b>This is meant to extend the agent with new low level behavior, handle
	 * with care</b>
	 *
	 * @param sender
	 *            the new sender
	 */
	protected void setSender(Handler<Caller> sender) {
		this.sender = sender;
	}

	/**
	 * Gets the sender handler.
	 *
	 * @return the sender
	 */
	@JsonIgnore
	protected Handler<Caller> getSender() {
		return sender;
	}

	@Access(AccessType.UNAVAILABLE)
	@Override
	public void receive(final Object msg, final URI senderUrl, final String tag) {
		if (protocolStack == null) {
			// E.g. during destroy()!
			return;
		}
		protocolStack.inbound(msg, senderUrl, tag);
	}

	/**
	 * Gets the instantiationService.
	 *
	 * @return the checks if is
	 */
	@JsonIgnore
	protected InstantiationService getInstantiationService() {
		return is;
	}

	private void loadInstantiationService(final ObjectNode config) {
		if (config != null) {
			InstantiationServiceConfig iscfg = InstantiationServiceConfig
					.decorate(config);
			final StateConfig stateConfig = StateConfig.decorate(iscfg
					.getState());
			if (agentId != null && stateConfig.getId() == null) {
				stateConfig.setId(agentId);
				iscfg.setState(stateConfig);
			}
			is = new InstantiationServiceBuilder().withConfig(iscfg).build();
			is.register(agentId, literalConfig, this.getClass().getName());
		}
	}

	/**
	 * Gets the protocol stack.
	 *
	 * @return the protocol stack
	 */
	@JsonIgnore
	protected ProtocolStack getProtocolStack() {
		return protocolStack;
	}

	private void loadProtocols(final ArrayNode config) {
		boolean found = false;
		if (config != null) {
			for (JsonNode item : config) {
				ProtocolConfig conf = ProtocolConfig
						.decorate((ObjectNode) item);
				if (agentId != null && conf.getId() == null) {
					conf.setId(agentId);
				}

				final Protocol protocol = new ProtocolBuilder()
						.withConfig((ObjectNode) conf).withHandle(handler)
						.build();
				if (JSONRpcProtocolBuilder.class.getName().equals(
						conf.getClassName())) {
					found = true;
				}
				if (protocol instanceof RpcBasedProtocol) {
					final RpcBasedProtocol prot = (RpcBasedProtocol) protocol;
					prot.setCaller(sender);
				}
				protocolStack.add(protocol);
			}
		}
		if (config == null || !found) {
			// each agent has at least a JSONRPC protocol handler
			final JSONRpcProtocolConfig conf = new JSONRpcProtocolConfig();
			if (agentId != null && conf.getId() == null) {
				conf.setId(agentId);
			}
			final JSONRpcProtocol protocol = new JSONRpcProtocolBuilder()
					.withConfig(conf).withHandle(handler).build();
			protocol.setCaller(sender);
			protocolStack.add(protocol);
		}
	}

	/**
	 * Schedule an RPC call at a specified due time.
	 *
	 * @param request
	 *            the request
	 * @param due
	 *            the due time
	 * @return the task id of this scheduled task, for cancellation.
	 */
	@Access(AccessType.UNAVAILABLE)
	protected String schedule(final JSONRequest request, final DateTime due) {
		final Scheduler scheduler = this.scheduler;
		if (scheduler == null || request == null) {
			return "";
		}
		final JsonNode id = request.getId();
		return scheduler.schedule(id != null ? id.toString() : null, request,
				due);
	}

	/**
	 * Cancel the given scheduled task.
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
	 * Sets the scheduler, updates the agent's configuration in the process.
	 * <b>Note: this does not update the literal config</b>
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
	 * Gets the scheduler.
	 * 
	 * @return the scheduler
	 */
	@JsonIgnore
	protected Scheduler getScheduler() {
		return scheduler;
	}

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
	 * Sets the state, updates the agent's runtime configuration in the process.
	 * <b>Note: this does not update the literal config</b>
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
	 * Gets the state.
	 * 
	 * @return the state
	 */
	@Access(AccessType.UNAVAILABLE)
	@JsonIgnore
	protected State getState() {
		return state;
	}

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
	 * Override configuration and set the transport router
	 * <b>Caution: this will replace the router, which will drop all earlier
	 * configured outbound transports! This should be called before any
	 * transports are set and all transports should be disconnected.</b>
	 * 
	 * @param transport
	 *            the new transport router
	 */
	protected void setTransport(Router transport) {
		this.transport = transport;
	}

	/**
	 * Adds a new transport to this router/array, updates the agent's
	 * configuration in the process.
	 * <b>Note: this does not update the literal config</b>
	 *
	 * @param transport
	 *            the transport router
	 */
	protected void addTransport(final Transport transport) {
		this.transport.register(transport);
		this.config.addTransport(transport.getParams());
	}

	/**
	 * Gets the transport router.
	 * 
	 * @return the transport
	 */
	@JsonIgnore
	protected Router getTransport() {
		return transport;
	}

	private void addTransport(final ObjectNode transconfig) {
		// TODO: Somewhat ugly, not every transport requires an id.
		TransportConfig transconf = TransportConfig.decorate(transconfig);
		if (transconf.get("id") == null) {
			transconf.put("id", agentId);
		}
		final Transport transport = new TransportBuilder()
				.withConfig(transconf).withHandle(receiver).build();

		this.transport.register(transport);
	}

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

	private class DefaultCaller implements Caller {

		@Override
		public <T> void call(final URI url, final JSONMessage message,
				final String tag) throws IOException {

			final Meta wrapper = protocolStack.outbound(message, url, tag);
			if (wrapper != null) {
				transport.send(wrapper.getPeer(), wrapper.getMsg(),
						wrapper.getTag());
			}
		}

		@Override
		public void call(final URI url, final JSONMessage message)
				throws IOException {
			call(url, message, null);
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
			final Meta wrapper = protocolStack.outbound(message, url, null);
			if (wrapper != null) {
				transport.send(wrapper.getPeer(), wrapper.getMsg(),
						wrapper.getTag());
			}
		}

		@Override
		public void call(final URI url, final String method,
				final ObjectNode params) throws IOException {
			call(url, method, params, null);
		}

		@Override
		public void call(final URI url, final Method method,
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

			final Meta wrapper = protocolStack.outbound(message, url, null);
			if (wrapper != null) {
				transport.send(wrapper.getPeer(), wrapper.getMsg(),
						wrapper.getTag());
			}
			try {
				return callback.get();
			} catch (final Exception e) {
				throw new IOException(e);
			}
		}

		@Override
		public List<URI> getSenderUrls() {
			return transport.getAddresses();
		}

		@Override
		public URI getSenderUrlByScheme(final String scheme) {
			return transport.getAddressByScheme(scheme);
		}
	}
}
