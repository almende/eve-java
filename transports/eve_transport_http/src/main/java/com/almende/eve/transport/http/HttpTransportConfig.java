/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.transport.http;

import com.almende.eve.transport.TransportConfig;
import com.almende.util.jackson.JOM;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class HttpTransportConfig.
 */
public class HttpTransportConfig extends TransportConfig {
	
	/**
	 * Instantiates a new http transport config.
	 */
	public HttpTransportConfig(){
		super(JOM.createObjectNode());
		this.setClassPath(HttpTransportConfig.class.getPackage().getName()
				+ ".HttpService");
	}
	/**
	 * Instantiates a new http transport config.
	 * 
	 * @param node
	 *            the node
	 */
	public HttpTransportConfig(ObjectNode node) {
		super(node);
		if (!node.has("class")) {
			this.setClassPath(HttpTransportConfig.class.getPackage().getName()
					+ ".HttpService");
		}
	}
	
	/**
	 * Sets the url. (Required)
	 * 
	 * @param url
	 *            the new url
	 */
	public void setServletUrl(String url) {
		this.put("servletUrl", url.replace("/$", ""));
	}
	
	/**
	 * Gets the url.
	 * 
	 * @return the url
	 */
	public String getServletUrl() {
		if (this.has("servletUrl")) {
			return this.get("servletUrl").asText();
		}
		return null;
	}
	
	/**
	 * Sets the id. (Required)
	 * 
	 * @param id
	 *            the new id
	 */
	public void setId(String id) {
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
	 * Sets the servlet launcher class path. (Optional)
	 * 
	 * @param servletLauncher
	 *            the new servlet launcher class path;
	 */
	public void setServletLauncher(String servletLauncher) {
		this.put("servletLauncher", servletLauncher);
	}
	
	/**
	 * Gets the servlet launcher class path.
	 * 
	 * @return the servlet launcher
	 */
	public String getServletLauncher() {
		if (this.has("servletLauncher")) {
			return this.get("servletLauncher").asText();
		}
		return null;
	}
}
