/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.algorithms.test.agents;

import com.almende.eve.agent.AgentConfig;
import com.almende.eve.algorithms.agents.NodeAgent;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class TestLinkedListAgent.
 */
public class TestLinkedListAgent extends NodeAgent {
	private final int[]	value	= new int[] { 0 };

	/**
	 * Instantiates a new test linked list agent.
	 *
	 * @param id
	 *            the id
	 * @param val
	 *            the val
	 */
	public TestLinkedListAgent(final String id, final int val) {
		super(id, AgentConfig.create());
		this.setValue(val);
		this.getGraph().llcomparator("X", new Comparable<ObjectNode>() {

			@Override
			public int compareTo(ObjectNode o) {
				if (value[0] == o.get("value").asInt()) {
					return 0;
				} else {
					return value[0] > o.get("value").asInt() ? 1 : -1;
				}
			}

		});
	}

	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	public int getValue() {
		return value[0];
	}

	/**
	 * Sets the value.
	 *
	 * @param value
	 *            the new value
	 */
	public void setValue(int value) {
		this.value[0] = value;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getId() + ":" + getValue();
	}
}
