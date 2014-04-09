/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.capabilities;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * The Class CapabilityFactory.
 */
public abstract class CapabilityFactory {
	private static final Logger LOG = Logger.getLogger(CapabilityFactory.class.getName());
	/**
	 * Gets an instance of the provided capability
	 * 
	 * @param <T>
	 *            the generic type
	 * @param params
	 *            the params
	 * @param handle
	 *            the handle (Callback method 
	 * @param type
	 *            the capability type (e.g. State, Transport, etc.)
	 * @return the t
	 */
	public static <T> T get(JsonNode params, MethodHandle handle, Class<T> type){
		if (params.has("class")){
			String className = params.get("class").asText();
			try {
				Class<?> clazz = Class.forName(className);
				Method method = clazz.getMethod("getInstanceByParams", JsonNode.class);
				Capability instance = (Capability) method.invoke(null, params);
				return instance.get(params, handle, type);
			} catch (ClassNotFoundException e) {
				LOG.log(Level.WARNING,"Couldn't find class:"+className,e);
			} catch (NoSuchMethodException e) {
				LOG.log(Level.WARNING,"Class:"+className+" doesn't extends CapabilityFactory.",e);
			} catch (SecurityException e) {
				LOG.log(Level.WARNING,"Couldn't access class:"+className+" methods",e);
			} catch (IllegalAccessException e) {
				LOG.log(Level.WARNING,"Couldn't access class:"+className+" methods",e);
			} catch (IllegalArgumentException e) {
				LOG.log(Level.WARNING,"Method of class:"+className+" has incorrect arguments",e);
			} catch (InvocationTargetException e) {
				LOG.log(Level.WARNING,"Couldn't access class:"+className+" methods",e);
			} 
		} else {
			LOG.warning("Parameter 'class' is required!");
		}
		return null;
	}
}
