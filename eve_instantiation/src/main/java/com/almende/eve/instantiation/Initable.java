/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.instantiation;

import com.almende.eve.capabilities.handler.Handler;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Interface Initable. Objects implementing this interface can be
 * instatiated by the InstantiationService.
 */
public interface Initable {

	/**
	 * Init.
	 *
	 * @param params
	 *            the params
	 * @param onBoot
	 *            the onBoot flag, set to true if this wake came from
	 *            WakeService.boot(). False in all other cases.
	 */
	void init(ObjectNode params, boolean onBoot);

	/**
	 * Get Handler to this initable object
	 * 
	 * @return The Handle
	 */
	Handler<Initable> getHandler();
}
