package com.almende.eve.transport.http;

import java.net.URI;

import javax.servlet.Servlet;

import com.almende.eve.config.Config;

public interface Launcher {
	public void startServlet(final Servlet servlet, final URI servletPath, final Config config);
}
