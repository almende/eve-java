/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.protocol.jsonrpc.formats;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.almende.util.jackson.JOM;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class JSONResponse.
 */
public final class JSONResponse extends JSONMessage {
	private static final long		serialVersionUID	= 12392962249054051L;
	private static final Logger		LOG					= Logger.getLogger(JSONResponse.class
																.getName());
	private static final JavaType	JSONNODETYPE		= JOM.getTypeFactory()
																.constructType(
																		JsonNode.class);

	private JsonNode				result				= null;
	private JSONRPCException		error				= null;

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
	protected void init(final JsonNode jsonNode) {
		super.init(jsonNode);
		final boolean hasError = jsonNode.has(ERROR)
				&& !jsonNode.get(ERROR).isNull();
		if (hasError && !(jsonNode.get(ERROR).isObject())) {
			throw new JSONRPCException(JSONRPCException.CODE.INVALID_REQUEST,
					"Member 'error' is no ObjectNode");
		}
		try {
			JOM.getInstance().readerForUpdating(this).readValue(jsonNode);
		} catch (IOException e) {
			LOG.log(Level.WARNING, "Couldn't parse incoming response", e);
			throw new JSONRPCException(JSONRPCException.CODE.INVALID_REQUEST,
					e.getLocalizedMessage(), e);
		}
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
		setId(id);
		setResult(result);
		setError(error);
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
			this.result = (JsonNode) mapper.convertValue(result, JSONNODETYPE);
			setError(null);
		} else {
			this.result = null;
		}
	}

	/**
	 * Gets the result.
	 * 
	 * @return the result
	 */
	public JsonNode getResult() {
		return result;
	}

	/**
	 * Sets the error.
	 * 
	 * @param error
	 *            the new error
	 */
	public void setError(final JSONRPCException error) {
		this.error = error;
	}

	/**
	 * Gets the error.
	 * 
	 * @return the error
	 */
	public JSONRPCException getError() {
		return error;
	}

	@Override
	@JsonIgnore
	public boolean isResponse() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		final ObjectMapper mapper = JOM.getInstance();
		try {
			final ObjectNode tree = mapper.valueToTree(this);
			if (tree.get(ERROR) == null || tree.get(ERROR).isNull()) {
				tree.remove(ERROR);
			}
			if (tree.get(RESULT) == null || tree.get(RESULT).isNull()) {
				tree.remove(RESULT);
			}
			if (tree.get(EXTRA) == null || tree.get(EXTRA).isNull()) {
				tree.remove(EXTRA);
			}
			return tree.toString();
		} catch (final Exception e) {
			LOG.log(Level.SEVERE, "Failed to stringify response.", e);
		}
		return null;
	}
}