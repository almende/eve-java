/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.transport.http;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.Servlet;

import com.almende.eve.capabilities.Capability;
import com.almende.eve.capabilities.handler.Handler;
import com.almende.eve.transport.Receiver;
import com.almende.eve.transport.Transport;
import com.almende.eve.transport.TransportService;
import com.almende.util.ClassUtil;
import com.almende.util.URIUtil;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class HttpService.
 */
public class HttpService implements TransportService {
	private static final Logger				LOG			= Logger.getLogger(HttpService.class
																.getName());
	private URI								myUrl		= null;
	private final Map<URI, HttpTransport>	transports	= new HashMap<URI, HttpTransport>();
	private HttpTransportConfig				myParams	= null;

	/**
	 * Instantiates a new http service.
	 * 
	 * @param servletUrl
	 *            the servlet url
	 * @param params
	 *            the params
	 */
	public HttpService(final URI servletUrl, final ObjectNode params) {
		myUrl = servletUrl;
		myParams = HttpTransportConfig.decorate(params);

		String servletLauncher = myParams.getServletLauncher();
		if (servletLauncher != null) {
			if (servletLauncher.equals("JettyLauncher")) {
				servletLauncher = "com.almende.eve.transport.http.embed.JettyLauncher";
			}
			try {
				final Class<?> launcherClass = Class.forName(servletLauncher);
				if (!ClassUtil.hasInterface(launcherClass,
						ServletLauncher.class)) {
					throw new IllegalArgumentException("ServletLauncher class "
							+ launcherClass.getName() + " must implement "
							+ ServletLauncher.class.getName());
				}
				final ServletLauncher launcher = (ServletLauncher) launcherClass
						.newInstance();

				final Class<?> servletClass = Class.forName(myParams
						.getServletClass());
				if (!ClassUtil.hasInterface(servletClass, Servlet.class)) {
					throw new IllegalArgumentException("Servlet class "
							+ servletClass.getName() + " must implement "
							+ Servlet.class.getName());
				}
				final Servlet servlet = (Servlet) servletClass.getConstructor(
						URI.class).newInstance(myUrl);
				if (servlet != null) {
					launcher.add(servlet, myUrl, params);
				} else {
					LOG.log(Level.WARNING, "Couldn't instantiate servlet!");
				}

			} catch (final Exception e1) {
				LOG.log(Level.WARNING,
						"Failed to load servlet in servletlauncher!", e1);
			}
		}
	}

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
	 * @return the http transport
	 */
	public <T extends Capability, V> HttpTransport get(final ObjectNode params,
			final Handler<V> handle) {
		final Handler<Receiver> newHandle = Transport.TYPEUTIL.inject(handle);
		final HttpTransportConfig config = HttpTransportConfig.decorate(params);
		HttpTransport result = null;
		final String id = config.getId();
		if (id != null) {
			try {
				final URI fullUrl = URIUtil.parse(myUrl.toASCIIString() + id);
				if (transports.containsKey(fullUrl)) {
					result = transports.get(fullUrl);
					result.getHandle().update(newHandle);
				} else {
					result = new HttpTransport(fullUrl, newHandle, this, params);
					transports.put(fullUrl, result);
				}
			} catch (final URISyntaxException e) {
				LOG.log(Level.WARNING, "Couldn't parse full Url:" + myUrl
						+ params.get("id").asText(), e);
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

	/**
	 * Gets the transport
	 * 
	 * @param id
	 *            the id
	 * @return the http transport
	 */
	private HttpTransport get(final String id) {
		try {
			final URI fullUrl = URIUtil.parse(myUrl + id);
			return transports.get(fullUrl);
		} catch (final URISyntaxException e) {
			LOG.log(Level.WARNING, "Couldn't parse full Url:" + myUrl + id, e);
		}
		return null;
	}

	/**
	 * Gets a pre-existing HttpTransport by URI and id. Called from Servlets
	 * to
	 * retrieve the HttpTransport. Returns null if no callbacks have been
	 * registered for the given URL/id combination.
	 * 
	 * @param servletUrl
	 *            the servlet url
	 * @param id
	 *            the id
	 * @return the http transport
	 */
	public static HttpTransport get(final URI servletUrl, final String id) {
		final HttpService service = HttpTransportBuilder.getServices().get(
				servletUrl);
		if (service != null) {
			return service.get(id);
		}
		return null;
	}

	/**
	 * Should the Servlet handle authentication?.
	 * 
	 * @param servletUrl
	 *            the servlet url
	 * @return true, if authentication is needed.
	 */
	public static boolean doAuthentication(final URI servletUrl) {
		final HttpService service = HttpTransportBuilder.getServices().get(
				servletUrl);
		if (service != null) {
			return service.doAuthentication();
		}
		return false;
	}

	/**
	 * Should the Servlet handle authentication?
	 * 
	 * @return true, if authentication is needed.
	 */
	private boolean doAuthentication() {
		return myParams.getDoAuthentication();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.almende.eve.transport.TransportService#getLocal(java.net.URI)
	 */
	@Override
	public Transport getLocal(final URI address) {
		if (!myParams.getDoShortcut()) {
			return null;
		}
		if (transports.containsKey(address)) {
			return transports.get(address);
		} else {
			// TODO: check for other HttpServices with this address?
		}
		return null;
	}
}