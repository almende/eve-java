/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.goldemo;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import com.almende.eve.agent.Agent;
import com.almende.eve.transform.rpc.annotation.Access;
import com.almende.eve.transform.rpc.annotation.AccessType;
import com.almende.eve.transform.rpc.annotation.Name;
import com.almende.util.TypeUtil;
import com.almende.util.jackson.JOM;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class Cell.
 */
@Access(AccessType.PUBLIC)
public class Cell extends Agent {
	private ArrayList<String>	neighbors	= null;
	
	/**
	 * Instantiates a new cell.
	 * 
	 * @param config
	 *            the config
	 */
	public Cell(ObjectNode config){
		this.setConfig(config);
	}
	
	/**
	 * @param odd
	 * @param even
	 * @param initState
	 * @param totalSize
	 */
	public void create(@Name("pathOdd") String odd,
			@Name("pathEven") String even, @Name("state") Boolean initState,
			@Name("totalSize") int totalSize) {
		getState().put("Stopped", false);
		getState().put("val_0", new CycleState(0, initState));
		getState().put("current_cycle", 1);
		String id = getId();
		int agentNo = Integer.parseInt(id.substring(id.indexOf('_') + 1));
		calcNeighbours(odd, even, agentNo, totalSize);
	}
	
	/**
	 * @param agentNo
	 * @param totalSize
	 */
	private void calcNeighbours(String odd, String even, int agentNo,
			int totalSize) {
		int N = (int) Math.floor(Math.sqrt(totalSize));
		int M = N;
		int cN = 0;
		int cM = 0;
		if (agentNo != 0) {
			cM = agentNo % M;
			cN = (int) Math.floor(agentNo / N);
		}
		neighbors = new ArrayList<String>(8);
		
		for (int id = 0; id < 8; id++) {
			int neighborNo = getNeighborNo(id, cN, cM, N, M);
			neighbors.add(addPath(odd, even, Goldemo.AGENTPREFIX + neighborNo,
					neighborNo));
		}
		getState().put("neighbors", neighbors);
	}
	
	private String addPath(String odd, String even, String path, int agentNo) {
		return (agentNo % 2 == 0 ? even : odd) + path;
	}
	
	private int calcBack(int cN, int cM, int M) {
		return cM + cN * M;
	}
	
	private int getNeighborNo(int id, int cN, int cM, int N, int M) {
		switch (id) {
			case 0:
				return calcBack(((N + cN - 1) % N), ((M + cM - 1) % M), M);
			case 1:
				return calcBack(((N + cN) % N), ((M + cM - 1) % M), M);
			case 2:
				return calcBack(((N + cN + 1) % N), ((M + cM - 1) % M), M);
			case 3:
				return calcBack(((N + cN - 1) % N), ((M + cM) % M), M);
			case 4:
				return calcBack(((N + cN + 1) % N), ((M + cM) % M), M);
			case 5:
				return calcBack(((N + cN - 1) % N), ((M + cM + 1) % M), M);
			case 6:
				return calcBack(((N + cN) % N), ((M + cM + 1) % M), M);
			case 7:
				return calcBack(((N + cN + 1) % N), ((M + cM + 1) % M), M);
		}
		System.err.println("SHould never happen!");
		return 0;
	}
	
	/**
	 * Stop.
	 */
	public void stop(){
		getState().put("Stopped", true);
	}
	
	/**
	 * 
	 */
	public void start() {
		if (neighbors == null) {
			neighbors = getState().get("neighbors",
					new TypeUtil<ArrayList<String>>() {
					});
		}
		CycleState myState = getState().get("val_0", CycleState.class);
		final ObjectNode params = JOM.createObjectNode();
		params.put("alive", myState.isAlive());
		params.put("cycle", 0);
		params.put("from", getId().substring(6));
		for (String neighbor : neighbors) {
			final URI uri = URI.create(neighbor);
			try {
				send(uri, "collect", params, null);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return;
	}
	
	/**
	 * @param alive
	 * @param cycle
	 * @param neighborNo
	 */
	public void collect(@Name("alive") boolean alive, @Name("cycle") int cycle,
			@Name("from") int neighborNo) {
		if (neighbors == null) {
			neighbors = getState().get("neighbors",
					new TypeUtil<ArrayList<String>>() {
					});
		}
		CycleState state = new CycleState(cycle, alive);
		// System.out.println(getId()+": Received state:" + state + " from:" +
		// neighborNo);
		getState().put(neighborNo + "_" + state.getCycle(), state);
		try {
			calcCycle();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
	
	private String getNeighborId(final String neighbor) {
		String neighborId = null;
		if (neighbor.startsWith("local:")) {
			neighborId = neighbor.replace("local:", "");
		} else {
			URI neighborUrl = URI.create(neighbor);
			neighborId = neighborUrl.getPath().replaceFirst("agents/", "");
		}
		neighborId = neighborId.replaceFirst(".*_", "");
		return neighborId;
	}
	
	private void calcCycle() throws URISyntaxException {
		final Integer currentCycle = getState().get("current_cycle",
				Integer.class);
		if (currentCycle != null && currentCycle != 0) {
			int aliveNeighbors = 0;
			int knownNeighbors = 0;
			for (String neighbor : neighbors) {
				String neighborId = getNeighborId(neighbor);
				CycleState nState = getState()
						.get(neighborId + "_" + (currentCycle - 1),
								CycleState.class);
				if (nState == null) {
					return;
					// continue;
				} else if (nState.isAlive()) {
					aliveNeighbors++;
				}
				knownNeighbors++;
			}
			if (knownNeighbors < 8) {
				// System.out.println(getId()+"/"+currentCycle+" has seen: "+knownNeighbors+" neighbors.");
				return;
			}
			CycleState myState = getState().get("val_" + (currentCycle - 1),
					CycleState.class);
			CycleState newState = null;
			if (aliveNeighbors < 2 || aliveNeighbors > 3) {
				newState = new CycleState(currentCycle, false);
			} else if (aliveNeighbors == 3) {
				newState = new CycleState(currentCycle, true);
			} else {
				newState = new CycleState(currentCycle, myState.isAlive());
			}
			if (getState()
					.putIfUnchanged("val_" + currentCycle, newState, null)) {
				// System.out.println(getId()+" :"+newState);
				getState().put("current_cycle", currentCycle + 1);
				if (getState().get("Stopped", Boolean.class)) {
					return;
				}
				final ObjectNode params = JOM.createObjectNode();
				params.put("alive", newState.isAlive());
				params.put("cycle", currentCycle);
				params.put("from", getId().substring(6));
				for (String neighbor : neighbors) {
					final URI uri = URI.create(neighbor);
					try {
						send(uri, "collect", params, null);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				for (String neighbor : neighbors) {
					String neighborId = getNeighborId(neighbor);
					getState().remove(neighborId + "_" + (currentCycle - 1));
				}
				calcCycle();
			}
		}
	}
	
	/**
	 * Gets the cycle state.
	 * 
	 * @param cycle
	 *            the cycle
	 * @return the cycle state
	 */
	public CycleState getCycleState(@Name("cycle") int cycle) {
		if (getState().containsKey("val_" + cycle)) {
			return getState().get("val_" + cycle, CycleState.class);
		}
		return null;
	}
	
	/**
	 * Gets the all cycle states.
	 * 
	 * @return the all cycle states
	 */
	public ArrayList<CycleState> getAllCycleStates() {
		ArrayList<CycleState> result = new ArrayList<CycleState>();
		int count = 0;
		while (getState().containsKey("val_" + count)) {
			result.add(getState().get("val_" + count, CycleState.class));
			count++;
		}
		return result;
	}
	
}
