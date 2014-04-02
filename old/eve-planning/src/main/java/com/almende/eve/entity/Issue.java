/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.entity;

import java.io.Serializable;
import java.util.List;

/**
 * The Class Issue.
 */
@SuppressWarnings("serial")
public class Issue implements Serializable {
	
	/**
	 * Instantiates a new issue.
	 */
	public Issue() {
	}
	
	/**
	 * Sets the code.
	 * 
	 * @param code
	 *            the new code
	 */
	public void setCode(final Integer code) {
		this.code = code;
	}
	
	/**
	 * Gets the code.
	 * 
	 * @return the code
	 */
	public Integer getCode() {
		return code;
	}
	
	/**
	 * Sets the type.
	 * 
	 * @param type
	 *            the new type
	 */
	public void setType(final TYPE type) {
		this.type = type;
	}
	
	/**
	 * Gets the type.
	 * 
	 * @return the type
	 */
	public TYPE getType() {
		return type;
	}
	
	/**
	 * Sets the message.
	 * 
	 * @param message
	 *            the new message
	 */
	public void setMessage(final String message) {
		this.message = message;
	}
	
	/**
	 * Gets the message.
	 * 
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}
	
	/**
	 * Sets the timestamp.
	 * 
	 * @param timestamp
	 *            the new timestamp
	 */
	public void setTimestamp(final String timestamp) {
		this.timestamp = timestamp;
	}
	
	/**
	 * Gets the timestamp.
	 * 
	 * @return the timestamp
	 */
	public String getTimestamp() {
		return timestamp;
	}
	
	/**
	 * Sets the hints.
	 * 
	 * @param hints
	 *            the new hints
	 */
	public void setHints(final List<Hint> hints) {
		this.hints = hints;
	}
	
	/**
	 * Gets the hints.
	 * 
	 * @return the hints
	 */
	public List<Hint> getHints() {
		return hints;
	}
	
	/**
	 * Checks for hints.
	 * 
	 * @return true, if successful
	 */
	public boolean hasHints() {
		return (hints != null && hints.size() > 0);
	}
	
	/**
	 * The Enum TYPE.
	 */
	public static enum TYPE {
		
		/**
		 * The error.
		 */
		error, 
 /**
	 * The warning.
	 */
 warning, 
 /**
	 * The weak warning.
	 */
 weakWarning, 
 /**
	 * The info.
	 */
 info
	};
	
	// error codes
	// TODO: better implement error codes
	/**
	 * The no planning.
	 */
	public static Integer	NO_PLANNING			= 1000;
	
	/**
	 * The exception.
	 */
	public static Integer	EXCEPTION			= 2000;
	
	/**
	 * The jsonrpcexception.
	 */
	public static Integer	JSONRPCEXCEPTION	= 2001;
	
	private Integer			code				= null;
	private TYPE			type				= null;
	private String			message				= null;
	private String			timestamp			= null;
	private List<Hint>		hints				= null;
}
