/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.protocol.jsonrpc;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.almende.eve.protocol.auth.Authorizor;
import com.almende.eve.protocol.jsonrpc.NamespaceUtil.CallTuple;
import com.almende.eve.protocol.jsonrpc.annotation.Access;
import com.almende.eve.protocol.jsonrpc.annotation.AccessType;
import com.almende.eve.protocol.jsonrpc.annotation.Name;
import com.almende.eve.protocol.jsonrpc.annotation.Optional;
import com.almende.eve.protocol.jsonrpc.annotation.RequestId;
import com.almende.eve.protocol.jsonrpc.annotation.Sender;
import com.almende.eve.protocol.jsonrpc.formats.JSONRPCException;
import com.almende.eve.protocol.jsonrpc.formats.JSONRequest;
import com.almende.eve.protocol.jsonrpc.formats.JSONResponse;
import com.almende.util.AnnotationUtil;
import com.almende.util.AnnotationUtil.AnnotatedMethod;
import com.almende.util.AnnotationUtil.AnnotatedParam;
import com.almende.util.AnnotationUtil.CachedAnnotation;
import com.almende.util.Defines;
import com.almende.util.TypeUtil;
import com.almende.util.URIUtil;
import com.almende.util.jackson.JOM;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class JSONRPC.
 */
public final class JSONRpc {
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
	 * @param senderUrl
	 *            the sender url
	 * @param auth
	 *            the auth
	 * @return the string
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static String invoke(final Object destination, final String request,
			final URI senderUrl, final Authorizor auth) throws IOException {
		JSONRequest jsonRequest = null;
		JSONResponse jsonResponse = null;
		try {
			jsonRequest = new JSONRequest(request);
			jsonResponse = invoke(destination, jsonRequest, senderUrl, auth);
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
	 * @param senderUrl
	 *            the sender url
	 * @param auth
	 *            the auth
	 * @return the jSON response
	 */
	public static JSONResponse invoke(final Object destination,
			final JSONRequest request, final URI senderUrl,
			final Authorizor auth) {
		JSONResponse resp = null;
		final JsonNode id = request.getId();
		if (id != null && !id.isNull()) {
			resp = new JSONResponse(id, null);
		}
		try {
			final CallTuple tuple = NamespaceUtil.get(destination,
					request.getMethod());

			final Object realDest = tuple.getDestination();
			final AnnotatedMethod annotatedMethod = tuple.getMethod();
			if (!isAvailable(annotatedMethod, realDest, senderUrl, auth)) {
				throw new JSONRPCException(
						JSONRPCException.CODE.METHOD_NOT_FOUND,
						"Method '"
								+ request.getMethod()
								+ "' not found. The method does not exist or you are not authorized.");
			}
			final MethodHandle methodHandle = annotatedMethod.getMethodHandle();
			final Method method = annotatedMethod.getActualMethod();

			Object result = null;

			if (Defines.HASMETHODHANDLES) {
				final Object[] params = castParams(realDest,
						request.getParams(), annotatedMethod.getParams(),
						senderUrl, request.getId());
				if (annotatedMethod.isVoid()) {
					methodHandle.invokeExact(params);
				} else {
					result = methodHandle.invokeExact(params);
				}
			} else {
				final Object[] params = castParams(request.getParams(),
						annotatedMethod.getParams(), senderUrl, request.getId());
				result = method.invoke(realDest, params);
			}
			if (resp != null) {
				if (result == null) {
					result = JOM.createNullNode();
				}
				resp.setResult(result);
			}
		} catch (final JSONRPCException err) {
			if (resp != null) {
				resp.setError(err);
			}
		} catch (final Throwable err) {
			final Throwable cause = err.getCause();
			if (cause instanceof JSONRPCException) {
				if (resp != null) {
					resp.setError((JSONRPCException) cause);
				}
			} else {
				if (err instanceof InvocationTargetException && cause != null) {
					LOG.log(Level.WARNING,
							"Exception raised, returning its cause as JSONRPCException. Request:"
									+ request, cause);

					final JSONRPCException jsonError = new JSONRPCException(
							JSONRPCException.CODE.INTERNAL_ERROR,
							getMessage(cause), cause);
					jsonError.setData(cause);
					if (resp != null) {
						resp.setError(jsonError);
					}
				} else {
					LOG.log(Level.WARNING,
							"Exception raised, returning it as JSONRPCException. Request:"
									+ request, err);

					final JSONRPCException jsonError = new JSONRPCException(
							JSONRPCException.CODE.INTERNAL_ERROR,
							getMessage(err), err);
					jsonError.setData(err);
					if (resp != null) {
						resp.setError(jsonError);
					}
				}
			}
		}
		return resp;
	}

	/**
	 * Describe all JSON-RPC methods of given class.
	 * Format:
	 * http://www.simple-is-better.org/json-rpc/jsonrpc20-schema-service-
	 * descriptor.html
	 *
	 * @param c
	 *            The class to be described
	 * @param auth
	 *            the authorizor
	 * @return the Map
	 */
	public static ObjectNode describe(final Object c, final Authorizor auth) {
		final ObjectNode methods = JOM.createObjectNode();
		if (c == null) {
			return methods;
		}
		for (final String path : NamespaceUtil.getAllMethodPaths(c)) {
			try {
				final CallTuple method = NamespaceUtil.get(c, path);
				if (isAvailable(method.getMethod(), method.getDestination(),
						URIUtil.create("local:null"), auth)) {
					final ObjectNode result = JOM.createObjectNode();
					result.put("type", "method");
					result.put("description", typeToString(method.getMethod()
							.getGenericReturnType()));
					result.set("returns", typeToJsonSchema(method.getMethod()
							.getGenericReturnType()));
					final ArrayNode params = JOM.createArrayNode();
					for (final AnnotatedParam param : method.getMethod()
							.getParams()) {
						if (param.getAnnotation(Sender.class) == null
								&& param.getAnnotation(RequestId.class) == null) {
							final ObjectNode paramData = JOM.createObjectNode();

							paramData.put("name", getName(param));
							paramData.put("description",
									typeToString(param.getGenericType()));
							paramData.set("type",
									typeToJsonSchema(param.getGenericType()));
							paramData.put("required", isRequired(param));
							params.add(paramData);
						}
					}
					result.set("params", params);
					methods.set(path, result);
				}
			} catch (Exception e) {
				LOG.log(Level.WARNING, "Failed to describe: " + path
						+ " in class:" + c.getClass().getName());
			}
		}
		return methods;
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

	private static ObjectNode typeToJsonSchema(final Type c)
			throws JsonMappingException {
		return JOM.getTypeSchema(c);
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
			final List<AnnotatedParam> annotatedParams, final URI senderUrl,
			final JsonNode requestId) {
		return castParams(null, params, annotatedParams, senderUrl, requestId);
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
			final List<AnnotatedParam> annotatedParams, final URI senderUrl,
			final JsonNode requestId) {

		switch (annotatedParams.size()) {
			case 0:
				if (realDest != null) {
					return new Object[] { realDest };
				} else {
					return new Object[0];
				}
				/* -- Unreachable, explicit no break -- */
			case 1:
				final AnnotatedParam parm = annotatedParams.get(0);
				if (parm.getType().equals(ObjectNode.class)
						&& parm.getAnnotations().isEmpty()) {
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
					final String name = getName(p);
					if (name == null) {
						CachedAnnotation a = p.getAnnotation(Sender.class);
						if (a != null) {
							// this is a systems parameter
							if (p.getType().equals(String.class)) {
								LOG.warning("Deprecated parameter usage: @Sender should now by an URI i.s.o. String");
								objects[i + offset] = senderUrl.toString();
							} else {
								objects[i + offset] = senderUrl;
							}
						} else {
							a = p.getAnnotation(RequestId.class);
							if (a != null) {
								objects[i + offset] = requestId;
							} else {
								// this is a problem
								throw new ClassCastException(
										"Name of parameter " + i
												+ " not defined");
							}
						}
					} else {
						// this is a named parameter
						if (paramsObject.has(name)) {
							objects[i + offset] = TypeUtil.inject(
									paramsObject.get(name), p.getGenericType());
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
			final Object destination, final URI senderUrl, final Authorizor auth) {

		if (method == null) {
			return false;
		}
		if (destination != null
				&& !method.getActualMethod().getDeclaringClass()
						.isAssignableFrom(destination.getClass())) {
			return false;
		}
		final int mod = method.getActualMethod().getModifiers();
		if (!(Modifier.isPublic(mod) && hasNamedParams(method))) {
			return false;
		}

		CachedAnnotation methodAccess = method.getAnnotation(Access.class);
		if (methodAccess == null) {
			methodAccess = AnnotationUtil.get(
					destination != null ? destination.getClass() : method
							.getActualMethod().getDeclaringClass())
					.getAnnotation(Access.class);
		}
		if (methodAccess == null) {
			// Default: UNAVAILABLE!
			return false;
		}
		final AccessType value = (AccessType) methodAccess.value();
		switch (value) {
			case PUBLIC:
				return true;
			case UNAVAILABLE:
				return false;
			case PRIVATE:
				return auth != null ? auth.onAccess(senderUrl,
						((Access) methodAccess.getAnnotation()).tag()) : false;
			case SELF:
				return auth != null ? auth.isSelf(senderUrl) : false;
			default:
				return false;
		}
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
	private static boolean hasNamedParams(final AnnotatedMethod method) {
		for (final AnnotatedParam param : method.getParams()) {
			CachedAnnotation a = param.getAnnotation(Name.class);
			if (a == null) {
				a = param.getAnnotation(Sender.class);
				if (a == null) {
					a = param.getAnnotation(RequestId.class);
					if (a == null) {
						return false;
					}
				}
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
		final CachedAnnotation requiredAnnotation = param
				.getAnnotation(com.almende.eve.protocol.jsonrpc.annotation.Required.class);
		if (requiredAnnotation != null) {
			required = (boolean) requiredAnnotation.value();
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
		final CachedAnnotation nameAnnotation = param.getAnnotation(Name.class);
		if (nameAnnotation != null) {
			name = (String) nameAnnotation.value();
		}
		return name;
	}

}