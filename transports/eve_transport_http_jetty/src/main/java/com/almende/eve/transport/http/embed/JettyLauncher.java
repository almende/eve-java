/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.transport.http.embed;

import java.net.URI;
import java.util.EnumSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.DispatcherType;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.websocket.DeploymentException;
import javax.websocket.server.ServerContainer;
import javax.websocket.server.ServerEndpointConfig;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;

import com.almende.eve.transport.http.ServletLauncher;
import com.almende.util.jackson.JOM;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class JettyLauncher.
 */
public class JettyLauncher implements ServletLauncher {
	private static final Logger				LOG			= Logger.getLogger(JettyLauncher.class
																.getName());
	protected static Server					server		= null;
	protected static ServletContextHandler	context		= null;
	protected static ServerContainer		wscontainer	= null;

	/**
	 * Inits the server.
	 *
	 * @param params
	 *            the params
	 * @throws ServletException
	 *             the servlet exception
	 */
	public void initServer(final ObjectNode params) throws ServletException {
		int port = 8080;
		if (params != null && params.has("port")) {
			port = params.get("port").asInt();
		}
		server = new Server(port);
		context = new ServletContextHandler(ServletContextHandler.SESSIONS
				| ServletContextHandler.NO_SECURITY);

		context.setContextPath("/");
		server.setHandler(context);
		wscontainer = WebSocketServerContainerInitializer
				.configureContext(context);

		if (params != null && params.has("cors")) {
			String corsClass = "com.thetransactioncompany.cors.CORSFilter";
			if (params.get("cors").has("class")) {
				corsClass = params.get("cors").get("class").asText();
			}
			String corsPath = "/*";
			if (params.get("cors").has("path")) {
				corsPath = params.get("cors").get("path").asText();
			}
			addFilter(corsClass, corsPath);
		}

		try {
			server.start();
		} catch (final Exception e) {
			LOG.log(Level.SEVERE, "Couldn't start embedded Jetty server!", e);
		}

	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.almende.eve.transport.http.ServletLauncher#add(javax.servlet.Servlet,
	 * java.net.URI, com.almende.eve.config.Config)
	 */
	@Override
	public void add(final Servlet servlet, final URI servletPath,
			final ObjectNode config) throws ServletException {
		// TODO: config hierarchy...
		if (server == null) {
			if (config != null) {
				initServer((ObjectNode) config.get("jetty"));
			} else {
				initServer(JOM.createObjectNode());
			}
		}
		LOG.info("Registering servlet:" + servletPath.getPath());
		ServletHolder sh = new ServletHolder(servlet);

		if (config.has("initParams")) {
			ArrayNode params = (ArrayNode) config.get("initParams");
			for (JsonNode param : params) {
				LOG.warning("Setting init param:" + param.toString());
				sh.setInitParameter(param.get("key").asText(),
						param.get("value").asText());
			}

		}

		context.addServlet(sh, servletPath.getPath() + "*");
	}

	@Override
	public void addFilter(final String filterpath, final String path) {
		LOG.info("Adding filter:" + filterpath + " / " + path);
		context.addFilter(filterpath, path,
				EnumSet.of(DispatcherType.INCLUDE, DispatcherType.REQUEST));
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.almende.eve.transport.http.ServletLauncher#add(javax.servlet.Servlet,
	 * java.net.URI, com.almende.eve.config.Config)
	 */
	@Override
	public void add(final ServerEndpointConfig serverConfig,
			final ObjectNode config) throws ServletException {
		// TODO: config hierarchy...
		if (server == null) {
			if (config != null) {
				initServer((ObjectNode) config.get("jetty"));
			} else {
				initServer(JOM.createObjectNode());
			}
		}
		LOG.info("Registering websocket server endpoint:"
				+ serverConfig.getPath());
		try {
			wscontainer.addEndpoint(serverConfig);
		} catch (final DeploymentException e) {
			LOG.log(Level.WARNING,
					"Couldn't initialize websocket server endpoint.", e);
		}
	}
}
