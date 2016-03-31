/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.util;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLContext;

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
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

		final HttpClientBuilder builder = HttpClients.custom();

		// Allow self-signed SSL certificates:
		try {
			final SSLContext sslContext = new SSLContextBuilder()
					.loadTrustMaterial(null, new TrustStrategy() {

						@Override
						public boolean isTrusted(
								java.security.cert.X509Certificate[] arg0,
								String arg1)
								throws java.security.cert.CertificateException {
							return true;
						}
					}).build();

			// For HttpClient 4.3.x
			final SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
					sslContext,
					SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
			builder.setSSLSocketFactory(sslsf);

			// For HttpClient 4.4+
			// builder.setSslcontext(sslContext).setSSLHostnameVerifier(
			// new NoopHostnameVerifier());

		} catch (final Exception e) {
			LOG.log(Level.WARNING, "Couldn't init SSL strategy", e);
		}
		// Work with PoolingClientConnectionManager
		final PoolingHttpClientConnectionManager connection = new PoolingHttpClientConnectionManager();
		
		//Make sure we have enough connections available for outbound traffic....
		connection.setDefaultMaxPerRoute(1000);
		connection.setMaxTotal(1000);
		
		// For HttpClient 4.4+
		// connection.setValidateAfterInactivity(1000);

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

				// For HttpClient 4.3.X:
				.setCookieSpec(CookieSpecs.BROWSER_COMPATIBILITY)
				.setStaleConnectionCheckEnabled(false)

				// For HttpClient 4.4+
				// .setCookieSpec(CookieSpecs.DEFAULT)

				.setConnectTimeout(20000).build();

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
