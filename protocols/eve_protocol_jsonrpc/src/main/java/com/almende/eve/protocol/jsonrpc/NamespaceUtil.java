/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.protocol.jsonrpc;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.almende.eve.protocol.jsonrpc.annotation.Namespace;
import com.almende.util.AnnotationUtil;
import com.almende.util.AnnotationUtil.AnnotatedClass;
import com.almende.util.AnnotationUtil.AnnotatedMethod;
import com.almende.util.jackson.JOM;
import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * The Class NamespaceUtil.
 */
final class NamespaceUtil {

	private static final Map<String, AnnotatedMethod[]>	CACHE		= new HashMap<String, AnnotatedMethod[]>();
	private static final NamespaceUtil					INSTANCE	= new NamespaceUtil();
	private static final Pattern						PATTERN		= Pattern
																			.compile("\\.[^.]+$");

	/**
	 * Instantiates a new namespace util.
	 */
	private NamespaceUtil() {};

	/**
	 * Gets the.
	 * 
	 * @param destination
	 *            the destination
	 * @param path
	 *            the path
	 * @return the call tuple
	 * @throws IllegalAccessException
	 *             the illegal access exception
	 * @throws InvocationTargetException
	 *             the invocation target exception
	 * @throws NoSuchMethodException
	 *             the no such method exception
	 */
	public static CallTuple get(final Object destination, final String path)
			throws IllegalAccessException, InvocationTargetException,
			NoSuchMethodException {

		return INSTANCE._get(destination, path);
	}

	/**
	 * Populate cache.
	 * 
	 * @param destination
	 *            the destination
	 * @param steps
	 *            the steps
	 * @param methods
	 *            the methods
	 * @throws IllegalAccessException
	 *             the illegal access exception
	 * @throws InvocationTargetException
	 *             the invocation target exception
	 */
	private void populateCache(final Object destination, final String steps,
			final AnnotatedMethod[] methods) throws IllegalAccessException,
			InvocationTargetException {
		final AnnotatedClass clazz = AnnotationUtil.get(destination.getClass());
		for (final AnnotatedMethod method : clazz
				.getAnnotatedMethods(Namespace.class)) {
			String namespace = method.getAnnotation(Namespace.class).value();
			final Object newDest = method.getActualMethod().invoke(destination,
					(Object[]) null);
			if (namespace.equals("*")) {
				// divert namespace labeling to referred class.
				if (newDest != null) {
					final AnnotatedClass destClazz = AnnotationUtil.get(newDest
							.getClass());
					namespace = destClazz.getAnnotation(Namespace.class)
							.value();
				} else {
					return;
				}
			}
			final String path = steps + "." + namespace;
			methods[methods.length - 1] = method;
			CACHE.put(path, Arrays.copyOf(methods, methods.length));

			// recurse:
			if (newDest != null) {
				populateCache(newDest, path,
						Arrays.copyOf(methods, methods.length + 1));
			}
		}
	}

	/**
	 * _get.
	 * 
	 * @param destination
	 *            the destination
	 * @param path
	 *            the path
	 * @return the call tuple
	 * @throws IllegalAccessException
	 *             the illegal access exception
	 * @throws InvocationTargetException
	 *             the invocation target exception
	 * @throws NoSuchMethodException
	 *             the no such method exception
	 */
	private CallTuple _get(final Object destination, final String path)
			throws IllegalAccessException, InvocationTargetException,
			NoSuchMethodException {
		final CallTuple result = new CallTuple();
		String reducedPath = "";
		String reducedMethod = path;
		if (path.indexOf('.') >= 0) {
			reducedPath = destination.getClass().getName() + "." + path;
			final Matcher matcher = PATTERN.matcher(reducedPath);
			reducedPath = matcher.replaceFirst("");
			reducedMethod = matcher.group().substring(1);
		}
		if (!CACHE.containsKey(reducedPath)) {
			final AnnotatedMethod[] methods = new AnnotatedMethod[1];
			final String newSteps = destination.getClass().getName();
			CACHE.put("", new AnnotatedMethod[0]);
			populateCache(destination, newSteps, methods);
		}
		if (!CACHE.containsKey(reducedPath)) {
			try {
				throw new IllegalStateException("Non resolveable path given:'"
						+ path + "' \n checked:"
						+ JOM.getInstance().writeValueAsString(CACHE));
			} catch (final JsonProcessingException e) {
				throw new IllegalStateException("Non resolveable path given:'"
						+ path + "' \n checked:" + CACHE);
			}
		}
		final AnnotatedMethod[] methodPath = CACHE.get(reducedPath);
		Object newDestination = destination;
		for (final AnnotatedMethod method : methodPath) {
			if (method != null) {
				newDestination = method.getActualMethod().invoke(destination,
						(Object[]) null);
			}
		}
		if (newDestination == null) {
			// Oops, namespace getter returned null pointer!
			return result;
		}
		result.setDestination(newDestination);
		final AnnotatedClass newClazz = AnnotationUtil.get(newDestination
				.getClass());
		final List<AnnotatedMethod> methods = newClazz
				.getMethods(reducedMethod);
		if (!methods.isEmpty()) {
			result.setMethod(methods.get(0));
		}
		return result;
	}

	/**
	 * The Class CallTuple.
	 */
	public class CallTuple {

		/** The destination. */
		private Object			destination;

		/** The method name. */
		private AnnotatedMethod	method;

		/**
		 * Gets the destination.
		 * 
		 * @return the destination
		 */
		public Object getDestination() {
			return destination;
		}

		/**
		 * Sets the destination.
		 * 
		 * @param destination
		 *            the new destination
		 */
		public void setDestination(final Object destination) {
			this.destination = destination;
		}

		/**
		 * Gets the method name.
		 * 
		 * @return the method name
		 */
		public AnnotatedMethod getMethod() {
			return method;
		}

		/**
		 * Sets the method name.
		 * 
		 * @param method
		 *            The method
		 */
		public void setMethod(final AnnotatedMethod method) {
			this.method = method;
		}
	}
}
