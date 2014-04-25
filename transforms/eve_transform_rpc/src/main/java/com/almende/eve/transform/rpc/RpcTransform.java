/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.transform.rpc;

import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.almende.eve.auth.Authorizor;
import com.almende.eve.auth.DefaultAuthorizor;
import com.almende.eve.capabilities.handler.Handler;
import com.almende.eve.transform.Transform;
import com.almende.eve.transform.TransformService;
import com.almende.eve.transform.rpc.annotation.Sender;
import com.almende.eve.transform.rpc.jsonrpc.JSONMessage;
import com.almende.eve.transform.rpc.jsonrpc.JSONRPC;
import com.almende.eve.transform.rpc.jsonrpc.JSONRPCException;
import com.almende.eve.transform.rpc.jsonrpc.JSONRequest;
import com.almende.eve.transform.rpc.jsonrpc.JSONResponse;
import com.almende.eve.transform.rpc.jsonrpc.RequestParams;
import com.almende.util.TypeUtil;
import com.almende.util.callback.AsyncCallback;
import com.almende.util.callback.AsyncCallbackQueue;
import com.almende.util.jackson.JOM;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class RpcTransform.
 */
public class RpcTransform implements Transform {
	private static final Logger					LOG			= Logger.getLogger(RpcTransform.class
																	.getName());
	private Authorizor							auth		= new DefaultAuthorizor();
	private AsyncCallbackQueue<JSONResponse>	callbacks	= new AsyncCallbackQueue<JSONResponse>();
	private final Handler<Object>				destination;
	
	/**
	 * Instantiates a new rpc transform.
	 * 
	 * @param params
	 *            the params
	 * @param handle
	 *            the handle
	 * @param service
	 *            the service
	 */
	public RpcTransform(final ObjectNode params, final Handler<Object> handle,
			final TransformService service) {
		this.destination = handle;
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
	public void setAuth(Authorizor auth) {
		this.auth = auth;
	}
	
	/**
	 * Json convert.
	 * 
	 * @param msg
	 *            the msg
	 * @return the JSON message
	 */
	public static JSONMessage jsonConvert(final Object msg) {
		JSONMessage jsonMsg = null;
		try {
			if (msg instanceof JSONMessage) {
				jsonMsg = (JSONMessage) msg;
			} else {
				ObjectNode json = null;
				if (msg instanceof String) {
					final String message = (String) msg;
					if (message.startsWith("{")
							|| message.trim().startsWith("{")) {
						
						json = JOM.getInstance().readValue(message,
								ObjectNode.class);
					}
				} else if (msg instanceof ObjectNode) {
					json = (ObjectNode) msg;
				} else if (msg == null) {
					LOG.warning("Message null!");
				} else {
					LOG.warning("Message unknown type:" + msg.getClass());
				}
				if (json != null) {
					if (JSONRPC.isResponse(json)) {
						final JSONResponse response = new JSONResponse(json);
						jsonMsg = response;
					} else if (JSONRPC.isRequest(json)) {
						final JSONRequest request = new JSONRequest(json);
						jsonMsg = request;
					} else {
						LOG.warning("Message contains valid JSON, but is not JSON-RPC:"
								+ json);
					}
				}
			}
		} catch (Exception e) {
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
		JsonNode id = null;
		try {
			final JSONMessage jsonMsg = jsonConvert(msg);
			if (jsonMsg != null) {
				id = jsonMsg.getId();
				if (jsonMsg instanceof JSONRequest) {
					final JSONRequest request = (JSONRequest) jsonMsg;
					final RequestParams params = new RequestParams();
					params.put(Sender.class, senderUrl.toASCIIString());
					return JSONRPC.invoke(destination.get(), request, params, auth);
				} else if (jsonMsg instanceof JSONResponse && callbacks != null
						&& id != null && !id.isNull()) {
					final JSONResponse response = (JSONResponse) jsonMsg;
					final AsyncCallback<JSONResponse> callback = callbacks
							.pull(id);
					if (callback != null) {
						if (response.getError() != null) {
							callback.onFailure(response.getError());
						} else {
							callback.onSuccess(response);
						}
					}
				} else {
					LOG.log(Level.WARNING, "Received non-JSON message:'" + msg
							+ "'");
				}
			}
		} catch (final Exception e) {
			// generate JSON error response, skipped if it was an incoming
			// notification i.s.o. request.
			final JSONRPCException jsonError = new JSONRPCException(
					JSONRPCException.CODE.INTERNAL_ERROR, e.getMessage(), e);
			LOG.log(Level.WARNING, "Exception in receiving message", jsonError);
			
			final JSONResponse response = new JSONResponse(jsonError);
			response.setId(id);
			return response;
		}
		return null;
	}
	
	/**
	 * Builds the msg.
	 * 
	 * @param <T>
	 *            the generic type
	 * @param method
	 *            the method
	 * @param params
	 *            the params
	 * @param callback
	 *            the callback
	 * @param type
	 *            the type
	 * @return the JSON request
	 */
	public <T> JSONRequest buildMsg(final String method,
			final ObjectNode params, final AsyncCallback<T> callback,
			final JavaType type) {
		
		final JSONRequest request = new JSONRequest(method, params);
		// Create a callback to retrieve a JSONResponse and extract the result
		// or error from this. This is double nested, mostly because of the type
		// conversions required on the result.
		final AsyncCallback<JSONResponse> responseCallback = new AsyncCallback<JSONResponse>() {
			@SuppressWarnings("unchecked")
			@Override
			public void onSuccess(final JSONResponse response) {
				if (callback == null) {
					final Exception err = response.getError();
					if (err != null) {
						LOG.warning("async RPC call failed, and no callback handler available:"
								+ err.getLocalizedMessage());
					}
				} else {
					final Exception err = response.getError();
					if (err != null) {
						callback.onFailure(err);
					}
					if (type != null && !type.hasRawClass(Void.class)) {
						try {
							final T res = (T) TypeUtil.inject(
									response.getResult(), type);
							callback.onSuccess(res);
						} catch (final ClassCastException cce) {
							callback.onFailure(new JSONRPCException(
									"Incorrect return type received for JSON-RPC call:"
											+ request.getMethod(), cce));
						}
						
					} else {
						callback.onSuccess(null);
					}
				}
			}
			
			@Override
			public void onFailure(final Exception exception) {
				if (callback == null) {
					LOG.warning("async RPC call failed and no callback handler available:"
							+ exception.getLocalizedMessage());
				} else {
					callback.onFailure(exception);
				}
			}
		};
		
		if (responseCallback != null && callbacks != null) {
			callbacks.push(((JSONMessage) request).getId(), request.toString(),
					responseCallback);
		}
		return request;
	}

	/**
	 * Gets the handle.
	 * 
	 * @return the handle
	 */
	public Handler<Object> getHandle() {
		return destination;
	}
}
