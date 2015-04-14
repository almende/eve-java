/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.protocol.jsonrpc;

import java.net.URI;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.almende.eve.capabilities.handler.Handler;
import com.almende.eve.protocol.Protocol;
import com.almende.eve.protocol.auth.Authorizor;
import com.almende.eve.protocol.auth.DefaultAuthorizor;
import com.almende.eve.protocol.jsonrpc.annotation.Sender;
import com.almende.eve.protocol.jsonrpc.formats.JSONMessage;
import com.almende.eve.protocol.jsonrpc.formats.JSONRPCException;
import com.almende.eve.protocol.jsonrpc.formats.JSONRequest;
import com.almende.eve.protocol.jsonrpc.formats.JSONResponse;
import com.almende.eve.protocol.jsonrpc.formats.RequestParams;
import com.almende.util.URIUtil;
import com.almende.util.callback.AsyncCallback;
import com.almende.util.callback.AsyncCallbackQueue;
import com.almende.util.jackson.JOM;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class JSONRpcProtocol.
 */
public class JSONRpcProtocol implements Protocol {
	private static final Logger						LOG					= Logger.getLogger(JSONRpcProtocol.class
																				.getName());
	private static final RequestParams				EVEREQUESTPARAMS	= new RequestParams();
	static {
		EVEREQUESTPARAMS.put(Sender.class, URIUtil.create("local:null"));
	}
	private Authorizor								auth				= new DefaultAuthorizor();
	private final AsyncCallbackQueue<JSONResponse>	callbacks			= new AsyncCallbackQueue<JSONResponse>();
	private final Handler<Object>					destination;
	private JSONRpcProtocolConfig					myParams;

	/**
	 * Instantiates a new JSON rpc protocol.
	 * 
	 * @param params
	 *            the params
	 * @param handle
	 *            the handle
	 */
	public JSONRpcProtocol(final ObjectNode params, final Handler<Object> handle) {
		destination = handle;
		myParams = JSONRpcProtocolConfig.decorate(params);
		callbacks.setDefTimeout(myParams.getCallbackTimeout());
	}

	@Override
	public Meta inbound(final Object msg, final URI senderUrl) {
		final JSONResponse response = invoke(msg, senderUrl);
		return new Meta(response, response == null, response != null);

	}

	public Meta outbound(final Object msg, final URI recipientUrl) {
		if (msg instanceof JSONRequest) {
			final JSONRequest request = (JSONRequest) msg;
			addCallback(request, request.getCallback());
		}
		return new Meta(msg);
	}

	/**
	 * Gets the auth.
	 * 
	 * @return the auth
	 */
	public Authorizor getAuth() {
		return auth;
	}

	/**
	 * Sets the auth.
	 * 
	 * @param auth
	 *            the new auth
	 */
	public void setAuth(final Authorizor auth) {
		this.auth = auth;
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
					if (JSONRpc.isResponse(json)) {
						final JSONResponse response = new JSONResponse(json);
						jsonMsg = response;
					} else if (JSONRpc.isRequest(json)) {
						final JSONRequest request = new JSONRequest(json);
						jsonMsg = request;
					} else {
						LOG.info("Message contains valid JSON, but is not JSON-RPC:"
								+ json);
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

	/**
	 * Invoke this RPC msg.
	 * 
	 * @param msg
	 *            the msg
	 * @param senderUrl
	 *            the sender url
	 * @return the JSON response
	 */
	public JSONResponse invoke(final Object msg, final URI senderUrl) {
		final JSONMessage jsonMsg = jsonConvert(msg);
		if (jsonMsg == null) {
			LOG.log(Level.INFO, "Received non-JSONRPC message:'" + msg + "'");
			return null;
		}
		final JsonNode id = jsonMsg.getId();
		try {
			if (jsonMsg.isRequest()) {
				final JSONRequest request = (JSONRequest) jsonMsg;
				final RequestParams params = new RequestParams();
				params.put(Sender.class, senderUrl);
				return JSONRpc.invoke(destination.get(), request, params, auth);
			} else if (jsonMsg.isResponse() && callbacks != null && id != null
					&& !id.isNull()) {
				final AsyncCallback<JSONResponse> callback = callbacks.pull(id);
				if (callback != null) {
					final JSONResponse response = (JSONResponse) jsonMsg;
					final JSONRPCException error = response.getError();
					if (error != null) {
						callback.onFailure(error);
					} else {
						callback.onSuccess(response);
					}
				}
			}
		} catch (final Exception e) {
			// generate JSON error response, skipped if it was an incoming
			// notification i.s.o. request.
			final JSONRPCException jsonError = new JSONRPCException(
					JSONRPCException.CODE.INTERNAL_ERROR, e.getMessage(), e);
			LOG.log(Level.WARNING, "Exception in receiving message", jsonError);
			if (id != null) {
				final JSONResponse response = new JSONResponse(jsonError);
				response.setId(id);
				return response;
			}
		}
		return null;
	}

	/**
	 * Gets the methods.
	 * 
	 * @return the methods
	 */
	public List<Object> getMethods() {
		return JSONRpc.describe(getHandle().get(), EVEREQUESTPARAMS, auth);
	}

	private <T> void addCallback(final JSONRequest request,
			final AsyncCallback<T> asyncCallback) {
		if (asyncCallback == null || request.getId() == null
				|| request.getId().isNull()) {
			return;
		}

		// Create a callback to retrieve a JSONResponse and extract the result
		// or error from this. This is double nested, mostly because of the type
		// conversions required on the result.
		final AsyncCallback<JSONResponse> responseCallback = new AsyncCallback<JSONResponse>() {
			@Override
			public void onSuccess(final JSONResponse response) {
				final Exception err = response.getError();
				if (err != null) {
					asyncCallback.onFailure(err);
				}
				if (asyncCallback.getType() != null
						&& !asyncCallback.getType().getJavaType().getRawClass()
								.equals(Void.class)) {
					try {
						final T res = asyncCallback.getType().inject(
								response.getResult());
						asyncCallback.onSuccess(res);
					} catch (final ClassCastException cce) {
						asyncCallback.onFailure(new JSONRPCException(
								"Incorrect return type received for JSON-RPC call:"
										+ request.getMethod(), cce));
					}

				} else {
					asyncCallback.onSuccess(null);
				}
			}

			@Override
			public void onFailure(final Exception exception) {
				asyncCallback.onFailure(exception);
			}
		};

		if (callbacks != null) {
			callbacks.push(((JSONMessage) request).getId(), request.toString(),
					responseCallback);
		}
	}

	/**
	 * Gets the handle.
	 * 
	 * @return the handle
	 */
	public Handler<Object> getHandle() {
		return destination;
	}

	/*
	 * (non-Javadoc)
	 * @see com.almende.eve.capabilities.Capability#getParams()
	 */
	@Override
	public ObjectNode getParams() {
		return myParams;
	}

	@Override
	public void delete() {
		callbacks.clear();
		JSONRpcProtocolConfig config = JSONRpcProtocolConfig
				.decorate(getParams());
		JSONRpcProtocolBuilder.delete(config.getId());
	}

}
