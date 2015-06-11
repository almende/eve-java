/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.scheduling;

import com.almende.eve.capabilities.Config;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class SimpleSchedulerConfig.
 */
public class SimpleSchedulerConfig extends Config {

	/**
	 * Instantiates a new simple scheduler config.
	 */
	public SimpleSchedulerConfig() {
		super();
		setClassName(SimpleSchedulerBuilder.class.getName());
	}
	
	private SimpleSchedulerConfig(boolean local){
		super();
	}

	/**
	 * Instantiates a new simple scheduler config.
	 * 
	 * @param node
	 *            the node
	 */
	public static SimpleSchedulerConfig decorate(final ObjectNode node) {
		final SimpleSchedulerConfig res = new SimpleSchedulerConfig(true);
		res.extend(node);
		if (!res.has("class")) {
			res.setClassName(SimpleSchedulerBuilder.class.getName());
		}
		return res;
	}

	/**
	 * Sets the id.
	 * 
	 * @param id
	 *            the new id
	 */
	public void setId(final String id) {
		this.put("id", id);
	}

	/**
	 * Gets the id.
	 * 
	 * @return the id
	 */
	public String getId() {
		if (this.has("id")) {
			return this.get("id").asText();
		}
		return null;
	}

	/**
	 * Sets the sender url.
	 * 
	 * @param senderUrl
	 *            the new sender url
	 */
	public void setSenderUrl(String senderUrl) {
		this.put("senderUrl", senderUrl);
	}

	/**
	 * Gets the sender url.
	 * 
	 * @return the sender url
	 */
	public String getSenderUrl() {
		if (this.has("senderUrl")) {
			return this.get("senderUrl").asText();
		}
		return "local:scheduler_"+getId();
	}
}
