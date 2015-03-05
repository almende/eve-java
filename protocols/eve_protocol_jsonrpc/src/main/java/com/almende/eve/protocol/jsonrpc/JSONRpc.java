/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.protocol.jsonrpc;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.almende.eve.protocol.auth.Authorizor;
import com.almende.eve.protocol.jsonrpc.NamespaceUtil.CallTuple;
import com.almende.eve.protocol.jsonrpc.annotation.Access;
import com.almende.eve.protocol.jsonrpc.annotation.AccessType;
import com.almende.eve.protocol.jsonrpc.annotation.Name;
import com.almende.eve.protocol.jsonrpc.annotation.Namespace;
import com.almende.eve.protocol.jsonrpc.annotation.Optional;
import com.almende.eve.protocol.jsonrpc.annotation.Sender;
import com.almende.eve.protocol.jsonrpc.formats.JSONRPCException;
import com.almende.eve.protocol.jsonrpc.formats.JSONRequest;
import com.almende.eve.protocol.jsonrpc.formats.JSONResponse;
import com.almende.eve.protocol.jsonrpc.formats.RequestParams;
import com.almende.util.AnnotationUtil;
import com.almende.util.AnnotationUtil.AnnotatedClass;
import com.almende.util.AnnotationUtil.AnnotatedMethod;
import com.almende.util.AnnotationUtil.AnnotatedParam;
import com.almende.util.Defines;
import com.almende.util.TypeUtil;
import com.almende.util.jackson.JOM;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class JSONRPC.
 */
final public class JSONRpc {
	private static final Logger	LOG	= Logger.getLogger(JSONRpc.class.getName());

	static {
		if (Defines.HASMETHODHANDLES) {
			LOG.log(Level.FINE, "Using MethodHandle i.s.o. plain reflection!");
		} else {
			LOG.log(Level.FINE, "Using plain reflection i.s.o. MethodHandle!");
		}
	}

	/**
	 * Instantiates a new jsonrpc.
	 */
	private JSONRpc() {}

	// TODO: implement JSONRPC 2.0 Batch
	/**
	 * Invoke a method on an object.
	 * 
	 * @param destination
	 *            the destination
	 * @param request
	 *            A request in JSON-RPC format
	 * @param auth
	 *            the auth
	 * @return the string
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static String invoke(final Object destination, final String request,
			final Authorizor auth) throws IOException {
		return invoke(destination, request, null, auth);
	}

	/**
	 * Invoke a method on an object.
	 * 
	 * @param destination
	 *            the destination
	 * @param request
	 *            A request in JSON-RPC format
	 * @param requestParams
	 *            Optional request parameters
	 * @param auth
	 *            the auth
	 * @return the string
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static String invoke(final Object destination, final String request,
			final RequestParams requestParams, final Authorizor auth)
			throws IOException {
		JSONRequest jsonRequest = null;
		JSONResponse jsonResponse = null;
		try {
			jsonRequest = new JSONRequest(request);
			jsonResponse = invoke(destination, jsonRequest, requestParams, auth);
		} catch (final JSONRPCException err) {
			jsonResponse = new JSONResponse(err);
		}

		return jsonResponse.toString();
	}

	/**
	 * Invoke a method on an object.
	 * 
	 * @param destination
	 *            destination url
	 * @param request
	 *            the request
	 * @param auth
	 *            the auth
	 * @return the jSON response
	 */
	public static JSONResponse invoke(final Object destination,
			final JSONRequest request, final Authorizor auth) {
		return invoke(destination, request, null, auth);
	}

	/**
	 * Invoke a method on an object.
	 * 
	 * @param destination
	 *            the destination
	 * @param request
	 *            A request in JSON-RPC format
	 * @param requestParams
	 *            Optional request parameters
	 * @param auth
	 *            the auth
	 * @return the jSON response
	 */
	public static JSONResponse invoke(final Object destination,
			final JSONRequest request, final RequestParams requestParams,
			final Authorizor auth) {
		final JSONResponse resp = new JSONResponse(request.getId(), null);
		try {
			final CallTuple tuple = NamespaceUtil.get(destination,
					request.getMethod());

			final Object realDest = tuple.getDestination();
			final AnnotatedMethod annotatedMethod = tuple.getMethod();
			if (!isAvailable(annotatedMethod, realDest, requestParams, auth)) {
				throw new JSONRPCException(
						JSONRPCException.CODE.METHOD_NOT_FOUND,
						"Method '"
								+ request.getMethod()
								+ "' not found. The method does not exist or you are not authorized.");
			}

			final MethodHandle methodHandle = annotatedMethod.getMethodHandle();
			final Method method = annotatedMethod.getActualMethod();

			Object result;
			if (Defines.HASMETHODHANDLES) {
				final Object[] params = castParams(realDest,
						request.getParams(), annotatedMethod.getParams(),
						requestParams);
				result = methodHandle.invokeExact(params);
			} else {
				final Object[] params = castParams(request.getParams(),
						annotatedMethod.getParams(), requestParams);
				result = method.invoke(realDest, params);
			}
			if (result == null) {
				result = JOM.createNullNode();
			}
			resp.setResult(result);
		} catch (final JSONRPCException err) {
			resp.setError(err);
		} catch (final Throwable err) {
			final Throwable cause = err.getCause();
			if (cause instanceof JSONRPCException) {
				resp.setError((JSONRPCException) cause);
			} else {
				if (err instanceof InvocationTargetException && cause != null) {
					LOG.log(Level.WARNING,
							"Exception raised, returning its cause as JSONRPCException. Request:"
									+ request, cause);

					final JSONRPCException jsonError = new JSONRPCException(
							JSONRPCException.CODE.INTERNAL_ERROR,
							getMessage(cause), cause);
					jsonError.setData(cause);
					resp.setError(jsonError);
				} else {
					LOG.log(Level.WARNING,
							"Exception raised, returning it as JSONRPCException. Request:"
									+ request, err);

					final JSONRPCException jsonError = new JSONRPCException(
							JSONRPCException.CODE.INTERNAL_ERROR,
							getMessage(err), err);
					jsonError.setData(err);
					resp.setError(jsonError);
				}
			}
		}
		if (resp.getId() == null || resp.getId().isNull()) {
			return null;
		} else {
			return resp;
		}
	}

	/**
	 * Validate whether the given class contains valid JSON-RPC methods. A class
	 * if valid when:<br>
	 * - There are no public methods with equal names<br>
	 * - The parameters of all public methods have the @Name annotation<br>
	 * If the class is not valid, an Exception is thrown
	 * 
	 * @param c
	 *            The class to be verified
	 * @param requestParams
	 *            optional request parameters
	 * @return errors A list with validation errors. When no problems are found,
	 *         an empty list is returned
	 */
	public static List<String> validate(final Class<?> c,
			final RequestParams requestParams) {
		final List<String> errors = new ArrayList<String>(0);
		final Set<String> methodNames = new HashSet<String>(10);

		AnnotatedClass ac = null;
		try {
			ac = AnnotationUtil.get(c);
			if (ac != null) {
				for (final AnnotatedMethod method : ac.getMethods()) {
					final boolean available = isAvailable(method, null,
							requestParams, null);
					if (available) {
						// The method name may only occur once
						final String name = method.getName();
						if (methodNames.contains(name)) {
							errors.add("Public method '"
									+ name
									+ "' is defined more than once, which is not"
									+ " allowed for JSON-RPC.");
						}
						methodNames.add(name);

						// TODO: I removed duplicate @Name check. If you reach
						// this point the function at least has named
						// parameters, due to the isAvailable() call. Should we
						// add a duplicates check to isAvailable()?
					}
				}
			}
		} catch (final Exception e) {
			LOG.log(Level.WARNING, "Problems wrapping class for annotation", e);
			errors.add("Class can't be wrapped for annotation, exception raised:"
					+ e.getLocalizedMessage());
		}
		return errors;
	}

	/**
	 * _describe.
	 * 
	 * @param c
	 *            the c
	 * @param requestParams
	 *            the request params
	 * @param namespace
	 *            the namespace
	 * @param auth
	 *            the auth
	 * @return the map
	 */
	private static Map<String, Object> _describe(final Object c,
			final RequestParams requestParams, String namespace,
			final Authorizor auth) {
		final Map<String, Object> methods = new TreeMap<String, Object>();
		try {
			if (c == null) {
				return methods;
			}
			final AnnotatedClass annotatedClass = AnnotationUtil.get(c
					.getClass());
			for (final AnnotatedMethod method : annotatedClass.getMethods()) {
				if (isAvailable(method, null, requestParams, auth)) {
					// format as JSON
					final List<Object> descParams = new ArrayList<Object>(4);
					for (final AnnotatedParam param : method.getParams()) {
						if (getRequestAnnotation(param, requestParams) == null) {
							final String name = getName(param);
							final Map<String, Object> paramData = new HashMap<String, Object>(3);
							paramData.put("name", name);
							paramData.put("type",
									typeToString(param.getGenericType()));
							paramData.put("required", isRequired(param));
							descParams.add(paramData);
						}
					}

					final Map<String, Object> result = new HashMap<String, Object>(1);
					result.put("type",
							typeToString(method.getGenericReturnType()));

					final Map<String, Object> desc = new HashMap<String, Object>(3);
					if (namespace.equals("*")) {
						namespace = annotatedClass.getAnnotation(
								Namespace.class).value();
					}
					final String methodName = namespace.equals("") ? method
							.getName() : namespace + "." + method.getName();
					desc.put("method", methodName);
					desc.put("params", descParams);
					desc.put("result", result);
					methods.put(methodName, desc);
				}
			}
			for (final AnnotatedMethod method : annotatedClass
					.getAnnotatedMethods(Namespace.class)) {
				final String innerNamespace = method.getAnnotation(
						Namespace.class).value();
				methods.putAll(_describe(
						method.getActualMethod().invoke(c, (Object[]) null),
						requestParams, innerNamespace, auth));
			}
		} catch (final Exception e) {
			LOG.log(Level.WARNING, "Failed to describe class:" + c.toString(),
					e);
			return null;
		}
		return methods;
	}

	/**
	 * Describe all JSON-RPC methods of given class.
	 * 
	 * @param c
	 *            The class to be described
	 * @param requestParams
	 *            Optional request parameters.
	 * @param auth
	 *            the authorizor
	 * @return the list
	 */
	public static List<Object> describe(final Object c,
			final RequestParams requestParams, final Authorizor auth) {
		try {
			final Map<String, Object> methods = _describe(c, requestParams, "",
					auth);

			// create a sorted array
			final TreeSet<String> methodNames = new TreeSet<String>(
					methods.keySet());
			final List<Object> sortedMethods = new ArrayList<Object>(methodNames.size());
			for (final String methodName : methodNames) {
				sortedMethods.add(methods.get(methodName));
			}
			return sortedMethods;
		} catch (final Exception e) {
			LOG.log(Level.WARNING, "Failed to describe class:" + c.toString(),
					e);
			return null;
		}
	}

	/**
	 * Get type description from a class. Returns for example "String" or
	 * "List<String>".
	 * 
	 * @param c
	 *            the c
	 * @return the string
	 */
	private static String typeToString(final Type c) {
		String s = c.toString();

		// replace full namespaces to short names
		int point = s.lastIndexOf('.');
		while (point >= 0) {
			final int angle = s.lastIndexOf('<', point);
			final int space = s.lastIndexOf(' ', point);
			final int start = Math.max(angle, space);
			s = s.substring(0, start + 1) + s.substring(point + 1);
			point = s.lastIndexOf('.');
		}

		// remove modifiers like "class blabla" or "interface blabla"
		final int space = s.indexOf(' ');
		final int angle = s.indexOf('<', point);
		if (space >= 0 && (angle < 0 || angle > space)) {
			s = s.substring(space + 1);
		}

		return s;
	}

	/**
	 * Retrieve a description of an error.
	 * 
	 * @param error
	 *            the error
	 * @return message String with the error description of the cause
	 */
	private static String getMessage(final Throwable error) {
		Throwable cause = error;
		while (cause.getCause() != null) {
			cause = cause.getCause();
		}
		return cause.toString();
	}

	/**
	 * @param params
	 * @param annotatedParams
	 * @param requestParams
	 * @return the Object[]
	 */
	private static Object[] castParams(final ObjectNode params,
			final List<AnnotatedParam> annotatedParams,
			final RequestParams requestParams) {
		return castParams(null, params, annotatedParams, requestParams);
	}

	/**
	 * Cast a JSONArray or JSONObject params to the desired paramTypes.
	 * 
	 * @param params
	 *            the params
	 * @param annotatedParams
	 *            the annotated params
	 * @param requestParams
	 *            the request params
	 * @return the object[]
	 */
	private static Object[] castParams(final Object realDest,
			final ObjectNode params,
			final List<AnnotatedParam> annotatedParams,
			final RequestParams requestParams) {

		switch (annotatedParams.size()) {
			case 0:
				if (realDest != null) {
					return new Object[] { realDest };
				} else {
					return new Object[0];
				}
				/* -- Unreachable, explicit no break -- */
			case 1:
				if (annotatedParams.get(0).getType().equals(ObjectNode.class)
						&& annotatedParams.get(0).getAnnotations().size() == 0) {
					// the method expects one parameter of type JSONObject
					// feed the params object itself to it.
					if (realDest != null) {
						return new Object[] { realDest, params };
					} else {
						return new Object[] { params };
					}
				}
				/* -- Explicit no break -- */
			default:
				final ObjectNode paramsObject = (ObjectNode) params;
				int offset = 0;
				if (realDest != null) {
					offset = 1;
				}
				final Object[] objects = new Object[annotatedParams.size()
						+ offset];
				if (realDest != null) {
					objects[0] = realDest;
				}
				for (int i = 0; i < annotatedParams.size(); i++) {
					final AnnotatedParam p = annotatedParams.get(i);

					final Annotation a = getRequestAnnotation(p, requestParams);
					if (a != null) {
						// this is a systems parameter
						objects[i + offset] = requestParams.get(a);
					} else {
						final String name = getName(p);
						if (name != null) {
							// this is a named parameter
							if (paramsObject.has(name)) {
								objects[i + offset] = TypeUtil.inject(
										paramsObject.get(name),
										p.getGenericType());
							} else {
								if (isRequired(p)) {
									throw new ClassCastException(
											"Required parameter '" + name
													+ "' missing.");
								} else if (p.getType().isPrimitive()) {
									// TODO: should this test be moved to
									// isAvailable()?
									throw new ClassCastException("Parameter '"
											+ name
											+ "' cannot be both optional and "
											+ "a primitive type ("
											+ p.getType().getSimpleName() + ")");
								} else {
									objects[i + offset] = null;
								}
							}
						} else {
							// this is a problem
							throw new ClassCastException("Name of parameter "
									+ i + " not defined");
						}
					}
				}
				return objects;
		}
	}

	/**
	 * Check whether a method is available for JSON-RPC calls. This is the case
	 * when it is public, has named parameters, and has a public or private @Access
	 * annotation
	 * 
	 * @param method
	 *            the method
	 * @param destination
	 *            the destination
	 * @param requestParams
	 *            the request params
	 * @param auth
	 *            the auth
	 * @return available
	 */
	private static boolean isAvailable(final AnnotatedMethod method,
			final Object destination, final RequestParams requestParams,
			final Authorizor auth) {

		if (method == null) {
			return false;
		}
		Access methodAccess = method.getAnnotation(Access.class);
		if (destination != null
				&& !method.getActualMethod().getDeclaringClass()
						.isAssignableFrom(destination.getClass())) {
			return false;
		}
		final int mod = method.getActualMethod().getModifiers();
		if (!(Modifier.isPublic(mod) && hasNamedParams(method, requestParams))) {
			return false;
		}

		final Access classAccess = AnnotationUtil.get(
				destination != null ? destination.getClass() : method
						.getActualMethod().getDeclaringClass()).getAnnotation(
				Access.class);
		if (methodAccess == null) {
			methodAccess = classAccess;
		}
		if (methodAccess == null) {
			// New default: UNAVAILABLE!
			return false;
		}
		if (methodAccess.value() == AccessType.UNAVAILABLE) {
			return false;
		}

		if (methodAccess.value() == AccessType.PRIVATE) {
			return auth != null ? auth.onAccess(requestParams.get(Sender.class)
					.toString(), methodAccess.tag()) : false;
		}
		if (methodAccess.value() == AccessType.SELF) {
			return auth != null ? auth.isSelf(requestParams.get(Sender.class)
					.toString()) : false;
		}
		return true;
	}

	/**
	 * Test whether a method has named parameters.
	 * 
	 * @param method
	 *            the method
	 * @param requestParams
	 *            the request params
	 * @return hasNamedParams
	 */
	private static boolean hasNamedParams(final AnnotatedMethod method,
			final RequestParams requestParams) {
		for (final AnnotatedParam param : method.getParams()) {
			boolean found = false;
			for (final Annotation a : param.getAnnotations()) {
				if (requestParams != null && requestParams.has(a)) {
					found = true;
					break;
				} else if (a instanceof Name) {
					found = true;
					break;
				}
			}

			if (!found) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Test if a parameter is required Reads the parameter annotation @Required.
	 * Returns True if the annotation is not provided.
	 * 
	 * @param param
	 *            the param
	 * @return required
	 */
	@SuppressWarnings("deprecation")
	static boolean isRequired(final AnnotatedParam param) {
		boolean required = true;
		final com.almende.eve.protocol.jsonrpc.annotation.Required requiredAnnotation = param
				.getAnnotation(com.almende.eve.protocol.jsonrpc.annotation.Required.class);
		if (requiredAnnotation != null) {
			required = requiredAnnotation.value();
		}
		if (param.getAnnotation(Optional.class) != null) {
			required = false;
		}
		return required;
	}

	/**
	 * Get the name of a parameter Reads the parameter annotation @Name. Returns
	 * null if the annotation is not provided.
	 * 
	 * @param param
	 *            the param
	 * @return name
	 */
	static String getName(final AnnotatedParam param) {
		String name = null;
		final Name nameAnnotation = param.getAnnotation(Name.class);
		if (nameAnnotation != null) {
			name = nameAnnotation.value();
		}
		return name;
	}

	/**
	 * Find a request annotation in the given parameters Returns null if no
	 * system annotation is not found.
	 * 
	 * @param param
	 *            the param
	 * @param requestParams
	 *            the request params
	 * @return annotation
	 */
	private static Annotation getRequestAnnotation(final AnnotatedParam param,
			final RequestParams requestParams) {
		for (final Annotation annotation : param.getAnnotations()) {
			if (requestParams != null && requestParams.has(annotation)) {
				return annotation;
			}
		}
		return null;
	}

	/**
	 * Check if given json object contains all fields required for a
	 * json-rpc request (id, method, params).
	 * 
	 * @param json
	 *            the json
	 * @return true, if is request
	 */
	public static boolean isRequest(final ObjectNode json) {
		return json.has("method");
	}

	/**
	 * Check if given json object contains all fields required for a
	 * json-rpc response (id, result or error).
	 * 
	 * @param json
	 *            the json
	 * @return true, if is response
	 */
	public static boolean isResponse(final ObjectNode json) {
		return (json.has("result") || json.has("error"));
	}

}