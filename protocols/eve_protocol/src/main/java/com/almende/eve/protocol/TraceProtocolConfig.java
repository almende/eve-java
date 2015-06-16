/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.protocol;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class JSONRpcProtocolConfig.
 */
public class TraceProtocolConfig extends ProtocolConfig {
	private static final String	BUILDER	= TraceProtocolBuilder.class.getName();

	protected TraceProtocolConfig() {
		super();
	}

	/**
	 * Instantiates a new Trace protocol config.
	 *
	 * @return the trace protocol config
	 */
	public static TraceProtocolConfig create() {
		final TraceProtocolConfig res = new TraceProtocolConfig();
		res.setBuilder(BUILDER);
		return res;
	}

	/**
	 * Instantiates a new Trace protocol config.
	 * 
	 * @param node
	 *            the node
	 */
	public static TraceProtocolConfig decorate(final ObjectNode node) {
		final TraceProtocolConfig res = new TraceProtocolConfig();
		res.extend(node);
		return res;
	}

	/**
	 * Sets the file name.
	 *
	 * @param filename
	 *            the new file name
	 */
	public void setFileName(final String filename) {
		this.put("filename", filename);
	}

	/**
	 * Gets the file name.
	 *
	 * @return the file name or null if non is given.
	 */
	public String getFileName() {
		if (this.has("filename")) {
			return this.get("filename").asText();
		}
		return null;
	}

	/**
	 * Checks if is flat.
	 *
	 * @return true, if is flat
	 */
	public boolean isFlat() {
		return this.has("flat") ? this.get("flat").asBoolean() : true;
	}

	/**
	 * Sets the flat.
	 *
	 * @param flat
	 *            the new flat
	 */
	public void setFlat(final boolean flat) {
		this.put("flat", flat);
	}
}
