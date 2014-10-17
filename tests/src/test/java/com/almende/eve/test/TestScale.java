/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.junit.Test;

import com.almende.eve.agent.AgentConfig;
import com.almende.eve.agent.TestScaleAgent;
import com.almende.eve.state.file.FileStateConfig;

/**
 * The Class TestScale.
 */
public class TestScale extends TestCase {
	private static final Logger	LOG	= Logger.getLogger(TestScale.class
											.getName());
	
	/**
	 * Test Scale.
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws URISyntaxException
	 *             the URI syntax exception
	 * @throws InterruptedException
	 *             the interrupted exception
	 */
	@Test
	public void testScale() throws IOException, URISyntaxException,
			InterruptedException {
		AgentConfig config = new AgentConfig();
		FileStateConfig filestateconfig = new FileStateConfig();
		filestateconfig.setPath(".scaleTest");
		config.setState(filestateconfig);
		TestScaleAgent top = new TestScaleAgent("top",config,null,new ArrayList<Integer>());
		List<Integer> left = new ArrayList<Integer>();
		left.add(20);
		left.add(10);
		List<Integer> right = new ArrayList<Integer>();
		right.add(3);
		right.add(3);
		right.add(3);
		right.add(30);
		
		final long start = System.currentTimeMillis();
		LOG.warning("Starting!"+start);
		new TestScaleAgent("left",config,top.getUrls().get(0),left);
		TestScaleAgent rightAgent = new TestScaleAgent("right",config,top.getUrls().get(0),right);
		final long created = System.currentTimeMillis();
		LOG.warning("Created!"+ created + "("+(created-start)+" ms)");
		
		List<URI> leafs = rightAgent.getAllLeafs();
		assertEquals(3*270,leafs.size());
		
		final long end = System.currentTimeMillis();
		LOG.warning("Done!"+ end + "("+(end-created)+" ms)");
		
	}
	
}
