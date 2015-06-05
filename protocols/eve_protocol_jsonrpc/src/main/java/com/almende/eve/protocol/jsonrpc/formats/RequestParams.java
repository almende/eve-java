/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.protocol.jsonrpc.formats;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

// TODO: rework the RequestParams class to something more generic
/**
 * The Class RequestParams.
 */
public class RequestParams {
	/**
	 * Map with full class path of an annotation type as key,
	 * and an arbitrary object as value.
	 */
	private final Map<Class<?>, Object>	params	= new HashMap<Class<?>, Object>(
														1);

	/**
	 * Instantiates a new request params.
	 */
	public RequestParams() {}

	/**
	 * Put.
	 * 
	 * @param annotationType
	 *            the annotation type
	 * @param value
	 *            the value
	 */
	public void put(final Class<?> annotationType, final Object value) {
		params.put(annotationType, value);
	}

	/**
	 * Gets the.
	 * 
	 * @param annotationType
	 *            the annotation type
	 * @return the object
	 */
	public Object get(final Class<?> annotationType) {
		return params.get(annotationType);
	}

	/**
	 * Gets the.
	 * 
	 * @param annotation
	 *            the annotation
	 * @return the object
	 */
	public Object get(final Annotation annotation) {
		return get(annotation.annotationType());
	}

	/**
	 * Checks for.
	 * 
	 * @param annotation
	 *            the annotation
	 * @return true, if successful
	 */
	public boolean has(final Annotation annotation) {
		return params.containsKey(annotation.annotationType());
	}

	/**
	 * Values.
	 *
	 * @return the collection
	 */
	public Collection<Class<?>> keys() {
		return params.keySet();
	}

}
