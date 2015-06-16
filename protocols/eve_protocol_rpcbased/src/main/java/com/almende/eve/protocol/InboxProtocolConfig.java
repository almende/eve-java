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
	private static final String	BUILDER	= InboxProtocolBuilder.class.getName();

	protected InboxProtocolConfig() {
		super();
	}

	/**
	 * Instantiates a new Inbox protocol config.
	 *
	 * @return the inbox protocol config
	 */
	public static InboxProtocolConfig create() {
		final InboxProtocolConfig res = new InboxProtocolConfig();
		res.setBuilder(BUILDER);
		return res;
	}

	/**
	 * Instantiates a new Inbox protocol config.
	 *
	 * @param node
	 *            the node
	 * @return the inbox protocol config
	 */
	public static InboxProtocolConfig decorate(final ObjectNode node) {
		final InboxProtocolConfig res = new InboxProtocolConfig();
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
