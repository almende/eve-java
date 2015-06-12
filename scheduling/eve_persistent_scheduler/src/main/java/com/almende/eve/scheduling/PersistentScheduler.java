/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.scheduling;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.joda.time.DateTime;

import com.almende.eve.capabilities.handler.Handler;
import com.almende.eve.scheduling.clock.RunnableClock;
import com.almende.eve.state.State;
import com.almende.eve.state.StateBuilder;
import com.almende.eve.transport.Receiver;
import com.almende.util.jackson.JOM;
import com.almende.util.uuid.UUID;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class PersistentScheduler.
 */
public class PersistentScheduler extends SimpleScheduler {
	private static final Logger	LOG		= Logger.getLogger(PersistentScheduler.class
												.getName());
	private State				state	= null;

	public void delete() {
		super.delete();
		if (state != null) {
			state.delete();
			state = null;
		}
		PersistentSchedulerConfig config = PersistentSchedulerConfig
				.decorate(getParams());
		SimpleSchedulerBuilder.delete(config.getId());
	}

	/**
	 * Instantiates a new persistent scheduler.
	 * 
	 * @param params
	 *            the params
	 * @param handle
	 *            the handle
	 */
	public PersistentScheduler(final ObjectNode params,
			final Handler<Receiver> handle) {
		super(params, handle);
		final PersistentSchedulerConfig config = PersistentSchedulerConfig
				.decorate(params);
		if (getClock() == null) {
			setClock(new RunnableClock());
		}

		final ObjectNode stateConfig = config.getState();
		if (stateConfig == null) {
			LOG.warning("Parameter 'state' is required, falling back to SimpleScheduler.");
		} else {
			state = new StateBuilder().withConfig(stateConfig).build();
			for (final String key : state.keySet()) {
				final TaskEntry entry = state.get(key, TaskEntry.class);
				run(entry);
			}
		}

	}

	private void run(final TaskEntry entry) {
		if (entry != null) {
			if (getClock() == null) {
				setClock(new RunnableClock());
			}
			getClock().requestTrigger(entry.getTaskId(), entry.getDue(),
					new Runnable() {

						@Override
						public void run() {
							if (state != null) {
								state.remove(entry.getTaskId());
							}
							getHandle().get().receive(entry.getMessage(),
									getSchedulerUrl(), null);
						}
					});
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.almende.eve.scheduling.SimpleScheduler#schedule(java.lang.Object,
	 * org.joda.time.DateTime)
	 */
	@Override
	public String schedule(final String id, final Object msg, final DateTime due) {
		final TaskEntry entry = new TaskEntry((id != null ? id
				: new UUID().toString()), due, JOM.getInstance().valueToTree(
				msg));
		if (state != null) {
			state.put(entry.getTaskId(), entry);
		}
		run(entry);
		return entry.getTaskId();
	}

	/*
	 * (non-Javadoc)
	 * @see com.almende.eve.scheduling.SimpleScheduler#cancel(java.lang.String)
	 */
	@Override
	public void cancel(final String id) {
		if (getClock() == null) {
			setClock(new RunnableClock());
		}
		if (state != null) {
			state.remove(id);
		}
		getClock().cancel(id);
	}

	/*
	 * (non-Javadoc)
	 * @see com.almende.eve.scheduling.SimpleScheduler#clear()
	 */
	@Override
	public void clear() {
		if (getClock() == null) {
			setClock(new RunnableClock());
		}
		if (state != null) {
			state.clear();
		}
		getClock().clear();
	}
}

/**
 * @author Almende
 */
class TaskEntry implements Comparable<TaskEntry>, Serializable {
	private static final Logger	LOG					= Logger.getLogger(TaskEntry.class
															.getCanonicalName());
	private static final long	serialVersionUID	= -2402975617148459433L;
	private String				taskId				= null;
	private JsonNode			message;
	private DateTime			due;
	private boolean				active				= false;

	/**
	 * Instantiates a new task entry.
	 */
	public TaskEntry() {};

	/**
	 * Instantiates a new task entry.
	 *
	 * @param id
	 *            the id
	 * @param due
	 *            the due
	 * @param message
	 *            the message
	 */
	public TaskEntry(final String id, final DateTime due, final JsonNode message) {
		taskId = id;
		setMessage(message);
		this.due = due;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof TaskEntry)) {
			return false;
		}
		final TaskEntry other = (TaskEntry) o;
		return taskId.equals(other.taskId);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return taskId.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(final TaskEntry o) {
		if (equals(o)) {
			return 0;
		}
		if (due.equals(o.due)) {
			return taskId.compareTo(o.taskId);
		}
		return due.compareTo(o.due);
	}

	public JsonNode getMessage() {
		return message;
	}

	public void setMessage(final JsonNode message) {
		this.message = message;
	}

	/**
	 * Gets the task id.
	 * 
	 * @return the task id
	 */
	public String getTaskId() {
		return taskId;
	}

	/**
	 * Gets the due as string.
	 * 
	 * @return the due as string
	 */
	public String getDueAsString() {
		return due.toString();
	}

	/**
	 * Gets the due.
	 * 
	 * @return the due
	 */
	@JsonIgnore
	public DateTime getDue() {
		return due;
	}

	/**
	 * Sets the task id.
	 * 
	 * @param taskId
	 *            the new task id
	 */
	public void setTaskId(final String taskId) {
		this.taskId = taskId;
	}

	/**
	 * Sets the due as string.
	 * 
	 * @param due
	 *            the new due as string
	 */
	public void setDueAsString(final String due) {
		this.due = new DateTime(due);
	}

	/**
	 * Sets the due.
	 * 
	 * @param due
	 *            the new due
	 */
	public void setDue(final DateTime due) {
		this.due = due;
	}

	/**
	 * Sets the active.
	 * 
	 * @param active
	 *            the new active
	 */
	public void setActive(final boolean active) {
		this.active = active;
	}

	/**
	 * Checks if is active.
	 * 
	 * @return true, if is active
	 */
	public boolean isActive() {
		return active;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		try {
			return JOM.getInstance().writeValueAsString(this);
		} catch (final Exception e) {
			LOG.log(Level.WARNING, "Couldn't use Jackson to print task.", e);
			return "{\"taskId\":" + taskId + ",\"due\":" + due + "}";
		}
	}
}
