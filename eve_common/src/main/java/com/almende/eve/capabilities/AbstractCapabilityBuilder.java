/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.capabilities;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.almende.eve.capabilities.handler.Handler;
import com.almende.util.ClassUtil;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class CapabilityBuilder.
 *
 * @param <T>
 *            the generic type
 */
public abstract class AbstractCapabilityBuilder<T extends Capability> {
	private static final Logger	LOG		= Logger.getLogger(AbstractCapabilityBuilder.class
												.getName());
	private ClassLoader			cl		= getClass().getClassLoader();

	private ObjectNode			params	= null;
	private Handler<?>			handle	= null;

	/**
	 * With config.
	 * 
	 * @param params
	 *            the params
	 * @return the capability builder
	 */
	public AbstractCapabilityBuilder<T> withConfig(final ObjectNode params) {
		this.params = params;
		return this;
	}

	/**
	 * With handle.
	 * 
	 * @param handle
	 *            the handle
	 * @return the capability builder
	 */
	public AbstractCapabilityBuilder<T> withHandle(final Handler<?> handle) {
		this.handle = handle;
		return this;
	}

	/**
	 * With class loader.
	 *
	 * @param cl
	 *            the cl
	 * @return the abstract capability builder
	 */
	public AbstractCapabilityBuilder<T> withClassLoader(final ClassLoader cl) {
		if (cl != null) {
			this.cl = cl;
		}
		return this;
	}

	/**
	 * Builds or retrieves the Capability.
	 * 
	 * @return the t
	 */
	public T build() {
		final Config config = new Config(params).compress();
		final String className = config.getClassName();
		if (className != null) {
			try {
				final Class<?> clazz = Class.forName(className, true, cl);
				if (ClassUtil.hasSuperClass(clazz,
						AbstractCapabilityBuilder.class)) {
					@SuppressWarnings("unchecked")
					final AbstractCapabilityBuilder<T> instance = (AbstractCapabilityBuilder<T>) clazz
							.newInstance();
					return instance.withClassLoader(cl).withConfig(config)
							.withHandle(handle).build();
				} else {
					LOG.log(Level.WARNING,
							className
									+ " is not a CapabilityBuilder, which is required.");
				}
			} catch (final ClassNotFoundException e) {
				LOG.log(Level.WARNING, "Couldn't find class:" + className, e);
			} catch (InstantiationException e) {
				LOG.log(Level.WARNING, "Couldn't instantiate class:"
						+ className, e);
			} catch (final SecurityException e) {
				LOG.log(Level.WARNING, "Couldn't access class:" + className
						+ " methods", e);
			} catch (final IllegalAccessException e) {
				LOG.log(Level.WARNING, "Couldn't access class:" + className
						+ " methods", e);
			} catch (final IllegalArgumentException e) {
				LOG.log(Level.WARNING, "Method of class:" + className
						+ " has incorrect arguments", e);
			}
		} else {
			LOG.warning("Parameter 'class' is required!");
		}
		return null;
	}

	/**
	 * Gets the params.
	 *
	 * @return the params
	 */
	protected final ObjectNode getParams() {
		return params;
	}

	/**
	 * Gets the handle.
	 *
	 * @return the handle
	 */
	protected final Handler<?> getHandle() {
		return handle;
	}

}
