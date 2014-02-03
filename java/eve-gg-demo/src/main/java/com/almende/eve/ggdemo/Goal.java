/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.ggdemo;

import java.io.Serializable;
import java.util.UUID;

/**
 * The Class Goal.
 */
public class Goal implements Serializable {

	private static final long	serialVersionUID	= -463898073095891971L;
	private double goalPct = 70;
	private double percentage = 0;
	private int agentCnt = 0;
	private int ttl=0;
	private String id = UUID.randomUUID().toString();
	
	/**
	 * Gets the goal pct.
	 * 
	 * @return the goal pct
	 */
	public double getGoalPct() {
		return goalPct;
	}
	
	/**
	 * Sets the goal pct.
	 * 
	 * @param goalPct
	 *            the new goal pct
	 */
	public void setGoalPct(double goalPct) {
		this.goalPct = goalPct;
	}
	
	/**
	 * Gets the percentage.
	 * 
	 * @return the percentage
	 */
	public double getPercentage() {
		return percentage;
	}
	
	/**
	 * Sets the percentage.
	 * 
	 * @param percentage
	 *            the new percentage
	 */
	public void setPercentage(double percentage) {
		this.percentage = percentage;
	}
	
	/**
	 * Gets the id.
	 * 
	 * @return the id
	 */
	public String getId() {
		return id;
	}
	
	/**
	 * Sets the id.
	 * 
	 * @param id
	 *            the new id
	 */
	public void setId(String id) {
		this.id = id;
	}
	
	/**
	 * Gets the agent cnt.
	 * 
	 * @return the agent cnt
	 */
	public int getAgentCnt() {
		return agentCnt;
	}
	
	/**
	 * Sets the agent cnt.
	 * 
	 * @param agentCnt
	 *            the new agent cnt
	 */
	public void setAgentCnt(int agentCnt) {
		this.agentCnt = agentCnt;
	}
	
	/**
	 * Gets the ttl.
	 * 
	 * @return the ttl
	 */
	public int getTtl() {
		return ttl;
	}
	
	/**
	 * Sets the ttl.
	 * 
	 * @param ttl
	 *            the new ttl
	 */
	public void setTtl(int ttl) {
		this.ttl = ttl;
	}
	
	
}
