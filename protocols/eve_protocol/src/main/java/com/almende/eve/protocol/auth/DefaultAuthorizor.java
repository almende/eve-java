/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.protocol.auth;

import java.net.URI;

/**
 * The Class DefaultAuthorizor.
 */
public class DefaultAuthorizor implements Authorizor {
	
	/*
	 * (non-Javadoc)
	 * @see com.almende.eve.auth.Authorizor#onAccess(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public boolean onAccess(final URI senderUrl, final String functionTag) {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see com.almende.eve.auth.Authorizor#onAccess(java.lang.String)
	 */
	@Override
	public boolean onAccess(final URI senderUrl) {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see com.almende.eve.auth.Authorizor#isSelf(java.lang.String)
	 */
	@Override
	public boolean isSelf(final URI senderUrl) {
		return true;
	}
}
