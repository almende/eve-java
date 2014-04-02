/**
 * @brief
 *        The MeetingAgent can dynamically schedule a meeting with multiple
 *        attendees.
 * 
 *        The MeetingAgent synchronizes a meeting for one or multiple attendees,
 *        and dynamically schedules the meeting on a free time slot in all
 *        calendars,
 *        reckoning with office hours (Mon-Fri, 9:00-17:00, CET). The duration,
 *        summary, location, and one or multiple attendees can be specified.
 *        After having created a meeting, the meeting can be updated (add/remove
 *        attendees, change summary, duration, start time, etc.). The meetings
 *        can be
 *        changed in both your Google Calendar and via the MeetingAgent itself.
 * 
 *        The MeetingAgent regularly checks for updates its meeting and
 *        reschedules it
 *        when needed. The update frequency depends on the time the meeting was
 *        last
 *        changed. When just changed, the MeetingAgent checks every 10 seconds,
 *        and
 *        this interval is linearly decreased towards once an hour.
 * 
 *        The MeetingAgent uses Activity as data structure, and uses this
 *        structure
 *        to describe a meeting. To setup a MeetingAgent call the method
 *        setActivity
 *        or updateActivity. The MeetingAgent will automatically start
 *        scheduling and
 *        monitoring the meeting, and stops with monitoring once the event is
 *        past.
 * 
 *        Core methods are:
 *        setActivity Clear current meeting and setup a new meeting
 *        updateActivity Update current meeting
 *        update Force an update: synchronize and reschedule the meeting
 *        clear Remove the meetings from the attendees calendars, and
 *        delete all stored information.
 * 
 *        A minimal, valid Activity structure looks like:
 *        {
 *        "summary": "Test C",
 *        "constraints": {
 *        "attendees": [
 *        {
 *        "agent": "http://myserver.com/agents/googlecalendaragent/123/",
 *        },
 *        {
 *        "agent": "http://myserver.com/agents/googlecalendaragent/456/",
 *        }
 *        ]
 *        }
 *        }
 * 
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
 *          Copyright © 2012 Almende B.V.
 * 
 * @author Jos de Jong, <jos@almende.org>
 * @date 2012-08-09
 */

package com.almende.eve.agent;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.joda.time.MutableDateTime;

import com.almende.eve.entity.Issue;
import com.almende.eve.entity.Issue.TYPE;
import com.almende.eve.entity.Weight;
import com.almende.eve.entity.activity.Activity;
import com.almende.eve.entity.activity.Attendee;
import com.almende.eve.entity.activity.Attendee.RESPONSE_STATUS;
import com.almende.eve.entity.activity.Preference;
import com.almende.eve.entity.activity.Status;
import com.almende.eve.entity.calendar.AgentData;
import com.almende.eve.rpc.annotation.Access;
import com.almende.eve.rpc.annotation.AccessType;
import com.almende.eve.rpc.annotation.Name;
import com.almende.eve.rpc.annotation.Optional;
import com.almende.eve.rpc.jsonrpc.JSONRPCException;
import com.almende.eve.rpc.jsonrpc.JSONRequest;
import com.almende.eve.rpc.jsonrpc.jackson.JOM;
import com.almende.eve.state.State;
import com.almende.util.IntervalsUtil;
import com.almende.util.TypeUtil;
import com.almende.util.WeightsUtil;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class MeetingAgent.
 */
@Access(AccessType.PUBLIC)
public class MeetingAgent extends Agent {
	private static final Logger	LOG								= Logger.getLogger(MeetingAgent.class
																		.getName());
	/* number of days to look ahead when planning a meeting */
	private static final int	LOOK_AHEAD_DAYS					= 7;
	private static final Double	WEIGHT_BUSY_OPTIONAL_ATTENDEE	= -1.0;
	private static final Double	WEIGHT_OFFICE_HOURS				= 10.0;
	private static final Double	WEIGHT_PREFERRED_INTERVAL		= 0.1;
	// private static final Double WEIGHT_UNDESIRED_INTERVAL = -0.1;
	private static final Double	WEIGHT_DELAY_PER_DAY			= -0.1;
	
	/**
	 * Convenience method to quickly set a new activity.
	 * Currently stored activity will be removed.
	 * 
	 * @param summary
	 *            Description for the meeting
	 * @param location
	 *            the location
	 * @param duration
	 *            Duration in minutes
	 * @param agents
	 *            List with calendar agent urls of the attendees
	 */
	public void setActivityQuick(@Name("summary") final String summary,
			@Optional @Name("location") final String location,
			@Name("duration") final Integer duration,
			@Name("agents") final List<String> agents) {
		final Activity activity = new Activity();
		activity.setSummary(summary);
		activity.withConstraints().withLocation().setSummary(location);
		for (final String agent : agents) {
			final Attendee attendee = new Attendee();
			attendee.setAgent(agent);
			activity.withConstraints().withAttendees().add(attendee);
		}
		
		update();
	}
	
	/**
	 * Set a new activity. Currently stored activity will be removed.
	 * 
	 * @param activity
	 *            the activity
	 * @return the activity
	 * @throws Exception
	 *             the exception
	 */
	public Activity setActivity(@Name("activity") final Activity activity)
			throws Exception {
		clear();
		
		return updateActivity(activity);
	}
	
	/**
	 * Get a set with attendee agent urls from an activity returns an empty list
	 * when no attendees
	 * 
	 * @param activity
	 * @return
	 */
	private Set<String> getAgents(final Activity activity) {
		final Set<String> agents = new TreeSet<String>();
		for (final Attendee attendee : activity.withConstraints()
				.withAttendees()) {
			final String agent = attendee.getAgent();
			if (agent != null) {
				agents.add(agent);
			}
		}
		
		return agents;
	}
	
	/**
	 * update the activity for meeting agent.
	 * 
	 * @param updatedActivity
	 *            the updated activity
	 * @return the activity
	 * @throws Exception
	 *             the exception
	 */
	public Activity updateActivity(
			@Name("activity") final Activity updatedActivity) throws Exception {
		Activity activity = getState().get("activity", Activity.class);
		if (activity == null) {
			activity = new Activity();
		}
		
		final Set<String> prevAttendees = getAgents(activity);
		
		// if no updated timestamp is provided, set the timestamp to now
		if (updatedActivity.withStatus().getUpdated() == null) {
			updatedActivity.withStatus().setUpdated(DateTime.now().toString());
		}
		
		// synchronize with the stored activity
		activity = Activity.sync(activity, updatedActivity);
		
		// ensure the url of the meeting agent is filled in
		final URI myUrl = getFirstUrl("http");
		activity.setAgent(myUrl);
		
		// create duration when missing
		Long duration = activity.withConstraints().withTime().getDuration();
		if (duration == null) {
			duration = Duration.standardHours(1).getMillis(); // 1 hour in ms
			activity.withConstraints().withTime().setDuration(duration);
		}
		
		// remove calendar events from removed attendees
		final Set<String> currentAttendees = getAgents(activity);
		final Set<String> removedAttendees = new TreeSet<String>(prevAttendees);
		removedAttendees.removeAll(currentAttendees);
		for (final String attendee : removedAttendees) {
			clearAttendee(attendee);
		}
		
		getState().put("activity", activity);
		
		// update all attendees, start timer to regularly check
		update();
		
		return getState().get("activity", Activity.class);
	}
	
	/**
	 * Get meeting summary.
	 * 
	 * @return the summary
	 */
	public String getSummary() {
		final Activity activity = getState().get("activity", Activity.class);
		return (activity != null) ? activity.getSummary() : null;
	}
	
	/**
	 * get meeting activity returns null if no activity has been initialized.
	 * 
	 * @return activity
	 */
	public Activity getActivity() {
		return getState().get("activity", Activity.class);
	}
	
	/**
	 * The the complete state of the agent.
	 * TODO: remove this temporary method
	 * 
	 * @return state
	 */
	public Object getEverything() {
		return getState();
	}
	
	/**
	 * Apply the constraints of the the activity (for example duration)
	 * 
	 * @param activity
	 * @return changed Returns true if the activity is changed
	 */
	private boolean applyConstraints() {
		final Activity activity = getState().get("activity", Activity.class);
		boolean changed = false;
		if (activity == null) {
			return false;
		}
		
		// constraints on attendees/resources
		/*
		 * TODO: copy actual attendees to status.attendees
		 * List<Attendee> constraintsAttendees =
		 * activity.withConstraints().withAttendees();
		 * List<Attendee> attendees = new ArrayList<Attendee>();
		 * for (Attendee attendee : constraintsAttendees) {
		 * attendees.add(attendee.clone());
		 * }
		 * activity.withStatus().setAttendees(attendees);
		 * // TODO: is it needed to check if the attendees are changed?
		 */
		
		// check time constraints
		final Long duration = activity.withConstraints().withTime()
				.getDuration();
		if (duration != null) {
			final String start = activity.withStatus().getStart();
			final String end = activity.withStatus().getEnd();
			if (start != null && end != null) {
				final DateTime startTime = new DateTime(start);
				DateTime endTime = new DateTime(end);
				final Interval interval = new Interval(startTime, endTime);
				if (interval.toDurationMillis() != duration) {
					LOG.info("status did not match constraints. "
							+ "Changed end time to match the duration of "
							+ duration + " ms");
					
					// duration does not match. adjust the end time
					endTime = startTime.plus(duration);
					activity.withStatus().setEnd(endTime.toString());
					activity.withStatus().setUpdated(DateTime.now().toString());
					
					changed = true;
				}
			}
		}
		
		// location constraints
		final String newLocation = activity.withConstraints().withLocation()
				.getSummary();
		final String oldLocation = activity.withStatus().withLocation()
				.getSummary();
		if (newLocation != null && !newLocation.equals(oldLocation)) {
			activity.withStatus().withLocation().setSummary(newLocation);
			changed = true;
		}
		
		if (changed) {
			// store the updated activity
			getState().put("activity", activity);
		}
		return changed;
	}
	
	/**
	 * synchronize the meeting in all attendees calendars
	 */
	private boolean syncEvents() {
		LOG.info("syncEvents started");
		Activity activity = getActivity();
		
		boolean changed = false;
		if (activity != null) {
			final String updatedBefore = activity.withStatus().getUpdated();
			
			for (final Attendee attendee : activity.withConstraints()
					.withAttendees()) {
				final String agent = attendee.getAgent();
				if (agent != null) {
					if (attendee.getResponseStatus() != RESPONSE_STATUS.declined) {
						syncEvent(agent);
					} else {
						clearAttendee(agent);
					}
				}
			}
			
			activity = getActivity();
			final String updatedAfter = activity.withStatus().getUpdated();
			
			changed = !updatedBefore.equals(updatedAfter);
		}
		
		return changed;
	}
	
	/**
	 * Schedule or re-schedule the meeting. Synchronize the events, retrieve
	 * busy profiles, re-schedule the event.
	 */
	public void update() {
		// TODO: optimize the update method
		LOG.info("update started");
		
		// stop running tasks
		stopAutoUpdate();
		
		clearIssues();
		
		// synchronize the events
		boolean changedEvent = syncEvents();
		if (changedEvent) {
			syncEvents();
		}
		
		// Check if the activity is finished
		// If not, schedule a new update task. Else we are done
		Activity activity = getActivity();
		final String start = (activity != null) ? activity.withStatus()
				.getStart() : null;
		final String updated = (activity != null) ? activity.withStatus()
				.getUpdated() : null;
		boolean isFinished = false;
		if (start != null && (new DateTime(start)).isBefore(DateTime.now())) {
			// start of the event is in the past
			isFinished = true;
			if (updated != null
					&& (new DateTime(updated)).isAfter(new DateTime(start))) {
				// if changed after the last planned start time, then it is
				// updated afterwards, so do not mark as finished
				isFinished = false;
			}
		}
		if (activity != null && !isFinished) {
			// not yet finished. Reschedule the activity
			updateBusyIntervals();
			
			final boolean changedConstraints = applyConstraints();
			final boolean rescheduled = scheduleActivity();
			
			if (changedConstraints || rescheduled) {
				changedEvent = syncEvents();
				if (changedEvent) {
					syncEvents();
				}
			}
			
			// TODO: not so nice adjusting the activityStatus here this way
			activity = getActivity();
			if (activity.withStatus().getActivityStatus() != Status.ACTIVITY_STATUS.error) {
				// store status of a activity as "planned"
				activity.withStatus().setActivityStatus(
						Status.ACTIVITY_STATUS.planned);
				getState().put("activity", activity);
			}
			
			startAutoUpdate();
		} else {
			// store status of a activity as "executed"
			activity.withStatus().setActivityStatus(
					Status.ACTIVITY_STATUS.executed);
			getState().put("activity", activity);
			
			LOG.info("The activity is over, my work is done. Goodbye world.");
		}
	}
	
	/**
	 * Get the timestamp rounded to the next half hour
	 * 
	 * @return
	 */
	private DateTime getNextHalfHour() {
		DateTime next = DateTime.now();
		next = next.minusMillis(next.getMillisOfSecond());
		next = next.minusSeconds(next.getSecondOfMinute());
		
		if (next.getMinuteOfHour() > 30) {
			next = next.minusMinutes(next.getMinuteOfHour());
			next = next.plusMinutes(60);
		} else {
			next = next.minusMinutes(next.getMinuteOfHour());
			next = next.plusMinutes(30);
		}
		
		return next;
	}
	
	/**
	 * Schedule the meeting based on currently known event status, infeasible
	 * intervals, and preferences
	 * 
	 * @return rescheduled Returns true if the activity has been rescheduled
	 *         When rescheduled, events must be synchronized again with
	 *         syncEvents.
	 */
	private boolean scheduleActivity() {
		LOG.info("scheduleActivity started"); // TODO: cleanup
		final State state = getState();
		final Activity activity = state.get("activity", Activity.class);
		if (activity == null) {
			return false;
		}
		
		// read planned start and end from the activity
		DateTime activityStart = null;
		if (activity.withStatus().getStart() != null) {
			activityStart = new DateTime(activity.withStatus().getStart());
		}
		DateTime activityEnd = null;
		if (activity.withStatus().getEnd() != null) {
			activityEnd = new DateTime(activity.withStatus().getEnd());
		}
		Interval activityInterval = null;
		if (activityStart != null && activityEnd != null) {
			activityInterval = new Interval(activityStart, activityEnd);
		}
		
		// calculate solutions
		final List<Weight> solutions = calculateSolutions();
		if (solutions.size() > 0) {
			// there are solutions. yippie!
			final Weight solution = solutions.get(0);
			if (activityInterval == null
					|| !solution.getInterval().equals(activityInterval)) {
				// interval is changed, save new interval
				final Status status = activity.withStatus();
				status.setStart(solution.getStart().toString());
				status.setEnd(solution.getEnd().toString());
				status.setActivityStatus(Status.ACTIVITY_STATUS.planned);
				status.setUpdated(DateTime.now().toString());
				state.put("activity", activity);
				// TODO: cleanup logging
				LOG.info("Activity replanned at " + solution.toString());
				try {
					// TODO: cleanup
					LOG.info("Replanned activity: "
							+ JOM.getInstance().writeValueAsString(activity));
				} catch (final Exception e) {
				}
				return true;
			}
			// planning did not change. nothing to do.
		} else {
			if (activityStart != null || activityEnd != null) {
				// no solution
				final Issue issue = new Issue();
				issue.setCode(Issue.NO_PLANNING);
				issue.setType(Issue.TYPE.error);
				issue.setMessage("No free interval found for the meeting");
				issue.setTimestamp(DateTime.now().toString());
				// TODO: generate hints
				addIssue(issue);
				
				final Status status = activity.withStatus();
				status.setStart(null);
				status.setEnd(null);
				status.setActivityStatus(Status.ACTIVITY_STATUS.error);
				status.setUpdated(DateTime.now().toString());
				state.put("activity", activity);
				LOG.info(issue.getMessage()); // TODO: cleanup logging
				return true;
			}
			// planning did not change (no solution was already the case)
		}
		
		return false;
	}
	
	/**
	 * Calculate all feasible intervals with their preference weight, based on
	 * the event status, stored infeasible intervals, and preferred intervals.
	 * If there are no solutions, an empty array is returned.
	 * 
	 * @return solutions
	 */
	private List<Weight> calculateSolutions() {
		LOG.info("calculateSolutions started"); // TODO: cleanup
		
		final State state = getState();
		final List<Weight> solutions = new ArrayList<Weight>();
		
		// get the activity
		final Activity activity = state.get("activity", Activity.class);
		if (activity == null) {
			return solutions;
		}
		
		// get infeasible intervals
		ArrayList<Interval> infeasible = state.get("infeasible",
				new TypeUtil<ArrayList<Interval>>() {
				});
		if (infeasible == null) {
			infeasible = new ArrayList<Interval>();
		}
		
		// get preferred intervals
		List<Weight> preferred = state.get("preferred",
				new TypeUtil<ArrayList<Weight>>() {
				});
		if (preferred == null) {
			preferred = new ArrayList<Weight>();
		}
		
		// get the duration of the activity
		final Long durationLong = activity.withConstraints().withTime()
				.getDuration();
		Duration duration = null;
		if (durationLong != null) {
			duration = new Duration(durationLong);
		} else {
			// TODO: give error when duration is not defined?
			duration = Duration.standardHours(1);
		}
		
		// check interval at next half hour
		final DateTime firstTimeslot = getNextHalfHour();
		Interval test = new Interval(firstTimeslot,
				firstTimeslot.plus(duration));
		testInterval(infeasible, preferred, test, solutions);
		
		// loop over all infeasible intervals
		for (final Interval i : infeasible) {
			// test timeslot left from the infeasible interval
			test = new Interval(i.getStart().minus(duration), i.getStart());
			testInterval(infeasible, preferred, test, solutions);
			
			// test timeslot right from the infeasible interval
			test = new Interval(i.getEnd(), i.getEnd().plus(duration));
			testInterval(infeasible, preferred, test, solutions);
		}
		
		// loop over all preferred intervals
		for (final Weight w : preferred) {
			// test timeslot left from the start of the preferred interval
			test = new Interval(w.getStart().minus(duration), w.getStart());
			testInterval(infeasible, preferred, test, solutions);
			
			// test timeslot right from the start of the preferred interval
			test = new Interval(w.getStart(), w.getStart().plus(duration));
			testInterval(infeasible, preferred, test, solutions);
			
			// test timeslot left from the end of the preferred interval
			test = new Interval(w.getEnd().minus(duration), w.getEnd());
			testInterval(infeasible, preferred, test, solutions);
			
			// test timeslot right from the end of the preferred interval
			test = new Interval(w.getEnd(), w.getEnd().plus(duration));
			testInterval(infeasible, preferred, test, solutions);
		}
		
		// order the calculated feasible timeslots by weight, from highest to
		// lowest. In case of equals weights, the timeslots are ordered by
		// start date
		class WeightComparator implements Comparator<Weight> {
			@Override
			public int compare(final Weight a, final Weight b) {
				if (a.getWeight() != null && b.getWeight() != null) {
					final int cmp = Double
							.compare(a.getWeight(), b.getWeight());
					if (cmp == 0) {
						return a.getStart().compareTo(b.getStart());
					} else {
						return -cmp;
					}
				}
				return 0;
			}
		}
		final WeightComparator comparator = new WeightComparator();
		Collections.sort(solutions, comparator);
		
		// remove duplicates
		int i = 1;
		while (i < solutions.size()) {
			if (solutions.get(i).equals(solutions.get(i - 1))) {
				solutions.remove(i);
			} else {
				i++;
			}
		}
		
		return solutions;
	}
	
	/**
	 * Test if given interval is feasible. If so, calculate the preference
	 * weight and add it to the provided array with solutions
	 * 
	 * @param infeasible
	 * @param preferred
	 * @param test
	 * @param solutions
	 */
	private void testInterval(final List<Interval> infeasible,
			final List<Weight> preferred, final Interval test,
			final List<Weight> solutions) {
		final boolean feasible = calculateFeasible(infeasible, test);
		if (feasible) {
			final double weight = calculatePreference(preferred, test);
			solutions.add(new Weight(test, weight));
		}
	}
	
	/**
	 * Start automatic updating
	 * The interval of the update task depends on the timestamp the activity
	 * is last updated. When recently updated, the interval is smaller.
	 * interval is minimum 10 sec and maximum 1 hour.
	 */
	public void startAutoUpdate() {
		final State state = getState();
		final Activity activity = getActivity();
		
		// determine the interval (1 hour by default)
		final long TEN_SECONDS = 10 * 1000;
		final long ONE_HOUR = 60 * 60 * 1000;
		long interval = ONE_HOUR; // default is 1 hour
		if (activity != null) {
			final String updated = activity.withStatus().getUpdated();
			if (updated != null) {
				final DateTime dateUpdated = new DateTime(updated);
				final DateTime now = DateTime.now();
				interval = new Interval(dateUpdated, now).toDurationMillis();
			}
		}
		if (interval < TEN_SECONDS) {
			interval = TEN_SECONDS;
		}
		if (interval > ONE_HOUR) {
			interval = ONE_HOUR;
		}
		
		// stop any running task
		stopAutoUpdate();
		
		// schedule an update task and store the task id
		final JSONRequest request = new JSONRequest("update", null);
		final String task = getScheduler().createTask(request, interval);
		state.put("updateTask", task);
		
		LOG.info("Auto update started. Interval = " + interval
				+ " milliseconds");
	}
	
	/**
	 * Stop automatic updating.
	 */
	public void stopAutoUpdate() {
		final State state = getState();
		
		final String task = state.get("updateTask", String.class);
		if (task != null) {
			getScheduler().cancelTask(task);
			state.remove("updateTask");
		}
		
		LOG.info("Auto update stopped");
	}
	
	/**
	 * Convert a calendar event into an activity
	 * 
	 * @param event
	 * @return activity
	 */
	private Activity convertEventToActivity(final ObjectNode event) {
		final Activity activity = new Activity();
		
		// agent
		URI agent = null;
		if (event.has("agent")) {
			agent = URI.create(event.get("agent").asText());
		}
		activity.setAgent(agent);
		
		// summary
		String summary = null;
		if (event.has("summary")) {
			summary = event.get("summary").asText();
		}
		activity.setSummary(summary);
		
		// description
		String description = null;
		if (event.has("description")) {
			description = event.get("description").asText();
		}
		activity.setDescription(description);
		
		// updated
		String updated = null;
		if (event.has("updated")) {
			updated = event.get("updated").asText();
		}
		activity.withStatus().setUpdated(updated);
		
		// start
		String start = null;
		if (event.with("start").has("dateTime")) {
			start = event.with("start").get("dateTime").asText();
		}
		activity.withStatus().setStart(start);
		
		// end
		String end = null;
		if (event.with("end").has("dateTime")) {
			end = event.with("end").get("dateTime").asText();
		}
		activity.withStatus().setEnd(end);
		
		// duration
		if (start != null && end != null) {
			final Interval interval = new Interval(new DateTime(start),
					new DateTime(end));
			final Long duration = interval.toDurationMillis();
			activity.withConstraints().withTime().setDuration(duration);
		}
		
		// location
		String location = null;
		if (event.has("location")) {
			location = event.get("location").asText();
		}
		activity.withConstraints().withLocation().setSummary(location);
		
		return activity;
	}
	
	/**
	 * Merge an activity into an event
	 * All fields that are in the event will be left as they are
	 * 
	 * @param event
	 * @param activity
	 */
	private void mergeActivityIntoEvent(final ObjectNode event,
			final Activity activity) {
		// merge static information
		event.put("agent", activity.getAgent().toASCIIString());
		event.put("summary", activity.getSummary());
		event.put("description", activity.getDescription());
		
		// / merge status information
		final Status status = activity.withStatus();
		event.put("updated", status.getUpdated());
		event.with("start").put("dateTime", status.getStart());
		event.with("end").put("dateTime", status.getEnd());
		event.put("location", status.withLocation().getSummary());
	}
	
	/**
	 * Retrieve all current issues. If there are no issues, an empty array
	 * is returned
	 * 
	 * @return the issues
	 */
	public ArrayList<Issue> getIssues() {
		ArrayList<Issue> issues = getState().get("issues",
				new TypeUtil<ArrayList<Issue>>() {
				});
		if (issues == null) {
			issues = new ArrayList<Issue>();
		}
		return issues;
	}
	
	/**
	 * Remove all issues
	 */
	private void clearIssues() {
		getState().remove("issues");
	}
	
	/**
	 * Add an issue to the issue list
	 * The issue will trigger an event
	 * 
	 * @param issue
	 */
	private void addIssue(final Issue issue) {
		final ArrayList<Issue> issues = getIssues();
		issues.add(issue);
		getState().put("issues", issues);
		
		// trigger an error event
		try {
			final String event = issue.getType().toString();
			final ObjectNode data = JOM.createObjectNode();
			data.put("issue",
					JOM.getInstance().convertValue(issue, ObjectNode.class));
			final ObjectNode params = JOM.createObjectNode();
			params.put("description", issue.getMessage());
			params.put("data", data);
			getEventsFactory().trigger(event, params);
		} catch (final Exception e) {
			LOG.log(Level.WARNING, "", e);
		}
	}
	
	/**
	 * Create an issue with type, code, and message
	 * timestamp will be set to NOW
	 * 
	 * @param type
	 * @param code
	 * @param message
	 */
	private void addIssue(final TYPE type, final Integer code,
			final String message) {
		final Issue issue = new Issue();
		issue.setType(type);
		issue.setCode(code);
		issue.setMessage(message);
		issue.setTimestamp(DateTime.now().toString());
		addIssue(issue);
	}
	
	/**
	 * Retrieve the data of a single calendar agent from the state
	 * 
	 * @param agentUrl
	 * @return data returns the calendar data. If not available, a new, empty
	 *         CalendarAgentData is returned.
	 */
	// TODO: create some separate AgentData handling class, instead of methods
	// in MeetingAgent
	private AgentData getAgentData(final String agentUrl) {
		final HashMap<String, AgentData> calendarAgents = getState().get(
				"calendarAgents", new TypeUtil<HashMap<String, AgentData>>() {
				});
		
		if (calendarAgents != null && calendarAgents.containsKey(agentUrl)) {
			return calendarAgents.get(agentUrl);
		}
		return new AgentData();
	}
	
	/**
	 * Put data for a calendar agent into the state
	 * 
	 * @param agentUrl
	 * @param data
	 */
	private void putAgentData(final String agentUrl, final AgentData data) {
		final State state = getState();
		Map<String, AgentData> calendarAgents = getState().get(
				"calendarAgents", new TypeUtil<HashMap<String, AgentData>>() {
				});
		
		if (calendarAgents == null) {
			calendarAgents = new HashMap<String, AgentData>();
		}
		
		calendarAgents.put(agentUrl, data);
		state.put("calendarAgents", calendarAgents);
	}
	
	/**
	 * Remove a calendar agent data from the state
	 * 
	 * @param agent
	 * @param data
	 */
	private void removeAgentData(final String agent) {
		final State state = getState();
		final Map<String, AgentData> calendarAgents = getState().get(
				"calendarAgents", new TypeUtil<HashMap<String, AgentData>>() {
				});
		if (calendarAgents != null && calendarAgents.containsKey(agent)) {
			calendarAgents.remove(agent);
			state.put("calendarAgents", calendarAgents);
		}
	}
	
	/**
	 * Retrieve the busy intervals of a calendar agent from the state
	 * 
	 * @param agent
	 * @return busy returns busy intervals, or null if not available
	 */
	private List<Interval> getAgentBusy(final String agent) {
		final AgentData data = getAgentData(agent);
		return data.busy;
	}
	
	/**
	 * Put the busy intervals for a calendar agent into the state
	 * 
	 * @param agent
	 * @param busy
	 */
	private void putAgentBusy(final String agent, final List<Interval> busy) {
		final AgentData data = getAgentData(agent);
		data.busy = busy;
		putAgentData(agent, data);
	}
	
	/**
	 * Retrieve calendar event from calendaragent
	 * 
	 * @param agent
	 * @return event Calendar event, or null if not found
	 */
	private ObjectNode getEvent(final String agent) {
		ObjectNode event = null;
		final String eventId = getAgentData(agent).eventId;
		if (eventId != null) {
			final ObjectNode params = JOM.createObjectNode();
			params.put("eventId", eventId);
			try {
				event = send(URI.create(agent), "getEvent", params,
						ObjectNode.class);
			} catch (final JSONRPCException e) {
				if (e.getCode() == 404) {
					// event was deleted by the user.
					final Activity activity = getState().get("activity",
							Activity.class);
					final Attendee attendee = activity.withConstraints()
							.withAttendee(agent);
					attendee.setResponseStatus(RESPONSE_STATUS.declined);
					getState().put("activity", activity);
					
					clearAttendee(agent); // TODO: seems not to work
				} else {
					LOG.log(Level.WARNING, "", e);
				}
			} catch (final Exception e) {
				addIssue(TYPE.warning, Issue.EXCEPTION, e.getMessage());
				LOG.log(Level.WARNING, "", e);
			}
		}
		return event;
	}
	
	// TODO: comment
	private boolean equalsDateTime(final String a, final String b) {
		if (a != null && b != null) {
			return new DateTime(a).equals(new DateTime(b));
		}
		if (a == null && b == null) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * Synchronize the event with given calendar agent
	 * 
	 * @param agent
	 */
	// TODO: the method syncEvent has grown to large. split it up
	private void syncEvent(@Name("agent") final String agent) {
		LOG.info("syncEvent started for agent " + agent);
		final State state = getState();
		
		// retrieve event from calendar agent
		ObjectNode event = getEvent(agent);
		if (event == null) {
			event = JOM.createObjectNode();
		}
		final Activity eventActivity = convertEventToActivity(event);
		
		// verify all kind of stuff
		final Activity activity = state.get("activity", Activity.class);
		if (activity == null) {
			return; // oops no activity at all
		}
		if (activity.withStatus().getStart() == null
				|| activity.withStatus().getEnd() == null) {
			return; // activity is not yet planned. cancel synchronization
		}
		final Attendee attendee = activity.withConstraints().getAttendee(agent);
		if (attendee == null) {
			return; // unknown attendee
		}
		if (attendee.getResponseStatus() == Attendee.RESPONSE_STATUS.declined) {
			// attendee does not want to attend
			clearAttendee(agent);
			return;
		}
		
		// check if the activity or the retrieved event is changed since the
		// last synchronization
		final AgentData agentData = getAgentData(agent);
		final boolean activityChanged = !equalsDateTime(
				agentData.activityUpdated, activity.withStatus().getUpdated());
		final boolean eventChanged = !equalsDateTime(agentData.eventUpdated,
				eventActivity.withStatus().getUpdated());
		final boolean changed = activityChanged || eventChanged;
		if (changed && activity.isNewerThan(eventActivity)) {
			// activity is updated (event is out-dated or not yet existing)
			
			// merge the activity into the event
			mergeActivityIntoEvent(event, activity);
			
			// TODO: if attendee cannot attend (=optional or declined), show
			// this somehow in the event
			
			// save the event
			final ObjectNode params = JOM.createObjectNode();
			params.put("event", event);
			try {
				// TODO: only update/create the event when the attendee
				// is not optional or is available at the planned time
				final String method = event.has("id") ? "updateEvent"
						: "createEvent";
				final ObjectNode updatedEvent = send(URI.create(agent), method,
						params, ObjectNode.class);
				
				// update the agent data
				agentData.eventId = updatedEvent.get("id").asText();
				agentData.eventUpdated = updatedEvent.get("updated").asText();
				agentData.activityUpdated = activity.withStatus().getUpdated();
				putAgentData(agent, agentData);
			} catch (final JSONRPCException e) {
				addIssue(TYPE.warning, Issue.JSONRPCEXCEPTION, e.getMessage());
				LOG.log(Level.WARNING, "", e);
			} catch (final Exception e) {
				addIssue(TYPE.warning, Issue.EXCEPTION, e.getMessage());
				LOG.log(Level.WARNING, "", e);
			}
		} else if (changed) {
			// event is updated (activity is out-dated or both have the same
			// updated timestamp)
			
			// if start is changed, add this as preferences to the constraints
			if (!equalsDateTime(activity.withStatus().getStart(), eventActivity
					.withStatus().getStart())) {
				/*
				 * TODO: store the old interval as undesired?
				 * String oldStart = activity.withStatus().getStart();
				 * String oldEnd = activity.withStatus().getEnd();
				 * if (oldStart != null && oldEnd != null) {
				 * Preference undesired = new Preference ();
				 * undesired.setStart(oldStart);
				 * undesired.setEnd(oldEnd);
				 * undesired.setWeight(WEIGHT_UNDESIRED_INTERVAL);
				 * activity.getConstraints().getTime().addPreference(undesired);
				 * }
				 */
				
				// store the new interval as preferred
				final String newStart = eventActivity.withStatus().getStart();
				final String newEnd = eventActivity.withStatus().getEnd();
				if (newStart != null && newEnd != null) {
					final Preference preferred = new Preference();
					preferred.setStart(newStart);
					preferred.setEnd(newEnd);
					preferred.setWeight(WEIGHT_PREFERRED_INTERVAL);
					
					// overwrite other preferences with this new preference
					// TODO: all preferences are overwritten for now. Behavior
					// should be changed.
					final List<Preference> preferences = new ArrayList<Preference>();
					preferences.add(preferred);
					activity.getConstraints().getTime()
							.setPreferences(preferences);
					
					// activity.getConstraints().getTime().addPreference(preferred);
				}
			}
			// else events are in sync, nothing to do
			
			// update the activity
			activity.merge(eventActivity);
			state.put("activity", activity);
			
			// update the agent data
			agentData.eventId = event.get("id").asText();
			agentData.eventUpdated = event.get("updated").asText();
			agentData.activityUpdated = activity.withStatus().getUpdated();
			putAgentData(agent, agentData);
		} else {
			// activity and eventActivity have the same updated timestamp
			// nothing to do.
			LOG.info("event and activity are in sync"); // TODO: cleanup
		}
	}
	
	/**
	 * Update the busy intervals of all attendees, and merge the results
	 */
	private void updateBusyIntervals() {
		final Activity activity = getActivity();
		if (activity != null) {
			final List<Attendee> attendees = activity.withConstraints()
					.withAttendees();
			for (final Attendee attendee : attendees) {
				final String agent = attendee.getAgent();
				if (attendee.getResponseStatus() != RESPONSE_STATUS.declined) {
					updateBusyInterval(agent);
				}
			}
		}
		
		mergeTimeConstraints();
	}
	
	/**
	 * Merge the busy intervals of all attendees, and the preferred intervals
	 */
	private void mergeTimeConstraints() {
		final ArrayList<Interval> infeasibleIntervals = new ArrayList<Interval>();
		final ArrayList<Weight> preferredIntervals = new ArrayList<Weight>();
		
		final Activity activity = getActivity();
		if (activity != null) {
			// read and merge the stored busy intervals of all attendees
			for (final Attendee attendee : activity.withConstraints()
					.withAttendees()) {
				final String agent = attendee.getAgent();
				if (attendee.getResponseStatus() != RESPONSE_STATUS.declined) {
					if (new Boolean(true).equals(attendee.getOptional())) {
						// This attendee is optional.
						// Add its busy intervals to the soft constraints
						final List<Interval> attendeeBusy = getAgentBusy(agent);
						if (attendeeBusy != null) {
							for (final Interval i : attendeeBusy) {
								final Weight wi = new Weight(i.getStart(),
										i.getEnd(),
										WEIGHT_BUSY_OPTIONAL_ATTENDEE);
								
								preferredIntervals.add(wi);
							}
						}
					} else {
						// this attendee is required.
						// Add its busy intervals to the hard constraints
						final List<Interval> attendeeBusy = getAgentBusy(agent);
						if (attendeeBusy != null) {
							infeasibleIntervals.addAll(attendeeBusy);
						}
					}
				}
				// else This attendee declined. Ignore this attendees busy
				// interval
			}
			
			// read the time preferences and add them to the soft constraints
			final List<Preference> preferences = activity.withConstraints()
					.withTime().withPreferences();
			for (final Preference p : preferences) {
				if (p != null) {
					final Weight wi = new Weight(new DateTime(p.getStart()),
							new DateTime(p.getEnd()), p.getWeight());
					
					preferredIntervals.add(wi);
				}
			}
		}
		
		// add office hours profile to the soft constraints
		// TODO: don't include (hardcoded) office hours here, should be handled
		// by a PersonalAgent
		final DateTime timeMin = DateTime.now();
		final DateTime timeMax = timeMin.plusDays(LOOK_AHEAD_DAYS);
		final List<Interval> officeHours = IntervalsUtil.getOfficeHours(
				timeMin, timeMax);
		for (final Interval i : officeHours) {
			final Weight wi = new Weight(i, WEIGHT_OFFICE_HOURS);
			preferredIntervals.add(wi);
		}
		
		// add delay penalties to the soft constraints
		final DateTime now = DateTime.now();
		final MutableDateTime d = new MutableDateTime(now.getYear(),
				now.getMonthOfYear(), now.getDayOfMonth(), 0, 0, 0, 0);
		for (int i = 0; i <= LOOK_AHEAD_DAYS; i++) {
			final DateTime start = d.toDateTime();
			final DateTime end = start.plusDays(1);
			final Weight wi = new Weight(start, end, WEIGHT_DELAY_PER_DAY * i);
			preferredIntervals.add(wi);
			d.addDays(1);
		}
		
		// order and store the aggregated lists with intervals
		IntervalsUtil.order(infeasibleIntervals);
		getState().put("infeasible", infeasibleIntervals);
		WeightsUtil.order(preferredIntervals);
		getState().put("preferred", preferredIntervals);
	}
	
	/**
	 * Calculate the average preference for given interval.
	 * The method aggregates over all stored preferences
	 * Default preference is 0.
	 * 
	 * @param preferredIntervals
	 *            list with intervals ordered by start
	 * @param test
	 *            test interval
	 * @return preference
	 */
	private double calculatePreference(final List<Weight> preferredIntervals,
			final Interval test) {
		double preference = 0;
		
		for (final Weight interval : preferredIntervals) {
			final Interval overlap = test.overlap(interval.getInterval());
			if (overlap != null) {
				final Double weight = interval.getWeight();
				if (weight != null) {
					final double durationCheck = test.toDurationMillis();
					final double durationOverlap = overlap.toDurationMillis();
					final double avgWeight = (durationOverlap / durationCheck)
							* weight;
					preference += avgWeight;
				}
			}
			
			if (interval.getStart().isAfter(test.getEnd())) {
				// as the list is ordered, we can exit as soon as we have an
				// interval which starts after the wanted interval.
				break;
			}
		}
		
		return preference;
	}
	
	/**
	 * Calculate whether given interval is feasible (i.e. does not overlap with
	 * any of the infeasible intervals, and is not in the past)
	 * 
	 * @param infeasibleIntervals
	 *            list with intervals ordered by start
	 * @param timeMin
	 * @param timeMax
	 * @return feasible
	 */
	private boolean calculateFeasible(final List<Interval> infeasibleIntervals,
			final Interval test) {
		if (test.getStart().isBeforeNow()) {
			// interval starts in the past
			return false;
		}
		
		for (final Interval interval : infeasibleIntervals) {
			if (test.overlaps(interval)) {
				return false;
			}
			if (interval.getStart().isAfter(test.getEnd())) {
				// as the list is ordered, we can exit as soon as we have an
				// interval which starts after the wanted interval.
				break;
			}
		}
		
		return true;
	}
	
	/**
	 * Retrieve the feasible and preferred intervals.
	 * 
	 * @return the intervals
	 */
	// TODO: remove this temporary method
	public ObjectNode getIntervals() {
		final ObjectNode intervals = JOM.createObjectNode();
		
		final List<Interval> infeasible = getState().get("infeasible",
				new TypeUtil<ArrayList<Interval>>() {
				});
		final List<Weight> preferred = getState().get("preferred",
				new TypeUtil<ArrayList<Weight>>() {
				});
		final List<Weight> solutions = calculateSolutions();
		
		// merge the intervals
		List<Interval> mergedInfeasible = null;
		List<Weight> mergedPreferred = null;
		if (infeasible != null) {
			mergedInfeasible = IntervalsUtil.merge(infeasible);
		}
		if (preferred != null) {
			mergedPreferred = WeightsUtil.merge(preferred);
		}
		
		if (infeasible != null) {
			final ArrayNode arr = JOM.createArrayNode();
			for (final Interval interval : infeasible) {
				final ObjectNode o = JOM.createObjectNode();
				o.put("start", interval.getStart().toString());
				o.put("end", interval.getEnd().toString());
				arr.add(o);
			}
			intervals.put("infeasible", arr);
		}
		
		if (preferred != null) {
			final ArrayNode arr = JOM.createArrayNode();
			for (final Weight weight : preferred) {
				final ObjectNode o = JOM.createObjectNode();
				o.put("start", weight.getStart().toString());
				o.put("end", weight.getEnd().toString());
				o.put("weight", weight.getWeight());
				arr.add(o);
			}
			intervals.put("preferred", arr);
		}
		
		if (solutions != null) {
			final ArrayNode arr = JOM.createArrayNode();
			for (final Weight weight : solutions) {
				final ObjectNode o = JOM.createObjectNode();
				o.put("start", weight.getStart().toString());
				o.put("end", weight.getEnd().toString());
				o.put("weight", weight.getWeight());
				arr.add(o);
			}
			intervals.put("solutions", arr);
		}
		
		if (mergedInfeasible != null) {
			final ArrayNode arr = JOM.createArrayNode();
			for (final Interval i : mergedInfeasible) {
				final ObjectNode o = JOM.createObjectNode();
				o.put("start", i.getStart().toString());
				o.put("end", i.getEnd().toString());
				arr.add(o);
			}
			intervals.put("mergedInfeasible", arr);
		}
		
		if (mergedPreferred != null) {
			final ArrayNode arr = JOM.createArrayNode();
			for (final Weight wi : mergedPreferred) {
				final ObjectNode o = JOM.createObjectNode();
				o.put("start", wi.getStart().toString());
				o.put("end", wi.getEnd().toString());
				o.put("weight", wi.getWeight());
				arr.add(o);
			}
			intervals.put("mergedPreferred", arr);
		}
		
		return intervals;
	}
	
	/**
	 * Retrieve the busy intervals of a calendar agent
	 * 
	 * @param agent
	 */
	private void updateBusyInterval(@Name("agent") final String agent) {
		try {
			// create parameters with the boundaries of the interval to be
			// retrieved
			final ObjectNode params = JOM.createObjectNode();
			final DateTime timeMin = DateTime.now();
			final DateTime timeMax = timeMin.plusDays(LOOK_AHEAD_DAYS);
			params.put("timeMin", timeMin.toString());
			params.put("timeMax", timeMax.toString());
			
			// exclude the event managed by this agent from the busy intervals
			final String eventId = getAgentData(agent).eventId;
			if (eventId != null) {
				final ArrayNode excludeEventIds = JOM.createArrayNode();
				excludeEventIds.add(eventId);
				params.put("excludeEventIds", excludeEventIds);
			}
			
			// get the busy intervals from the agent
			final ArrayNode array = send(URI.create(agent), "getBusy", params,
					ArrayNode.class);
			
			// convert from ArrayNode to List
			final List<Interval> busy = new ArrayList<Interval>();
			for (int i = 0; i < array.size(); i++) {
				final ObjectNode obj = (ObjectNode) array.get(i);
				final String start = obj.has("start") ? obj.get("start")
						.asText() : null;
				final String end = obj.has("end") ? obj.get("end").asText()
						: null;
				busy.add(new Interval(new DateTime(start), new DateTime(end)));
			}
			
			// store the interval in the state
			putAgentBusy(agent, busy);
			
		} catch (final JSONRPCException e) {
			addIssue(TYPE.warning, Issue.JSONRPCEXCEPTION, e.getMessage());
			LOG.log(Level.WARNING, "", e);
		} catch (final Exception e) {
			addIssue(TYPE.warning, Issue.EXCEPTION, e.getMessage());
			LOG.log(Level.WARNING, "", e);
		}
	}
	
	/**
	 * Delete everything of the agent.
	 */
	@Override
	public void onDelete() {
		clear();
		
		// super class will delete the state
		super.onDelete();
	}
	
	/**
	 * Clear the stored activity, and remove events from attendees.
	 */
	@Access(AccessType.UNAVAILABLE)
	public void clear() {
		final Activity activity = getActivity();
		
		if (activity != null) {
			final List<Attendee> attendees = activity.withConstraints()
					.withAttendees();
			for (final Attendee attendee : attendees) {
				final String agent = attendee.getAgent();
				if (agent != null) {
					clearAttendee(agent);
				}
			}
		}
		
		// stop auto update timer (if any)
		stopAutoUpdate();
	}
	
	/**
	 * Clear an event from given agent
	 * 
	 * @param agent
	 */
	private void clearAttendee(@Name("agent") final String agent) {
		final AgentData data = getAgentData(agent);
		if (data != null) {
			try {
				if (data.eventId != null) {
					final ObjectNode params = JOM.createObjectNode();
					params.put("eventId", data.eventId);
					send(URI.create(agent), "deleteEvent", params);
					data.eventId = null;
				}
			} catch (final JSONRPCException e) {
				if (e.getCode() == 404) {
					// event was already deleted. fine!
					data.eventId = null;
				} else {
					LOG.log(Level.WARNING, "", e);
				}
			} catch (final Exception e) {
				LOG.log(Level.WARNING, "", e);
			}
			
			if (data.eventId == null) {
				removeAgentData(agent);
				LOG.info("clearAttendee " + agent + " cleared");
			}
		}
	}
	
	// TODO: cleanup this temporary method
	/**
	 * Gets the office hours.
	 * 
	 * @param timeMin
	 *            the time min
	 * @param timeMax
	 *            the time max
	 * @return the office hours
	 */
	public ArrayNode getOfficeHours(@Name("timeMin") final String timeMin,
			@Name("timeMax") final String timeMax) {
		final List<Interval> available = IntervalsUtil.getOfficeHours(
				new DateTime(timeMin), new DateTime(timeMax));
		
		// convert to JSON array
		final ArrayNode array = JOM.createArrayNode();
		for (final Interval interval : available) {
			final ObjectNode obj = JOM.createObjectNode();
			obj.put("start", interval.getStart().toString());
			obj.put("end", interval.getEnd().toString());
			array.add(obj);
		}
		return array;
	}
	
	/*
	 * Get the first url filtered by a specific protocol
	 * 
	 * @param protocol For example "http"
	 * 
	 * @return url Returns url or null if not found
	 */
	protected URI getFirstUrl(final String protocol) {
		final List<String> urls = getUrls();
		
		for (final String url : urls) {
			if (url.startsWith(protocol + ":")) {
				return URI.create(url);
			}
		}
		
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.almende.eve.agent.Agent#getDescription()
	 */
	@Override
	public String getDescription() {
		return "A MeetingAgent can dynamically plan and manage a meeting.";
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.almende.eve.agent.Agent#getVersion()
	 */
	@Override
	public String getVersion() {
		return "0.1";
	}
}
