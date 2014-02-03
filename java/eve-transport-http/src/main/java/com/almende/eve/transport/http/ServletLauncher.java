package com.almende.eve.transport.http;

import java.net.URI;

import javax.servlet.Servlet;

import com.almende.eve.config.Config;

public interface ServletLauncher {
	public void add(final Servlet servlet, final URI servletPath, final Config config);
}
