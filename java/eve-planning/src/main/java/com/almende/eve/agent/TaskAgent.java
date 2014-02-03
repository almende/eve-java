/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.agent;

import com.almende.eve.rpc.annotation.Access;
import com.almende.eve.rpc.annotation.AccessType;
import com.almende.eve.rpc.annotation.Name;
import com.almende.eve.rpc.annotation.Optional;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Interface TaskAgent.
 */
@Access(AccessType.PUBLIC)
public interface TaskAgent {
	
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
	 * Gets the task list.
	 * 
	 * @return the task list
	 * @throws Exception
	 *             the exception
	 */
	ArrayNode getTaskList() throws Exception;
	
	/**
	 * Gets the tasks.
	 * 
	 * @param start
	 *            the start
	 * @param end
	 *            the end
	 * @param taskListId
	 *            the task list id
	 * @return the tasks
	 * @throws Exception
	 *             the exception
	 */
	ArrayNode getTasks(@Optional @Name("start") String start,
			@Optional @Name("end") String end,
			@Optional @Name("listId") String taskListId) throws Exception;
	
	/**
	 * Gets the task.
	 * 
	 * @param taskId
	 *            the task id
	 * @param taskListId
	 *            the task list id
	 * @return the task
	 * @throws Exception
	 *             the exception
	 */
	ObjectNode getTask(@Name("taskId") String taskId,
			@Optional @Name("taskListId") String taskListId) throws Exception;
	
	/**
	 * Creates the task.
	 * 
	 * @param task
	 *            the task
	 * @param taskListId
	 *            the task list id
	 * @return the object node
	 * @throws Exception
	 *             the exception
	 */
	ObjectNode createTask(@Name("taskId") ObjectNode task,
			@Optional @Name("taskListId") String taskListId) throws Exception;
	
	/**
	 * Update task.
	 * 
	 * @param event
	 *            the event
	 * @param taskListId
	 *            the task list id
	 * @return the object node
	 * @throws Exception
	 *             the exception
	 */
	ObjectNode updateTask(@Name("taskId") ObjectNode event,
			@Optional @Name("taskListId") String taskListId) throws Exception;
	
	/**
	 * Delete task.
	 * 
	 * @param eventId
	 *            the event id
	 * @param taskListId
	 *            the task list id
	 * @throws Exception
	 *             the exception
	 */
	void deleteTask(@Name("taskId") String eventId,
			@Optional @Name("taskListId") String taskListId) throws Exception;
}
