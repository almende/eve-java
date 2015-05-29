/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.protocol.jsonrpc;

import com.almende.eve.capabilities.handler.Handler;
import com.almende.eve.protocol.Protocol;
import com.almende.eve.protocol.jsonrpc.formats.Caller;

/**
 * The Interface RpcBasedProtocol, indicating that this protocol can send RPC
 * messages (and therefor needs a caller)
 */
public interface RpcBasedProtocol extends Protocol {

	/**
	 * Sets the caller.
	 *
	 * @param caller
	 *            the new caller
	 */
	void setCaller(final Handler<Caller> caller);
}
