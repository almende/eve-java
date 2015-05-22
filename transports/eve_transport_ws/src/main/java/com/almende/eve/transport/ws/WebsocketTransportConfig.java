/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.transport.ws;

import com.almende.eve.transport.TransportConfig;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class WebsocketTransportConfig.
 */
public class WebsocketTransportConfig extends TransportConfig {

	/**
	 * Instantiates a new websocket transport config.
	 */
	public WebsocketTransportConfig() {
		super();
		setClassName(WebsocketTransportBuilder.class.getName());
	}

	/**
	 * Instantiates a new websocket transport config.
	 * 
	 * @param node
	 *            the node
	 */
	public static WebsocketTransportConfig decorate(final ObjectNode node) {
		final WebsocketTransportConfig res = new WebsocketTransportConfig();
		res.copy(node);
		return res;
	}

	/**
	 * Is this transport the server side of a websocket?.
	 * 
	 * @param server
	 *            the new server
	 */
	public void setServer(final boolean server) {
		this.put("server", server);
	}

	/**
	 * Is this transport the server side of a websocket?.
	 * 
	 * @return true, if is server
	 */
	public boolean isServer() {
		if (this.has("server")) {
			return this.get("server").asBoolean();
		}
		return false;
	}

	/**
	 * Sets the url. (server)
	 * 
	 * @param address
	 *            the new address
	 */
	public void setAddress(final String address) {
		this.put("address", address.replace("/$", ""));
	}

	/**
	 * Gets the url. (server)
	 * 
	 * @return the url
	 */
	public String getAddress() {
		String address = "";
		if (this.isServer()) {
			if (this.has("address")) {
				address += this.get("address").asText();
			}
			if (this.has("id")) {
				address += "/" + this.get("id").asText();
			}
		} else {
			address = "wsclient:" + this.get("id").asText();
		}
		return address;
	}

	/**
	 * Sets the servlet launcher class path. (Optional, server)
	 * 
	 * @param servletLauncher
	 *            the new servlet launcher class path;
	 */
	public void setServletLauncher(final String servletLauncher) {
		this.put("servletLauncher", servletLauncher);
	}

	/**
	 * Gets the servlet launcher class path. (server)
	 * 
	 * @return the servlet launcher
	 */
	public String getServletLauncher() {
		if (this.has("servletLauncher")) {
			return this.get("servletLauncher").asText();
		}
		return null;
	}

	/**
	 * Sets the id. (client)
	 * 
	 * @param id
	 *            the new id
	 */
	public void setId(final String id) {
		this.put("id", id);
		setAddress("wsclient:" + id);
	}

	/**
	 * Gets the id. (client)
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
	 * Sets the server url. (client)
	 * 
	 * @param serverUrl
	 *            the new server url
	 */
	public void setServerUrl(final String serverUrl) {
		this.put("serverUrl", serverUrl);
	}

	/**
	 * Gets the server url. (client)
	 * 
	 * @return the server url
	 */
	public String getServerUrl() {
		if (this.has("serverUrl")) {
			return this.get("serverUrl").asText();
		}
		return null;
	}
}
