package com.almende.eve.transport.http.embed;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.Servlet;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import com.almende.eve.config.Config;
import com.almende.eve.transport.http.ServletLauncher;

public class JettyLauncher implements ServletLauncher {
	private static final Logger				LOG		= Logger.getLogger(JettyLauncher.class
															.getName());
	private static Server					server	= null;
	private static ServletContextHandler	context	= null;
	
	public void initServer(final Map<String, Object> params) {
		int port = 8080;
		if (params != null && params.containsKey("port")) {
			port = (Integer)params.get("port");
		}
		server = new Server(port);
		
		context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		
		context.setContextPath("/");
		server.setHandler(context);
		
		try {
			server.start();
		} catch (Exception e) {
			LOG.log(Level.SEVERE, "Couldn't start embedded Jetty server!", e);
		}
	}
	
	@SuppressWarnings("unchecked")
	public void add(final Servlet servlet, final URI servletPath,
			final Config config) {
		if (server == null) {
			if (config != null) {
				initServer((Map<String, Object>) config.get("jetty"));
			} else {
				initServer(new HashMap<String, Object>());
			}
		}
		LOG.info("Registering servlet:" + servletPath.getPath());
		context.addServlet(new ServletHolder(servlet), servletPath.getPath()
				+ "*");
	}
}
