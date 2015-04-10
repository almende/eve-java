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

import com.almende.eve.capabilities.AbstractCapabilityBuilder;
import com.almende.util.URIUtil;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class HttpService.
 */
public class HttpTransportBuilder extends AbstractCapabilityBuilder<HttpTransport> {
	private static final Logger				LOG			= Logger.getLogger(HttpTransportBuilder.class
																.getName());
	private static Map<URI, HttpService>	services	= new HashMap<URI, HttpService>();
	
	@Override
	public HttpTransport build() {
		final HttpService service = getInstanceByParams(getParams());
		if (service != null){
			return service.get(getParams(), getHandle());
		} else {
			LOG.warning("Couldn't initiate HttpService!");
			return null;
		}
	}
	
	/**
	 * Gets the instance by params.
	 * 
	 * @param params
	 *            the params
	 * @return the instance by params
	 */
	public HttpService getInstanceByParams(final ObjectNode params) {
		HttpService service = null;
		final HttpTransportConfig config = new HttpTransportConfig(params);
		final String servletUrl = config.getServletUrl();
		if (servletUrl != null) {
			try {
				final URI servletUri = URIUtil.parse(servletUrl);
				if (services.containsKey(servletUri)) {
					// Shortcut, it already exists and is launched.
					return services.get(servletUri);
				}
				service = new HttpService(servletUri, params);
				services.put(servletUri, service);
				
			} catch (final URISyntaxException e) {
				LOG.log(Level.WARNING, "Couldn't parse 'servletUrl'", e);
			}
		} else {
			LOG.warning("Parameter 'servletUrl' is required.");
		}
		return service;
	}

	static final Map<URI, HttpService> getServices() {
		return services;
	}
}
