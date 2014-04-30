/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.goldemo;

import java.io.Serializable;

import com.almende.util.jackson.JOM;
import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * The Class CycleState.
 */
public class CycleState implements Serializable {
	
	private static final long	serialVersionUID	= 3063215915628652369L;
	private int					cycle;
	private boolean				alive;
	
	/**
	 * Instantiates a new cycle state.
	 */
	public CycleState() {
	}
	
	/**
	 * Instantiates a new cycle state.
	 * 
	 * @param cycle
	 *            the cycle
	 * @param alive
	 *            the alive
	 */
	public CycleState(final int cycle, final boolean alive) {
		this.cycle = cycle;
		this.alive = alive;
	}
	
	/**
	 * Gets the cycle.
	 * 
	 * @return the cycle
	 */
	public int getCycle() {
		return cycle;
	}
	
	/**
	 * Sets the cycle.
	 * 
	 * @param cycle
	 *            the new cycle
	 */
	public void setCycle(final int cycle) {
		this.cycle = cycle;
	}
	
	/**
	 * Checks if is alive.
	 * 
	 * @return true, if is alive
	 */
	public boolean isAlive() {
		return alive;
	}
	
	/**
	 * Sets the alive.
	 * 
	 * @param alive
	 *            the new alive
	 */
	public void setAlive(final boolean alive) {
		this.alive = alive;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		try {
			return JOM.getInstance().writeValueAsString(this);
		} catch (final JsonProcessingException e) {
			return super.toString();
		}
	}
}
