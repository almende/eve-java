/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.logging.Logger;

import org.jodah.typetools.TypeResolver;

import com.almende.util.jackson.JOM;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * The Class TypeUtil.
 * 
 * @param <T>
 *            the generic type
 */
public abstract class TypeUtil<T> {

	/**
	 * The Constant LOG.
	 */
	static final Logger			LOG		= Logger.getLogger(TypeUtil.class
												.getName());
	private final JavaType		valueType;

	/**
	 * Instantiates a new type util.
	 * 
	 * @param type
	 *            the type
	 */
	public TypeUtil(final JavaType type) {
		this.valueType = type;
	}

	/**
	 * Instantiates a new type util.
	 * 
	 * @param type
	 *            the type
	 */
	public TypeUtil(final Class<?> type) {
		this.valueType = JOM.getTypeFactory().constructType(type);
	}

	/**
	 * Usage example: <br>
	 * TypeUtil&lt;TreeSet&lt;TaskEntry>> injector = new
	 * TypeUtil&lt;TreeSet&lt;TaskEntry>>(){};<br>
	 * TreeSet&lt;TaskEntry> value = injector.inject(Treeset_with_tasks);<br>
	 * <br>
	 * Note the trivial anonymous class declaration, extending abstract class
	 * TypeUtil...
	 */
	public TypeUtil() {
		this.valueType = JOM.getTypeFactory()
				.constructType(
						((ParameterizedType) TypeResolver.resolveGenericType(
								TypeUtil.class, getClass()))
								.getActualTypeArguments()[0]);
	}

	/**
	 * Gets an instances of this TypeUtil.
	 * 
	 * @param <T>
	 *            the generic type
	 * @return the type util
	 */
	public static <T> TypeUtil<T> get() {
		return new TypeUtil<T>() {};
	}

	/**
	 * Gets an instances of this TypeUtil.
	 * 
	 * @param <T>
	 *            the generic type
	 * @param type
	 *            the type
	 * @return the type util
	 */
	public static <T> TypeUtil<T> get(final Class<T> type) {
		return new TypeUtil<T>(type) {};
	}

	/**
	 * Gets an instances of this TypeUtil.
	 * 
	 * @param <T>
	 *            the generic type
	 * @param type
	 *            the type
	 * @return the type util
	 */
	public static <T> TypeUtil<T> get(final JavaType type) {
		return new TypeUtil<T>(type) {};
	}

	/**
	 * Gets an instances of this TypeUtil.
	 * 
	 * @param <T>
	 *            the generic type
	 * @param type
	 *            the type
	 * @return the type util
	 */
	public static <T> TypeUtil<T> get(final Type type) {
		JavaType jtype = null;
		if (type instanceof TypeVariable) {
			jtype = JOM.getTypeFactory().constructType(
					((TypeVariable<?>) type).getBounds()[0]);
		} else if (type instanceof ParameterizedType) {
			jtype = JOM.getTypeFactory().constructType(
					((ParameterizedType) type).getActualTypeArguments()[0]);
		} else {
			jtype = JOM.getTypeFactory().constructType(type);
		}

		return new TypeUtil<T>(jtype) {};
	}

	/**
	 * Resolve.
	 * 
	 * @param <T>
	 *            the generic type
	 * @param target
	 *            the target
	 * @return the type util
	 */
	@SuppressWarnings("unchecked")
	public static <T> TypeUtil<T> resolve(final Object target) {
		if (target == null) {
			return new TypeUtil<T>(Void.class) {};
		}
		if (target instanceof Class<?>) {
			// Security, but price is unchecked warning....
			return new TypeUtil<T>((Class<T>) target) {};
		}
		Type gsc = target.getClass().getGenericSuperclass();
		ParameterizedType ptype = null;
		if (gsc instanceof ParameterizedType) {
			ptype = (ParameterizedType) gsc;
		} else {
			ptype = (ParameterizedType) TypeResolver.resolveGenericType(
					(Class<?>) gsc, target.getClass());
		}
		if (ptype == null) {
			gsc = target.getClass().getGenericInterfaces()[0];
			if (gsc instanceof ParameterizedType) {
				ptype = (ParameterizedType) gsc;
			}
		}
		if (ptype == null) {
			LOG.warning("Couldn't find generic type.");
			return null;
		}
		final JavaType type = JOM.getTypeFactory().constructType(
				ptype.getActualTypeArguments()[0]);
		if (type == null) {
			LOG.warning("Couldn't find generic type.");
			return null;
		}
		return new TypeUtil<T>(type) {};
	}

	/**
	 * Gets the type.
	 * 
	 * @return the {@link TypeUtil} value's type
	 */
	public Type getType() {
		return this.valueType;
	}

	/**
	 * Gets the type.
	 * 
	 * @return the {@link TypeUtil} value's type
	 */
	public JavaType getJavaType() {
		return this.valueType;
	}

	/**
	 * Inject.
	 * 
	 * @param value
	 *            the value
	 * @return the t
	 */
	public T inject(final Object value) {
		return inject(value, valueType);
	}

	/**
	 * Inject.
	 * 
	 * @param <T>
	 *            the generic type
	 * @param value
	 *            the value
	 * @param type
	 *            the type
	 * @return the t
	 */
	public static <T> T inject(final Object value, final Class<T> type) {
		return inject(value, JOM.getTypeFactory().constructType(type));
	}

	/**
	 * Inject.
	 * 
	 * @param <T>
	 *            the generic type
	 * @param value
	 *            the value
	 * @param type
	 *            the type
	 * @return the t
	 */
	public static <T> T inject(final Object value, final Type type) {
		return inject(value, JOM.getTypeFactory().constructType(type));
	}

	/**
	 * Inject.
	 * 
	 * @param <T>
	 *            the generic type
	 * @param value
	 *            the value
	 * @param fullType
	 *            the full type
	 * @return the t
	 */
	@SuppressWarnings("unchecked")
	public static <T> T inject(final Object value, final JavaType fullType) {
		if (value == null) {
			return null;
		}
		if (fullType.hasRawClass(Void.class)) {
			return null;
		}
		if (value instanceof JsonNode) {
			if (fullType.getRawClass().equals(JsonNode.class)) {
				return (T) value;
			}
			if (((JsonNode) value).isNull()) {
				return null;
			}
			try {
				return JOM.getInstance().convertValue(value, fullType);
			} catch (final Exception e) {
				final ClassCastException cce = new ClassCastException(
						"Failed to convert value:" + value + " -----> "
								+ fullType);
				cce.initCause(e);
				throw cce;
			}
		}
		if (fullType.getRawClass().isAssignableFrom(value.getClass())) {
			return (T) fullType.getRawClass().cast(value);
		} else {
			throw new ClassCastException(value.getClass().getCanonicalName()
					+ " can't be converted to: "
					+ fullType.getRawClass().getCanonicalName());
		}
	}

}
