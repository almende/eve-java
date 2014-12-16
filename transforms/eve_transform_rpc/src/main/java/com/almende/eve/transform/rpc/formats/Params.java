/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.transform.rpc.formats;

import com.almende.util.jackson.JOM;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class Params.
 */
public class Params extends ObjectNode {
	
	/**
	 * Instantiates a new config.
	 */
	public Params() {
		super(JOM.getInstance().getNodeFactory());
	}
	
	/**
	 * Instantiates a new params.
	 *
	 * @param node
	 *            the node
	 */
	public Params(final ObjectNode node){
		super(JOM.getInstance().getNodeFactory());
		if (node != null){
			this.setAll(node);
		}
	}
	
	/**
	 * Adds a parameter
	 *
	 * @param name
	 *            the name
	 * @param value
	 *            the value
	 */
	public void add(final String name, final Object value){
		super.set(name, JOM.getInstance().valueToTree(value));
	}
	
	/**
	 * Extend these params with the other tree, overwriting existing
	 * fields, adding new ones.
	 * 
	 * @param other
	 *            the other
	 * @return the params
	 */
	public Params extend(final ObjectNode other) {
		this.setAll(other);
		return this;
	}
}
