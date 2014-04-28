/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.almende.eve.capabilities.Config;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

/**
 * The Class YamlReader.
 */
public class YamlReader {
	private static final Logger LOG = Logger.getLogger(YamlReader.class.getName());
	
	/**
	 * Load.
	 * 
	 * @param is
	 *            the is
	 * @return the config
	 */
	public static Config load(InputStream is){
		ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
		try {
			return new Config((ObjectNode)mapper.readTree(is));
		} catch (JsonProcessingException e) {
			LOG.log(Level.WARNING,"Couldn't parse Yaml file",e);
		} catch (IOException e) {
			LOG.log(Level.WARNING,"Couldn't read Yaml file",e);
		}
		return null;
	}
}
