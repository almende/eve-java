/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.auth;

/**
 * The Class DefaultAuthorizor.
 */
public class DefaultAuthorizor implements Authorizor {
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.almende.eve.auth.Authorizor#onAccess(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public boolean onAccess(final String senderUrl, final String functionTag) {
		return true;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.almende.eve.auth.Authorizor#onAccess(java.lang.String)
	 */
	@Override
	public boolean onAccess(final String senderUrl) {
		return true;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.almende.eve.auth.Authorizor#isSelf(java.lang.String)
	 */
	@Override
	public boolean isSelf(final String senderUrl) {
		return true;
	}
}
