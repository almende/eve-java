/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.capabilities;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.almende.eve.capabilities.handler.Handler;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * The Class CapabilityFactory.
 */
public abstract class CapabilityFactory {
	private static final Logger	LOG	= Logger.getLogger(CapabilityFactory.class
											.getName());
	
	/**
	 * Gets an instance of the provided capability.
	 * 
	 * @param <T>
	 *            the generic type
	 * @param <V>
	 *            the value type
	 * @param params
	 *            the params
	 * @param handle
	 *            the handle (Callback method
	 * @param type
	 *            the capability type (e.g. State, Transport, etc.)
	 * @return the t
	 */
	public static <T, V> T get(final JsonNode params, final Handler<V> handle,
			final Class<T> type) {
		if (params.has("class")) {
			final String className = params.get("class").asText();
			try {
				final Class<?> clazz = Class.forName(className);
				final Method method = clazz.getMethod("getInstanceByParams",
						JsonNode.class);
				final Capability instance = (Capability) method.invoke(null,
						params);
				return instance.get(params, handle, type);
			} catch (final ClassNotFoundException e) {
				LOG.log(Level.WARNING, "Couldn't find class:" + className, e);
			} catch (final NoSuchMethodException e) {
				LOG.log(Level.WARNING, "Class:" + className
						+ " doesn't extends CapabilityFactory.", e);
			} catch (final SecurityException e) {
				LOG.log(Level.WARNING, "Couldn't access class:" + className
						+ " methods", e);
			} catch (final IllegalAccessException e) {
				LOG.log(Level.WARNING, "Couldn't access class:" + className
						+ " methods", e);
			} catch (final IllegalArgumentException e) {
				LOG.log(Level.WARNING, "Method of class:" + className
						+ " has incorrect arguments", e);
			} catch (final InvocationTargetException e) {
				LOG.log(Level.WARNING, "Couldn't access class:" + className
						+ " methods", e);
			}
		} else {
			LOG.warning("Parameter 'class' is required!");
		}
		return null;
	}
}
