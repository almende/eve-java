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
import java.util.ArrayList;
import java.util.HashMap;

import com.almende.eve.agent.AgentConfig;
import com.almende.eve.config.Config;
import com.almende.util.threads.ThreadPool;

/**
 * The Class Goldemo.
 */
public class Goldemo {

	/**
	 * The Constant AGENTPREFIX.
	 */
	// final static String BASE = "inproc://";
	// final static String BASE = "ipc:///tmp/zmq-socket-";
	// final static String PATH = "zmq:"+BASE;

	public final static String	AGENTPREFIX	= "Agent_";

	/**
	 * The Constant PATH.
	 */
	private static String		PATHodd		= "local:";
	private static String		PATHeven	= "local:";

	// private static String PATHodd = "http://127.0.0.1:8081/agents/";
	// private static String PATHeven = "http://127.0.0.1:8081/agents/";

	// final static String PATHodd = PATH;
	// final static String PATHeven = PATH;

	/**
	 * The main method.
	 * 
	 * @param args
	 *            the arguments
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static void main(final String[] args) throws IOException {

		if (args.length == 0) {
			System.err
					.println("Missing configuration file! Usage: java -jar gol.jar <configPath> < <startup_file>");
			return;
		}
		final String configFileName = args[0];
		final Config config = Config.load(Config.getType(configFileName),
				new FileInputStream(new File(configFileName)));
		
		final Integer runTime = config.get("gol", "runTime");
		final Integer N = config.get("gol", "columns");
		final Integer M = config.get("gol", "rows");

		final String oddUrl = config.get("gol", "OddUrl");
		if (oddUrl != null) {
			PATHodd = oddUrl;
		}
		final String evenUrl = config.get("gol", "EvenUrl");
		if (evenUrl != null) {
			PATHeven = evenUrl;
		}

		if (runTime == null || N == null || M == null) {
			System.err.println("Configuration missing in configFile.");
			return;

		}

		Boolean random = config.get("gol", "random");
		if (random == null) {
			random = false;
		}

		Boolean reportOnly = config.get("gol", "reportOnly");
		if (reportOnly == null) {
			reportOnly = false;
		}

		Boolean annimate = config.get("gol", "annimate");
		if (annimate == null) {
			annimate = true;
		}

		Integer nofCores = config.get("gol", "nofCores");
		if (nofCores != null) {
			ThreadPool.setNofCores(nofCores);
		} else {
			nofCores = ThreadPool.getNofCores();
		}

		final ArrayList<Cell> cells = new ArrayList<Cell>(N * M);
		if (!random) {
			final BufferedReader br = new BufferedReader(new InputStreamReader(
					System.in));

			String input;

			int cN = 0;
			int no = 0;
			while ((input = br.readLine()) != null && cN < N) {
				final String trimmedInput = input.trim();
				if (trimmedInput.isEmpty()) {
					break;
				}
				if (trimmedInput.length() != M) {
					throw new IllegalArgumentException(
							"Incorrect input line detected:" + input);
				}
				for (int cM = 0; cM < M; cM++) {
					final AgentConfig agent_config = AgentConfig
							.decorate(config.deepCopy());
					agent_config.setId(AGENTPREFIX + no++);
					final Cell cell = new Cell(agent_config);
					cell.create(PATHodd, PATHeven,
							(trimmedInput.charAt(cM) == '+'), M * N);
					cells.add(cell);
				}
				cN++;
			}
		} else {
			int no = 0;
			for (int cN = 0; cN < N; cN++) {
				for (int cM = 0; cM < M; cM++) {
					final AgentConfig agent_config = AgentConfig
							.decorate(config.deepCopy());
					agent_config.setId(AGENTPREFIX + no++);
					final Cell cell = new Cell(agent_config);
					cell.create(PATHodd, PATHeven, (Math.random() > 0.5), M * N);
					cells.add(cell);
				}
			}
		}

		System.err.println("Waiting before start, 10s");
		try {
			Thread.sleep(10000);
		} catch (final InterruptedException e) {
			System.err.println("Early interrupt");
		}
		System.err.println("Trigger start!");
		for (final Cell cell : cells) {
			cell.start();
		}
		System.err.println("Started for " + runTime + "s!");
		try {
			Thread.sleep(runTime * 1000);
		} catch (final InterruptedException e) {
			System.err.println("Early interrupt");
		}
		for (final Cell cell : cells) {
			cell.stop();
		}
		System.err.println("Stopped!");
		final HashMap<String, ArrayList<CycleState>> results = new HashMap<String, ArrayList<CycleState>>();
		int max_full = 0;
		for (final Cell cell : cells) {
			final ArrayList<CycleState> res = cell.getAllCycleStates();
			max_full = (max_full == 0 || max_full > res.size() ? res.size()
					: max_full);
			results.put(cell.getId(), res);
		}

		if (reportOnly) {
			System.out.println("Cycles:" + (max_full - 1) + "(" + N + "x" + M
					+ ": ~" + ((max_full - 1) / (runTime)) + " cycles/second)");
			System.out.println(((max_full - 1) * M * N * 8) / (runTime)
					+ " RPCs/second (" + runTime + " sec)");
		} else {
			int cycle = 0;
			for (int j = 0; j < max_full; j++) {
				if (annimate) {
					try {
						Thread.sleep(500);
					} catch (final InterruptedException e) {}
					final String ESC = "\033[";
					System.out.print(ESC + "2J");
				}
				System.out.println("nofCores:" + nofCores);
				System.out.println("Cycle:" + cycle + "/" + (max_full - 1));
				System.out.println(((max_full - 1) * M * N * 8) / (runTime)
						+ " RPCs/second (" + runTime + " sec)");
				System.out.print("/");
				for (int i = 0; i < M * 2; i++) {
					System.out.print("-");
				}
				System.out.println("-\\");
				int no = 0;
				for (int cN = 0; cN < N; cN++) {
					System.out.print("| ");
					for (int cM = 0; cM < M; cM++) {
						final String id = AGENTPREFIX + no++;
						final ArrayList<CycleState> states = results.get(id);
						if (states.size() <= cycle) {
							break;
						}
						System.out.print(states.get(cycle).isAlive() ? "# "
								: "- ");
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
		}
		// System.out.println(results);
		System.exit(0);
	}

}
