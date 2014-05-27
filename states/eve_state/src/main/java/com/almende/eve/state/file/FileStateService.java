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
import java.util.logging.Level;
import java.util.logging.Logger;

import com.almende.eve.capabilities.Capability;
import com.almende.eve.capabilities.handler.Handler;
import com.almende.eve.state.State;
import com.almende.eve.state.StateService;
import com.almende.util.TypeUtil;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * A factory for creating FileState objects.
 */
public class FileStateService implements StateService {
	private static final Logger						LOG			= Logger.getLogger(FileStateService.class
																		.getSimpleName());
	private String									path		= null;
	private Boolean									json		= true;
	private Boolean									multilevel	= false;
	private final Map<String, State>				states		= new HashMap<String, State>();
	private static Map<String, FileStateService>	instances	= new ConcurrentHashMap<String, FileStateService>();
	
	/**
	 * This constructor is called when constructed by the AgentHost.
	 * 
	 * @param params
	 *            the params
	 */
	public FileStateService(final ObjectNode params) {
		final FileStateConfig config = new FileStateConfig(params);
		json = config.getJson();
		setPath(config.getPath());
		
		if (params.has("multilevel")) {
			multilevel = params.get("multilevel").asBoolean();
		}
	}
	
	/**
	 * Instantiates a new file state factory.
	 * 
	 * @param path
	 *            the path
	 * @param json
	 *            the json
	 * @param multilevel
	 *            Whether the path contains a subdirectory for agent categories.
	 */
	public FileStateService(final String path, final Boolean json,
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
	public FileStateService(final String path, final Boolean json) {
		this.json = json;
		setPath(path);
	}
	
	/**
	 * Instantiates a new file state factory.
	 * 
	 * @param path
	 *            the path
	 */
	public FileStateService(final String path) {
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
					+ "configuration. Using the default path '" + path + "'");
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
	
	/**
	 * Get state with given id. Will return null if not found
	 * 
	 * @param agentId
	 *            the agent id
	 * @param json
	 *            the json
	 * @param params
	 *            the params
	 * @return state
	 */
	public State get(final String agentId, final boolean json,
			final ObjectNode params) {
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
	public synchronized State create(final String agentId, final boolean json,
			final ObjectNode params) throws IOException {
		if (exists(agentId)) {
			throw new IllegalStateException("Cannot create state, "
					+ "state with id '" + agentId + "' already exists.");
		}
		
		// store the new (empty) file
		// TODO: it is not so nice solution to create an empty file to mark the
		// state as created.
		final String filename = getFilename(agentId);
		final File file = new File(filename);
		file.createNewFile();
		
		State state = null;
		// instantiate the state
		if (json) {
			state = new ConcurrentJsonFileState(agentId, filename, this, params);
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
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		final Map<String, Object> data = new HashMap<String, Object>();
		data.put("class", this.getClass().getName());
		data.put("path", path);
		return data.toString();
	}
	
	/**
	 * Gets the instance by params.
	 * 
	 * @param params
	 *            the params
	 * @return the instance by params
	 */
	public static FileStateService getInstanceByParams(final ObjectNode params) {
		
		final FileStateConfig config = new FileStateConfig(params);
		final String key = config.getPath();
		
		if (instances.containsKey(key)) {
			return instances.get(key);
		} else {
			synchronized (instances) {
				if (!instances.containsKey(key)) {
					final FileStateService result = new FileStateService(params);
					if (result != null) {
						instances.put(key, result);
					}
				}
				return instances.get(key);
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.almende.eve.capabilities.CapabilityService#get(com.fasterxml.jackson.
	 * databind
	 * .JsonNode, java.lang.invoke.MethodHandle, java.lang.Class)
	 */
	@Override
	public <T extends Capability, V> T get(final ObjectNode params,
			final Handler<V> handle, final Class<T> type) {
		final FileStateConfig config = new FileStateConfig(params);
		
		final String id = config.getId();
		
		synchronized (this) {
			if (exists(id)) {
				return TypeUtil.inject(get(id, json, params), type);
			} else {
				try {
					return TypeUtil.inject(create(id, json, params), type);
				} catch (final IOException e) {
					LOG.log(Level.WARNING, "Couldn't create state file", e);
				}
			}
		}
		return null;
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
}
