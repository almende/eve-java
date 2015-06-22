/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.protocol.jsonrpc;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.almende.eve.capabilities.handler.Handler;
import com.almende.eve.capabilities.handler.SimpleHandler;
import com.almende.eve.protocol.Meta;
import com.almende.eve.protocol.auth.Authorizor;
import com.almende.eve.protocol.auth.DefaultAuthorizor;
import com.almende.eve.protocol.jsonrpc.formats.Caller;
import com.almende.eve.protocol.jsonrpc.formats.JSONMessage;
import com.almende.eve.protocol.jsonrpc.formats.JSONRPCException;
import com.almende.eve.protocol.jsonrpc.formats.JSONRequest;
import com.almende.eve.protocol.jsonrpc.formats.JSONResponse;
import com.almende.util.TypeUtil;
import com.almende.util.callback.AsyncCallback;
import com.almende.util.callback.AsyncCallbackStore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class JSONRpcProtocol.
 */
public class JSONRpcProtocol implements RpcBasedProtocol {
	private static final Logger						LOG					= Logger.getLogger(JSONRpcProtocol.class
																				.getName());
	private static final TypeUtil<JSONResponse>		JSONRESPONSETYPE	= new TypeUtil<JSONResponse>() {};
	private final AsyncCallbackStore<JSONResponse>	callbacks;
	private final Handler<Object>					destination;
	private Handler<Caller>							caller				= null;
	private Handler<Authorizor>						auth				= new SimpleHandler<Authorizor>(
																				new DefaultAuthorizor());
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
		callbacks = new AsyncCallbackStore<JSONResponse>("Rpc_"
				+ myParams.getId());
		callbacks.setTimeout(myParams.getCallbackTimeout());
	}

	/**
	 * Sets the caller.
	 *
	 * @param caller
	 *            the new caller
	 */
	public void setCaller(final Handler<Caller> caller) {
		this.caller = caller;
	}

	@Override
	public boolean inbound(final Meta input) {
		final JSONResponse response = invoke(input.getMsg(), input.getPeer());
		if (response != null) {
			if (caller == null) {
				LOG.warning("JSONRpcProtocol has response, but no caller given.");
				return false;
			}
			try {
				caller.get().call(input.getPeer(), response, input.getTag());
			} catch (IOException e) {
				LOG.log(Level.WARNING, "Couldn't send response", e);
			}
		} else if (input.getTag() != null) {
			// Always send a response if tag is set. (also a response on a
			// response, for simulation feedback)
			final JSONResponse ok = new JSONResponse();
			try {
				caller.get().call(input.getPeer(), ok, input.getTag());
			} catch (IOException e) {
				LOG.log(Level.WARNING, "Couldn't send response", e);
			}
		}
		// TODO: currently not calling next on protocol stack, in the future use
		// this as a filter, sometimes forward.
		return true;
	}

	public boolean outbound(final Meta output) {
		if (output.getMsg() instanceof JSONRequest) {
			final JSONRequest request = (JSONRequest) output.getMsg();
			addCallback(request, request.getCallback());
		}
		return output.nextOut();
	}

	/**
	 * Gets the auth.
	 * 
	 * @return the auth
	 */
	public Authorizor getAuth() {
		return auth.get();
	}

	/**
	 * Sets the auth.
	 * 
	 * @param auth
	 *            the new auth
	 */
	public void setAuth(final Handler<Authorizor> auth) {
		this.auth = auth;
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
		final JSONMessage jsonMsg = JSONMessage.jsonConvert(msg);
		if (jsonMsg == null) {
			LOG.log(Level.INFO, "Received non-JSONRPC message:'" + msg + "'");
			return null;
		}
		final JsonNode id = jsonMsg.getId();
		try {
			if (jsonMsg.isRequest()) {
				final JSONRequest request = (JSONRequest) jsonMsg;
				return JSONRpc.invoke(destination.get(), request, senderUrl,
						auth.get());
			} else if (jsonMsg.isResponse() && callbacks != null && id != null
					&& !id.isNull()) {
				final AsyncCallback<JSONResponse> callback = callbacks.get(id);
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
	public ObjectNode getMethods() {
		return JSONRpc.describe(getHandle().get(), auth.get());
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
		final AsyncCallback<JSONResponse> responseCallback = new AsyncCallback<JSONResponse>(
				JSONRESPONSETYPE) {
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
			callbacks.put(((JSONMessage) request).getId(),
					"Outbound message callback.", responseCallback);
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
		JSONRpcProtocolBuilder.delete(myParams.getId());
	}

}
