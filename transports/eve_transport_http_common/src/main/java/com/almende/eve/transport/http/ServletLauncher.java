/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.transport.http;

import java.net.URI;
import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.websocket.server.ServerEndpointConfig;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Interface ServletLauncher.
 * 
 * @author ludo
 *         Interface to plugin embedded Servlet environments, e.g. see
 *         http-embedded for a jetty example.
 */
public interface ServletLauncher {

	/**
	 * Add a servlet to a ServletContainer, and potentially starting the
	 * container if required.
	 *
	 * @param servlet
	 *            the servlet
	 * @param servletPath
	 *            the servlet path
	 * @param config
	 *            the config
	 * @throws ServletException
	 *             the servlet exception
	 */
	void add(final Servlet servlet, final URI servletPath,
			final ObjectNode config) throws ServletException;

	/**
	 * Adds a Websocket Server configuration, and potentially starting the
	 * container if required.
	 *
	 * @param serverConfig
	 *            the server config
	 * @param config
	 *            the config
	 * @throws ServletException
	 *             the servlet exception
	 */
	void add(ServerEndpointConfig serverConfig, ObjectNode config)
			throws ServletException;

	/**
	 * Adds the filter.
	 *
	 * @param filterpath
	 *            the filterpath
	 * @param path
	 *            the path
	 * @param initParams
	 *            the init params for this filter - leave null if non.
	 */
	void addFilter(String filterpath, String path,
			Map<String, String> initParams);
}
