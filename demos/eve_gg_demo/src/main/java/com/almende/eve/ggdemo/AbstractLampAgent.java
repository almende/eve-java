/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.ggdemo;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.almende.eve.agent.Agent;
import com.almende.eve.protocol.jsonrpc.annotation.Access;
import com.almende.eve.protocol.jsonrpc.annotation.AccessType;
import com.almende.eve.protocol.jsonrpc.formats.JSONRPCException;
import com.almende.util.TypeUtil;

/**
 * The Class AbstractLampAgent.
 */
@Access(AccessType.PUBLIC)
public abstract class AbstractLampAgent extends Agent implements LampAgent {
	protected Set<String>	neighbours	= null;

	/*
	 * (non-Javadoc)
	 * @see com.almende.eve.ggdemo.LampAgent#create(java.util.ArrayList,
	 * java.lang.Integer)
	 */
	@Override
	public void create(List<String> neighbours, Integer stepSize)
			throws JSONRPCException, IOException {
		final Set<String> nbs = new LinkedHashSet<String>(neighbours);
		getState().put("neighbours", nbs);
		if (stepSize > neighbours.size()) {
			stepSize = neighbours.size();
		}
		getState().put("stepSize", stepSize);
	}

	/**
	 * Lamp on.
	 */
	public void lampOn() {
		getState().put("lamp", true);
	}

	/**
	 * Lamp off.
	 */
	public void lampOff() {
		getState().put("lamp", false);
	}

	/*
	 * (non-Javadoc)
	 * @see com.almende.eve.ggdemo.LampAgent#isOn()
	 */
	public boolean isOn() {
		Boolean isOn = getState().get("lamp", Boolean.class);
		if (isOn == null)
			isOn = false;
		return isOn;
	}

	/*
	 * (non-Javadoc)
	 * @see com.almende.eve.ggdemo.LampAgent#isOnBlock()
	 */
	public boolean isOnBlock() throws InterruptedException {
		Boolean isOn = getState().get("lamp", Boolean.class);
		while (isOn == null) {
			Thread.sleep(1000);
			isOn = getState().get("lamp", Boolean.class);
		}
		return isOn;
	}

	/*
	 * (non-Javadoc)
	 * @see com.almende.eve.ggdemo.LampAgent#getNeighbours()
	 */
	@Override
	public Set<String> getNeighbours() {
		Set<String> result = getState().get("neighbours",
				new TypeUtil<LinkedHashSet<String>>() {});
		if (result == null) {
			result = new LinkedHashSet<String>(0);
		}
		return result;
	}
	
	/**
	 * Gets the goal.
	 *
	 * @return the goal
	 */
	public Goal getGoal() {
		return getState().get("goal", Goal.class);
	}
}
