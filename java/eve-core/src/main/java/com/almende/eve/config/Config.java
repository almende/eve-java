/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The Class Config.
 */
public class Config implements EveConfig {
	// TODO: https://github.com/mojombo/toml
	private static final Logger					LOG					= Logger.getLogger(Config.class
																			.getCanonicalName());
	private static final String					ENVIRONMENTPATH[]	= new String[] {
			"com.google.appengine.runtime.environment",
			"com.almende.eve.runtime.environment"					};
	private static String						environment			= null;
	private static ThreadFactory				threadFactory		= Executors
																			.defaultThreadFactory();
	
	private static EveConfig					config				= null;
	
	/*
	 * Several classname maps for configuration conveniency:
	 */
	private static final Map<String, String>	LABELS				= new HashMap<String, String>();
	static {
		LABELS.put("couchdbstatefactory",
				"com.almende.eve.state.couchdb.CouchDBStateFactory");
		LABELS.put("filestatefactory", "com.almende.eve.state.FileStateFactory");
		LABELS.put("memorystatefactory",
				"com.almende.eve.state.MemoryStateFactory");
		LABELS.put("datastorestatefactory",
				"com.almende.eve.state.google.DatastoreStateFactory");
		LABELS.put("runnableschedulerfactory",
				"com.almende.eve.scheduler.RunnableSchedulerFactory");
		LABELS.put("clockschedulerfactory",
				"com.almende.eve.scheduler.ClockSchedulerFactory");
		LABELS.put("gaeschedulerfactory",
				"com.almende.eve.scheduler.google.GaeSchedulerFactory");
		LABELS.put("xmppservice", "com.almende.eve.transport.xmpp.XmppService");
		LABELS.put("httpservice", "com.almende.eve.transport.http.HttpService");
		LABELS.put("zmqservice", "com.almende.eve.transport.zmq.ZmqService");
	}
	
	/**
	 * Instantiates a new empty config.
	 * 
	 */
	public Config() {
	}
	
	/**
	 * Load the configuration file by filename (absolute path)
	 * 
	 * @param filename
	 *            the filename
	 */
	public Config(final String filename) {
		final File file = new File(filename);
		if (!file.exists() || !file.isFile() || !file.canRead()) {
			LOG.severe("Couldn't find or read given config file at:" + filename);
			return;
		}
		LOG.info("Loading configuration file " + file.getAbsoluteFile() + "...");
		try {
			final FileInputStream in = new FileInputStream(filename);
			config = new YamlConfig(in);
			in.close();
		} catch (Exception e) {
			LOG.log(Level.SEVERE,
					"Exception while reading given config file at:" + filename,
					e);
			
		}
	}
	
	/**
	 * Instantiates a new config.
	 * 
	 * @param inputStream
	 *            the input stream
	 */
	public Config(final InputStream inputStream) {
		config = new YamlConfig(inputStream);
	}
	
	/**
	 * Load the configuration from a map.
	 * 
	 * @param mapConfig
	 *            the config
	 */
	public Config(final Map<String, Object> mapConfig) {
		config = new YamlConfig(mapConfig);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.almende.eve.config.EveConfig#get()
	 */
	@Override
	public Map<String, Object> get() {
		if (config == null){
			LOG.warning("Configuration not initialized, returning null.");
			return null;
		}
		return config.get();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.almende.eve.config.YamlConfig#get(java.lang.String[])
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T> T get(final String... params) {
		if (config == null){
			LOG.warning("Configuration not initialized, returning null.");
			return null;
		}
		final ArrayList<String> envParams = new ArrayList<String>(
				params.length + 2);
		envParams.add("environment");
		envParams.add(getEnvironment());
		envParams.addAll(Arrays.asList(params));
		T result = config.get(envParams.toArray(new String[0]));
		if (result == null) {
			result = config.get(params);
		}
		
		if (result != null && String.class.isAssignableFrom(result.getClass())) {
			result = (T) map((String) result);
		}
		return result;
	}
	
	/**
	 * Map.
	 * 
	 * @param result
	 *            the result
	 * @return the string
	 */
	public static String map(String result) {
		if (LABELS.containsKey(result.toLowerCase())) {
			result = LABELS.get(result.toLowerCase());
		}
		return result;
	}
	
	/**
	 * Gets the environment.
	 * 
	 * @return the environment
	 */
	public static String getEnvironment() {
		if (environment == null) {
			for (final String path : ENVIRONMENTPATH) {
				environment = System.getProperty(path);
				if (environment != null) {
					LOG.info("Current environment: '" + environment
							+ "' (read from path '" + path + "')");
					break;
				}
			}
			
			if (environment == null) {
				// no environment variable found. Fall back to "Production"
				environment = "Production";
				
				String msg = "No environment variable found. "
						+ "Environment set to '" + environment
						+ "'. Checked paths: ";
				for (final String path : ENVIRONMENTPATH) {
					msg += path + ", ";
				}
				LOG.warning(msg);
			}
		}
		
		return environment;
	}
	
	/**
	 * Sets the environment.
	 * 
	 * @param env
	 *            the new environment
	 */
	public static final void setEnvironment(final String env) {
		environment = env;
	}
	
	/**
	 * @return
	 */
	public static ThreadFactory getThreadFactory() {
		return threadFactory;
	}
	
	/**
	 * @param factory
	 */
	public static void setThreadFactory(ThreadFactory factory) {
		threadFactory = factory;
	}
	
}
