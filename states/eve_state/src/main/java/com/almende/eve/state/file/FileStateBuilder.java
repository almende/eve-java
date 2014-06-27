/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.state.file;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.almende.eve.capabilities.AbstractCapabilityBuilder;
import com.almende.eve.state.State;
import com.almende.eve.state.StateService;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * A factory for creating FileState objects.
 */
public class FileStateBuilder extends AbstractCapabilityBuilder<State> {
	private static final Logger						LOG			= Logger.getLogger(FileStateBuilder.class
																		.getSimpleName());
	private static Map<String, FileStateProvider>	instances	= new ConcurrentHashMap<String, FileStateProvider>();
	
	@Override
	public State build() {
		final FileStateProvider provider = getInstanceByParams(getParams());
		if (provider != null) {
			return provider.get(getParams());
		} else {
			LOG.warning("Couldn't get FileStateProvider!");
			return null;
		}
	}
	
	/**
	 * Gets the instance by params.
	 * 
	 * @param params
	 *            the params
	 * @return the instance by params
	 */
	private FileStateProvider getInstanceByParams(final ObjectNode params) {
		
		final FileStateConfig config = new FileStateConfig(params);
		final String key = config.getPath();
		
		if (instances.containsKey(key)) {
			return instances.get(key);
		} else {
			synchronized (instances) {
				if (!instances.containsKey(key)) {
					final FileStateProvider result = new FileStateProvider(
							params);
					if (result != null) {
						instances.put(key, result);
					}
				}
				return instances.get(key);
			}
		}
	}
	
	class FileStateProvider implements StateService {
		private String						path		= null;
		private Boolean						json		= true;
		private Boolean						multilevel	= false;
		private final Map<String, WeakReference<State>>	states		= new ConcurrentHashMap<String, WeakReference<State>>();
		
		/**
		 * Instantiates a new file state provider.
		 * 
		 * @param params
		 *            the params
		 */
		public FileStateProvider(final ObjectNode params) {
			final FileStateConfig config = new FileStateConfig(params);
			json = config.getJson();
			setPath(config.getPath());
			
			if (params.has("multilevel")) {
				multilevel = params.get("multilevel").asBoolean();
			}
		}
		
		/**
		 * Instantiates a new file state provider.
		 * 
		 * @param path
		 *            the path
		 * @param json
		 *            the json
		 * @param multilevel
		 *            Whether the path contains a subdirectory for agent
		 *            categories.
		 */
		public FileStateProvider(final String path, final Boolean json,
				final Boolean multilevel) {
			this.json = json;
			this.multilevel = multilevel;
			setPath(path);
		}
		
		/**
		 * Instantiates a new file state factory.
		 * 
		 * @param path
		 *            the path
		 * @param json
		 *            the json
		 */
		public FileStateProvider(final String path, final Boolean json) {
			this.json = json;
			setPath(path);
		}
		
		/**
		 * Instantiates a new file state factory.
		 * 
		 * @param path
		 *            the path
		 */
		public FileStateProvider(final String path) {
			this(path, false);
		}
		
		/**
		 * Set the path where the agents data will be stored.
		 * 
		 * @param path
		 *            the new path
		 */
		private synchronized void setPath(final String path) {
			String actualPath = path;
			if (actualPath == null) {
				actualPath = ".eveagents";
				LOG.warning("Config parameter 'path' missing in State "
						+ "configuration. Using the default path '" + path
						+ "'");
			}
			if (!actualPath.endsWith("/")) {
				actualPath += "/";
			}
			this.path = actualPath;
			
			// make the directory
			final File file = new File(actualPath);
			if (!file.exists() && !file.mkdir()) {
				LOG.severe("Could not create State folder!");
				throw new IllegalStateException();
			}
			
			// log info
			String info = "Agents will be stored in ";
			try {
				info += file.getCanonicalPath();
			} catch (final IOException e) {
				info += actualPath;
			}
			LOG.info(info
					+ ". "
					+ (json ? "(stored in JSON format)"
							: "(stored in JavaObject format)"));
		}
		
		/**
		 * Gets the.
		 * 
		 * @param params
		 *            the params
		 * @return the state
		 */
		public State get(final ObjectNode params) {
			final FileStateConfig config = new FileStateConfig(params);
			final String agentId = config.getId();
			
			State state = null;
			try {
				if (!exists(agentId)) {
					final String filename = getFilename(agentId);
					final File file = new File(filename);
					
					file.createNewFile();
				}

				if (states.containsKey(agentId)) {
					WeakReference<State> ref = states.get(agentId);
					if (ref != null){
						state = ref.get();
					}
				}
				if (state == null){
					if (json) {
						state = new ConcurrentJsonFileState(agentId,
								getFilename(agentId), this, params);
					} else {
						state = new ConcurrentSerializableFileState(
								agentId, getFilename(agentId), this, params);
					}
					states.put(agentId, new WeakReference<State>(state));
				}
			} catch (IOException e) {
				LOG.log(Level.WARNING, "Couldn't create FileState", e);
			}
			return state;
		}
		
		/**
		 * Test if a state with given agentId exists.
		 * 
		 * @param agentId
		 *            the agent id
		 * @return true, if successful
		 */
		public boolean exists(final String agentId) {
			final File file = new File(getFilename(agentId));
			return file.exists();
		}
		
		/**
		 * Get the filename of the saved.
		 * 
		 * @param agentId
		 *            the agent id
		 * @return the filename
		 */
		private String getFilename(final String agentId) {
			
			final String apath = path != null ? path : "./";
			
			if (multilevel) {
				// try 1 level of subdirs. I need this badly, tymon
				final File folder = new File(apath);
				final File[] files = folder.listFiles();
				final List<File> totalList = Arrays.asList(files);
				for (final File file : totalList) {
					if (!file.isDirectory()) {
						continue;
					}
					final String ret = apath + file.getName() + "/" + agentId;
					if (new File(ret).exists()) {
						return ret;
					}
				}
			}
			return apath + agentId;
		}
		
		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * com.almende.eve.state.StateService#delete(com.almende.eve.state.State)
		 */
		@Override
		public void delete(final State instance) {
			final String id = instance.getId();
			final File file = new File(getFilename(id));
			if (file.exists()) {
				file.delete();
			}
			states.remove(id);
		}
		
		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			final Map<String, Object> data = new HashMap<String, Object>();
			data.put("class", this.getClass().getName());
			data.put("path", path);
			return data.toString();
		}
	}
	
}
