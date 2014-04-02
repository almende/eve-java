/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.entity;

import java.io.Serializable;

import com.almende.eve.entity.activity.Constraints;

/**
 * The Class Hint.
 */
@SuppressWarnings("serial")
public class Hint implements Serializable {
	
	/**
	 * Instantiates a new hint.
	 */
	public Hint() {
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
	 * Sets the constraints.
	 * 
	 * @param constraints
	 *            the new constraints
	 */
	public void setConstraints(final Constraints constraints) {
		this.constraints = constraints;
	}
	
	/**
	 * Gets the constraints.
	 * 
	 * @return the constraints
	 */
	public Constraints getConstraints() {
		return constraints;
	}
	
	private Integer		code		= null;
	private String		message		= null;
	private Constraints	constraints	= null;
}
