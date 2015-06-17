/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.transport.tokens;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.joda.time.DateTime;

import com.almende.util.uuid.UUID;

/**
 * Simple token system: Each outbound call gets a token, which is newly
 * generated each hour. Last 5 tokens are
 * kept in memory. If remote peer wants to check if this host has actually send
 * the call, it can request a resend of the
 * token at time X.
 * 
 * @author ludo
 */
public final class TokenStore {
	private static final Logger	LOG		= Logger.getLogger(TokenStore.class
												.getCanonicalName());
	private static final int	SIZE	= 5;
	private Map<String, String>	tokens	= new ConcurrentHashMap<String, String>(
												5);
	private DateTime			last	= DateTime.now();

	/**
	 * Instantiates a new token store.
	 */
	public TokenStore() {}

	/**
	 * Gets the.
	 * 
	 * @param time
	 *            the time
	 * @return the string
	 */
	public String get(final String time) {
		try {
			return tokens.get(time);
		} catch (final Exception e) {
			LOG.log(Level.WARNING, "Exception during TokenStore get:", e);
			return null;
		}
	}

	/**
	 * Creates the.
	 * 
	 * @return the token ret
	 */
	public TokenRet create() {
		TokenRet result;
		if (tokens.size() == 0 || tokens.get(last.toString()) == null
				|| last.plus(3600000).isBeforeNow()) {
			final DateTime now = DateTime.now();
			final String token = new UUID().toString();
			result = new TokenRet(token, now);
			tokens.put(now.toString(), token);
			last = now;

			if (tokens.size() > SIZE + 2) {
				DateTime oldest = last;
				for (final String time : tokens.keySet()) {
					try {
						if (DateTime.parse(time).isBefore(oldest)) {
							oldest = DateTime.parse(time);
						}
					} catch (final Exception e) {
						LOG.log(Level.WARNING, "Failed in eviction of tokens:",
								e);
					}
				}
				tokens.remove(oldest.toString());
			}
		} else {
			result = new TokenRet(tokens.get(last.toString()), last);
		}
		return result;
	}
}
