/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.state.file;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.almende.eve.state.AbstractState;
import com.almende.eve.state.file.FileStateBuilder.FileStateProvider;
import com.almende.util.jackson.JOM;
import com.almende.util.jackson.JsonNullAwareDeserializer;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class ConcurrentJsonFileState.
 * 
 * @author jos
 * @author ludo
 * @class FileState
 * 
 *        A persistent state for an Eve Agent, which stores the data on disk.
 *        Data is stored in the path provided by the configuration file.
 * 
 *        The state provides general information for the agent (about itself,
 *        the environment, and the system configuration), and the agent can
 *        store its state in the state. The state extends a standard Java
 *        Map.
 * 
 *        All operations on this FileState are thread-safe. It also provides two
 *        aditional methods: PutIfNotChanged() and PutAllIfNotChanged().
 * 
 *        Usage:<br>
 *        AgentHost factory = AgentHost.getInstance(config);<br>
 *        ConcurrentFileState state = new
 *        ConcurrentFileState("agentId",".eveagents");<br>
 *        state.put("key", "value");<br>
 *        System.out.println(state.get("key")); // "value"<br>
 */
public class ConcurrentJsonFileState extends AbstractState<JsonNode> {
	private class Lock {
		boolean	locked	= false;
	}
	
	private static final Logger				LOG			= Logger.getLogger("ConcurrentFileState");
	private String							filename	= null;
	private FileChannel						channel		= null;
	private FileLock						lock		= null;
	private InputStream						fis			= null;
	private OutputStream					fos			= null;
	private ObjectMapper					om			= null;
	private static final Map<String, Lock>	locked		= new ConcurrentHashMap<String, Lock>();
	private Map<String, JsonNode>			properties	= Collections
																.synchronizedMap(new HashMap<String, JsonNode>());
	private static final JavaType			MAPTYPE		= JOM.getTypeFactory()
																.constructMapLikeType(
																		HashMap.class,
																		String.class,
																		JsonNode.class);
	
	/**
	 * Instantiates a new concurrent json file state.
	 * 
	 * @param agentId
	 *            the agent id
	 * @param filename
	 *            the filename
	 * @param service
	 *            the service
	 * @param params
	 *            the params
	 */
	public ConcurrentJsonFileState(final String agentId, final String filename,
			final FileStateProvider service, final ObjectNode params) {
		super(agentId, service, params);
		this.filename = filename;
		om = JOM.getInstance();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#finalize()
	 */
	@Override
	public void finalize() throws Throwable {
		closeFile();
		super.finalize();
	}
	
	/**
	 * Open file.
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@SuppressWarnings("resource")
	protected void openFile() throws IOException {
		Lock llock = null;
		synchronized (locked) {
			llock = locked.get(filename);
			if (llock == null) {
				llock = new Lock();
				locked.put(filename, llock);
			}
		}
		synchronized (llock) {
			while (llock.locked) {
				try {
					llock.wait();
				} catch (final InterruptedException e) {
				}
			}
			llock.locked = true;
			
			final File file = new File(filename);
			if (!file.exists()) {
				llock.locked = false;
				llock.notifyAll();
				throw new IllegalStateException(
						"Warning: File doesn't exist (anymore):'" + filename
								+ "'");
			}
			
			channel = new RandomAccessFile(file, "rw").getChannel();
			try {
				// TODO: add support for shared locks, allowing parallel reading
				// operations.
				lock = channel.lock();
				
			} catch (final Exception e) {
				channel.close();
				channel = null;
				lock = null;
				llock.locked = false;
				llock.notifyAll();
				throw new IllegalStateException(
						"error, couldn't obtain file lock on:" + filename, e);
			}
			fis = Channels.newInputStream(channel);
			fos = Channels.newOutputStream(channel);
		}
	}
	
	/**
	 * Close file.
	 */
	protected void closeFile() {
		final Lock llock = locked.get(filename);
		if (llock == null) {
			return;
		}
		synchronized (llock) {
			
			if (lock != null && lock.isValid()) {
				try {
					lock.release();
				} catch (final IOException e) {
					LOG.log(Level.WARNING, "", e);
				}
			}
			try {
				if (fos != null) {
					fos.close();
				}
				if (fis != null) {
					fis.close();
				}
				
				if (channel != null) {
					channel.close();
				}
				
			} catch (final IOException e) {
				LOG.log(Level.WARNING, "", e);
			}
			channel = null;
			fis = null;
			fos = null;
			lock = null;
			llock.locked = false;
			llock.notifyAll();
		}
	}
	
	/**
	 * write properties to disk.
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private void write() throws IOException {
		if (channel != null) {
			channel.position(0);
		}
		om.writeValue(fos, properties);
		fos.flush();
		
		if (channel != null) {
			channel.truncate(channel.position());
		}
		
	}
	
	/**
	 * read properties from disk.
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws ClassNotFoundException
	 *             the class not found exception
	 */
	@SuppressWarnings("unchecked")
	@JsonDeserialize(using = JsonNullAwareDeserializer.class)
	private void read() throws IOException, ClassNotFoundException {
		try {
			if (channel != null) {
				channel.position(0);
			}
			properties.clear();
			properties.putAll((Map<String, JsonNode>) om
					.readValue(fis, MAPTYPE));
		} catch (final EOFException eof) {
			// empty file, new agent?
		} catch (final JsonMappingException jme) {
			// empty file, new agent?
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.almende.eve.state.State#clear()
	 */
	@Override
	public void clear() {
		try {
			openFile();
			properties.clear();
			write();
		} catch (final IllegalStateException e) {
			LOG.log(Level.WARNING,
					"Couldn't handle Statefile: " + e.getMessage(), e);
		} catch (final Exception e) {
			LOG.log(Level.WARNING, "", e);
		}
		closeFile();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.almende.eve.state.State#keySet()
	 */
	@Override
	public Set<String> keySet() {
		Set<String> result = null;
		try {
			openFile();
			read();
			result = new HashSet<String>(properties.keySet());
		} catch (final IllegalStateException e) {
			LOG.log(Level.WARNING,
					"Couldn't handle Statefile: " + e.getMessage(), e);
		} catch (final Exception e) {
			LOG.log(Level.WARNING, "", e);
		}
		closeFile();
		return result;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.almende.eve.state.State#containsKey(java.lang.String)
	 */
	@Override
	public boolean containsKey(final String key) {
		boolean result = false;
		try {
			openFile();
			read();
			result = properties.containsKey(key);
		} catch (final IllegalStateException e) {
			LOG.log(Level.WARNING,
					"Couldn't handle Statefile: " + e.getMessage(), e);
		} catch (final Exception e) {
			LOG.log(Level.WARNING, "", e);
		}
		closeFile();
		return result;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.almende.eve.state.AbstractState#get(java.lang.String)
	 */
	@Override
	@JsonIgnore
	public JsonNode get(final String key) {
		JsonNode result = NullNode.getInstance();
		try {
			openFile();
			read();
			result = properties.get(key);
		} catch (final IllegalStateException e) {
			LOG.log(Level.WARNING,
					"Couldn't handle Statefile: " + e.getMessage(), e);
		} catch (final Exception e) {
			LOG.log(Level.WARNING, "", e);
		}
		closeFile();
		return result;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.almende.eve.state.AbstractState#locPut(java.lang.String,
	 * com.fasterxml.jackson.databind.JsonNode)
	 */
	@Override
	public JsonNode locPut(final String key, JsonNode value) {
		JsonNode result = null;
		try {
			openFile();
			read();
			if (value == null) {
				value = NullNode.getInstance();
			}
			result = properties.put(key, value);
			write();
		} catch (final IllegalStateException e) {
			LOG.log(Level.WARNING,
					"Couldn't handle Statefile: " + e.getMessage(), e);
		} catch (final Exception e) {
			LOG.log(Level.WARNING, "", e);
		}
		closeFile();
		return result;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.almende.eve.state.AbstractState#locPutIfUnchanged(java.lang.String,
	 * com.fasterxml.jackson.databind.JsonNode,
	 * com.fasterxml.jackson.databind.JsonNode)
	 */
	@Override
	public boolean locPutIfUnchanged(final String key, final JsonNode newVal,
			JsonNode oldVal) {
		boolean result = false;
		try {
			openFile();
			read();
			
			JsonNode cur = NullNode.getInstance();
			if (properties.containsKey(key)) {
				cur = properties.get(key);
			}
			if (oldVal == null) {
				oldVal = NullNode.getInstance();
			}
			
			// Poor mans equality as some Numbers are compared incorrectly: e.g.
			// IntNode versus LongNode
			if (oldVal.equals(cur) || oldVal.toString().equals(cur.toString())) {
				properties.put(key, newVal);
				write();
				result = true;
			}
		} catch (final IllegalStateException e) {
			LOG.log(Level.WARNING,
					"Couldn't handle Statefile: " + e.getMessage(), e);
		} catch (final Exception e) {
			LOG.log(Level.WARNING, "", e);
			// Don't let users loop if exception is thrown. They
			// would get into a deadlock....
			result = true;
		}
		closeFile();
		return result;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.almende.eve.state.State#remove(java.lang.String)
	 */
	@Override
	public Object remove(final String key) {
		Object result = null;
		try {
			openFile();
			read();
			result = properties.remove(key);
			
			write();
		} catch (final IllegalStateException e) {
			LOG.log(Level.WARNING,
					"Couldn't handle Statefile: " + e.getMessage(), e);
		} catch (final Exception e) {
			LOG.log(Level.WARNING, "", e);
		}
		closeFile();
		return result;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.almende.eve.state.State#size()
	 */
	@Override
	public int size() {
		int result = -1;
		try {
			openFile();
			read();
			result = properties.size();
			
		} catch (final IllegalStateException e) {
			LOG.log(Level.WARNING,
					"Couldn't handle Statefile: " + e.getMessage(), e);
		} catch (final Exception e) {
			LOG.log(Level.WARNING, "", e);
		}
		closeFile();
		return result;
	}
	
}
