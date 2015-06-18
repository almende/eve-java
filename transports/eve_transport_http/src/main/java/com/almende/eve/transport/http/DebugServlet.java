/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.transport.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.almende.util.StreamingUtil;
import com.almende.util.StringUtil;
import com.almende.util.URIUtil;

/**
 * The Class EveServlet.
 */
public class DebugServlet extends EveServlet {
	private static final long	serialVersionUID	= -4635490705591217600L;
	private static final String	RESOURCES			= "/com/almende/eve/resources/";
	private static final Logger	LOG					= Logger.getLogger(DebugServlet.class
															.getSimpleName());

	/**
	 * Instantiates a new eve servlet.
	 */
	public DebugServlet() {}

	/**
	 * Instantiates a new eve servlet.
	 * 
	 * @param servletUrl
	 *            the servlet url
	 */
	public DebugServlet(final URI servletUrl) {
		super(servletUrl);
	}

	private String getResource(final String url) {
		String id = "";
		if (myUrl != null) {
			id = url.replace(myUrl.getRawPath(), "");
		} else {
			// TODO: this doesn't work with resources behind agentId
			id = url.replaceAll(".*/[^/]+", "");
		}
		return id.indexOf('/') < 0 ? null : id.substring(id.indexOf('/') + 1);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest
	 * , javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void doPost(final HttpServletRequest req,
			final HttpServletResponse resp) throws IOException,
			ServletException {

		if (!handleSession(req, resp)) {
			if (!resp.isCommitted()) {
				resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			}
			resp.flushBuffer();
			return;
		}

		// retrieve the url and the request body
		final String body = StringUtil.streamToString(req.getInputStream());
		final String url = req.getRequestURI();
		final String id = getId(url);
		if (id == null || id.isEmpty() || id.equals(myUrl.toASCIIString())) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
					"Couldn't parse URL, missing 'id'");
			resp.flushBuffer();
			return;
		}

		String sender = req.getHeader("X-Eve-SenderUrl");
		if (sender == null || sender.isEmpty()) {
			sender = "web://" + req.getRemoteUser() + "@" + req.getRemoteAddr();
		}
		URI senderUrl = null;
		try {
			senderUrl = URIUtil.parse(sender);
		} catch (final URISyntaxException e) {
			LOG.log(Level.WARNING, "Couldn't parse senderUrl:" + sender, e);
		}
		final HttpTransport transport = HttpService.get(myUrl, id);
		if (transport != null) {
			try {
				final String response = transport.receive(body, senderUrl);
				// TODO: It doesn't need to be json, should we handle mime-types
				// better?
				resp.addHeader("Content-Type", "application/json");
				resp.getWriter().println(response);
				resp.getWriter().close();
			} catch (final IOException e) {
				resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
						"Receiver raised exception:" + e.getMessage());
				LOG.log(Level.WARNING, "Receiver raised exception:", e);
			}
		} else {
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					"Couldn't load transport");
		}
		resp.flushBuffer();
	}

	@Override
	protected void doGet(final HttpServletRequest req,
			final HttpServletResponse resp) throws ServletException,
			IOException {

		// If this is a handshake request, handle it.
		if (handleHandShake(req, resp)) {
			return;
		}

		final String url = req.getRequestURI();
		final String id = getId(url);
		if (id == null || id.isEmpty() || id.equals(myUrl.toASCIIString())) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
					"Couldn't parse URL, missing 'id'");
			resp.flushBuffer();
			return;
		}
		if (myUrl != null) {
			final HttpTransport transport = HttpService.get(myUrl, id);
			if (transport != null) {
				// get the resource name from the end of the url

				String resource = getResource(url);
				if (resource == null || resource.isEmpty()) {
					if (!url.endsWith("/") && !resp.isCommitted()) {
						final String redirect = url + "/";
						resp.sendRedirect(redirect);
						return;
					}
					resource = "index.html";
				}
				final String extension = resource.substring(resource
						.lastIndexOf('.') + 1);
				// load the resource
				final String mimetype = StreamingUtil.getMimeType(extension);

				final String filename = RESOURCES + resource;
				final InputStream is = this.getClass().getResourceAsStream(
						filename);
				if (is != null) {
					StreamingUtil.streamBinaryData(is, mimetype, resp);
				} else {
					throw new ServletException("Resource '" + resource
							+ "' not found. (filename:'" + filename + "')");
				}
				resp.flushBuffer();
			} else {
				resp.setContentType("text/plain");
				resp.getWriter().println("Agent:" + id + " is unknown!");
				resp.getWriter().close();
				resp.flushBuffer();
			}
		} else {
			resp.setContentType("text/plain");
			resp.getWriter()
					.println("You've found the servlet for agent:" + id);
			resp.getWriter().close();
			resp.flushBuffer();
		}
	}
}
