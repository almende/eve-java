package com.almende.test;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.xml.XmlConfiguration;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.RunListener;

/**
 * @author ludo
 * 
 */
public class TestRunListener extends RunListener {
	private static final Logger	LOG	= Logger.getLogger(TestRunListener.class
											.getName());
	private static Server server= null;
	
	public void testRunStarted(final Description description) throws Exception {
		LOG.info("Starting Jetty for test run");
		System.setProperty("com.almende.eve.runtime.environment", "Development");
		
		Resource fileserver_xml = Resource
				.newSystemResource("jetty-combined.xml");
		XmlConfiguration configuration = new XmlConfiguration(
				fileserver_xml.getInputStream());
		server = (Server) configuration.configure();
		
		Resource webApp_xml = Resource.newSystemResource("jetty-test.xml");
		configuration = new XmlConfiguration(
				webApp_xml.getInputStream());
		
		WebAppContext context = (WebAppContext) configuration.configure();
		context.setDescriptor("./src/test/webapp/WEB-INF/web.xml");
		context.setResourceBase("./src/test/webapp");
		context.setContextPath("/");
		context.setParentLoaderPriority(true);
		
		server.setHandler(context);
		
		
		try {
			server.start();
		} catch (Exception e) {
			LOG.log(Level.SEVERE, "Couldn't start embedded Jetty server!", e);
		}
	}
	
	public void testRunFinished(final Result result) throws Exception {
		LOG.info("Test run finished, stopping Jetty.");
		if (server != null){
			server.stop();
		}
	}
	
}
