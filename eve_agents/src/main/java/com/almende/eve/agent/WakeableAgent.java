/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.agent;

import com.almende.eve.capabilities.wake.WakeHandler;
import com.almende.eve.capabilities.wake.WakeService;
import com.almende.eve.capabilities.wake.Wakeable;
import com.almende.eve.transform.rpc.RpcTransformFactory;
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
		setConfig(config, true);
		registerAt(ws);
	}
	
	/**
	 * Register at.
	 * 
	 * @param ws
	 *            the ws
	 */
	public void registerAt(final WakeService ws) {
		rpc = RpcTransformFactory
				.get(new WakeHandler<Object>(this, getId(), ws));
		receiver = new WakeHandler<Receiver>(this, getId(), ws);
		loadConfig(false);
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
		rpc = RpcTransformFactory
				.get(new WakeHandler<Object>(this, wakeKey, ws));
		receiver = new WakeHandler<Receiver>(this, wakeKey, ws);
		setConfig(params, onBoot);
	}
	
}
