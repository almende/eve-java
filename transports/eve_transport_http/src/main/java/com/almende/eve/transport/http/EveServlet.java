/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.transport.http;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;

import com.almende.util.ApacheHttpClient;
import com.almende.util.StringUtil;
import com.almende.util.URIUtil;
import com.almende.util.jackson.JOM;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class EveServlet.
 */
public class EveServlet extends HttpServlet {
	private static final long	serialVersionUID	= -4635490705591217600L;

	private static final Logger	LOG					= Logger.getLogger(EveServlet.class
															.getSimpleName());
	protected URI				myUrl				= null;

	/**
	 * Instantiates a new eve servlet.
	 */
	public EveServlet() {}

	/**
	 * Instantiates a new eve servlet.
	 * 
	 * @param servletUrl
	 *            the servlet url
	 */
	public EveServlet(final URI servletUrl) {
		if (servletUrl != null) {
			myUrl = servletUrl;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.GenericServlet#init(javax.servlet.ServletConfig)
	 */
	@Override
	public void init(final ServletConfig config) throws ServletException {
		if (myUrl == null) {
			final String servletUrl = config.getInitParameter("ServletUrl");
			if (servletUrl != null) {
				try {
					myUrl = URIUtil.parse(servletUrl);
				} catch (final URISyntaxException e) {
					LOG.log(Level.WARNING,
							"Couldn't init servlet, url invalid. ('ServletUrl' init param)",
							e);
				}
			} else {
				LOG.warning("Servlet init parameter 'ServletUrl' is required!");
			}
		}
		super.init(config);
	}

	/**
	 * The Enum Handshake.
	 */
	enum Handshake {

		/** The ok. */
		OK,
		/** The nak. */
		NAK,
		/** The invalid. */
		INVALID
	}

	/**
	 * Handle hand shake.
	 * 
	 * @param req
	 *            the req
	 * @param res
	 *            the res
	 * @return true, if successful
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	protected boolean handleHandShake(final HttpServletRequest req,
			final HttpServletResponse res) throws IOException {

		final String time = req.getHeader("X-Eve-requestToken");
		if (time == null) {
			return false;
		}
		final String url = req.getRequestURI();
		final String id = getId(url);
		final HttpTransport transport = HttpService.get(myUrl, id);
		if (transport != null) {
			final String token = transport.getTokenstore().get(time);
			if (token == null) {
				res.sendError(HttpServletResponse.SC_PRECONDITION_FAILED);
			} else {
				res.setHeader("X-Eve-replyToken", token);
				res.setStatus(HttpServletResponse.SC_OK);
				res.flushBuffer();
			}
		} else {
			res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					"Can't find the correct transport:'" + myUrl + "' for:'"
							+ id + "'");
		}
		return true;
	}

	/**
	 * Do hand shake.
	 * 
	 * @param req
	 *            the req
	 * @return the handshake
	 */
	protected Handshake doHandShake(final HttpServletRequest req) {
		final String tokenTupple = req.getHeader("X-Eve-Token");
		if (tokenTupple == null) {
			// This is a webpage, no HandShake available.
			return Handshake.NAK;
		}

		try {
			final String senderUrl = req.getHeader("X-Eve-SenderUrl");
			if (senderUrl != null && !senderUrl.equals("")) {
				final ObjectNode tokenObj = (ObjectNode) JOM.getInstance()
						.readTree(tokenTupple);
				final HttpGet httpGet = new HttpGet(senderUrl);
				httpGet.setHeader("X-Eve-requestToken", tokenObj.get("time")
						.textValue());
				final HttpResponse response = ApacheHttpClient.get().execute(
						httpGet);
				if (response != null
						&& response.getStatusLine().getStatusCode() == HttpServletResponse.SC_OK) {
					Header replyToken = response
							.getLastHeader("X-Eve-replyToken");
					if (replyToken == null) {
						LOG.log(Level.WARNING,
								"Failed to receive valid handshake, replyToken missing!:"
										+ response);
						return Handshake.INVALID;
					}
					if (tokenObj.get("token").textValue()
							.equals(replyToken.getValue())) {
						return Handshake.OK;
					}
				} else {
					LOG.log(Level.WARNING, "Failed to receive valid handshake:"
							+ response);
				}
			}
		} catch (final Exception e) {
			LOG.log(Level.WARNING, "", e);
		}

		return Handshake.INVALID;
	}

	/**
	 * Handle session.
	 * 
	 * @param req
	 *            the req
	 * @param res
	 *            the res
	 * @return true, if successful
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	protected boolean handleSession(final HttpServletRequest req,
			final HttpServletResponse res) throws IOException {
		try {

			if (req.getSession(false) != null) {
				return true;
			}

			final boolean doAuthentication = HttpService
					.doAuthentication(myUrl);
			if (doAuthentication) {
				final Handshake hs = doHandShake(req);
				if (hs.equals(Handshake.INVALID)) {
					return false;
				}

				if (hs.equals(Handshake.NAK)) {
					if (!req.authenticate(res)) {
						return false;
					}
				}
			}

			// generate new session:
			req.getSession(true);
		} catch (final Exception e) {
			res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					"Exception running HandleSession:" + e.getMessage());
			LOG.log(Level.WARNING, "", e);
			return false;
		}
		return true;
	}

	protected String getId(final String url) {
		String id = "";
		if (myUrl != null) {
			id = url.replace(myUrl.getRawPath(), "");
		} else {
			// TODO: this doesn't work with resources behind agentId
			id = url.replaceAll(".*/[^/]+", "");
		}
		return id.indexOf('/') > 0 ? id.substring(0, id.indexOf('/')) : id;
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
		if (id == null || id.equals("") || id.equals(myUrl.toASCIIString())) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
					"Couldn't parse URL, missing 'id'");
			resp.flushBuffer();
			return;
		}

		String sender = req.getHeader("X-Eve-SenderUrl");
		if (sender == null || sender.equals("")) {
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
		if (id == null || id.equals("") || id.equals(myUrl.toASCIIString())) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
					"Couldn't parse URL, missing 'id'");
			resp.flushBuffer();
			return;
		}
		final HttpTransport transport = HttpService.get(myUrl, id);

		resp.setContentType("text/plain");
		resp.getWriter().println(
				"You've found the servlet for agent:" + id + " ("
						+ (transport == null ? "not " : "") + " configured)");
		resp.getWriter().close();
		resp.flushBuffer();
	}
}
