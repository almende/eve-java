/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.ggdemo;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import com.almende.eve.algorithms.DAA;
import com.almende.eve.algorithms.DAAValueBean;
import com.almende.eve.algorithms.TrickleRPC;
import com.almende.eve.protocol.jsonrpc.annotation.Name;
import com.almende.eve.protocol.jsonrpc.annotation.Namespace;
import com.almende.eve.protocol.jsonrpc.formats.JSONRPCException;
import com.almende.eve.protocol.jsonrpc.formats.Params;
import com.almende.util.jackson.JOM;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class DAALampAgent.
 */
public class DAALampAgent extends AbstractLampAgent {
	private DAA			lampCnt		= new DAA();
	private DAA			onCnt		= new DAA();

	private TrickleRPC	trickle		= null;
	private double		requiredOn	= 0.0;

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
		for (String neighbour : neighbours) {
			ObjectNode params = JOM.createObjectNode();
			params.set("goal", JOM.getInstance().valueToTree(goal));
			call(URI.create(neighbour), "handleGoal", params, null);
		}
		requiredOn = Math.round((goal.getGoalPct() * lampCnt.computeSum()) / 100);
		checkLamps();
	}

	private void checkLamps() {
		System.out.println(getId()+": Checking lamps!"+lampCnt.computeSum()+"/"+onCnt.computeSum());
		if (requiredOn > Math.round(onCnt.computeSum())) {
			if (!super.isOn()) {
				System.out.print(".");
				super.lampOn();
				onCnt.setNewValue(1.0);
				trickle.reset();
			}
		} else if (requiredOn < Math.round(onCnt.computeSum())){
			if (super.isOn()) {
				System.out.print("!");
				super.lampOff();
				onCnt.setNewValue(0.0);
				trickle.reset();
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
		config.put("evictionFactor", 3);

		lampCnt.configure(config);
		lampCnt.setNewValue(1.0);

		onCnt.configure(config);
		if (super.isOn()){
			System.out.print("_");
		} else {
			System.out.print("=");
		}
		onCnt.setNewValue(super.isOn() ? 1.0 : 0.0);

		final List<String> nbs = neighbours;
		trickle = new TrickleRPC(config, getScheduler(), new Runnable() {
			@Override
			public void run() {
				checkLamps();
//				lampCnt.getCurrentEstimate().decreaseTTL();
//				onCnt.getCurrentEstimate().decreaseTTL();
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
			final @Name("nofLamps") DAAValueBean nofLamps,
			final @Name("nofOn") DAAValueBean nofOn)
			throws JsonProcessingException {
		if (lampCnt.getCurrentEstimate() == null
				|| !lampCnt.getCurrentEstimate().computeSum()
						.equals(nofLamps.computeSum())
				|| onCnt.getCurrentEstimate() == null
				|| !onCnt.getCurrentEstimate().computeSum()
						.equals(nofOn.computeSum())) {
			lampCnt.receive(nofLamps);
			onCnt.receive(nofOn);
			trickle.reset();
		} else {
			lampCnt.receive(nofLamps);
			onCnt.receive(nofOn);
			trickle.incr();
		}
	}
}
