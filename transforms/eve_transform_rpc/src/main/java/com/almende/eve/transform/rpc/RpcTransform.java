/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.transform.rpc;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.almende.eve.auth.Authorizor;
import com.almende.eve.auth.DefaultAuthorizor;
import com.almende.eve.capabilities.handler.Handler;
import com.almende.eve.transform.Transform;
import com.almende.eve.transform.rpc.annotation.Sender;
import com.almende.eve.transform.rpc.formats.JSONMessage;
import com.almende.eve.transform.rpc.formats.JSONRPCException;
import com.almende.eve.transform.rpc.formats.JSONRequest;
import com.almende.eve.transform.rpc.formats.JSONResponse;
import com.almende.eve.transform.rpc.formats.RequestParams;
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
	private static final Logger						LOG					= Logger.getLogger(RpcTransform.class
																				.getName());
	private static final RequestParams				EVEREQUESTPARAMS	= new RequestParams();
	private static final JavaType					OBJECTNODETYPE		= JOM.getTypeFactory()
																				.constructType(
																						ObjectNode.class);
	static {
		EVEREQUESTPARAMS.put(Sender.class, null);
	}
	private Authorizor								auth				= new DefaultAuthorizor();
	private final AsyncCallbackQueue<JSONResponse>	callbacks			= new AsyncCallbackQueue<JSONResponse>();
	private final Handler<Object>					destination;
	private final ObjectNode						myParams;
	
	/**
	 * Instantiates a new rpc transform.
	 * 
	 * @param params
	 *            the params
	 * @param handle
	 *            the handle
	 */
	public RpcTransform(final ObjectNode params, final Handler<Object> handle) {
		destination = handle;
		myParams = params;
		final RpcTransformConfig config = new RpcTransformConfig(params);
		callbacks.setDefTimeout(config.getCallbackTimeout());
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
						
						json = JOM.getInstance().readValue(message,
								OBJECTNODETYPE);
					}
				} else if (msg instanceof ObjectNode) {
					json = (ObjectNode) msg;
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
			LOG.log(Level.WARNING, "Received non-JSONRPC message:'" + msg + "'");
			return null;
		}
		final JsonNode id = jsonMsg.getId();
		try {
			if (jsonMsg.isRequest()) {
				final JSONRequest request = (JSONRequest) jsonMsg;
				final RequestParams params = new RequestParams();
				params.put(Sender.class, senderUrl.toASCIIString());
				return JSONRPC.invoke(destination.get(), request, params, auth);
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
			
			final JSONResponse response = new JSONResponse(jsonError);
			response.setId(id);
			return response;
		}
		return null;
	}
	
	/**
	 * Gets the methods.
	 * 
	 * @return the methods
	 */
	public List<Object> getMethods() {
		return JSONRPC.describe(getHandle().get(), EVEREQUESTPARAMS, auth);
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
	 * @return the JSON request
	 */
	public <T> JSONRequest buildMsg(final String method,
			final ObjectNode params) {
		return new JSONRequest(method, params);
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
	 * @return the JSON request
	 */
	public <T> JSONRequest buildMsg(final String method,
			final ObjectNode params, final AsyncCallback<T> callback) {
		final JSONRequest request = new JSONRequest(method, params);
		addCallback(request, callback);
		return request;
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
	 * @return the JSON request
	 */
	public <T> JSONRequest buildMsg(final Method method, final Object[] params,
			final AsyncCallback<T> callback) {
		final JSONRequest request = JSONRPC.createRequest(method, params);
		addCallback(request, callback);
		return request;
	}
	
	private <T> void addCallback(final JSONRequest request,
			final AsyncCallback<T> callback) {
		if (callback == null) {
			return;
		}
		final TypeUtil<T> type = TypeUtil.resolve(callback);
		
		// Create a callback to retrieve a JSONResponse and extract the result
		// or error from this. This is double nested, mostly because of the type
		// conversions required on the result.
		final AsyncCallback<JSONResponse> responseCallback = new AsyncCallback<JSONResponse>() {
			@Override
			public void onSuccess(final JSONResponse response) {
				final Exception err = response.getError();
				if (err != null) {
					callback.onFailure(err);
				}
				if (type != null
						&& !type.getJavaType().getRawClass().equals(Void.class)) {
					try {
						final T res = type.inject(response.getResult());
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
			
			@Override
			public void onFailure(final Exception exception) {
				callback.onFailure(exception);
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
	 * 
	 * @see com.almende.eve.capabilities.Capability#getParams()
	 */
	@Override
	public ObjectNode getParams() {
		return myParams;
	}
	
}
