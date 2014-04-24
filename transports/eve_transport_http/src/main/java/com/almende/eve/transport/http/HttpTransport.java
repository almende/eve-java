/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.transport.http;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;

import com.almende.eve.capabilities.handler.Handler;
import com.almende.eve.transport.AbstractTransport;
import com.almende.eve.transport.Receiver;
import com.almende.eve.transport.TransportService;
import com.almende.eve.transport.tokens.TokenStore;
import com.almende.util.ApacheHttpClient;
import com.almende.util.callback.AsyncCallback;
import com.almende.util.callback.AsyncCallbackQueue;
import com.almende.util.callback.SyncCallback;
import com.almende.util.threads.ThreadPool;
import com.almende.util.uuid.UUID;

/**
 * The Class HttpTransport.
 */
public class HttpTransport extends AbstractTransport {
	private static final Logger			LOG			= Logger.getLogger(HttpTransport.class
															.getName());
	private AsyncCallbackQueue<String>	callbacks	= new AsyncCallbackQueue<String>();
	private final List<String>	protocols	= Arrays.asList("http", "https",
			"web");

	/**
	 * Instantiates a new http transport.
	 * 
	 * @param address
	 *            the address
	 * @param handle
	 *            the handle
	 * @param service
	 *            the service
	 */
	public HttpTransport(final URI address, final Handler<Receiver> handle,
			final TransportService service) {
		super(address, handle, service);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.almende.eve.transport.Transport#send(java.net.URI,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public void send(final URI receiverUri, final String message,
			final String tag) throws IOException {
		if (tag != null) {
			if (callbacks != null) {
				final AsyncCallback<String> callback = callbacks.pull(tag);
				if (callback != null) {
					callback.onSuccess(message);
					return;
				} else {
					LOG.warning("Tag set, but no callback found! " + callback);
				}
			} else {
				LOG.warning("Tag set, but no callbacks found!");
			}
			// Chicken out
			return;
		}
		// Check and deliver local shortcut.
		if (sendLocal(receiverUri, message)) {
			return;
		}
		final String senderUrl = super.getAddress().toASCIIString();
		final Handler<Receiver> handle = super.getHandle();
		ThreadPool.getPool().execute(new Runnable() {
			@Override
			public void run() {
				HttpPost httpPost = null;
				try {
					httpPost = new HttpPost(receiverUri);
					// invoke via Apache HttpClient request:
					httpPost.setEntity(new StringEntity(message));
					
					// Add token for HTTP handshake
					httpPost.addHeader("X-Eve-Token", TokenStore.create()
							.toString());
					httpPost.addHeader("X-Eve-SenderUrl", senderUrl);
					final HttpResponse webResp = ApacheHttpClient.get()
							.execute(httpPost);
					final String result = EntityUtils.toString(webResp
							.getEntity());
					if (webResp.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
						LOG.warning("Received HTTP Error Status:"
								+ webResp.getStatusLine().getStatusCode() + ":"
								+ webResp.getStatusLine().getReasonPhrase());
						LOG.warning(result);
					} else {
						handle.get().receive(result, receiverUri, null);
					}
				} catch (final Exception e) {
					LOG.log(Level.WARNING,
							"HTTP roundtrip resulted in exception!", e);
				} finally {
					if (httpPost != null) {
						httpPost.reset();
					}
				}
			}
		});
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.almende.eve.transport.Transport#send(java.net.URI, byte[],
	 * java.lang.String)
	 */
	@Override
	public void send(final URI receiverUri, final byte[] message,
			final String tag) throws IOException {
		send(receiverUri, Base64.encodeBase64String(message), tag);
	}
	
	/**
	 * Receive.
	 * 
	 * @param body
	 *            the body
	 * @param senderUrl
	 *            the sender url
	 * @return the response string
	 * @throws IOException
	 */
	public String receive(final String body, final URI senderUrl)
			throws IOException {
		final String tag = new UUID().toString();
		final SyncCallback<String> callback = new SyncCallback<String>();
		callbacks.push(tag, "", callback);
		
		super.getHandle().get().receive(body, senderUrl, tag);
		try {
			return callback.get();
		} catch (Exception e) {
			throw new IOException(
					"Receiver raised exception:" + e.getMessage(), e);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.almende.eve.transport.Transport#connect()
	 */
	@Override
	public void connect() throws IOException {
		// Nothing todo at this point, maybe re-register the Servlet if
		// ServletLauncher is configured?
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.almende.eve.transport.Transport#disconnect()
	 */
	@Override
	public void disconnect() {
		// Nothing todo at this point, maybe disable receival through the
		// handler?
	}

	/* (non-Javadoc)
	 * @see com.almende.eve.transport.Transport#getProtocols()
	 */
	@Override
	public List<String> getProtocols() {
		return protocols;
	}
	
}
