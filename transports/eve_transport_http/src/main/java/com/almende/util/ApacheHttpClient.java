/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import com.almende.eve.state.State;
import com.almende.eve.state.StateFactory;
import com.almende.util.jackson.JOM;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class ApacheHttpClient.
 */
public final class ApacheHttpClient {
	private static final Logger	LOG			= Logger.getLogger(ApacheHttpClient.class
													.getCanonicalName());
	private static HttpClient	httpClient	= null;
	static {
		new ApacheHttpClient();
	}
	
	/**
	 * Instantiates a new apache http client.
	 * 
	 */
	private ApacheHttpClient() {
		
		final HttpClientBuilder builder = HttpClientBuilder.create();
		
		// Allow self-signed SSL certificates:
		try {
			final SSLContextBuilder SSLbuilder = new SSLContextBuilder();
			SSLbuilder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
			final SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
					SSLbuilder.build(),
					SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
			
			builder.setSSLSocketFactory(sslsf);
		} catch (final Exception e) {
			LOG.log(Level.WARNING, "Couldn't init SSL strategy", e);
		}
		// Work with PoolingClientConnectionManager
		final HttpClientConnectionManager connection = new PoolingHttpClientConnectionManager();
		builder.setConnectionManager(connection);
		
		// Provide eviction thread to clear out stale threads.
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					while (true) {
						synchronized (this) {
							wait(5000);
							connection.closeExpiredConnections();
							connection.closeIdleConnections(30,
									TimeUnit.SECONDS);
						}
					}
				} catch (final InterruptedException ex) {
				}
			}
		}).start();
		
		try {
			builder.setDefaultCookieStore(new MyCookieStore());
		} catch (final IOException e) {
			LOG.log(Level.WARNING, "Couldn't init cookie store", e);
		}
		
		final RequestConfig globalConfig = RequestConfig.custom()
				.setCookieSpec(CookieSpecs.BROWSER_COMPATIBILITY)
				.setConnectTimeout(20000).setStaleConnectionCheckEnabled(false)
				.build();
		
		builder.setDefaultRequestConfig(globalConfig);
		
		final SocketConfig socketConfig = SocketConfig.custom()
				.setSoTimeout(60000).setTcpNoDelay(true).build();
		builder.setDefaultSocketConfig(socketConfig);
		
		// generate httpclient
		httpClient = builder.build();
	}
	
	/**
	 * Gets the.
	 * 
	 * @return the default http client
	 */
	public static HttpClient get() {
		return httpClient;
	}
	
	/**
	 * The Class MyCookieStore.
	 */
	class MyCookieStore implements CookieStore {
		// TODO: make COOKIESTORE config parameters
		
		/** The Constant COOKIESTORE. */
		static final String	COOKIESTORE	= "_CookieStore";
		
		/** The my state. */
		private State		myState		= null;
		
		/**
		 * Instantiates a new my cookie store.
		 * 
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		MyCookieStore() throws IOException {
			// TODO: use Config service
			final ObjectNode params = JOM.createObjectNode();
			params.put("class",
					"com.almende.eve.state.memory.MemoryStateService");
			params.put("id", COOKIESTORE);
			
			myState = StateFactory.getState(params);
		}
		
		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.apache.http.client.CookieStore#addCookie(org.apache.http.cookie
		 * .Cookie)
		 */
		@Override
		public void addCookie(final Cookie cookie) {
			myState.put(Integer.valueOf(COOKIESTORE.hashCode()).toString(),
					cookie);
		}
		
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.apache.http.client.CookieStore#getCookies()
		 */
		@Override
		public List<Cookie> getCookies() {
			final List<Cookie> result = new ArrayList<Cookie>(myState.size());
			for (final String entryKey : myState.keySet()) {
				result.add(myState.get(entryKey, Cookie.class));
			}
			return result;
		}
		
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.apache.http.client.CookieStore#clearExpired(java.util.Date)
		 */
		@Override
		public boolean clearExpired(final Date date) {
			boolean result = false;
			
			for (final String entryKey : myState.keySet()) {
				final Cookie cookie = myState.get(entryKey, Cookie.class);
				if (cookie.isExpired(date)) {
					myState.remove(entryKey);
					result = true;
				}
			}
			return result;
		}
		
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.apache.http.client.CookieStore#clear()
		 */
		@Override
		public void clear() {
			myState.clear();
		}
		
		/**
		 * Gets the my state.
		 * 
		 * @return the my state
		 */
		public State getMyState() {
			return myState;
		}
		
		/**
		 * Sets the my state.
		 * 
		 * @param myState
		 *            the new my state
		 */
		public void setMyState(final State myState) {
			this.myState = myState;
		}
	}
}
