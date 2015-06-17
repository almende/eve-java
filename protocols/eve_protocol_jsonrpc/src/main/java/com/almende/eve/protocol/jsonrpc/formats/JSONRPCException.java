/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.protocol.jsonrpc.formats;

import java.util.ArrayList;
import java.util.List;

import com.almende.util.TypeUtil;
import com.almende.util.jackson.JOM;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * The Class JSONRPCException.
 */
public class JSONRPCException extends RuntimeException {
	private static final long	serialVersionUID	= -4258336566828038603L;

	private boolean				remote				= false;
	private int					code				= -1;
	private String				message				= null;
	private JsonNode			data				= null;

	/**
	 * The Enum CODE.
	 */
	public static enum CODE {

		/** The unknown error. */
		UNKNOWN_ERROR,
		/** The parse error. */
		PARSE_ERROR,
		/** The invalid request. */
		INVALID_REQUEST,
		/** The remote exception. */
		REMOTE_EXCEPTION,
		/** The method not found. */
		METHOD_NOT_FOUND,
		/** The invalid params. */
		INVALID_PARAMS,
		/** The internal error. */
		INTERNAL_ERROR,
		/** The not found. */
		NOT_FOUND,
		/** The unauthorized. */
		UNAUTHORIZED
	}

	/**
	 * Instantiates a new jSONRPC exception.
	 */
	public JSONRPCException() {
		super();
		init(CODE.UNKNOWN_ERROR, null, null);
	}

	/**
	 * Instantiates a new jSONRPC exception.
	 * 
	 * @param code
	 *            the code
	 */
	public JSONRPCException(final CODE code) {
		super();
		init(code, null, null);
	}

	/**
	 * Instantiates a new jSONRPC exception.
	 * 
	 * @param code
	 *            the code
	 * @param description
	 *            the description
	 */
	public JSONRPCException(final CODE code, final String description) {
		super(description);
		init(code, description, null);
	}

	/**
	 * Instantiates a new jSONRPC exception.
	 * 
	 * @param code
	 *            the code
	 * @param description
	 *            the description
	 * @param t
	 *            the t
	 */
	public JSONRPCException(final CODE code, final String description,
			final Throwable t) {
		super(description, t);
		init(code, description, t);
	}

	/**
	 * Instantiates a new jSONRPC exception.
	 * 
	 * @param error
	 *            the error
	 */
	public JSONRPCException(final JSONRPCException error) {
		super(error);
		if (error != null) {
			setCode(error.getCode());
			setMessage(error.getMessage());
			if (error.hasData()) {
				setData(error.getData());
			}
		} else {
			init(CODE.UNKNOWN_ERROR, null, null);
		}
	}

	/**
	 * Instantiates a new jSONRPC exception.
	 * 
	 * @param message
	 *            the message
	 */
	public JSONRPCException(final String message) {
		super(message);
		init(CODE.UNKNOWN_ERROR, message, null);
	}

	/**
	 * Instantiates a new jSONRPC exception.
	 * 
	 * @param message
	 *            the message
	 * @param t
	 *            the t
	 */
	public JSONRPCException(final String message, final Throwable t) {
		super(message, t);
		init(CODE.UNKNOWN_ERROR, message, t);
	}

	/**
	 * Instantiates a new jSONRPC exception.
	 * 
	 * @param code
	 *            the code
	 * @param message
	 *            the message
	 */
	public JSONRPCException(final Integer code, final String message) {
		super(message);
		setCode(code);
		setMessage(message);
	}

	/**
	 * Instantiates a new jSONRPC exception.
	 * 
	 * @param code
	 *            the code
	 * @param message
	 *            the message
	 * @param data
	 *            the data
	 */
	public JSONRPCException(final Integer code, final String message,
			final Object data) {
		super(message);
		setCode(code);
		setMessage(message);
		setData(data);
	}

	/**
	 * Instantiates a new jSONRPC exception.
	 *
	 * @param exception
	 *            the exception
	 * @throws JsonProcessingException
	 *             the json processing exception
	 */
	public JSONRPCException(final JsonNode exception)
			throws JsonProcessingException {
		super();
		JSONRPCException cause = null;
		if (exception != null && !exception.isNull()) {
			cause = JOM.getInstance().treeToValue(exception,
					JSONRPCException.class);
			cause.setRemote(true);
			if (exception.has("stackTrace")) {
				final TypeUtil<List<StackTraceElement>> injector = new TypeUtil<List<StackTraceElement>>() {};
				final List<StackTraceElement> trace = injector.inject(exception
						.get("stackTrace"));
				cause.setStackTrace(trace.toArray(new StackTraceElement[0]));
			}
		}
		init(CODE.REMOTE_EXCEPTION, JSONRPCException.class.getSimpleName()
				+ " received!", cause);
	}

	/**
	 * Inits the.
	 * 
	 * @param code
	 *            the code
	 * @param message
	 *            the message
	 * @param t
	 *            the t
	 */
	private void init(final CODE code, final String message, final Throwable t) {
		switch (code) {
			case UNKNOWN_ERROR:
				setCode(-32000);
				setMessage("Unknown error");
				break;
			case PARSE_ERROR:
				setCode(-32700);
				setMessage("Parse error");
				break;
			case INVALID_REQUEST:
				setCode(-32600);
				setMessage("Invalid request");
				break;
			case REMOTE_EXCEPTION:
				setCode(-32500);
				setMessage("Remote application error");
				break;
			case METHOD_NOT_FOUND:
				setCode(-32601);
				setMessage("Method not found");
				break;
			case INVALID_PARAMS:
				setCode(-32602);
				setMessage("Invalid params");
				break;
			case INTERNAL_ERROR:
				setCode(-32603);
				setMessage("Internal error");
				break;
			case NOT_FOUND:
				setCode(404);
				setMessage("Not found");
				break;
			case UNAUTHORIZED:
				setCode(-32401);
				setMessage("Unauthorized");
				break;
		}
		setMessage(message);
		if (t != null && getCause() == null) {
			initCause(t);
		}
	}

	/**
	 * Sets the code.
	 * 
	 * @param code
	 *            the new code
	 */
	public final void setCode(final int code) {
		this.code = code;
	}

	/**
	 * Gets the code.
	 * 
	 * @return the code
	 */
	public int getCode() {
		return this.code;
	}

	/**
	 * Sets the message.
	 * 
	 * @param message
	 *            the new message
	 */
	public final void setMessage(final String message) {
		this.message = message;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Throwable#getMessage()
	 */
	@Override
	public String getMessage() {
		if (this.message == null) {
			return super.getMessage();
		}
		return this.message;
	}

	/**
	 * Sets the data.
	 * 
	 * @param data
	 *            the new data
	 */
	public final void setData(final Object data) {
		if (data != null) {
			this.data = JOM.getInstance().valueToTree(data);
		} else {
			this.data = null;
		}
	}

	/**
	 * Gets the data.
	 * 
	 * @return the data
	 */
	public Object getData() {
		if (this.data != null) {
			return this.data;
		}
		if (this.getCause() != null) {
			return this.getCause();
		}
		return null;
	}

	/**
	 * Checks for data.
	 * 
	 * @return true, if successful
	 */
	public boolean hasData() {
		return this.data != null;
	}

	/**
	 * Sets the remote.
	 * 
	 * @param remote
	 *            the new remote
	 */
	public void setRemote(final boolean remote) {
		this.remote = remote;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Throwable#toString()
	 */
	@Override
	public String toString() {
		return this.getClass().getName() + ": "
				+ (remote ? "(Remote stackTrace) " : "")
				+ getLocalizedMessage();
	}

	// Following methods are adopted from apache.commons.lang3
	/**
	 * Gets the throwable list.
	 *
	 * @return the throwable list
	 */
	@JsonIgnore
	public List<Throwable> getThrowableList() {
		Throwable throwable = this;
		final List<Throwable> list = new ArrayList<Throwable>(3);
		while (throwable != null && list.contains(throwable) == false) {
			list.add(throwable);
			throwable = throwable.getCause();
		}
		return list;
	}

	/**
	 * Gets the root cause.
	 *
	 * @return the root cause
	 */
	@JsonIgnore
	public Throwable getRootCause() {
		final List<Throwable> list = getThrowableList();
		return list.size() < 2 ? null : (Throwable) list.get(list.size() - 1);
	}

	/**
	 * Throw the root cause is available.
	 *
	 * @throws Throwable
	 *             the throwable
	 */
	@JsonIgnore
	public void throwRootCause() throws Throwable {
		if (getRootCause() != null) {
			throw getRootCause();
		}
	}
}