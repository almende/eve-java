/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.transport.http;

import java.net.URI;
import java.util.Map;

import javax.servlet.Servlet;


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
	 */
	void add(final Servlet servlet, final URI servletPath, final Map<String,Object> config);
}
