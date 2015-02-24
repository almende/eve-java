/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.util;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

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
	 */
	private ApacheHttpClient() {

		final HttpClientBuilder builder = HttpClientBuilder.create();

		// Allow self-signed SSL certificates:
		try {
			final SSLContextBuilder sslbuilder = new SSLContextBuilder();
			sslbuilder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
			final SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
					sslbuilder.build(),
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
				} catch (final InterruptedException ex) {}
			}
		}).start();

		builder.setDefaultCookieStore(new BasicCookieStore());
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

}
