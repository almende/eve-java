/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.state.file;

import com.almende.eve.state.StateConfig;
import com.almende.util.jackson.JOM;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class FileStateConfig.
 */
public class FileStateConfig extends StateConfig {
	
	/**
	 * Instantiates a new file state config.
	 */
	public FileStateConfig() {
		this(JOM.createObjectNode());
	}
	
	/**
	 * Instantiates a new file state config.
	 * 
	 * @param node
	 *            the node
	 */
	public FileStateConfig(final ObjectNode node) {
		super(node);
		if (!node.has("class")) {
			this.put("class", FileStateService.class.getName());
		}
	}
	
	/**
	 * Sets the json. (Optional, default is true)
	 * 
	 * @param json
	 *            the new json
	 */
	public void setJson(final boolean json) {
		this.put("json", json);
	}
	
	/**
	 * Gets the json.
	 * 
	 * @return the json
	 */
	public boolean getJson() {
		if (this.has("json")) {
			return this.get("json").asBoolean();
		}
		return true;
	}
	
	/**
	 * Sets the path. (Required)
	 * 
	 * @param path
	 *            the new path
	 */
	public void setPath(final String path) {
		this.put("path", path);
	}
	
	/**
	 * Gets the path.
	 * 
	 * @return the path
	 */
	public String getPath() {
		if (this.has("path")) {
			return this.get("path").asText();
		}
		return null;
	}
}
