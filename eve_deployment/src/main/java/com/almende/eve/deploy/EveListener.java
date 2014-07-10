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

import com.almende.eve.agent.Agent;
import com.almende.eve.agent.AgentBuilder;
import com.almende.eve.agent.AgentConfig;
import com.almende.eve.capabilities.Config;
import com.almende.eve.config.YamlReader;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

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
			final Config config = YamlReader.load(is).expand();
			
			// Config is now a Jackson JSON DOM, 'expand()' allows for template resolvement in the configuration.
			
			// Now we instantiate two example agents, getting their classpath (and configuration) from the DOM
			final ArrayNode agents = (ArrayNode) config.get("agents");
			for (final JsonNode agent : agents) {
				final AgentConfig agentConfig = new AgentConfig(
						(ObjectNode) agent);
				final Agent newAgent = new AgentBuilder().with(agentConfig)
						.build();
				LOG.info("Created agent:" + newAgent.getId());
			}
		}
		
	}
	
	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		// Doing nothing here.
	}
	
}
