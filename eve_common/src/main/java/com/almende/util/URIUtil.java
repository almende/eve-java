/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.util;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The Class URIUtil.
 */
public final class URIUtil {
	private static final Logger	LOG	= Logger.getLogger(URIUtil.class.getName());

	private URIUtil() {}

	private static final Map<String, WeakReference<URI>>	uris	= new HashMap<String, WeakReference<URI>>(
																			100);

	/**
	 * Creates or obtains a new URI;.
	 *
	 * @param uri
	 *            the uri
	 * @return the uri
	 * @throws URISyntaxException
	 *             the URI syntax exception
	 */
	public static URI parse(final String uri) throws URISyntaxException {
		final WeakReference<URI> ref = uris.get(uri);
		if (ref != null) {
			final URI res = ref.get();

			if (res != null) {
				return res;
			}
		}
		final URI newUri = new URI(uri);
		uris.put(uri, new WeakReference<URI>(newUri));
		return newUri;
	}

	/**
	 * Creates or obtains a new URI;.
	 *
	 * @param uri
	 *            the uri
	 * @return the uri
	 */
	public static URI create(final String uri) {
		final WeakReference<URI> ref = uris.get(uri);
		if (ref != null) {
			final URI res = ref.get();
			if (res != null) {
				return res;
			}
		}
		final URI newUri = URI.create(uri);
		uris.put(uri, new WeakReference<URI>(newUri));
		return newUri;
	}

	/**
	 * URL encode wrapper.
	 *
	 * @param input
	 *            the input
	 * @return the string
	 */
	public static String encode(String input) {
		try {
			return URLEncoder.encode(input, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			LOG.log(Level.WARNING, "Couldn't encode to UTF-8?", e);
			return input;
		}
	}
}
