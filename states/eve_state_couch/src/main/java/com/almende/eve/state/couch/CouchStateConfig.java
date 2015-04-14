/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.state.couch;

import com.almende.eve.state.StateConfig;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class CouchStateConfig.
 */
public class CouchStateConfig extends StateConfig {

	/**
	 * Instantiates a new couch state config.
	 */
	public CouchStateConfig() {
		super();
		setClassName(CouchStateBuilder.class.getName());
	}

	/**
	 * Instantiates a new couch state config.
	 * 
	 * @param node
	 *            the node
	 */
	public static CouchStateConfig decorate(final ObjectNode node) {
		final CouchStateConfig res = new CouchStateConfig();
		res.copy(node);
		return res;
	}

	/**
	 * Gets the key.
	 * 
	 * @return the key
	 */
	@JsonIgnore
	public String getKey() {
		return getUsername() + ":" + getPassword() + "@" + getUrl() + "/"
				+ getDatabase();
	}

	/**
	 * Sets the username.
	 * 
	 * @param username
	 *            the new username
	 */
	public void setUsername(final String username) {
		this.put("username", username);
	}

	/**
	 * Gets the username.
	 * 
	 * @return the username
	 */
	public String getUsername() {
		if (this.has("username")) {
			return this.get("username").asText();
		}
		return null;
	}

	/**
	 * Sets the password.
	 * 
	 * @param password
	 *            the new password
	 */
	public void setPassword(final String password) {
		this.put("password", password);
	}

	/**
	 * Gets the password.
	 * 
	 * @return the password
	 */
	public String getPassword() {
		if (this.has("password")) {
			return this.get("password").asText();
		}
		return null;
	}

	/**
	 * Sets the url.
	 * 
	 * @param url
	 *            the new url
	 */
	public void setUrl(final String url) {
		this.put("url", url);
	}

	/**
	 * Gets the url.
	 * 
	 * @return the url
	 */
	public String getUrl() {
		if (this.has("url")) {
			return this.get("url").asText();
		}
		return null;
	}

	/**
	 * Sets the database.
	 * 
	 * @param database
	 *            the new database
	 */
	public void setDatabase(final String database) {
		this.put("database", database);
	}

	/**
	 * Gets the database.
	 * 
	 * @return the database
	 */
	public String getDatabase() {
		if (this.has("database")) {
			return this.get("database").asText();
		}
		return "eve";
	}
}
