/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.ggdemo;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import org.joda.time.DateTime;

import com.almende.eve.algorithms.DAA;
import com.almende.eve.algorithms.DAAValueBean;
import com.almende.eve.algorithms.TrickleRPC;
import com.almende.eve.protocol.jsonrpc.annotation.Name;
import com.almende.eve.protocol.jsonrpc.annotation.Namespace;
import com.almende.eve.protocol.jsonrpc.annotation.Optional;
import com.almende.eve.protocol.jsonrpc.formats.JSONRPCException;
import com.almende.eve.protocol.jsonrpc.formats.Params;
import com.almende.util.jackson.JOM;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class DAALampAgent.
 */
public class DAALampAgent extends AbstractLampAgent {
	private DAA					lampCnt		= new DAA();
	private DAA					onCnt		= new DAA();

	private TrickleRPC			trickle		= null;
	private double				requiredOn	= 0.0;
	private DateTime			nextCheck	= DateTime.now();

	/*
	 * (non-Javadoc)
	 * @see
	 * com.almende.eve.ggdemo.LampAgent#handleGoal(com.almende.eve.ggdemo.Goal,
	 * java.lang.String)
	 */
	@Override
	public void handleGoal(Goal goal, String sender) throws JSONRPCException,
			JsonProcessingException, IOException {
		if (neighbours == null) {
			neighbours = getNeighbours();
		}
		Goal oldgoal = getState().get("goal", Goal.class);
		if (oldgoal != null && oldgoal.getId().equals(goal.getId())) {
			// EARLY exist, seen this goal before.
			return;
		}
		getState().put("goal", goal);

		requiredOn = Math
				.round((goal.getGoalPct() * lampCnt.computeSum()) / 100);
		
		checkLamps();
		for (String neighbour : neighbours) {
			ObjectNode params = JOM.createObjectNode();
			params.set("goal", JOM.getInstance().valueToTree(goal));
			call(URI.create(neighbour), "handleGoal", params, null);
		}
	}

	private void setOnValue(double value) {
		DAAValueBean old = onCnt.negateValue();
		// send old value to network to evict it.
		for (final String neighbour : getNeighbours()) {
			Params params = new Params();
			params.add("nofOn", old);
			try {
				call(URI.create(neighbour), "daaReceive", params);
			} catch (IOException e) {}
		}
		onCnt.setNewValue(value);
	}

	private synchronized void checkLamps() {
		if (nextCheck.isAfterNow()) return;
		nextCheck = DateTime.now().plus(Math.round(Math.random()*10000));
		System.out.println(getId() + ": Checking lamps:" + lampCnt.computeSum()
				+ "/" + onCnt.computeSum()+ "  req:"+requiredOn);
		if (requiredOn > Math.round(onCnt.computeSum())) {
			if (!super.isOn()) {
				lampOn();
				setOnValue(1.0);
				trickle.reset();
				System.out.print("!");
			}
		} else if (requiredOn < Math.round(onCnt.computeSum())) {
			if (super.isOn()) {
				lampOff();
				setOnValue(0.0);
				trickle.reset();
				System.out.print(".");
			}
		}
	}

	@Override
	public void create(List<String> neighbours, Integer stepSize)
			throws JSONRPCException, IOException {
		super.create(neighbours, stepSize);
		final ObjectNode config = JOM.createObjectNode();
		config.put("width", 1000);
		config.put("initialTTL", 10);
		config.put("evictionFactor", 20);

		config.put("intervalFactor", 1);
		config.put("intervalMin", 500);
		config.put("redundancyFactor", 3);

		lampCnt.configure(config);
		lampCnt.setNewValue(1.0);

		onCnt.configure(config);
		onCnt.setNewValue(super.isOn() ? 1.0 : 0.0);

		final List<String> nbs = neighbours;
		trickle = new TrickleRPC(config, getScheduler(), new Runnable() {
			@Override
			public void run() {
				checkLamps();
				lampCnt.getCurrentEstimate().decreaseTTL();
				onCnt.getCurrentEstimate().decreaseTTL();
			}
		}, new Runnable() {
			@Override
			public void run() {
				for (final String neighbour : nbs) {
					Params params = new Params();
					params.add("nofLamps", lampCnt.getCurrentEstimate());
					params.add("nofOn", onCnt.getCurrentEstimate());
					try {
						call(URI.create(neighbour), "daaReceive", params);
					} catch (IOException e) {}
				}
			}
		});
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
	 * Daa receive.
	 *
	 * @param nofLamps
	 *            the nof lamps
	 * @param nofOn
	 *            the nof on
	 * @throws JsonProcessingException
	 *             the json processing exception
	 */
	public synchronized void daaReceive(
			@Name("nofLamps") @Optional DAAValueBean nofLamps,
			@Name("nofOn") @Optional DAAValueBean nofOn)
			throws JsonProcessingException {
		if (nofLamps == null) {
			nofLamps = lampCnt.getCurrentEstimate();
		}
		if (nofOn == null) {
			nofOn = onCnt.getCurrentEstimate();
		}
		double oldNofLamps = 0.0;
		if (lampCnt.getCurrentEstimate() != null) {
			oldNofLamps = lampCnt.getCurrentEstimate().computeSum();
		}
		double oldNofOn = 0.0;
		if (onCnt.getCurrentEstimate() != null) {
			oldNofOn = onCnt.getCurrentEstimate().computeSum();
		}
		lampCnt.receive(nofLamps);
		onCnt.receive(nofOn);
		if (trickle != null) {
			if (oldNofLamps != lampCnt.getCurrentEstimate().computeSum()
					|| oldNofOn != onCnt.getCurrentEstimate().computeSum()) {
				trickle.reset();
			} else {
				trickle.incr();
			}
		}
	}
}
