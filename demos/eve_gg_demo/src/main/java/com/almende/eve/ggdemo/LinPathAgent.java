package com.almende.eve.ggdemo;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

import org.apache.http.annotation.ThreadSafe;

import com.almende.eve.protocol.jsonrpc.annotation.Access;
import com.almende.eve.protocol.jsonrpc.annotation.AccessType;
import com.almende.eve.protocol.jsonrpc.annotation.Name;
import com.almende.eve.protocol.jsonrpc.annotation.Sender;
import com.almende.eve.protocol.jsonrpc.formats.JSONRPCException;
import com.almende.util.jackson.JOM;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Access(AccessType.PUBLIC)
@ThreadSafe
public class LinPathAgent extends AbstractLampAgent {

	public void handleGoal(@Name("goal") Goal goal, @Sender String sender)
			throws IOException, JSONRPCException, JsonProcessingException {
		if (neighbours == null) {
			neighbours = getNeighbours();
		}
		
		final List<String> nbs = Arrays.asList(neighbours.toArray(new String[0]));
		Integer pointer = getState().get(goal.getId(), Integer.class);
		if (pointer == null) {
			for (int i = 0; i < nbs.size(); i++) {
				if (nbs.get(i).equals(sender)) {
					pointer = i;
				}
			}
			if (pointer == null) {
				pointer = 0; // should not happen.
			}
		}
		Integer stepSize = getState().get("stepSize", Integer.class);
		if (!getState().containsKey(goal.getId())) {
			// Determine my own influence on the goal
			double noOn = (goal.getPercentage() * goal.getAgentCnt()) / 100;
			goal.setAgentCnt(goal.getAgentCnt() + 1);

			double plus = (((noOn + 1) * 100) / (goal.getAgentCnt()));
			double minus = (((noOn) * 100) / (goal.getAgentCnt()));
			if (plus - goal.getGoalPct() < goal.getGoalPct() - minus) {
				lampOn();
				goal.setPercentage(plus);
			} else {
				lampOff();
				goal.setPercentage(minus);
			}
			goal.setTtl(0);
		} else {
			double noOn = (goal.getPercentage() * goal.getAgentCnt()) / 100;
			double newPerc = (((noOn + (isOn() ? -1 : 1)) * 100) / (goal
					.getAgentCnt()));
			if (Math.abs(goal.getGoalPct() - goal.getPercentage()) > Math
					.abs(goal.getGoalPct() - newPerc)) {
				goal.setPercentage(newPerc);
				if (isOn()) {
					lampOff();
				} else {
					lampOn();
				}
				goal.setTtl(0);
			} else {
				goal.setTtl(goal.getTtl() + 1);
			}
		}

		if (goal.getTtl() > 15) {
			// No changes, drop this goal.
			return;
		}
		// Send goal further to neighbours
		ObjectNode params = JOM.createObjectNode();
		params.set("goal", JOM.getInstance().valueToTree(goal));

		int count = 0;
		int original_pointer = pointer;
		boolean stop = false;
		while (count < stepSize && !stop) {
			String neighbour = nbs.get(pointer);
			pointer++;
			if (pointer >= nbs.size()) {
				pointer = 0;
			}
			getState().put(goal.getId(), pointer);
			if (nbs.size() > 1) {
				if (neighbour.equals(sender)) {
					continue;
				}
				if (pointer == original_pointer) {
					stop = true;
				}
			}
			count++;
			call(URI.create(neighbour), "handleGoal", params, null);
		}
		getState().put("goal", goal);
	}
}
