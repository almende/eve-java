/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.almende.util.jackson.JOM;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class YamlReader.
 */
public class JsonReader {
	private static final Logger	LOG	= Logger.getLogger(JsonReader.class
											.getName());

	/**
	 * Load.
	 * 
	 * @param is
	 *            the is
	 * @return the config
	 */
	public static Config load(final InputStream is) {
		try {
			return Config.decorate((ObjectNode) JOM.getInstance().readTree(is));
		} catch (final JsonProcessingException e) {
			LOG.log(Level.WARNING, "Couldn't parse Json file", e);
		} catch (final IOException e) {
			LOG.log(Level.WARNING, "Couldn't read Json file", e);
		}
		return null;
	}
}
