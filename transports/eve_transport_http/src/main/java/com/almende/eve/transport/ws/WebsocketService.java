/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.transport.ws;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.websocket.server.ServerEndpointConfig;

import com.almende.eve.capabilities.Capability;
import com.almende.eve.capabilities.handler.Handler;
import com.almende.eve.transport.Receiver;
import com.almende.eve.transport.Transport;
import com.almende.eve.transport.TransportService;
import com.almende.eve.transport.http.ServletLauncher;
import com.almende.util.ClassUtil;
import com.almende.util.TypeUtil;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class WebsocketService.
 */
public class WebsocketService implements TransportService {
	private static final Logger					LOG			= Logger.getLogger(WebsocketService.class
																	.getName());
	private static final WebsocketService		singleton	= new WebsocketService();
	private final Map<URI, WebsocketTransport>	transports	= new HashMap<URI, WebsocketTransport>();
	
	/**
	 * Instantiates a new webserver client service.
	 */
	public WebsocketService() {
	}
	
	/**
	 * Gets the instance by params.
	 * 
	 * @param params
	 *            the params
	 * @return the instance by params
	 */
	public static WebsocketService getInstanceByParams(final ObjectNode params) {
		return singleton;
	}
	
	/* (non-Javadoc)
	 * @see com.almende.eve.capabilities.CapabilityService#get(com.fasterxml.jackson.databind.node.ObjectNode, com.almende.eve.capabilities.handler.Handler, java.lang.Class)
	 */
	@Override
	public <T extends Capability, V> T get(ObjectNode params,
			Handler<V> handle, Class<T> type) {
		final Handler<Receiver> newHandle = Transport.TYPEUTIL.inject(handle);
		final WebsocketTransportConfig config = new WebsocketTransportConfig(
				params);
		WebsocketTransport result = null;
		if (config.isServer()) {
			final String address = config.getAddress();
			
			if (address != null) {
				try {
					final URI serverUri = new URI(address);
					if (transports.containsKey(serverUri)) {
						result = transports.get(serverUri);
						result.getHandle().update(newHandle);
					} else {
						result = new WsServerTransport(serverUri, newHandle,
								this, params);
						transports.put(serverUri, result);
						String servletLauncher = config.getServletLauncher();
						if (servletLauncher != null) {
							if (servletLauncher.equals("JettyLauncher")) {
								servletLauncher = "com.almende.eve.transport.http.embed.JettyLauncher";
							}
							try {
								final Class<?> launcherClass = Class
										.forName(servletLauncher);
								if (!ClassUtil.hasInterface(launcherClass,
										ServletLauncher.class)) {
									throw new IllegalArgumentException(
											"ServletLauncher class "
													+ launcherClass.getName()
													+ " must implement "
													+ ServletLauncher.class
															.getName());
								}
								final ServletLauncher launcher = (ServletLauncher) launcherClass
										.newInstance();
								
								ServerEndpointConfig sec = ServerEndpointConfig.Builder
										.create(WebsocketEndpoint.class,
												serverUri.getPath()).build();
								sec.getUserProperties().put("address",
										serverUri);
								
								launcher.add(sec, params);
								
							} catch (final Exception e1) {
								LOG.log(Level.WARNING,
										"Failed to load servlet in servletlauncher!",
										e1);
							}
						}
					}
				} catch (final URISyntaxException e) {
					LOG.log(Level.WARNING, "Couldn't parse address:" + address,
							e);
				}
			} else {
				LOG.warning("Parameter 'address' is required.");
			}
		} else {
			final String id = config.getId();
			if (id != null) {
				try {
					final URI key = new URI("wsclient:" + id);
					
					if (transports.containsKey(key)) {
						result = transports.get(key);
						result.getHandle().update(newHandle);
					} else {
						result = new WsClientTransport(key, newHandle, this,
								params);
						transports.put(key, result);
					}
				} catch (final URISyntaxException e) {
					LOG.log(Level.WARNING,
							"Couldn't parse Client Url: wsclient:" + id, e);
				}
			} else {
				LOG.warning("Parameter 'id' is required.");
			}
		}
		return TypeUtil.inject(result, type);
	}
	
	/* (non-Javadoc)
	 * @see com.almende.eve.transport.TransportService#delete(com.almende.eve.transport.Transport)
	 */
	@Override
	public void delete(Transport instance) {
		transports.remove(instance.getAddress());
	}
	
	/* (non-Javadoc)
	 * @see com.almende.eve.transport.TransportService#getLocal(java.net.URI)
	 */
	@Override
	public Transport getLocal(final URI address) {
		return null;
	}
	
	/**
	 * Gets the transport.
	 * 
	 * @param address
	 *            the address
	 * @return the transport
	 */
	public WebsocketTransport getTransport(final URI address) {
		return transports.get(address);
	}
	
	/**
	 * Gets the transport.
	 * 
	 * @param address
	 *            the address
	 * @return the ws client transport
	 */
	public static WebsocketTransport get(final URI address) {
		return singleton.getTransport(address);
	}
}
