/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.capabilities;

import com.almende.eve.capabilities.handler.Handler;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Interface CapabilityBuilderInterface.
 *
 * @param <T>
 *            the generic type
 */
public interface CapabilityBuilderInterface<T extends Capability> {

	/**
	 * With config.
	 *
	 * @param params
	 *            the params
	 * @return the capability builder interface
	 */
	CapabilityBuilderInterface<T> withConfig(final ObjectNode params);

	/**
	 * With handle.
	 *
	 * @param handle
	 *            the handle
	 * @return the capability builder interface
	 */
	CapabilityBuilderInterface<T> withHandle(final Handler<?> handle);

	/**
	 * With class loader.
	 *
	 * @param cl
	 *            the cl
	 * @return the capability builder interface
	 */
	CapabilityBuilderInterface<T> withClassLoader(final ClassLoader cl);

	/**
	 * Builds the.
	 *
	 * @return the t
	 */
	T build();
	
}
