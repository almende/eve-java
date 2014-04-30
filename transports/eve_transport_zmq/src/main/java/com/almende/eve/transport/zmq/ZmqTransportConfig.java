/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.transport.zmq;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Logger;

import com.almende.eve.transport.TransportConfig;
import com.almende.util.jackson.JOM;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class ZmqTransportConfig.
 */
public class ZmqTransportConfig extends TransportConfig {
	private static final Logger	LOG	= Logger.getLogger(ZmqTransportConfig.class
											.getName());
	
	/**
	 * Instantiates a new xmpp transport config.
	 */
	public ZmqTransportConfig() {
		this(JOM.createObjectNode());
	}
	
	/**
	 * Instantiates a new xmpp transport config.
	 * 
	 * @param node
	 *            the node
	 */
	public ZmqTransportConfig(final ObjectNode node) {
		super(node);
		if (!this.has("class")) {
			setClassName(ZmqService.class.getName());
		}
	}
	
	/**
	 * Gets the address.
	 * 
	 * @return the address
	 */
	public URI getAddress() {
		if (this.has("address")) {
			try {
				return new URI(this.get("address").asText());
			} catch (final URISyntaxException e) {
				LOG.warning("Couldn't parse URI from: "
						+ this.get("address").asText());
			}
		}
		return null;
	}

	/**
	 * Sets the address.
	 * 
	 * @param address
	 *            the new address
	 */
	public void setAddress(String address) {
		this.put("address", address);
	}
	
}
