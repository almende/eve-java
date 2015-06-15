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
import com.almende.eve.config.Config;
import com.almende.eve.instantiation.InstantiationService;
import com.almende.eve.instantiation.InstantiationServiceBuilder;
import com.almende.eve.instantiation.InstantiationServiceConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class Boot.
 */
public final class Boot {
	private static final Logger	LOG	= Logger.getLogger(Boot.class.getName());

	private Boot() {}

	/**
	 * The default agent booter. It takes an EVE config file and creates all
	 * agents mentioned in the "agents" section.
	 * 
	 * @param args
	 *            Single argument: args[0] -> Eve config file (either json, yaml
	 *            or XML
	 */
	public static void main(final String[] args) {
		if (args.length == 0) {
			LOG.warning("Missing argument pointing to config file:");
			LOG.warning("Usage: java -jar <jarfile> config");
			return;
		}
		final ClassLoader cl = new ClassLoader() {
			@Override
			protected Class<?> findClass(final String name)
					throws ClassNotFoundException {
				Class<?> result = null;
				try {
					result = super.findClass(name);
				} catch (ClassNotFoundException cne) {}
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
		String configFileName = args[0];
		try {
			InputStream is = new FileInputStream(new File(configFileName));
			boot(Config.getType(configFileName), is, cl);

		} catch (FileNotFoundException e) {
			LOG.log(Level.WARNING,
					"Couldn't find configfile:" + configFileName, e);
			return;
		}

	}

	/**
	 * Boot.
	 *
	 * @param type
	 *            the type of the configuration file, one of
	 *            ["yaml","xml","json"]
	 * @param is
	 *            The inputStream of the configuration file
	 * @return the actual configuration
	 */
	public static Config boot(final String type, final InputStream is) {
		return boot(type, is, null);
	}

	/**
	 * Boot.
	 *
	 * @param config
	 *            A JSON DOM containing the configuration
	 * @return the actual configuration
	 */
	public static Config boot(final ObjectNode config) {
		return boot(config, null);
	}

	/**
	 * Boot.
	 *
	 * @param config
	 *            A JSON DOM containing the configuration
	 * @param cl
	 *            the class loader to use.
	 * @return the actual configuration
	 */
	public static Config boot(final ObjectNode config, final ClassLoader cl) {
		final Config conf = Config.decorate(config);
		return boot(conf, null);
	}

	/**
	 * Boot.
	 *
	 * @param type
	 *            the type of the configuration file, one of
	 *            ["yaml","xml","json"]
	 * @param is
	 *            the inputStream of the configuration file
	 * @param cl
	 *            the class loader to use.
	 * @return the actual configuration
	 */
	public static Config boot(final String type, final InputStream is,
			final ClassLoader cl) {
		final Config config = Config.load(type, is);
		return boot(config, cl);
	}

	/**
	 * Boot.
	 *
	 * @param config
	 *            the config
	 * @param cl
	 *            the cl
	 * @return the actual configuration
	 */
	public static Config boot(final Config config, final ClassLoader cl) {
		if (config.has("templates")) {
			config.loadTemplates("templates");
		}
		loadAgents(config, cl);
		loadInstantiationServices(config, cl);
		return config;
	}

	/**
	 * Load instantiation services.
	 *
	 * @param config
	 *            the config
	 * @param cl
	 *            the cl
	 */
	public static void loadInstantiationServices(final Config config,
			final ClassLoader cl) {
		if (!config.has("instantiationServices")) {
			return;
		}
		final ArrayNode iss = (ArrayNode) config.get("instantiationServices");
		for (final JsonNode service : iss) {
			final InstantiationServiceConfig isconfig = InstantiationServiceConfig
					.decorate((ObjectNode) service);
			final InstantiationService is = new InstantiationServiceBuilder()
					.withClassLoader(cl).withConfig(isconfig).build();
			is.boot();
		}
	}

	/**
	 * Load agents.
	 *
	 * @param config
	 *            the config
	 * @param cl
	 *            the custom classloader
	 */
	public static void loadAgents(final Config config, final ClassLoader cl) {
		if (!config.has("agents")) {
			return;
		}
		final ArrayNode agents = (ArrayNode) config.get("agents");

		for (final JsonNode agent : agents) {
			final Agent newAgent = new AgentBuilder().withClassLoader(cl)
					.withConfig((ObjectNode) agent).build();
			if (newAgent != null) {
				LOG.info("Created agent:" + newAgent.getId());
			} else {
				LOG.warning("Failed to create agent:"
						+ (agent.has("id") ? agent.get("id").asText()
								: "unknown"));
			}
		}
	}
}
