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
import com.almende.util.uuid.UUID;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class WakeableAgent.
 */
public class WakeableAgent extends Agent implements Wakeable {
	
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
	public WakeableAgent(ObjectNode config, WakeService ws) {
		String agentId = "";
		if (config.has("id")) {
			agentId = config.get("id").asText();
		} else {
			agentId = new UUID().toString();
			config.put("id", agentId);
		}
		this.rpc = RpcTransformFactory.get(new WakeHandler<Object>(this,
				agentId, ws));
		this.receiver = new WakeHandler<Receiver>(this, agentId, ws);
		setConfig(config, true);
		ws.register(agentId, config, this.getClass().getName());
	}
	
	@Override
	public void wake(String wakeKey, ObjectNode params, boolean onBoot) {
		setConfig(params, onBoot);
	}
	
}
