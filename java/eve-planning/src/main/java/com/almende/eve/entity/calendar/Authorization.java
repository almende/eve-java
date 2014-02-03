/**
 * Helper class to store authorization tokens
 */
package com.almende.eve.entity.calendar;

import java.io.Serializable;

import org.joda.time.DateTime;

@SuppressWarnings("serial")
public class Authorization implements Serializable {
	public Authorization() {
	}
	
	public Authorization(final String accessToken, final String tokenType,
			final DateTime expiresAt, final String refreshToken) {
		this.accessToken = accessToken;
		this.tokenType = tokenType;
		this.expiresAt = expiresAt;
		this.refreshToken = refreshToken;
	}
	
	public String getAccessToken() {
		return accessToken;
	}
	
	public void setAccessToken(final String accessToken) {
		this.accessToken = accessToken;
	}
	
	public String getRefreshToken() {
		return refreshToken;
	}
	
	public void setRefreshToken(final String refreshToken) {
		this.refreshToken = refreshToken;
	}
	
	public DateTime getExpiresAt() {
		return expiresAt;
	}
	
	public void setExpiresAt(final DateTime expiresAt) {
		this.expiresAt = expiresAt;
	}
	
	public String getTokenType() {
		return tokenType;
	}
	
	public void setTokenType(final String tokenType) {
		this.tokenType = tokenType;
	}
	
	private String		accessToken		= null;
	private String		tokenType		= null;
	private DateTime	expiresAt		= null;
	private String		refreshToken	= null;
}
