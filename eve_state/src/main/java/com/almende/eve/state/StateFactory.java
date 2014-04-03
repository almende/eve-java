package com.almende.eve.state;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.almende.util.ClassUtil;

/**
 * 
 * A utility class that can initialize StateServices and provides a default StateService.
 * @author ludo
 *
 */
public class StateFactory implements StateService {
	private static final Logger LOG = Logger.getLogger(StateService.class.getName());
	private static StateService defaultStateService = null;
	
	
	/**
	 * @param className
	 * @param params
	 */
	public static void initDefault(final String className, final Map<String,Object> params){
		defaultStateService = getStateService(className,params);
	}
	
	/**
	 * @param className
	 * @param params
	 * @return Stateservice
	 */
	public static StateService getStateService(final String className, final Map<String,Object> params){
		StateService result = null;
		if (className == null) {
			throw new IllegalArgumentException("ClassName for state may not be empty.");
		}
		try {
			// get the class
			final Class<?> stateClass = Class.forName(className);
			if (!ClassUtil.hasInterface(stateClass, StateFactory.class)) {
				throw new IllegalArgumentException("State factory class "
						+ stateClass.getName() + " must extend "
						+ State.class.getName());
			}
			result = (StateFactory) stateClass.getConstructor(Map.class)
					.newInstance(params);
			
			LOG.info("Initialized state factory: " + result.toString());
		} catch (final Exception e) {
			LOG.log(Level.WARNING, "", e);
		}
		return result;
	}

	@Override
	public State get(String agentId) {
		if (defaultStateService == null){
			throw new IllegalStateException("Default State Service not set, please call initDefault() first!");
		}
		return defaultStateService.get(agentId);
	}

	@Override
	public State create(String agentId) throws IOException {
		if (defaultStateService == null){
			throw new IllegalStateException("Default State Service not set, please call initDefault() first!");
		}
		return defaultStateService.create(agentId);
	}

	@Override
	public void delete(String agentId) {
		if (defaultStateService == null){
			throw new IllegalStateException("Default State Service not set, please call initDefault() first!");
		}
		defaultStateService.delete(agentId);
	}

	@Override
	public boolean exists(String agentId) {
		if (defaultStateService == null){
			throw new IllegalStateException("Default State Service not set, please call initDefault() first!");
		}
		return defaultStateService.exists(agentId);
	}

	@Override
	public Iterator<String> getAllAgentIds() {
		if (defaultStateService == null){
			throw new IllegalStateException("Default State Service not set, please call initDefault() first!");
		}
		return defaultStateService.getAllAgentIds();
	}
}
