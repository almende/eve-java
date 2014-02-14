/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.goldemo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;

import com.almende.eve.agent.AgentHost;
import com.almende.eve.rpc.jsonrpc.JSONRPCException;

/**
 * The Class Goldemo.
 */
public class Goldemo {
	// final static String BASE = "inproc://";
	// final static String BASE = "ipc:///tmp/zmq-socket-";
	// final static String PATH = "zmq:"+BASE;
	
	/**
	 * The Constant PATH.
	 */
	final static String		PATHodd		= "local:";
	final static String		PATHeven	= "local:";
	// final static String PATHodd = "http://127.0.0.1:8081/agents/";
	// final static String PATHeven = "http://127.0.0.1:8080/agents/";
	
	final static boolean	NEW			= true;
	
	/**
	 * The main method.
	 * 
	 * @param args
	 *            the arguments
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws JSONRPCException
	 *             the jSONRPC exception
	 * @throws ClassNotFoundException
	 *             the class not found exception
	 * @throws InstantiationException
	 *             the instantiation exception
	 * @throws IllegalAccessException
	 *             the illegal access exception
	 * @throws InvocationTargetException
	 *             the invocation target exception
	 * @throws NoSuchMethodException
	 *             the no such method exception
	 */
	public static void main(String[] args) throws IOException,
			JSONRPCException, ClassNotFoundException, InstantiationException,
			IllegalAccessException, InvocationTargetException,
			NoSuchMethodException {
		if (args.length < 4) {
			throw new IllegalArgumentException(
					"Please use at least 4 arguments: Y=yamlFile & X=seconds & N=rows & M=columns");
		}
		String path = args[0];
		Integer X = Integer.valueOf(args[1]);
		Integer N = Integer.valueOf(args[2]);
		Integer M = Integer.valueOf(args[3]);
		
		Boolean annimate = false;
		if (args.length > 4) {
			annimate = Boolean.valueOf(args[4]);
		}
		
		AgentHost host = AgentHost.getInstance();
		host.loadConfig(path);
		
		if (args.length > 5) {
			boolean shortcut = Boolean.valueOf(args[5]);
			host.setDoesShortcut(shortcut);
		}
		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		
		String input;
		
		int cN = 0;
		int no = 0;
		while ((input = br.readLine()) != null && cN < N) {
			String trimmedInput = input.trim();
			if (trimmedInput.isEmpty()) break;
			if (trimmedInput.length() != M) throw new IllegalArgumentException(
					"Incorrect input line detected:" + input);
			for (int cM = 0; cM < M; cM++) {
				if (NEW) {
					String agentId = "Agent_" + no++;
					Cell cell = host.createAgent(Cell.class, agentId);
					cell.new_create(PATHodd, PATHeven,
							(trimmedInput.charAt(cM) == '+'), M * N);
				} else {
					createAgent(host, N, M, cN, cM,
							(trimmedInput.charAt(cM) == '+'));
				}
			}
			cN++;
		}
		if (NEW) {
			for (no = 0; no < (N * M); no++) {
				Cell cell = (Cell) host.getAgent("Agent_" + no);
				cell.new_start();
			}
		} else {
			for (cN = 0; cN < N; cN++) {
				for (int cM = 0; cM < M; cM++) {
					Cell cell = (Cell) host.getAgent("agent_" + cN + "_" + cM);
					cell.register();
				}
			}
			for (cN = 0; cN < N; cN++) {
				for (int cM = 0; cM < M; cM++) {
					Cell cell = (Cell) host.getAgent("agent_" + cN + "_" + cM);
					cell.start();
				}
			}
		}
		
		System.err.println("Started!");
		try {
			Thread.sleep(X * 1000);
		} catch (InterruptedException e) {
			System.err.println("Early interrupt");
		}
		if (NEW) {
			for (no = 0; no < (N * M); no++) {
				Cell cell = (Cell) host.getAgent("Agent_" + no);
				cell.stop();
			}
		} else {
			for (cN = 0; cN < N; cN++) {
				for (int cM = 0; cM < M; cM++) {
					Cell cell = (Cell) host.getAgent("agent_" + cN + "_" + cM);
					cell.stop();
				}
			}
		}
		HashMap<String, ArrayList<CycleState>> results = new HashMap<String, ArrayList<CycleState>>();
		int max_full = 0;
		if (NEW) {
			for (no = 0; no < (N * M); no++) {
				Cell cell = (Cell) host.getAgent("Agent_" + no);
				ArrayList<CycleState> res = cell.getAllCycleStates();
				max_full = (max_full == 0 || max_full > res.size() ? res.size()
						: max_full);
				results.put(cell.getId(), res);
			}
		} else {
			for (cN = 0; cN < N; cN++) {
				for (int cM = 0; cM < M; cM++) {
					Cell cell = (Cell) host.getAgent("agent_" + cN + "_" + cM);
					ArrayList<CycleState> res = cell.getAllCycleStates();
					max_full = (max_full == 0 || max_full > res.size() ? res
							.size() : max_full);
					results.put(cell.getId(), res);
				}
			}
		}
		int cycle = 0;
		for (int j = 0; j < max_full; j++) {
			if (annimate) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
				}
				final String ESC = "\033[";
				System.out.print(ESC + "2J");
			}
			System.out.println("Cycle:" + cycle + "/" + (max_full - 1));
			System.out.print("/");
			for (int i = 0; i < M * 2; i++) {
				System.out.print("-");
			}
			System.out.println("-\\");
			no = 0;
			for (cN = 0; cN < N; cN++) {
				System.out.print("| ");
				for (int cM = 0; cM < M; cM++) {
					String id = ("agent_" + cN + "_" + cM);
					if (NEW) {
						id = "Agent_" + no++;
					}
					ArrayList<CycleState> states = results.get(id);
					if (states.size() <= cycle) {
						break;
					}
					System.out.print(states.get(cycle).isAlive() ? "# " : "- ");
				}
				System.out.println("|");
			}
			System.out.print("\\");
			for (int i = 0; i < M * 2; i++) {
				System.out.print("-");
			}
			System.out.println("-/");
			cycle++;
		}
		// System.out.println(results);
		System.exit(0);
	}
	
	/**
	 * Creates the agent.
	 * 
	 * @param host
	 *            the host
	 * @param N
	 *            the n
	 * @param M
	 *            the m
	 * @param cN
	 *            the c n
	 * @param cM
	 *            the c m
	 * @param state
	 *            the state
	 * @throws JSONRPCException
	 *             the jSONRPC exception
	 * @throws InstantiationException
	 *             the instantiation exception
	 * @throws IllegalAccessException
	 *             the illegal access exception
	 * @throws InvocationTargetException
	 *             the invocation target exception
	 * @throws NoSuchMethodException
	 *             the no such method exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static void createAgent(AgentHost host, int N, int M, int cN,
			int cM, boolean state) throws JSONRPCException,
			InstantiationException, IllegalAccessException,
			InvocationTargetException, NoSuchMethodException, IOException {
		final String PATH = PATHeven;
		
		String agentId = "agent_" + cN + "_" + cM;
		ArrayList<String> neighbors = new ArrayList<String>(8);
		neighbors.add(PATH + "agent_" + ((N + cN - 1) % N) + "_"
				+ ((M + cM - 1) % M));
		neighbors.add(PATH + "agent_" + ((N + cN) % N) + "_"
				+ ((M + cM - 1) % M));
		neighbors.add(PATH + "agent_" + ((N + cN + 1) % N) + "_"
				+ ((M + cM - 1) % M));
		neighbors.add(PATH + "agent_" + ((N + cN - 1) % N) + "_"
				+ ((M + cM) % M));
		neighbors.add(PATH + "agent_" + ((N + cN + 1) % N) + "_"
				+ ((M + cM) % M));
		neighbors.add(PATH + "agent_" + ((N + cN - 1) % N) + "_"
				+ ((M + cM + 1) % M));
		neighbors.add(PATH + "agent_" + ((N + cN) % N) + "_"
				+ ((M + cM + 1) % M));
		neighbors.add(PATH + "agent_" + ((N + cN + 1) % N) + "_"
				+ ((M + cM + 1) % M));
		Cell cell = host.createAgent(Cell.class, agentId);
		cell.create(neighbors, state);
	}
	
}
