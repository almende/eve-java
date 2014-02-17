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
import com.almende.eve.agent.annotation.ThreadSafe;
import com.almende.eve.agent.callback.AsyncCallback;
import com.almende.eve.rpc.annotation.Access;
import com.almende.eve.rpc.annotation.AccessType;
import com.almende.eve.rpc.annotation.Name;
import com.almende.eve.rpc.annotation.Sender;
import com.almende.eve.rpc.jsonrpc.JSONRPCException;
import com.almende.eve.rpc.jsonrpc.JSONRequest;
import com.almende.eve.rpc.jsonrpc.jackson.JOM;
import com.almende.util.TypeUtil;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class Cell.
 */
@Access(AccessType.PUBLIC)
@ThreadSafe(true)
public class Cell extends Agent {
	private ArrayList<String>	neighbors	= null;
	
	/**
	 * Creates the.
	 * 
	 * @param neighbors
	 *            the neighbors
	 * @param initState
	 *            the init state
	 */
	public void create(@Name("neighbors") ArrayList<String> neighbors,
			@Name("state") Boolean initState) {
		getState().put("Stopped",false);
		getState().put("neighbors", neighbors);
		getState().put("val_0", new CycleState(0, initState));
		getState().put("current_cycle", 1);
		
	}
	
	/**
	 * @param odd
	 * @param even
	 * @param initState
	 * @param totalSize
	 */
	public void new_create(@Name("pathOdd") String odd,
			@Name("pathEven") String even, @Name("state") Boolean initState,
			@Name("totalSize") int totalSize) {
		getState().put("Stopped",false);
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
		String path = even;
		if (agentNo % 2 > 0) {
			path = odd;
		}
		if (agentNo != 0) {
			cN = agentNo % N;
			cM = (int) Math.floor(agentNo / M);
		}
		
		neighbors = new ArrayList<String>(8);
		neighbors.add(path + Goldemo.AGENTPREFIX
				+ calcBack(((N + cN - 1) % N), ((M + cM - 1) % M), N));
		neighbors.add(path + Goldemo.AGENTPREFIX
				+ calcBack(((N + cN) % N), ((M + cM - 1) % M), N));
		neighbors.add(path + Goldemo.AGENTPREFIX
				+ calcBack(((N + cN + 1) % N), ((M + cM - 1) % M), N));
		neighbors.add(path + Goldemo.AGENTPREFIX
				+ calcBack(((N + cN - 1) % N), ((M + cM) % M), N));
		neighbors.add(path + Goldemo.AGENTPREFIX
				+ calcBack(((N + cN + 1) % N), ((M + cM) % M), N));
		neighbors.add(path + Goldemo.AGENTPREFIX
				+ calcBack(((N + cN - 1) % N), ((M + cM + 1) % M), N));
		neighbors.add(path + Goldemo.AGENTPREFIX
				+ calcBack(((N + cN) % N), ((M + cM + 1) % M), N));
		neighbors.add(path + Goldemo.AGENTPREFIX
				+ calcBack(((N + cN + 1) % N), ((M + cM + 1) % M), N));
		getState().put("neighbors", neighbors);
	}
	
	private int calcBack(int cN, int cM, int N) {
		return cM * N + cN;
	}
	
	/**
	 * Register.
	 * 
	 * @throws JSONRPCException
	 *             the jSONRPC exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void register() throws JSONRPCException, IOException {
		if (neighbors == null) {
			neighbors = getState().get("neighbors",
					new TypeUtil<ArrayList<String>>() {
					});
		}
		for (String neighbor : neighbors) {
			getEventsFactory().subscribe(URI.create(neighbor),
					"cycleCalculated", "askCycleState");
		}
	}
	
	/**
	 * Stop.
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws JSONRPCException
	 *             the jSONRPC exception
	 */
	public void stop() throws IOException, JSONRPCException {
		getState().put("Stopped",true);
		getEventsFactory().clear();
	}
	
	/**
	 * Start.
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void start() throws IOException {
		getEventsFactory().trigger("cycleCalculated");
	}
	
	/**
	 * 
	 */
	public void new_start() {
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
				send(new JSONRequest("collect", params), uri, null, null);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return;
	}
	
	/**
	 * Ask cycle state.
	 * 
	 * @param neighbor
	 *            the neighbor
	 * @throws JSONRPCException
	 *             the jSONRPC exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws URISyntaxException
	 *             the uRI syntax exception
	 */
	public void askCycleState(@Sender final String neighbor)
			throws JSONRPCException, IOException, URISyntaxException {
		
		final String neighborId = getAgentHost().getAgentId(
				URI.create(neighbor));
		ObjectNode params = JOM.createObjectNode();
		params.put("cycle", getState().get("current_cycle", Integer.class) - 1);
		sendAsync(URI.create(neighbor), "getCycleState", params,
				new AsyncCallback<CycleState>() {
					
					@Override
					public void onSuccess(CycleState state) {
						if (state != null) {
							getState().put(neighborId + "_" + state.getCycle(),
									state);
							try {
								calcCycle(false);
							} catch (URISyntaxException e) {
								e.printStackTrace();
							}
						}
					}
					
					@Override
					public void onFailure(Exception exception) {
						// TODO Auto-generated method stub
						
					}
					
				}, CycleState.class);
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
		getState().put(neighborNo + "_" + state.getCycle(), state);
		try {
			calcCycle(true);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
	private void calcCycle(final boolean NEW) throws URISyntaxException {
		final Integer currentCycle = getState().get("current_cycle",
				Integer.class);
		if (currentCycle != null && currentCycle != 0) {
			int aliveNeighbors = 0;
			for (String neighbor : neighbors) {
				String neighborId = getAgentHost().getAgentId(
						URI.create(neighbor));
				if (NEW) {
					neighborId = neighborId.replaceFirst(".*_", "");
				}
				CycleState nState = getState()
						.get(neighborId + "_" + (currentCycle - 1),
								CycleState.class);
				if (nState == null){
					return;
				} else if (nState.isAlive()) {
					aliveNeighbors++;
				}
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
				getState().put("current_cycle", currentCycle + 1);
				if (getState().get("Stopped",Boolean.class)){
					return;
				}
				if (NEW) {
					final ObjectNode params = JOM.createObjectNode();
					params.put("alive", myState.isAlive());
					params.put("cycle", currentCycle);
					params.put("from", getId().substring(6));
					for (String neighbor : neighbors) {
						final URI uri = URI.create(neighbor);
						try {
							send(new JSONRequest("collect", params), uri, null,
									null);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				} else {
					try {
						getEventsFactory().trigger("cycleCalculated");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				for (String neighbor : neighbors) {
					String neighborId = getAgentHost().getAgentId(
							URI.create(neighbor));
					if (NEW) {
						neighborId = neighborId.replaceFirst(".*_", "");
					}
					getState().remove(neighborId + "_" + (currentCycle - 1));
				}
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
