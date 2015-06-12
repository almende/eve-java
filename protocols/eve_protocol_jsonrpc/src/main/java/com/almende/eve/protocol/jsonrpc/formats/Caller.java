/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.protocol.jsonrpc.formats;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.List;

import com.almende.util.TypeUtil;
import com.almende.util.callback.AsyncCallback;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Interface Caller, can be used to provide capabilities the possibility to
 * send messages on behalf of the entity implementing this interface.
 */
public interface Caller {

	/**
	 * Gets the sender addresses known to this caller.
	 * 
	 * @return the urls
	 */
	List<URI> getSenderUrls();

	/**
	 * Send async, expecting a response through the given callback.
	 * 
	 * @param <T>
	 *            the generic type of the result, controlled by the TypeUtil
	 *            injector.
	 * @param url
	 *            the address of the other agent
	 * @param method
	 *            the remote RPC method
	 * @param params
	 *            the remote RPC method's params
	 * @param callback
	 *            A callback with the expected result type.
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	<T> void call(final URI url, final String method, final ObjectNode params,
			final AsyncCallback<T> callback) throws IOException;

	/**
	 * Send async, expecting a response through the given callback.
	 * 
	 * @param <T>
	 *            the generic type of the result, controlled by the TypeUtil
	 *            injector.
	 * @param url
	 *            the address of the other agent
	 * @param method
	 *            the remote RPC method
	 * @param params
	 *            the remote RPC method's params
	 * @param callback
	 *            A callback with the expected result type.
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	<T> void call(final URI url, final Method method, final Object[] params,
			final AsyncCallback<T> callback) throws IOException;

	/**
	 * Send JSON-RPC notification, expecting no response.
	 * 
	 * @param url
	 *            the address of the other agent
	 * @param method
	 *            the remote RPC method
	 * @param params
	 *            the remote RPC method's params
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	void call(final URI url, final String method, final ObjectNode params)
			throws IOException;

	/**
	 * Send JSON-RPC notification, expecting no response.
	 * 
	 * @param url
	 *            the address of the other agent
	 * @param method
	 *            the remote RPC method
	 * @param params
	 *            the remote RPC method's params
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	void call(final URI url, final Method method, final Object[] params)
			throws IOException;

	/**
	 * Send JSON-RPC Message, could be a request, notification or a reponse.
	 * 
	 * @param url
	 *            the address of the other agent
	 * @param request
	 *            the JSONRequest to be send to the other agent
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	void call(final URI url, final JSONMessage request) throws IOException;

	/**
	 * Send JSON-RPC Message, could be a request, notification or a response.
	 * 
	 * @param url
	 *            the address of the other agent
	 * @param request
	 *            the JSONMessage to be send to the other agent
	 * @param tag
	 *            the tag for mapping this call to an earlier inbound call
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	<T> void call(final URI url, final JSONMessage request, final String tag)
			throws IOException;

	/**
	 * Send synchronous request, waiting for a response.
	 *
	 * @param <T>
	 *            the generic type of the result, controlled by the TypeUtil
	 *            injector.
	 * @param url
	 *            the address of the other agent
	 * @param method
	 *            the remote RPC method
	 * @param params
	 *            the remote RPC method's params
	 * @param clazz
	 *            the expected result type, in the form of a class.
	 * @return the result, cast/converted to the given type.
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	<T> T callSync(final URI url, final String method, final ObjectNode params,
			final Class<T> clazz) throws IOException;

	/**
	 * Send synchronous request, waiting for a response.
	 *
	 * @param <T>
	 *            the generic type of the result, controlled by the TypeUtil
	 *            injector.
	 * @param url
	 *            the address of the other agent
	 * @param method
	 *            the remote RPC method
	 * @param params
	 *            the remote RPC method's params
	 * @param type
	 *            the expected result type, in the form of a TypeUtil injector.
	 * @return the result, cast/converted to the given type.
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	<T> T callSync(final URI url, final String method, final ObjectNode params,
			final TypeUtil<T> type) throws IOException;

	/**
	 * Send synchronous request, waiting for a response.
	 *
	 * @param <T>
	 *            the generic type of the result, controlled by the TypeUtil
	 *            injector.
	 * @param url
	 *            the address of the other agent
	 * @param method
	 *            the remote RPC method
	 * @param params
	 *            the remote RPC method's params
	 * @param type
	 *            the expected result type, in the form of a Jackson JavaType.
	 * @return the result, cast/converted to the given type.
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	<T> T callSync(URI url, String method, ObjectNode params, JavaType type)
			throws IOException;

	/**
	 * Send synchronous request, waiting for a response.
	 *
	 * @param <T>
	 *            the generic type of the result, controlled by the TypeUtil
	 *            injector.
	 * @param url
	 *            the address of the other agent
	 * @param method
	 *            the remote RPC method
	 * @param params
	 *            the remote RPC method's params
	 * @param type
	 *            the expected result type, in the form of a Java Type.
	 * @return the result, cast/converted to the given type.
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	<T> T callSync(URI url, String method, ObjectNode params, Type type)
			throws IOException;

}
