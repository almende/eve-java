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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.almende.util.StringUtil;

/**
 * The Class EveServlet.
 */
public class EveServlet extends HttpServlet {
	private static final long	serialVersionUID	= -4635490705591217600L;
	
	private static final Logger	LOG					= Logger.getLogger(EveServlet.class
															.getSimpleName());
	private URI					myUrl				= null;
	
	/**
	 * Instantiates a new eve servlet.
	 * 
	 * @param servletUrl
	 *            the servlet url
	 */
	public EveServlet(URI servletUrl) {
		if (servletUrl != null) {
			this.myUrl = servletUrl;
		}
	}
	
	private String getId(String url){
		String id = "";
		if (myUrl != null) {
			id = url.replace(myUrl.getRawPath(), "");
		} else {
			id = url.replaceAll(".*/[^/]+", "");
		}
		return id;
	}
	
	@Override
	public void doPost(final HttpServletRequest req,
			final HttpServletResponse resp) throws IOException,
			ServletException {
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
			senderUrl = new URI(sender);
		} catch (URISyntaxException e) {
			LOG.log(Level.WARNING, "Couldn't parse senderUrl:" + sender, e);
		}
		HttpTransport transport = HttpService.get(myUrl, id);
		if (transport != null) {
			try {
				String response = transport.receive(body, senderUrl);
				// TODO: It doesn't need to be json, should we handle mime-types
				// better?
				resp.addHeader("Content-Type", "application/json");
				resp.getWriter().println(response);
				resp.getWriter().close();
			} catch (IOException e) {
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
		final String url = req.getRequestURI();
		final String id = getId(url);
		if (id == null || id.equals("") || id.equals(myUrl.toASCIIString())) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
					"Couldn't parse URL, missing 'id'");
			resp.flushBuffer();
			return;
		}
		HttpTransport transport = HttpService.get(myUrl, id);
		
		resp.setContentType("text/plain");
		resp.getWriter().println(
				"You've found the servlet for agent:" + id + " ("
						+ (transport == null ? "not " : "") + " configured)");
		resp.getWriter().close();
		resp.flushBuffer();
	}
}
