/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.agent;

import com.almende.eve.capabilities.wake.WakeHandler;
import com.almende.eve.capabilities.wake.WakeService;
import com.almende.eve.capabilities.wake.Wakeable;
import com.almende.eve.transform.rpc.RpcTransformBuilder;
import com.almende.eve.transport.Receiver;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class WakeableAgent.
 */
public class WakeableAgent extends Agent implements Wakeable {
	private WakeService	ws	= null;
	
	/**
	 * Instantiates a new wake agent.
	 */
	public WakeableAgent() {
	}
	
	/**
	 * Instantiates a new wake agent.
	 * 
	 * @param config
	 *            the config
	 * @param ws
	 *            the ws
	 */
	public WakeableAgent(final ObjectNode config, final WakeService ws) {
		this.ws = ws;
		final AgentConfig conf = new AgentConfig(config);
		setRpc(new RpcTransformBuilder().withHandle(
				new WakeHandler<Object>(this, conf.getId(), ws)).build());
		setReceiver(new WakeHandler<Receiver>(this, conf.getId(), ws));
		
		setConfig(conf, true);
		registerAt(ws);
	}
	
	/**
	 * Sets the config.
	 * 
	 * @param config
	 *            the config
	 * @param ws
	 *            the ws
	 */
	public void setConfig(final ObjectNode config, final WakeService ws){
		this.ws = ws;
		final AgentConfig conf = new AgentConfig(config);
		setRpc(new RpcTransformBuilder().withHandle(
				new WakeHandler<Object>(this, conf.getId(), ws)).build());
		setReceiver(new WakeHandler<Receiver>(this, conf.getId(), ws));
		
		setConfig(conf, true);
		registerAt(ws);
	}
	
	/**
	 * Register at.
	 * 
	 * @param ws
	 *            the ws
	 */
	public void registerAt(final WakeService ws) {
		ws.register(getId(), getConfig(), this.getClass().getName());
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.almende.eve.capabilities.wake.Wakeable#wake(java.lang.String,
	 * com.fasterxml.jackson.databind.node.ObjectNode, boolean)
	 */
	@Override
	public void wake(final String wakeKey, final ObjectNode params,
			final boolean onBoot) {
		setRpc(new RpcTransformBuilder().withHandle(
				new WakeHandler<Object>(this, getId(), ws)).build());
		setReceiver(new WakeHandler<Receiver>(this, wakeKey, ws));
		setConfig(params, onBoot);
	}
	
}
