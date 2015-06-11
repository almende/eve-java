/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.protocol;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class JSONRpcProtocolConfig.
 */
public class InboxProtocolConfig extends ProtocolConfig {

	/**
	 * Instantiates a new JSON rpc protocol config.
	 */
	public InboxProtocolConfig() {
		super();
		setClassName(InboxProtocolBuilder.class.getName());
	}

	private InboxProtocolConfig(boolean skipClass) {
		super();
	}

	/**
	 * Instantiates a new JSON rpc protocol config.
	 * 
	 * @param node
	 *            the node
	 */
	public static InboxProtocolConfig decorate(final ObjectNode node) {
		final InboxProtocolConfig res = new InboxProtocolConfig(true);
		res.extend(node);
		return res;
	}

	/**
	 * Checks if is support synccalls.
	 *
	 * @return true, if is support synccalls
	 */
	public boolean isSupportSynccalls() {
		if (this.has("supportSynccalls")) {
			return this.get("supportSynccalls").asBoolean();
		}
		return false;
	}

	/**
	 * Sets the support synccalls.
	 *
	 * @param supportSynccalls
	 *            the new support synccalls
	 */
	public void setSupportSynccalls(final boolean supportSynccalls) {
		this.put("supportSynccalls", supportSynccalls);
	}

}
