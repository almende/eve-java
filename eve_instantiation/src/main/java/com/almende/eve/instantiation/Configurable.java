/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.instantiation;

import com.almende.eve.capabilities.handler.Handler;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Interface Configurable. Objects implementing this interface can be
 * instantiated by the InstantiationService.
 */
public interface Configurable {

	/**
	 * Sets the config.
	 *
	 * @param config
	 *            the new config
	 */
	void setConfig(final ObjectNode config);
	
	/**
	 * Get Handler to this initable object.
	 *
	 * @return The Handle
	 */
	Handler<Configurable> getHandler();
}
