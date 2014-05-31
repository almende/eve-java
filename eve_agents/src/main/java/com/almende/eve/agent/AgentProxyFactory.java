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

import com.almende.eve.transform.rpc.formats.JSONRPCException;
import com.almende.eve.transform.rpc.formats.JSONRPCException.CODE;
import com.almende.util.TypeUtil;
import com.almende.util.callback.SyncCallback;

/**
 * A factory for creating AgentProxy objects.
 */
public final class AgentProxyFactory {
	
	private AgentProxyFactory() {
	}
	
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
					
					@Override
					public Object invoke(final Object proxy,
							final Method method, final Object[] args) {
						
						final SyncCallback<Object> callback = new SyncCallback<Object>(){};
						try {
							sender.send(receiverUrl, method, args, callback);
						} catch (final IOException e) {
							throw new JSONRPCException(CODE.REMOTE_EXCEPTION, e
									.getLocalizedMessage(), e);
						}
						
						try {
							return TypeUtil.inject(callback.get(),
									method.getGenericReturnType());
						} catch (final Exception e) {
							throw new JSONRPCException(CODE.REMOTE_EXCEPTION, e
									.getLocalizedMessage(), e);
						}
					}
				});
		return proxy;
	}
}
