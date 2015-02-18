/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.algorithms.test.agents;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import com.almende.eve.agent.Agent;
import com.almende.eve.algorithms.DAA;
import com.almende.eve.algorithms.DAAValueBean;
import com.almende.eve.algorithms.TrickleRPC;
import com.almende.eve.protocol.jsonrpc.annotation.Access;
import com.almende.eve.protocol.jsonrpc.annotation.AccessType;
import com.almende.eve.protocol.jsonrpc.annotation.Name;
import com.almende.eve.protocol.jsonrpc.annotation.Namespace;
import com.almende.eve.protocol.jsonrpc.formats.Params;
import com.almende.util.jackson.JOM;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class DAAAgent.
 */
@Access(AccessType.PUBLIC)
public class DAAAgent extends Agent {
	private DAA			daa			= new DAA();
	private TrickleRPC	trickle		= null;
	private List<URI>	neighbors	= new ArrayList<URI>();

	/**
	 * Instantiates a new DAA agent.
	 *
	 * @param string
	 *            the string
	 */
	public DAAAgent(String string) {
		super(string, null);
	}

	/**
	 * Gets the trickle.
	 *
	 * @return the trickle
	 */
	@Namespace("*")
	public TrickleRPC getTrickle() {
		return trickle;
	}

	/**
	 * Adds the neighbor.
	 *
	 * @param address
	 *            the address
	 */
	public void addNeighbor(final URI address) {
		neighbors.add(address);
	}

	/**
	 * Sets the initial DAA value.
	 *
	 * @param value
	 *            the new initial value
	 */
	public void start(final double value) {
		final ObjectNode config = JOM.createObjectNode();
		config.put("width", 10000);
		config.put("initialTTL", 10);
		config.put("evictionFactor", 3);

		daa.configure(config);
		daa.setNewValue(value);

		trickle = new TrickleRPC(config, getScheduler(), new Runnable() {
			@Override
			public void run() {
				daa.getCurrentEstimate().decreaseTTL();
			}
		}, new Runnable() {
			@Override
			public void run() {
				for (final URI agent : neighbors) {
					Params params = new Params();
					params.add("value", daa.getCurrentEstimate());
					try {
						call(agent, "daaReceive", params);
					} catch (IOException e) {}
				}
			}
		});
	}

	/**
	 * Daa receive.
	 *
	 * @param value
	 *            the value
	 * @throws JsonProcessingException
	 *             the json processing exception
	 */
	public synchronized void daaReceive(final @Name("value") DAAValueBean value)
			throws JsonProcessingException {
		if (daa.getCurrentEstimate() == null
				|| !daa.getCurrentEstimate().computeSum()
						.equals(value.computeSum())) {
			daa.receive(value);
			trickle.reset();
		} else {
			trickle.incr();
			daa.receive(value);
		}

	}

	/**
	 * Change value.
	 *
	 * @param value
	 *            the value
	 */
	public void changeValue(@Name("value") Double value) {
		DAAValueBean old = daa.negateValue();
		// send old value to network to evict it.
		for (final URI agent : neighbors) {
			Params params = new Params();
			params.add("value", old);
			try {
				call(agent, "daaReceive", params);
			} catch (IOException e) {}
		}

		// set new value for next round:
		daa.setNewValue(value);
		trickle.reset();
	}

	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	public double getValue() {
		return daa.getCurrentEstimate().computeSum();
	}

	public void destroy() {
		super.destroy();
	}

}
