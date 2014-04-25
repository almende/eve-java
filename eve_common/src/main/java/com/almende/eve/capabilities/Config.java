/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.capabilities;

import java.util.logging.Logger;

import com.almende.util.jackson.JOM;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class Config.
 */
public class Config extends ObjectNode {
	private static final Logger	LOG	= Logger.getLogger(Config.class.getName());
	
	/**
	 * Instantiates a new config.
	 */
	public Config(){
		super(JOM.getInstance().getNodeFactory());
	}
	
	/**
	 * Instantiates a new config.
	 * 
	 * @param node
	 *            the node
	 */
	public Config(JsonNode node) {
		super(JOM.getInstance().getNodeFactory());
		if (node.isObject()) {
			this.setAll((ObjectNode) node);
		} else {
			LOG.warning("Can't load non-ObjectNode as config:"
					+ node.toString());
		}
	}
	
}
