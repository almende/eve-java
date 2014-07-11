/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.deploy;

import java.io.InputStream;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * The listener interface for receiving ServletContext events.
 */
public class EveListener implements ServletContextListener {
	private static final Logger	LOG	= Logger.getLogger(EveListener.class
											.getName());
	
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		final ServletContext sc = sce.getServletContext();
		
		// Get the eve.yaml file:
		String path = sc.getInitParameter("eve_config");
		if (path != null && !path.isEmpty()) {
			final String fullname = "/WEB-INF/" + path;
			LOG.info("loading configuration file '" + sc.getRealPath(fullname)
					+ "'...");
			final InputStream is = sc.getResourceAsStream(fullname);
			if (is == null) {
				LOG.warning("Can't find the given configuration file:"
						+ sc.getRealPath(fullname));
				return;
			}

			Boot.loadAgents(is);
		}
		
	}
	
	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		// Doing nothing here.
	}
	
}
