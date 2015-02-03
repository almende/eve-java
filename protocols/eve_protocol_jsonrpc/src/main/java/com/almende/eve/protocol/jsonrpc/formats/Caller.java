/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.protocol.jsonrpc.formats;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;

import com.almende.util.TypeUtil;
import com.almende.util.callback.AsyncCallback;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Interface Receiver, implementations will be able to receive messages from
 * Eve's transport service.
 */
public interface Caller {

	/**
	 * Send async.
	 * 
	 * @param <T>
	 *            the generic type
	 * @param url
	 *            the url
	 * @param method
	 *            the method
	 * @param params
	 *            the params
	 * @param callback
	 *            the callback
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	<T> void call(final URI url, final String method, final ObjectNode params,
			final AsyncCallback<T> callback) throws IOException;

	/**
	 * Send async for usage in proxies.
	 * 
	 * @param <T>
	 *            the generic type
	 * @param url
	 *            the url
	 * @param method
	 *            the method
	 * @param params
	 *            the params
	 * @param callback
	 *            the callback
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	<T> void call(final URI url, final Method method, final Object[] params,
			final AsyncCallback<T> callback) throws IOException;

	/**
	 * Send async.
	 * 
	 * @param <T>
	 *            the generic type
	 * @param url
	 *            the url
	 * @param method
	 *            the method
	 * @param params
	 *            the params
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	<T> void call(final URI url, final String method, final ObjectNode params)
			throws IOException;

	/**
	 * Call.
	 *
	 * @param <T>
	 *            the generic type
	 * @param url
	 *            the url
	 * @param method
	 *            the method
	 * @param params
	 *            the params
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	<T> void call(final URI url, final Method method, final Object[] params)
			throws IOException;

	/**
	 * Call sync.
	 *
	 * @param <T>
	 *            the generic type
	 * @param url
	 *            the url
	 * @param method
	 *            the method
	 * @param params
	 *            the params
	 * @return the t
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	<T> T callSync(final URI url, final String method, final ObjectNode params)
			throws IOException;

	/**
	 * Call sync.
	 *
	 * @param <T>
	 *            the generic type
	 * @param url
	 *            the url
	 * @param method
	 *            the method
	 * @param params
	 *            the params
	 * @param clazz
	 *            the clazz
	 * @return the t
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	<T> T callSync(final URI url, final String method, final ObjectNode params,
			final Class<T> clazz) throws IOException;

	/**
	 * Call sync.
	 *
	 * @param <T>
	 *            the generic type
	 * @param url
	 *            the url
	 * @param method
	 *            the method
	 * @param params
	 *            the params
	 * @param type
	 *            the type
	 * @return the t
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	<T> T callSync(final URI url, final String method, final ObjectNode params,
			final TypeUtil<T> type) throws IOException;

}
