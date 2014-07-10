/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.deploy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.almende.eve.agent.Agent;
import com.almende.eve.agent.AgentBuilder;
import com.almende.eve.agent.AgentConfig;
import com.almende.eve.capabilities.Config;
import com.almende.eve.capabilities.wake.WakeService;
import com.almende.eve.config.YamlReader;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class Boot.
 */
public final class Boot {
	private static final Logger	LOG	= Logger.getLogger(Boot.class.getName());
	
	private Boot() {
	}
	
	/**
	 * The default agent booter. It takes an EVE yaml file and creates all
	 * agents mentioned in the "agents" section.
	 * 
	 * @param args
	 *            Single argument: args[0] -> Eve yaml
	 */
	public static void main(final String[] args) {
		if (args.length == 0) {
			LOG.warning("Missing argument pointing to yaml file:");
			LOG.warning("Usage: java -jar <jarfile> eve.yaml");
			return;
		}
		final ClassLoader cl = new ClassLoader() {
			@Override
			protected Class<?> findClass(final String name)
					throws ClassNotFoundException {
				Class<?> result = null;
				try {
					result = super.findClass(name);
				} catch (ClassNotFoundException cne) {
				}
				if (result == null) {
					FileInputStream fi = null;
					try {
						
						String path = name.replace('.', '/');
						fi = new FileInputStream(System.getProperty("user.dir")
								+ "/" + path + ".class");
						byte[] classBytes = new byte[fi.available()];
						fi.read(classBytes);
						fi.close();
						return defineClass(name, classBytes, 0,
								classBytes.length);
					} catch (Exception e) {
						LOG.log(Level.WARNING, "Failed to load class:", e);
					}
				}
				if (result == null) {
					throw new ClassNotFoundException(name);
				}
				return result;
			}
		};
		
		loadAgents(args[0], null, cl);
	}
	
	/**
	 * Load agents from config file, agent classes should be in the classpath.
	 * 
	 * @param configFileName
	 *            the config file name
	 */
	public static void loadAgents(final String configFileName) {
		loadAgents(configFileName, null, null);
	}
	
	/**
	 * Load agents from config file, agent classes should be in the classpath.
	 * This variant can load WakeableAgents.
	 * 
	 * @param configFileName
	 *            the config file name
	 * @param ws
	 *            the WakeService
	 */
	public static void loadAgents(final String configFileName,
			final WakeService ws) {
		loadAgents(configFileName, ws, null);
	}
	
	/**
	 * Load agents.
	 * 
	 * @param configFileName
	 *            the config file name
	 * @param ws
	 *            The WakeService
	 * @param cl
	 *            the custom classloader
	 */
	public static void loadAgents(final String configFileName,
			final WakeService ws, final ClassLoader cl) {
		try {
			InputStream is = new FileInputStream(new File(configFileName));
			loadAgents(is, ws, cl);
		} catch (FileNotFoundException e) {
			LOG.log(Level.WARNING,
					"Couldn't find configfile:" + configFileName, e);
			return;
		}
	}
	
	/**
	 * Load agents from config file, agent classes should be in the classpath.
	 * 
	 * @param is
	 *            An Inputstream to the yaml data
	 */
	public static void loadAgents(final InputStream is) {
		loadAgents(is, null, null);
	}
	
	/**
	 * Load agents from config file, agent classes should be in the classpath.
	 * This variant can load WakeableAgents.
	 * 
	 * @param is
	 *            An Inputstream to the yaml data
	 * @param ws
	 *            The WakeService
	 */
	public static void loadAgents(final InputStream is, final WakeService ws) {
		loadAgents(is, ws, null);
	}
	
	/**
	 * Load agents.
	 * 
	 * @param is
	 *            An Inputstream to the yaml data
	 * @param ws
	 *            The WakeService
	 * @param cl
	 *            the custom classloader
	 */
	public static void loadAgents(final InputStream is, final WakeService ws,
			final ClassLoader cl) {
		final Config config = YamlReader.load(is).expand();
		
		final ArrayNode agents = (ArrayNode) config.get("agents");
		for (final JsonNode agent : agents) {
			final AgentConfig agentConfig = new AgentConfig((ObjectNode) agent);
			final Agent newAgent = new AgentBuilder().withWakeService(ws)
					.withClassLoader(cl).with(agentConfig).build();
			LOG.info("Created agent:" + newAgent.getId());
		}
	}
	
}
