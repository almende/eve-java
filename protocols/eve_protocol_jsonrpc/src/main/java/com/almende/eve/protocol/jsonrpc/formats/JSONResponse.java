/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.protocol.jsonrpc.formats;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.almende.util.jackson.JOM;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class JSONResponse.
 */
public final class JSONResponse extends JSONMessage {
	private static final long		serialVersionUID	= 12392962249054051L;
	private final ObjectNode		resp				= JOM.createObjectNode();
	private static final Logger		LOG					= Logger.getLogger(JSONResponse.class
																.getName());
	private static final JavaType	JSONNODETYPE		= JOM.getTypeFactory()
																.constructType(
																		JsonNode.class);

	/**
	 * Instantiates a new jSON response.
	 */
	public JSONResponse() {
		init(null, null, null);
	}

	/**
	 * Instantiates a new jSON response.
	 * 
	 * @param json
	 *            the json
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public JSONResponse(final String json) throws IOException {
		final ObjectMapper mapper = JOM.getInstance();
		init(mapper.valueToTree(json));
	}

	/**
	 * Instantiates a new jSON response.
	 * 
	 * @param response
	 *            the response
	 */
	public JSONResponse(final ObjectNode response) {
		init(response);
	}

	/**
	 * Instantiates a new jSON response.
	 * 
	 * @param result
	 *            the result
	 */
	public JSONResponse(final Object result) {
		init(null, result, null);
	}

	/**
	 * Instantiates a new jSON response.
	 * 
	 * @param id
	 *            the id
	 * @param result
	 *            the result
	 */
	public JSONResponse(final JsonNode id, final Object result) {
		init(id, result, null);
	}

	/**
	 * Instantiates a new jSON response.
	 * 
	 * @param error
	 *            the error
	 */
	public JSONResponse(final JSONRPCException error) {
		init(null, null, error);
	}

	/**
	 * Instantiates a new jSON response.
	 * 
	 * @param id
	 *            the id
	 * @param error
	 *            the error
	 */
	public JSONResponse(final JsonNode id, final JSONRPCException error) {
		init(id, null, error);
	}

	/**
	 * Inits the.
	 * 
	 * @param jsonNode
	 *            the response
	 */
	private void init(final JsonNode jsonNode) {
		if (jsonNode == null || jsonNode.isNull()) {
			throw new JSONRPCException(JSONRPCException.CODE.INVALID_REQUEST,
					"Response is null");
		}
		if (jsonNode.has(JSONRPC) && jsonNode.get(JSONRPC).isTextual()
				&& !jsonNode.get(JSONRPC).asText().equals(VERSION)) {
			throw new JSONRPCException(JSONRPCException.CODE.INVALID_REQUEST,
					"Value of member 'jsonrpc' must be '2.0'");
		}
		final boolean hasError = jsonNode.has(ERROR)
				&& !jsonNode.get(ERROR).isNull();
		if (hasError && !(jsonNode.get(ERROR).isObject())) {
			throw new JSONRPCException(JSONRPCException.CODE.INVALID_REQUEST,
					"Member 'error' is no ObjectNode");
		}

		final JsonNode id = jsonNode.get(ID);
		final Object result = jsonNode.get(RESULT);
		JSONRPCException error = null;
		if (hasError) {
			try {
				error = new JSONRPCException(jsonNode.get(ERROR));
			} catch (JsonProcessingException e) {
				throw new JSONRPCException(JSONRPCException.CODE.INVALID_REQUEST,
						"Member 'error' is no a real JSONRPCException.");
			}
		}

		init(id, result, error);
	}

	/**
	 * Inits the.
	 * 
	 * @param id
	 *            the id
	 * @param result
	 *            the result
	 * @param error
	 *            the error
	 */
	private void init(final JsonNode id, final Object result,
			final JSONRPCException error) {
		this.setRequest(false);
		setVersion();
		setId(id);
		setResult(result);
		setError(error);
	}

	/**
	 * Sets the id.
	 * 
	 * @param id
	 *            the new id
	 */
	public void setId(final JsonNode id) {
		resp.set(ID, id);
	}

	@Override
	public JsonNode getId() {
		return resp.get(ID);
	}

	/**
	 * Sets the result.
	 * 
	 * @param result
	 *            the new result
	 */
	public void setResult(final Object result) {
		if (result != null) {
			final ObjectMapper mapper = JOM.getInstance();
			resp.set(RESULT,
					(JsonNode) mapper.convertValue(result, JSONNODETYPE));
			setError(null);
		} else {
			if (resp.has(RESULT)) {
				resp.remove(RESULT);
			}
		}
	}

	/**
	 * Gets the result.
	 * 
	 * @return the result
	 */
	public JsonNode getResult() {
		return resp.get(RESULT);
	}

	/**
	 * Sets the error.
	 * 
	 * @param error
	 *            the new error
	 */
	public void setError(final JSONRPCException error) {
		if (error != null) {
			resp.set(ERROR, error.getJsonNode());
			setResult(null);
		} else {
			if (resp.has(ERROR)) {
				resp.remove(ERROR);
			}
		}
	}

	/**
	 * Gets the error.
	 * 
	 * @return the error
	 */
	public JSONRPCException getError() {
		if (resp.has(ERROR)) {
			final JsonNode error = resp.get(ERROR);
			try {
				return new JSONRPCException(error);
			} catch (JsonProcessingException e) {
				LOG.log(Level.WARNING, "Couldn't parse error", e);
			}
		}
		return null;
	}

	/**
	 * Sets the version.
	 */
	private void setVersion() {
		resp.put(JSONRPC, VERSION);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		final ObjectMapper mapper = JOM.getInstance();
		try {
			return mapper.writeValueAsString(resp);
		} catch (final Exception e) {
			LOG.log(Level.SEVERE, "Failed to stringify response.", e);
		}
		return null;
	}
}