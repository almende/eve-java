/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.algorithms.test.agents;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

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
		config.put("width", 1000);
		config.put("initialTTL", 10);
		config.put("evictionFactor", 20);

		config.put("intervalFactor", 5);
		config.put("intervalMin", 200);
		config.put("redundancyFactor", 9999);

		daa.configure(config);
		daa.setNewValue(value);

		trickle = new TrickleRPC(config, getScheduler(), new Runnable() {
			@Override
			public void run() {
				daa.getLocalValue().setTTL(
						DateTime.now().plus((long) (trickle.getDelay() * 3.2))
								.getMillis());
			}
		}, new Runnable() {
			@Override
			public void run() {
				final Params params = new Params();
				params.add("value", daa.getCurrentEstimate());
				for (final URI agent : neighbors) {
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
		Double oldval = 0.0;
		if (daa.getCurrentEstimate() != null) {
			oldval = daa.getCurrentEstimate().computeSum();
		}
		daa.receive(value);
		if (trickle != null) {
			if (!oldval.equals(daa.getCurrentEstimate().computeSum())) {
				trickle.reset();
			} else {
				trickle.incr();
			}
		}

	}

	/**
	 * Change value.
	 *
	 * @param value
	 *            the value
	 */
	public void changeValue(@Name("value") Double value) {
		// set new value for next round:
		daa.setNewValue(value);
		if (trickle != null) {
			trickle.reset();
		}
	}

	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	public double getValue() {
		return daa.getCurrentEstimate().computeSum();
	}

	/**
	 * Gets the ttl average.
	 *
	 * @return the value
	 */
	public long getTTLAvg() {
		return daa.getCurrentEstimate().avgTTL();
	}

	public void destroy() {
		super.destroy();
	}

}
