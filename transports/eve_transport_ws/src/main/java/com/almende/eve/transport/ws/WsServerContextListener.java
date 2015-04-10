/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.transport.ws;

import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.websocket.DeploymentException;
import javax.websocket.server.ServerEndpointConfig;

import com.almende.util.URIUtil;

/**
 * The listener interface for receiving websocketContext events.
 * The class that is interested in processing a websocketContext
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addWebsocketContextListener<code> method. When
 * the websocketContext event occurs, that object's appropriate
 * method is invoked.
 * 
 */
public class WsServerContextListener implements ServletContextListener {
	private static final Logger	LOG	= Logger.getLogger(WsServerContextListener.class
											.getName());
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.servlet.ServletContextListener#contextInitialized(javax.servlet
	 * .ServletContextEvent)
	 */
	@Override
	public void contextInitialized(final ServletContextEvent sce) {
		final javax.websocket.server.ServerContainer serverContainer = (javax.websocket.server.ServerContainer) sce
				.getServletContext().getAttribute(
						"javax.websocket.server.ServerContainer");
		try {
			final URI myUrl = URIUtil.create(sce.getServletContext()
					.getInitParameter("servletUrl"));
			final ServerEndpointConfig config = ServerEndpointConfig.Builder
					.create(WebsocketEndpoint.class, myUrl.getPath()).build();
			config.getUserProperties().put("servletUrl", myUrl);
			serverContainer.addEndpoint(config);
		} catch (final DeploymentException e) {
			LOG.log(Level.WARNING,
					"Couldn't initialize websocket server endpoint.", e);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.
	 * ServletContextEvent)
	 */
	@Override
	public void contextDestroyed(final ServletContextEvent sce) {
		// TODO Auto-generated method stub
		
	}
	
}
