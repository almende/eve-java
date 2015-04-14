/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.transport.ws;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.websocket.server.ServerEndpointConfig;

import com.almende.eve.capabilities.AbstractCapabilityBuilder;
import com.almende.eve.capabilities.Capability;
import com.almende.eve.capabilities.handler.Handler;
import com.almende.eve.transport.Receiver;
import com.almende.eve.transport.Transport;
import com.almende.eve.transport.TransportService;
import com.almende.eve.transport.http.ServletLauncher;
import com.almende.util.ClassUtil;
import com.almende.util.URIUtil;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class WebsocketService.
 */
public class WebsocketTransportBuilder extends
		AbstractCapabilityBuilder<WebsocketTransport> {
	private static final Logger		LOG			= Logger.getLogger(WebsocketService.class
														.getName());
	private static WebsocketService	singleton	= null;

	@Override
	public WebsocketTransport build() {
		if (singleton == null) {
			singleton = new WebsocketService();
		}
		return singleton.get(getParams(), getHandle());
	}

	/**
	 * Gets the transport.
	 * 
	 * @param address
	 *            the address
	 * @return the ws client transport
	 */
	public static WebsocketTransport get(final URI address) {
		if (singleton != null) {
			return singleton.getTransport(address);
		}
		return null;
	}

	class WebsocketService implements TransportService {
		private final Map<URI, WebsocketTransport>	transports	= new HashMap<URI, WebsocketTransport>();

		/**
		 * Gets the.
		 * 
		 * @param <T>
		 *            the generic type
		 * @param <V>
		 *            the value type
		 * @param params
		 *            the params
		 * @param handle
		 *            the handle
		 * @return the websocket transport
		 */
		public <T extends Capability, V> WebsocketTransport get(
				final ObjectNode params, final Handler<V> handle) {
			final Handler<Receiver> newHandle = Transport.TYPEUTIL
					.inject(handle);
			final WebsocketTransportConfig config = WebsocketTransportConfig
					.decorate(params);
			if (config.isServer()) {
				return getServer(config, newHandle);
			} else {
				return getClient(config, newHandle);
			}
		}

		private WebsocketTransport getServer(
				final WebsocketTransportConfig config,
				final Handler<Receiver> handle) {
			WebsocketTransport result = null;
			final String address = config.getAddress();

			if (address != null) {
				try {
					final URI serverUri = URIUtil.parse(address);
					if (transports.containsKey(serverUri)) {
						result = transports.get(serverUri);
						result.getHandle().update(handle);
					} else {
						result = new WsServerTransport(serverUri, handle, this,
								config);
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

								final ServerEndpointConfig sec = ServerEndpointConfig.Builder
										.create(WebsocketEndpoint.class,
												serverUri.getPath()).build();
								sec.getUserProperties().put("address",
										serverUri);

								launcher.add(sec, config);

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
			return result;
		}

		private WebsocketTransport getClient(
				final WebsocketTransportConfig config,
				final Handler<Receiver> handle) {
			WebsocketTransport result = null;
			final String id = config.getId();
			if (id != null) {
				try {
					final URI key = URIUtil.parse("wsclient:" + id);
					LOG.log(Level.WARNING, "Looking up:" + key);
					if (transports.containsKey(key)) {
						result = transports.get(key);
						if (!(WebsocketTransportConfig.decorate(result
								.getParams())).getServerUrl().equals(
								config.getServerUrl())) {
							((WsClientTransport) result).updateConfig(config);
						}
						result.getHandle().update(handle);
					} else {
						result = new WsClientTransport(key, handle, this,
								config);
						transports.put(key, result);
					}
				} catch (final URISyntaxException e) {
					LOG.log(Level.WARNING,
							"Couldn't parse Client Url: wsclient:" + id, e);
				} catch (IOException e) {
					LOG.log(Level.WARNING,
							"Couldn't reconnect with new config.", e);
				}
			} else {
				LOG.warning("Parameter 'id' is required.");
			}
			return result;
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * com.almende.eve.transport.TransportService#delete(com.almende.eve.
		 * transport
		 * .Transport)
		 */
		@Override
		public void delete(final Transport instance) {
			transports.remove(instance.getAddress());
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * com.almende.eve.transport.TransportService#getLocal(java.net.URI)
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

	}
}
