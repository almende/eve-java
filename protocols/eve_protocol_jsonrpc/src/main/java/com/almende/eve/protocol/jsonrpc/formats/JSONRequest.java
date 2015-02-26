/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.protocol.jsonrpc.formats;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.almende.eve.protocol.jsonrpc.annotation.Name;
import com.almende.eve.protocol.jsonrpc.annotation.Optional;
import com.almende.util.AnnotationUtil;
import com.almende.util.AnnotationUtil.AnnotatedMethod;
import com.almende.util.AnnotationUtil.AnnotatedParam;
import com.almende.util.callback.AsyncCallback;
import com.almende.util.jackson.JOM;
import com.almende.util.uuid.UUID;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class JSONRequest.
 */
public final class JSONRequest extends JSONMessage {
	private static final Logger			LOG					= Logger.getLogger(JSONRequest.class
																	.getCanonicalName());
	private static final long			serialVersionUID	= 1970046457233622444L;
	private ObjectNode					req					= JOM.createObjectNode();
	transient private AsyncCallback<?>	callback			= null;

	/**
	 * Instantiates a new jSON request.
	 */
	public JSONRequest() {
		init(null, null, null, null);
	}

	/**
	 * Instantiates a new JSON request.
	 * 
	 * @param json
	 *            the json
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public JSONRequest(final String json) throws IOException {
		final ObjectMapper mapper = JOM.getInstance();
		init(mapper.readTree(json));
	}

	/**
	 * Instantiates a new JSON request.
	 * 
	 * @param request
	 *            the request
	 */
	public JSONRequest(final JsonNode request) {
		init(request);
	}

	/**
	 * Instantiates a new JSON request.
	 *
	 * @param method
	 *            the method
	 * @param params
	 *            the params
	 * @param callback
	 *            the callback
	 */
	public <T> JSONRequest(final String method, final ObjectNode params,
			final AsyncCallback<T> callback) {
		if (callback != null) {

		}
		init(null, method, params, callback);
	}

	/**
	 * Instantiates a new JSON request.
	 *
	 * @param method
	 *            the method
	 * @param params
	 *            the params
	 */
	public JSONRequest(final String method, final ObjectNode params) {
		init(null, method, params, null);
	}

	/**
	 * Instantiates a new jSON request.
	 *
	 * @param id
	 *            the id
	 * @param method
	 *            the method
	 * @param params
	 *            the params
	 * @param callback
	 *            the callback
	 */
	public <T> JSONRequest(final JsonNode id, final String method,
			final ObjectNode params, final AsyncCallback<T> callback) {
		init(id, method, params, callback);
	}

	/**
	 * Create a JSONRequest from a java method and arguments.
	 *
	 * @param method
	 *            the method
	 * @param args
	 *            the args
	 * @param callback
	 *            the callback
	 */
	public <T> JSONRequest(final Method method, final Object[] args,
			final AsyncCallback<T> callback) {
		AnnotatedMethod annotatedMethod = null;
		try {
			annotatedMethod = new AnnotationUtil.AnnotatedMethod(method);
		} catch (final Exception e) {
			LOG.log(Level.WARNING, "Method can't be used as annotated method",
					e);
			throw new IllegalArgumentException("Method '" + method.getName()
					+ "' can't be used as annotated method.", e);
		}
		final List<AnnotatedParam> annotatedParams = annotatedMethod
				.getParams();

		final ObjectNode params = JOM.createObjectNode();

		for (int i = 0; i < annotatedParams.size(); i++) {
			final AnnotatedParam annotatedParam = annotatedParams.get(i);
			if (i < args.length && args[i] != null) {
				final Name nameAnnotation = annotatedParam
						.getAnnotation(Name.class);
				if (nameAnnotation != null && nameAnnotation.value() != null) {
					final String name = nameAnnotation.value();
					final JsonNode paramValue = JOM.getInstance().valueToTree(
							args[i]);
					params.set(name, paramValue);
				} else {
					throw new IllegalArgumentException("Parameter " + i
							+ " in method '" + method.getName()
							+ "' is missing the @Name annotation.");
				}
			} else if (isRequired(annotatedParam)) {
				throw new IllegalArgumentException("Required parameter " + i
						+ " in method '" + method.getName() + "' is null.");
			}
		}
		JsonNode id = null;
		try {
			id = JOM.getInstance().valueToTree(new UUID().toString());
		} catch (final Exception e) {
			LOG.log(Level.SEVERE, "Failed to generate UUID for request", e);
		}
		init(id, method.getName(), params, callback);
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
	 * Inits the.
	 * 
	 * @param request
	 *            the request
	 */
	public void init(final JsonNode request) {
		if (request == null || request.isNull()) {
			throw new JSONRPCException(JSONRPCException.CODE.INVALID_REQUEST,
					"Request is null");
		}
		if (request.has(JSONRPC) && request.get(JSONRPC).isTextual()
				&& !request.get(JSONRPC).asText().equals(VERSION)) {
			throw new JSONRPCException(JSONRPCException.CODE.INVALID_REQUEST,
					"Value of member 'jsonrpc' is not equal to '2.0'");
		}
		if (!request.has(METHOD)) {
			throw new JSONRPCException(JSONRPCException.CODE.INVALID_REQUEST,
					"Member 'method' missing in request");
		}
		if (!(request.get(METHOD).isTextual())) {
			throw new JSONRPCException(JSONRPCException.CODE.INVALID_REQUEST,
					"Member 'method' is no String");
		}
		if (request.has(PARAMS) && !(request.get(PARAMS).isObject())) {
			throw new JSONRPCException(JSONRPCException.CODE.INVALID_REQUEST,
					"Member 'params' is no ObjectNode");
		}

		init(request.get(ID), request.get(METHOD).asText(),
				(ObjectNode) request.get(PARAMS), null);
	}

	/**
	 * Inits the.
	 * 
	 * @param id
	 *            the id
	 * @param method
	 *            the method
	 * @param params
	 *            the params
	 */
	private <T> void init(final JsonNode id, final String method,
			final ObjectNode params, final AsyncCallback<T> callback) {
		this.setRequest(true);
		setVersion();
		if (callback != null && (id == null || id.isNull())){
			setId(JOM.getInstance().valueToTree(new UUID().toString()));
		} else {
			setId(id);
		}
		setMethod(method);
		setParams(params);
		setCallback(callback);
	}

	/**
	 * Sets the id.
	 * 
	 * @param id
	 *            the new id
	 */
	public void setId(final JsonNode id) {
		req.set(ID, id);
	}

	@Override
	public JsonNode getId() {
		return req.get(ID);
	}

	/**
	 * Sets the method.
	 * 
	 * @param method
	 *            the new method
	 */
	public void setMethod(final String method) {
		req.put(METHOD, method);
	}

	/**
	 * Gets the method.
	 * 
	 * @return the method
	 */
	public String getMethod() {
		if (req.has(METHOD)) {
			return req.get(METHOD).asText();
		}
		return null;
	}

	/**
	 * Sets the params.
	 * 
	 * @param params
	 *            the new params
	 */
	public void setParams(final ObjectNode params) {
		final ObjectNode newParams = JOM.createObjectNode();
		if (params != null) {
			newParams.setAll(params);
		}
		req.set(PARAMS, newParams);
	}

	/**
	 * Gets the params.
	 * 
	 * @return the params
	 */
	public ObjectNode getParams() {
		return (ObjectNode) req.get(PARAMS);
	}

	/**
	 * Put param.
	 * 
	 * @param name
	 *            the name
	 * @param value
	 *            the value
	 */
	public void putParam(final String name, final Object value) {
		final ObjectMapper mapper = JOM.getInstance();
		req.with(PARAMS).set(name, mapper.convertValue(value, JsonNode.class));
	}

	/**
	 * Gets the param.
	 * 
	 * @param name
	 *            the name
	 * @return the param
	 */
	public Object getParam(final String name) {
		final ObjectMapper mapper = JOM.getInstance();
		final ObjectNode params = req.with(PARAMS);
		if (params.has(name)) {
			return mapper.convertValue(params.get(name), Object.class);
		}
		return null;
	}

	/**
	 * Checks for param.
	 * 
	 * @param name
	 *            the name
	 * @return the object
	 */
	public Object hasParam(final String name) {
		return req.get(PARAMS).has(name);
	}

	/**
	 * Sets the version.
	 */
	private void setVersion() {
		req.put(JSONRPC, VERSION);
	}

	/**
	 * Gets the callback.
	 *
	 * @return the callback
	 */
	public AsyncCallback<?> getCallback() {
		return callback;
	}

	/**
	 * Sets the callback.
	 *
	 * @param callback
	 *            the new callback
	 */
	public <T> void setCallback(AsyncCallback<T> callback) {
		this.callback = callback;
	}

	/**
	 * Gets the object node.
	 * 
	 * @return the object node
	 */
	@JsonIgnore
	public ObjectNode getObjectNode() {
		return req;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		final ObjectMapper mapper = JOM.getInstance();
		try {
			return mapper.writeValueAsString(req);
		} catch (final Exception e) {
			LOG.log(Level.WARNING, "", e);
		}
		return null;
	}

	/**
	 * Write object.
	 * 
	 * @param out
	 *            the out
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private void writeObject(final java.io.ObjectOutputStream out)
			throws IOException {
		final ObjectMapper mapper = new ObjectMapper();
		mapper.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
		mapper.writeValue(out, req);
	}

	/**
	 * Read object.
	 * 
	 * @param in
	 *            the in
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws ClassNotFoundException
	 *             the class not found exception
	 */
	private void readObject(final java.io.ObjectInputStream in)
			throws IOException, ClassNotFoundException {
		final ObjectMapper mapper = new ObjectMapper();
		mapper.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, false);
		req = mapper.readValue(in, ObjectNode.class);
	}

}