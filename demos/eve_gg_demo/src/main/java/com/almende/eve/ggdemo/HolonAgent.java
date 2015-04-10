package com.almende.eve.ggdemo;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.http.annotation.ThreadSafe;

import com.almende.eve.protocol.jsonrpc.annotation.Access;
import com.almende.eve.protocol.jsonrpc.annotation.AccessType;
import com.almende.eve.protocol.jsonrpc.annotation.Name;
import com.almende.eve.protocol.jsonrpc.annotation.Sender;
import com.almende.eve.protocol.jsonrpc.formats.JSONRPCException;
import com.almende.util.TypeUtil;
import com.almende.util.URIUtil;
import com.almende.util.jackson.JOM;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Access(AccessType.PUBLIC)
@ThreadSafe
public class HolonAgent extends AbstractLampAgent {
	private static final TypeUtil<ArrayList<Sub>>	type	= new TypeUtil<ArrayList<Sub>>() {};

	public void create(@Name("neighbours") List<String> neighbors,
			@Name("stepSize") Integer stepSize) throws JSONRPCException,
			IOException {
		super.create(neighbors, stepSize);

		String taskId = schedule("checkMerge", null, (int) Math.random() * 1500);
		System.out.println(getId() + ": schedulertask created:" + taskId
				+ " --> " + getScheduler());
	}

	public void checkMerge() throws JSONRPCException, IOException {
		System.out.println(getId() + ": checkMerge()");
		if (getState().containsKey("parent")) {
			System.out.println(getId() + ": Already merged, not doubling");
			return;
		}
		if (neighbours == null) {
			neighbours = getNeighbours();
		}
		ArrayList<Sub> subs = getState().get("subs", type);
		if (subs == null) {
			subs = new ArrayList<Sub>(0);
		}
		System.out.println(getId() + ": n:" + neighbours.size() + " - s:"
				+ subs.size());
		if (neighbours.size() - subs.size() == 1) {
			Set<String> nbs = new HashSet<String>(neighbours);
			int size = 1;
			for (Sub sub : subs) {
				nbs.remove(sub.getAddress());
				size += sub.getSize();
			}
			String neighbour = nbs.toArray(new String[0])[0];
			getState().put("parent", neighbour);

			ObjectNode params = JOM.createObjectNode();
			params.put("size", size);
			try {
				System.out.println(getId() + ": Merging with " + neighbour);
				Boolean res = callSync(URIUtil.create(neighbour), "merge", params,
						Boolean.class);
				if (!res) {
					getState().remove("parent");
					schedule("checkMerge", null, (int) Math.random() * 500);
				} else {
					System.out.println(getId() + ": Merged with " + neighbour);
				}
			} catch (Exception e) {
				schedule("checkMerge", null, (int) Math.random() * 500);
			}
		}
	}

	public Boolean merge(@Name("size") int size, @Sender URI sender)
			throws JSONRPCException, IOException {
		if (neighbours == null) {
			neighbours = getNeighbours();
		}
		String parent = getState().get("parent", String.class);
		if (parent != null && parent.equals(sender.toASCIIString())) {
			return false;
		}
		ArrayList<Sub> oldsubs = getState().get("subs", type);
		ArrayList<Sub> subs = null;
		if (oldsubs == null) {
			subs = new ArrayList<Sub>();
		} else {
			subs = type.inject(oldsubs.clone());
		}
		for (Sub sub : subs) {
			if (sub.getAddress().equals(sender)) {
				return true;
			}
		}
		if (!neighbours.contains(sender)) {
			System.err.println("Merge requested by non-neighbour??? : "
					+ sender);
			return false;
		}
		Sub sub = new Sub();
		sub.setAddress(sender.toASCIIString());
		sub.setSize(size);
		subs.add(sub);
		if (!getState().putIfUnchanged("subs", subs, oldsubs)) {
			merge(size, sender);
		}
		schedule("checkMerge", null, 0);
		return true;
	}

	public void handleTask(@Name("count") Integer count, @Sender URI sender)
			throws IOException, JSONRPCException {
		if (sender != null
				&& !getState().get("parent", String.class).equals(sender.toASCIIString())) {
			System.out.println("Warning: got task from non-parent!!!" + sender.toASCIIString());
		}
		if (count > 0) {
			lampOn();
			count--;
		} else {
			lampOff();
		}

		ArrayList<Sub> subs = getState().get("subs", type);
		if (subs == null) {
			return;
		}
		for (Sub sub : subs) {
			int cnt = Math.min(count, sub.getSize());
			count -= cnt;
			ObjectNode params = JOM.createObjectNode();
			params.put("count", cnt);
			call(URI.create(sub.getAddress()), "handleTask", params, null);
		}

	}

	public void handleGoal(@Name("goal") Goal goal, @Sender URI sender)
			throws IOException, JSONRPCException, JsonProcessingException {
		String parent = getState().get("parent", String.class);

		if (parent != null) {
			System.err.println(getId()
					+ ": HandleGoal received, sending it to my parent:"
					+ parent);
			ObjectNode params = JOM.createObjectNode();
			params.set("goal", JOM.getInstance().valueToTree(goal));
			call(URIUtil.create(parent), "handleGoal", params, null);
		} else {
			System.err.println(getId()
					+ ": HandleGoal received, handling it myself");

			Set<String> nbs = new HashSet<String>(neighbours);

			ArrayList<Sub> subs = getState().get("subs", type);

			int subSize = 0;
			if (subs != null) {
				for (Sub sub : subs) {
					nbs.remove(sub.getAddress());
					subSize += sub.getSize();
				}
			}
			Integer pointer = getState().get(goal.getId(), Integer.class);
			if (pointer == null) {
				for (int i = 0; i < nbs.size(); i++) {
					if (nbs.toArray(new String[0])[i].equals(sender)) {
						pointer = i;
					}
				}
				if (pointer == null) {
					pointer = 0;
				}
			}
			Integer stepSize = getState().get("stepSize", Integer.class);
			if (!getState().containsKey(goal.getId())) {
				// Determine my own influence on the goal
				double noOn = (goal.getPercentage() * goal.getAgentCnt()) / 100;
				goal.setAgentCnt(goal.getAgentCnt() + subSize + 1);

				double max = (((noOn + subSize + 1) * 100) / (goal
						.getAgentCnt()));
				double min = (((noOn) * 100) / (goal.getAgentCnt()));
				int subOn = -1;
				if (max < goal.getGoalPct()) {
					subOn = subSize + 1;
				} else if (min > goal.getGoalPct()) {
					subOn = 0;
				} else {
					double noLampsOn = (goal.getGoalPct() * goal.getAgentCnt()) / 100;
					subOn = (int) Math.round(noLampsOn - noOn);
				}
				handleTask(subOn, null);
				goal.setPercentage((noOn + subOn) * 100 / goal.getAgentCnt());
				goal.setTtl(0);
			} else {
				goal.setTtl(goal.getTtl() + 1);
			}
			getState().put("goal", goal);

			if (goal.getTtl() > 15) {
				// No changes, drop this goal.
				return;
			}
			if (nbs.size() == 0) {
				return;
			}
			// Send goal further to neighbours
			ObjectNode params = JOM.createObjectNode();
			params.set("goal", JOM.getInstance().valueToTree(goal));

			int count = 0;
			int original_pointer = pointer;
			boolean stop = false;

			while (count < stepSize && !stop) {
				String neighbour = nbs.toArray(new String[0])[pointer];
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
		}
	}

	class Sub {
		private int		size	= 0;
		private String	address	= "";

		public Sub() {}

		public int getSize() {
			return size;
		}

		public void setSize(int size) {
			this.size = size;
		}

		public String getAddress() {
			return address;
		}

		public void setAddress(String address) {
			this.address = address;
		}
	}
}
