/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.entity.calendar;

import java.io.Serializable;
import java.util.List;

import org.joda.time.Interval;

/**
 * Structure for storing data of a calendar agent, eventId and busy profile
 * this structure is used by the MeetingAgent.
 */
@SuppressWarnings("serial")
public class AgentData implements Serializable {
	
	/**
	 * The event id.
	 */
	public String			eventId			= null;
	
	/**
	 * The activity updated.
	 */
	public String			activityUpdated	= null;
	
	/**
	 * The event updated.
	 */
	public String			eventUpdated	= null;
	
	/**
	 * The busy.
	 */
	public List<Interval>	busy			= null;
}
