/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.agent;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.almende.eve.agent.annotation.ProxyAsync;
import com.almende.eve.protocol.jsonrpc.formats.JSONRPCException;
import com.almende.eve.protocol.jsonrpc.formats.JSONRPCException.CODE;
import com.almende.util.AnnotationUtil;
import com.almende.util.AnnotationUtil.AnnotatedClass;
import com.almende.util.AnnotationUtil.AnnotatedMethod;
import com.almende.util.TypeUtil;
import com.almende.util.callback.SyncCallback;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * A factory for creating AgentProxy objects.
 */
public final class AgentProxyFactory {

	private AgentProxyFactory() {}

	/**
	 * Gen proxy.
	 * 
	 * @param <T>
	 *            the generic type
	 * @param sender
	 *            the sender
	 * @param receiverUrl
	 *            the receiver url
	 * @param proxyInterface
	 *            the interface this proxy implements
	 * @return the t
	 */
	@SuppressWarnings("unchecked")
	public static <T> T genProxy(final Agent sender, final URI receiverUrl,
			final Class<T> proxyInterface) {
		// http://docs.oracle.com/javase/1.4.2/docs/guide/reflection/proxy.html
		final T proxy = (T) Proxy.newProxyInstance(
				proxyInterface.getClassLoader(),
				new Class[] { proxyInterface }, new InvocationHandler() {

					private Map<Method, Boolean>	cache	= new HashMap<Method, Boolean>();

					@Override
					public Object invoke(final Object proxy,
							final Method method, final Object[] args) {
						boolean doSync = true;
						if (cache.containsKey(method)) {
							doSync = cache.get(method);
						} else {
							AnnotatedClass clazz = AnnotationUtil
									.get(proxyInterface);
							if (clazz != null) {
								List<AnnotatedMethod> list = clazz
										.getMethods(method.getName());
								for (AnnotatedMethod m : list) {
									if (m.getAnnotation(ProxyAsync.class) != null) {
										doSync = false;
									}
								}
								if (doSync
										&& method.getReturnType().equals(
												void.class)
										&& clazz.getAnnotation(ProxyAsync.class) != null) {
									doSync = false;
								}
							}
							cache.put(method, doSync);
						}
						SyncCallback<JsonNode> callback = null;
						if (doSync) {
							callback = new SyncCallback<JsonNode>() {};
						}
						try {
							sender.call(receiverUrl, method, args, callback);
						} catch (final IOException e) {
							throw new JSONRPCException(CODE.REMOTE_EXCEPTION, e
									.getLocalizedMessage(), e);
						}
						if (callback != null) {
							try {
								return TypeUtil.inject(callback.get(),
										method.getGenericReturnType());
							} catch (final Exception e) {
								throw new JSONRPCException(
										CODE.REMOTE_EXCEPTION, e
												.getLocalizedMessage(), e);
							}
						}
						return null;
					}
				});
		return proxy;
	}
}
