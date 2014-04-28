/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.goldemo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;

import com.almende.eve.capabilities.Config;
import com.almende.eve.config.YamlReader;
import com.almende.eve.transform.rpc.jsonrpc.JSONRPCException;

/**
 * The Class Goldemo.
 */
public class Goldemo {
	/**
 * 
 */
	// final static String BASE = "inproc://";
	// final static String BASE = "ipc:///tmp/zmq-socket-";
	// final static String PATH = "zmq:"+BASE;
	
	public final static String	AGENTPREFIX	= "Agent_";
	
	/**
	 * The Constant PATH.
	 */
	// final static String PATHodd = "local:";
	// final static String PATHeven = "local:";
	private static String		PATHodd		= "http://127.0.0.1:8081/agents/";
	private static String		PATHeven	= "http://127.0.0.1:8081/agents/";
	
	// final static String PATHodd = PATH;
	// final static String PATHeven = PATH;
	
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
		
		if (args.length == 0) {
			System.err
					.println("Missing yaml file! Usage: java -jar gol.jar <yamlpath>");
			return;
		}
		Config config = YamlReader.load(new FileInputStream(new File(args[0])));
/*
		//Temporary:
		ObjectNode golConfig = JOM.createObjectNode();
		golConfig.put("runTime", 50);
		golConfig.put("columns", 5);
		golConfig.put("rows", 5);
		config.put("gol", golConfig);
		
		HttpTransportConfig transport = new HttpTransportConfig();
		transport.setServletUrl("http://127.0.0.1:8081/agents/");
		config.put("transport", transport);
		
		MemoryStateConfig state = new MemoryStateConfig();
		config.put("state", state);
*/		
		Integer runTime = config.get("gol", "runTime");
		Integer N = config.get("gol", "columns");
		Integer M = config.get("gol", "rows");
		
		String oddUrl = config.get("gol", "OddUrl");
		if (oddUrl != null) {
			PATHodd = oddUrl;
		}
		String evenUrl = config.get("gol", "EvenUrl");
		if (evenUrl != null) {
			PATHeven = evenUrl;
		}
		
		if (runTime == null || N == null || M == null) {
			System.err.println("Configuration missing in yaml.");
			return;
			
		}
		
		Boolean annimate = config.get("gol", "annimate");
		if (annimate == null) {
			annimate = true;
		}
		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		
		String input;
		
		int cN = 0;
		ArrayList<Cell> cells = new ArrayList<Cell>(N*M);
		
		int no = 0;
		while ((input = br.readLine()) != null && cN < N) {
			String trimmedInput = input.trim();
			if (trimmedInput.isEmpty()) break;
			if (trimmedInput.length() != M) throw new IllegalArgumentException(
					"Incorrect input line detected:" + input);
			for (int cM = 0; cM < M; cM++) {
				Config agent_config = new Config(config);
				agent_config.put("id",AGENTPREFIX + no++);
				Cell cell = new Cell(agent_config);
				cell.create(PATHodd, PATHeven,
						(trimmedInput.charAt(cM) == '+'), M * N);
				cells.add(cell);
			}
			cN++;
		}
		for (Cell cell: cells){
			cell.start();
		}
		System.err.println("Started!");
		try {
			Thread.sleep(runTime * 1000);
		} catch (InterruptedException e) {
			System.err.println("Early interrupt");
		}
		for (Cell cell: cells){
			cell.stop();
		}
		HashMap<String, ArrayList<CycleState>> results = new HashMap<String, ArrayList<CycleState>>();
		int max_full = 0;
		for (Cell cell: cells){
			ArrayList<CycleState> res = cell.getAllCycleStates();
			max_full = (max_full == 0 || max_full > res.size() ? res.size()
					: max_full);
			results.put(cell.getId(), res);
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
					String id = AGENTPREFIX + no++;
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
	
}
