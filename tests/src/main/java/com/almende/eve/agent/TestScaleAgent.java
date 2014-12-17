/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.agent;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import com.almende.eve.capabilities.Config;
import com.almende.eve.protocol.jsonrpc.annotation.Access;
import com.almende.eve.protocol.jsonrpc.annotation.AccessType;
import com.almende.eve.protocol.jsonrpc.annotation.Name;
import com.almende.util.jackson.JOM;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class TestScaleAgent.
 */
@Access(AccessType.PUBLIC)
public class TestScaleAgent extends Agent {

	URI			parent		= null;
	List<URI>	children	= new ArrayList<URI>();

	/**
	 * Instantiates a new test scale agent.
	 *
	 * @param id
	 *            the id
	 * @param config
	 *            the config
	 * @param parent
	 *            the parent
	 * @param nofChildren
	 *            the nof children
	 */
	public TestScaleAgent(final String id, final Config config,
			final URI parent, final List<Integer> nofChildren) {
		super(id, config);
		this.parent = parent;
		getState().put("key", "value");
		if (nofChildren.size() > 0) {
			for (int i = 0; i < nofChildren.get(0); i++) {
				Agent newAgent = new TestScaleAgent(id + "_" + i, config,
						getUrls().get(0), nofChildren.subList(1,
								nofChildren.size()));
				children.add(newAgent.getUrls().get(0));
			}
		}
	}

	final List<URI>	result		= new ArrayList<URI>();
	final int[]		resCount	= new int[1];

	/**
	 * Put leafs.
	 *
	 * @param res
	 *            the res
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void putLeafs(@Name("leafs") List<URI> res) throws IOException {
		synchronized (result) {
			result.addAll(res);
		}
		synchronized (resCount) {
			resCount[0]--;
			if (resCount[0] <= 0) {
				if (parent != null) {
					ObjectNode params = JOM.createObjectNode();
					params.set("leafs", JOM.getInstance().valueToTree(result));
					caller.call(parent, "putLeafs", params);
				}
				synchronized (result) {
					result.notifyAll();
				}
			}
		}
	}

	/**
	 * Request leafs.
	 *
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void requestLeafs() throws IOException {
		synchronized (result) {
			result.clear();
		}
		getState().get("key", String.class);
		resCount[0] = children.size();
		if (children.size() > 0) {
			for (URI child : children) {
				caller.call(child, "requestLeafs", null);
			}
		} else {
			synchronized (result) {
				result.add(getUrls().get(0));
			}
			ObjectNode params = JOM.createObjectNode();
			params.set("leafs", JOM.getInstance().valueToTree(result));
			caller.call(parent, "putLeafs", params);
		}

	}

	/**
	 * Gets the all leafs.
	 *
	 * @return the all leafs
	 * @throws IOException
	 *             IOException
	 */
	public List<URI> getAllLeafs() throws IOException {
		result.clear();
		resCount[0] = children.size();
		if (children.size() > 0) {
			for (URI child : children) {
				caller.call(child, "requestLeafs", null);
			}
		}
		synchronized (result) {
			try {
				result.wait();
			} catch (InterruptedException e) {}
		}
		return result;
	}
}
