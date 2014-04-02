/**
 * @file CalendarAgent.java
 * 
 * @brief
 *        TODO: brief
 * 
 * @license
 *          Licensed under the Apache License, Version 2.0 (the "License"); you
 *          may not
 *          use this file except in compliance with the License. You may obtain
 *          a copy
 *          of the License at
 * 
 *          http://www.apache.org/licenses/LICENSE-2.0
 * 
 *          Unless required by applicable law or agreed to in writing, software
 *          distributed under the License is distributed on an "AS IS" BASIS,
 *          WITHOUT
 *          WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 *          the
 *          License for the specific language governing permissions and
 *          limitations under
 *          the License.
 * 
 *          Copyright © 2010-2011 Almende B.V.
 * 
 * @author Jos de Jong, <jos@almende.org>
 * @date 2012-07-02
 */

package com.almende.eve.agent;

import com.almende.eve.rpc.annotation.Access;
import com.almende.eve.rpc.annotation.AccessType;
import com.almende.eve.rpc.annotation.Name;
import com.almende.eve.rpc.annotation.Optional;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Interface CalendarAgent.
 */
@Access(AccessType.PUBLIC)
public interface CalendarAgent {
	
	/**
	 * Gets the username.
	 * 
	 * @return the username
	 */
	String getUsername();
	
	/**
	 * Gets the email.
	 * 
	 * @return the email
	 */
	String getEmail();
	
	/**
	 * Gets the calendar list.
	 * 
	 * @return the calendar list
	 * @throws Exception
	 *             the exception
	 */
	ArrayNode getCalendarList() throws Exception;
	
	/**
	 * Gets the events.
	 * 
	 * @param start
	 *            the start
	 * @param end
	 *            the end
	 * @param calendarId
	 *            the calendar id
	 * @return the events
	 * @throws Exception
	 *             the exception
	 */
	ArrayNode getEvents(@Optional @Name("start") String start,
			@Optional @Name("end") String end,
			@Optional @Name("calendarId") String calendarId) throws Exception;
	
	/**
	 * Gets the event.
	 * 
	 * @param eventId
	 *            the event id
	 * @param calendarId
	 *            the calendar id
	 * @return the event
	 * @throws Exception
	 *             the exception
	 */
	ObjectNode getEvent(@Name("eventId") String eventId,
			@Optional @Name("calendarId") String calendarId) throws Exception;
	
	/**
	 * Gets the busy.
	 * 
	 * @param timeMin
	 *            the time min
	 * @param timeMax
	 *            the time max
	 * @param calendarId
	 *            the calendar id
	 * @param timeZone
	 *            the time zone
	 * @return the busy
	 * @throws Exception
	 *             the exception
	 */
	ArrayNode getBusy(@Name("timeMin") String timeMin,
			@Name("timeMax") String timeMax,
			@Optional @Name("calendarId") String calendarId,
			@Optional @Name("timeZone") String timeZone) throws Exception;
	
	/**
	 * Creates the event.
	 * 
	 * @param event
	 *            the event
	 * @param calendarId
	 *            the calendar id
	 * @return the object node
	 * @throws Exception
	 *             the exception
	 */
	ObjectNode createEvent(@Name("event") ObjectNode event,
			@Optional @Name("calendarId") String calendarId) throws Exception;
	
	/**
	 * Update event.
	 * 
	 * @param event
	 *            the event
	 * @param calendarId
	 *            the calendar id
	 * @return the object node
	 * @throws Exception
	 *             the exception
	 */
	ObjectNode updateEvent(@Name("event") ObjectNode event,
			@Optional @Name("calendarId") String calendarId) throws Exception;
	
	/**
	 * Delete event.
	 * 
	 * @param eventId
	 *            the event id
	 * @param calendarId
	 *            the calendar id
	 * @throws Exception
	 *             the exception
	 */
	void deleteEvent(@Name("eventId") String eventId,
			@Optional @Name("calendarId") String calendarId) throws Exception;
	
	/*
	 * TODO: implement methods getChangedEvents, getFree, getBusy
	 * abstract public List<Event> getChangedEvents(@Name("since") DateTime
	 * since)
	 * throws Exception;
	 */
}
