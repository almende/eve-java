/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.protocol.jsonrpc.formats;

import java.io.IOException;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.almende.util.jackson.JOM;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class JSONMessage.
 */
public class JSONMessage implements Serializable {
	private static final Logger		LOG					= Logger.getLogger(JSONMessage.class
																.getName());
	private static final long		serialVersionUID	= -3324436908445901707L;
	protected static final String	JSONRPC				= "jsonrpc";
	protected static final String	ID					= "id";
	protected static final String	METHOD				= "method";
	protected static final String	PARAMS				= "params";
	protected static final String	ERROR				= "error";
	protected static final String	RESULT				= "result";
	protected static final String	URL					= "url";
	protected static final String	CALLBACK			= "callback";
	protected static final String	EXTRA				= "extra";
	protected static final String	VERSION				= "2.0";

	private JsonNode				id					= null;
	private ObjectNode				extra				= null;

	/**
	 * Instantiates a new JSON message.
	 */
	public JSONMessage() {}

	/**
	 * Instantiates a new JSON message.
	 *
	 * @param jsonNode
	 *            the json node
	 */
	public JSONMessage(final JsonNode jsonNode) {
		init(jsonNode);
		try {
			JOM.getInstance().readerForUpdating(this).readValue(jsonNode);
		} catch (IOException e) {
			LOG.log(Level.WARNING, "Couldn't parse incoming message", e);
			throw new JSONRPCException(JSONRPCException.CODE.INVALID_REQUEST,
					e.getLocalizedMessage(), e);
		}
	}

	protected void init(final JsonNode jsonNode) {
		if (jsonNode == null || jsonNode.isNull()) {
			throw new JSONRPCException(JSONRPCException.CODE.INVALID_REQUEST,
					"JSON-RPC message is null");
		}
		if (jsonNode.has(JSONRPC) && jsonNode.get(JSONRPC).isTextual()
				&& !jsonNode.get(JSONRPC).asText().equals(VERSION)) {
			throw new JSONRPCException(JSONRPCException.CODE.INVALID_REQUEST,
					"Value of member 'jsonrpc' is not equal to '2.0'");
		}
		if (jsonNode.has(EXTRA) && !jsonNode.get(EXTRA).isObject()) {
			throw new JSONRPCException(JSONRPCException.CODE.INVALID_REQUEST,
					"Value of member 'extra' should be an object");
		}
	}

	/**
	 * Sets the id.
	 * 
	 * @param id
	 *            the new id
	 */
	public void setId(final JsonNode id) {
		this.id = id;
	}

	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	public JsonNode getId() {
		return this.id;
	}

	/**
	 * Gets the jsonrpc version.
	 *
	 * @return the jsonrpc version
	 */
	public String getJsonrpc() {
		return VERSION;
	}

	/**
	 * Checks if is request.
	 * 
	 * @return true, if is request
	 */
	@JsonIgnore
	public boolean isRequest() {
		return false;
	}

	/**
	 * Checks if is response.
	 * 
	 * @return true, if is response
	 */
	@JsonIgnore
	public boolean isResponse() {
		return false;
	}

	/**
	 * Set extra (non JSON-RPC) data;.
	 *
	 * @param extra
	 *            the new extra
	 */
	public void setExtra(final ObjectNode extra) {
		this.extra = extra;
	}

	/**
	 * Gets extra (non JSON-RPC) data.
	 *
	 * @return the extra
	 */
	public ObjectNode getExtra() {
		return this.extra;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (!(o instanceof JSONMessage)) {
			return false;
		}
		return JOM.getInstance().valueToTree(this)
				.equals(JOM.getInstance().valueToTree(o));
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return JOM.getInstance().valueToTree(this).hashCode();
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
		return json.has(METHOD);
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
		return (json.has(RESULT) || json.has(ERROR));
	}

	/**
	 * Convert incoming message object to JSONMessage if possible. Returns null
	 * if the message can't be interpreted as a JSONMessage.
	 * 
	 * @param msg
	 *            the msg
	 * @return the JSON message
	 */
	public static JSONMessage jsonConvert(final Object msg) {
		JSONMessage jsonMsg = null;
		if (msg == null) {
			LOG.warning("Message null!");
			return null;
		}
		try {
			if (msg instanceof JSONMessage) {
				jsonMsg = (JSONMessage) msg;
			} else {
				ObjectNode json = null;
				if (msg instanceof String) {
					final String message = (String) msg;
					if (message.startsWith("{")
							|| message.trim().startsWith("{")) {

						json = (ObjectNode) JOM.getInstance().readTree(message);
					}
				} else if (msg instanceof ObjectNode
						|| (msg instanceof JsonNode && ((JsonNode) msg)
								.isObject())) {
					json = (ObjectNode) msg;
				} else {
					LOG.info("Message unknown type:" + msg.getClass());
				}
				if (json != null) {
					if (isResponse(json)) {
						final JSONResponse response = new JSONResponse(json);
						jsonMsg = response;
					} else if (isRequest(json)) {
						final JSONRequest request = new JSONRequest(json);
						jsonMsg = request;
					} else {
						final JSONMessage generic = new JSONMessage(json);
						jsonMsg = generic;
					}
				}
			}
		} catch (final Exception e) {
			LOG.log(Level.WARNING,
					"Message triggered exception in trying to convert it to a JSONMessage.",
					e);
		}
		return jsonMsg;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		final ObjectMapper mapper = JOM.getInstance();
		try {
			ObjectNode tree = mapper.valueToTree(this);
			if (tree.get(ID) == null || tree.get(ID).isNull()) {
				tree.remove(ID);
			}
			if (tree.get(EXTRA) == null || tree.get(EXTRA).isNull()) {
				tree.remove(EXTRA);
			}
			return tree.toString();
		} catch (final Exception e) {
			LOG.log(Level.WARNING, "", e);
		}
		return null;
	}
}
