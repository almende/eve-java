package com.almende.eve.lightscity;

import com.almende.eve.agent.Agent;
import com.almende.eve.agent.AgentBuilder;
import com.almende.eve.agent.AgentConfig;
import com.almende.eve.protocol.jsonrpc.annotation.Access;
import com.almende.eve.protocol.jsonrpc.annotation.AccessType;

@Access(AccessType.PUBLIC)
public class Steward extends Agent {

	public void onReady() {
		// init all other agents
		for (int i = 0; i < 4; i++) {
			final AgentConfig carconfig = AgentConfig.create();
			carconfig.setId("carServer" + i);
			carconfig.setClassName("com.almende.eve.lightscity.Car");
			carconfig.put("extends", "templates/defaultCar");
			new AgentBuilder().withConfig(carconfig).build();
		}
		for (int i = 0; i < 200; i++) {
			final AgentConfig poleconfig = AgentConfig.create();
			poleconfig.setId("pole" + i);
			poleconfig.setClassName("com.almende.eve.lightscity.Pole");
			poleconfig.put("extends", "templates/defaultPole");
			new AgentBuilder().withConfig(poleconfig).build();
		}
	}

}
