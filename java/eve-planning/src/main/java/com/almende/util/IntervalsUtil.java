package com.almende.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.MutableDateTime;

/**
 * @class IntervalUtil
 *        Can order and merge a list with intervals
 * 
 * @author Jos de Jong, Almende B.V. 2012
 */
public class IntervalsUtil {
	/**
	 * Order the intervals by start date
	 * 
	 * @param intervals
	 *            A list with time intervals
	 */
	public static void order(final List<Interval> intervals) {
		class IntervalComparator implements Comparator<Interval> {
			@Override
			public int compare(final Interval a, final Interval b) {
				if (a.getStart().isAfter(b.getStart())) {
					return 1;
				}
				if (a.getStart().isBefore(b.getStart())) {
					return -1;
				}
				return 0;
			}
		}
		
		final IntervalComparator comparator = new IntervalComparator();
		Collections.sort(intervals, comparator);
	}
	
	/**
	 * Merge overlapping intervals in given list
	 * 
	 * @param intervals
	 * @return
	 */
	public static List<Interval> merge(final List<Interval> intervals) {
		// copy the intervals
		final List<Interval> merged = new ArrayList<Interval>();
		for (final Interval i : intervals) {
			merged.add(new Interval(i));
		}
		
		// order the intervals
		order(merged);
		
		// merge the intervals
		int i = 1; // important to start at 1 (this is not accidentally)
		while (i < merged.size()) {
			final Interval prev = merged.get(i - 1);
			final Interval cur = merged.get(i);
			
			if (cur.getStart().isBefore(prev.getEnd())
					|| cur.getStart().equals(prev.getEnd())) {
				// merge these two intervals
				final DateTime start = prev.getStart();
				DateTime end = null;
				if (cur.getEnd().isAfter(prev.getEnd())) {
					end = cur.getEnd();
				} else {
					end = prev.getEnd();
				}
				final Interval combi = new Interval(start, end);
				merged.set(i - 1, combi);
				merged.remove(i);
			} else {
				i++;
			}
		}
		
		// return merged intervals
		return merged;
	}
	
	/**
	 * Create the inverse of a given set with intervals.
	 * Provided intervals must be ordered, and may not have overlapping events
	 * (to ensure all of this is ok, put the intervals through the method merge)
	 * 
	 * @param intervals
	 * @param timeMin
	 *            Optional start time of the intervals. If the first
	 *            interval starts after timeMin, a gap from timeMin to
	 *            the first interval will be added to the inverse
	 * @param timeMax
	 *            Optional end time of the intervals. If the last
	 *            interval ends before timeMax, a gap from the last
	 *            interval to timeMax will be added to the inverse
	 * @return
	 */
	public static List<Interval> inverse(final List<Interval> intervals,
			final DateTime timeMin, final DateTime timeMax) {
		final List<Interval> inverse = new ArrayList<Interval>();
		
		// gap from timeMin to the first interval
		if (timeMin != null && intervals.size() > 0) {
			final DateTime start = new DateTime(timeMin);
			final DateTime end = intervals.get(0).getStart();
			if (start.isBefore(end)) {
				inverse.add(new Interval(start, end));
			}
		}
		
		// gaps between the intervals
		for (int i = 1, iMax = intervals.size(); i < iMax; i++) {
			final DateTime start = intervals.get(i - 1).getEnd();
			final DateTime end = intervals.get(i).getStart();
			
			if (end.isAfter(start)) {
				inverse.add(new Interval(start, end));
			}
		}
		
		// gap from last interval to timeMax
		if (timeMax != null && intervals.size() > 0) {
			final DateTime start = new DateTime(intervals.get(
					intervals.size() - 1).getEnd());
			final DateTime end = new DateTime(timeMax);
			if (start.isBefore(end)) {
				inverse.add(new Interval(start, end));
			}
		}
		
		// no intervals at all
		if (intervals.size() == 0) {
			final DateTime start = new DateTime(timeMin);
			final DateTime end = new DateTime(timeMax);
			inverse.add(new Interval(start, end));
		}
		
		return inverse;
	}
	
	/**
	 * check if interval to be checked overlaps with any any of the intervals
	 * in given list
	 * 
	 * @param intervals
	 * @param checkInterval
	 * @return isOverlapping
	 */
	public static boolean overlaps(final Interval checkInterval,
			final List<Interval> intervals) {
		for (final Interval interval : intervals) {
			if (interval.overlaps(checkInterval)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Create a busy profile with office hours. The method returns the
	 * the available hours, inside office hours.
	 * By default, the office hours are from Monday-Friday, 09:00-17:00, with
	 * time zone CET.
	 * 
	 * @param timeMin
	 * @param timeMax
	 * @return available
	 */
	public static List<Interval> getOfficeHours(final DateTime timeMin,
			final DateTime timeMax) {
		final Set<Integer> workingDays = new HashSet<Integer>(); // 1=Monday,
																	// 7=Sunday
		workingDays.add(1); // Monday
		workingDays.add(2); // Tuesday
		workingDays.add(3); // Wednesday
		workingDays.add(4); // Thursday
		workingDays.add(5); // Friday
		
		final int hourStart = 9;
		final int hourEnd = 17;
		final DateTimeZone timeZone = DateTimeZone.forID("CET"); // Central
																	// European
																	// Time
		
		return getOfficeHours(timeMin, timeMax, workingDays, hourStart,
				hourEnd, timeZone);
	}
	
	/**
	 * Create a busy profile with office hours. The method returns the
	 * the available hours, inside office hours.
	 * 
	 * @param timeMin
	 * @param timeMax
	 * @param workingDays
	 *            Set with working days. 1 = Monday, 7 = Sunday
	 * @param hourStart
	 *            start hour, for example 9
	 * @param hourStart
	 *            end hour, for example 17
	 * @param timeZone
	 *            the timezone to be used to determine the working hours
	 * @return available
	 */
	public static List<Interval> getOfficeHours(final DateTime timeMin,
			final DateTime timeMax, final Set<Integer> workingDays,
			final int hourStart, final int hourEnd, final DateTimeZone timeZone) {
		final List<Interval> available = new ArrayList<Interval>();
		
		final MutableDateTime workingDayMin = MutableDateTime.now(); // 09:00:00
		workingDayMin.setZoneRetainFields(timeZone);
		workingDayMin.setMillisOfDay(0);
		workingDayMin.setHourOfDay(hourStart);
		final MutableDateTime workingDayMax = MutableDateTime.now(); // 17:00:000
		workingDayMax.setZoneRetainFields(timeZone);
		workingDayMax.setMillisOfDay(0);
		workingDayMax.setHourOfDay(hourEnd);
		
		final MutableDateTime time = new MutableDateTime(timeMin);
		while (time.isBefore(timeMax)) {
			// find the first working day inside the interval
			while (!workingDays.contains(time.getDayOfWeek())) {
				time.addDays(1);
			}
			
			// set working hours to today
			workingDayMin.setDate(time);
			workingDayMax.setDate(time);
			
			// set time to the start of the day
			time.setMillisOfDay(0);
			
			// create the start of the working day
			DateTime start = null;
			if (workingDayMin.isAfter(timeMin)) {
				start = new DateTime(workingDayMin);
			} else {
				start = new DateTime(timeMin);
			}
			
			// create the end of the working day
			DateTime end = null;
			if (workingDayMax.isBefore(timeMax)) {
				end = new DateTime(workingDayMax);
			} else {
				end = new DateTime(timeMax);
			}
			
			// create interval if not empty
			if (start.isBefore(end)) {
				final Interval hours = new Interval(start, end);
				available.add(hours);
			}
			
			// move to the next day
			time.addDays(1);
		}
		
		return available;
	}
}
