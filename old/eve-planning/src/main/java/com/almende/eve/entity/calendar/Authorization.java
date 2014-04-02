/**
 * Helper class to store authorization tokens
 */
package com.almende.eve.entity.calendar;

import java.io.Serializable;

import org.joda.time.DateTime;

/**
 * The Class Authorization.
 */
@SuppressWarnings("serial")
public class Authorization implements Serializable {
	
	/**
	 * Instantiates a new authorization.
	 */
	public Authorization() {
	}
	
	/**
	 * Instantiates a new authorization.
	 * 
	 * @param accessToken
	 *            the access token
	 * @param tokenType
	 *            the token type
	 * @param expiresAt
	 *            the expires at
	 * @param refreshToken
	 *            the refresh token
	 */
	public Authorization(final String accessToken, final String tokenType,
			final DateTime expiresAt, final String refreshToken) {
		this.accessToken = accessToken;
		this.tokenType = tokenType;
		this.expiresAt = expiresAt;
		this.refreshToken = refreshToken;
	}
	
	/**
	 * Gets the access token.
	 * 
	 * @return the access token
	 */
	public String getAccessToken() {
		return accessToken;
	}
	
	/**
	 * Sets the access token.
	 * 
	 * @param accessToken
	 *            the new access token
	 */
	public void setAccessToken(final String accessToken) {
		this.accessToken = accessToken;
	}
	
	/**
	 * Gets the refresh token.
	 * 
	 * @return the refresh token
	 */
	public String getRefreshToken() {
		return refreshToken;
	}
	
	/**
	 * Sets the refresh token.
	 * 
	 * @param refreshToken
	 *            the new refresh token
	 */
	public void setRefreshToken(final String refreshToken) {
		this.refreshToken = refreshToken;
	}
	
	/**
	 * Gets the expires at.
	 * 
	 * @return the expires at
	 */
	public DateTime getExpiresAt() {
		return expiresAt;
	}
	
	/**
	 * Sets the expires at.
	 * 
	 * @param expiresAt
	 *            the new expires at
	 */
	public void setExpiresAt(final DateTime expiresAt) {
		this.expiresAt = expiresAt;
	}
	
	/**
	 * Gets the token type.
	 * 
	 * @return the token type
	 */
	public String getTokenType() {
		return tokenType;
	}
	
	/**
	 * Sets the token type.
	 * 
	 * @param tokenType
	 *            the new token type
	 */
	public void setTokenType(final String tokenType) {
		this.tokenType = tokenType;
	}
	
	private String		accessToken		= null;
	private String		tokenType		= null;
	private DateTime	expiresAt		= null;
	private String		refreshToken	= null;
}
