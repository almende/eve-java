/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.state.file;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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
		private final Map<String, State>	states		= new HashMap<String, State>();
		
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
		private synchronized void setPath(String path) {
			if (path == null) {
				path = ".eveagents";
				LOG.warning("Config parameter 'path' missing in State "
						+ "configuration. Using the default path '" + path
						+ "'");
			}
			if (!path.endsWith("/")) {
				path += "/";
			}
			this.path = path;
			
			// make the directory
			final File file = new File(path);
			if (!file.exists() && !file.mkdir()) {
				LOG.severe("Could not create State folder!");
				throw new IllegalStateException();
			}
			
			// log info
			String info = "Agents will be stored in ";
			try {
				info += file.getCanonicalPath();
			} catch (final IOException e) {
				info += path;
			}
			LOG.info(info
					+ ". "
					+ (json ? "(stored in JSON format)"
							: "(stored in JavaObject format)"));
		}
		
		public State get(final ObjectNode params) {
			final FileStateConfig config = new FileStateConfig(params);
			final String agentId = config.getId();

			State state = null;
			if (exists(agentId)) {
				if (states.containsKey(agentId)) {
					state = states.get(agentId);
				} else {
					if (json) {
						state = new ConcurrentJsonFileState(agentId,
								getFilename(agentId), this, params);
					} else {
						state = new ConcurrentSerializableFileState(agentId,
								getFilename(agentId), this, params);
					}
					states.put(agentId, state);
				}
			}
			return state;
		}
		
		/**
		 * Create a state with given id. Will throw an exception when already.
		 * existing.
		 * 
		 * @param agentId
		 *            the agent id
		 * @param json
		 *            the json
		 * @param params
		 *            the params
		 * @return state
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		public synchronized State create(final String agentId,
				final boolean json, final ObjectNode params) throws IOException {
			if (exists(agentId)) {
				throw new IllegalStateException("Cannot create state, "
						+ "state with id '" + agentId + "' already exists.");
			}
			
			// store the new (empty) file
			// TODO: it is not so nice solution to create an empty file to mark
			// the
			// state as created.
			final String filename = getFilename(agentId);
			final File file = new File(filename);
			file.createNewFile();
			
			State state = null;
			// instantiate the state
			if (json) {
				state = new ConcurrentJsonFileState(agentId, filename, this,
						params);
			} else {
				state = new ConcurrentSerializableFileState(agentId, filename,
						this, params);
			}
			states.put(agentId, state);
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
