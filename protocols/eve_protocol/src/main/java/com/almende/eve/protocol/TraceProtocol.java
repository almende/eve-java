/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.protocol;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import com.almende.eve.capabilities.handler.Handler;
import com.almende.util.jackson.JOM;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class InboxProtocol, provides an easy way to get a single threaded agent,
 * only one inbound message in a single thread at a time.
 */
public class TraceProtocol implements Protocol {
	private static final Logger	LOG		= Logger.getLogger(TraceProtocol.class
												.getName());
	private TraceProtocolConfig	params	= null;
	private Logger				logger	= null;
	private FileHandler			handler	= null;

	/**
	 * Instantiates a new protocol tracer.
	 *
	 * @param params
	 *            the params
	 * @param handle
	 *            the handle
	 */
	public TraceProtocol(final ObjectNode params, final Handler<Object> handle) {
		this.params = TraceProtocolConfig.decorate(params);
		this.logger = Logger.getLogger("TraceProtocol_" + this.params.getId());
		this.logger.setLevel(Level.ALL);
		if (this.params.getFileName() != null) {
			String filename = this.params.getFileName() + this.params.getId();
			try {
				handler = new FileHandler(filename);
				this.logger.addHandler(handler);

				if (this.params.isFlat()) {
					handler.setFormatter(new MyFormatter());
				}
				LOG.warning("Started tracelog:"+filename);
			} catch (IOException e) {
				LOG.log(Level.WARNING, "Couldn't open outputfile for tracing:"
						+ filename, e);
			}
		}

	}

	@Override
	public ObjectNode getParams() {
		return this.params;
	}

	@Override
	public void delete() {
		if (handler != null) {
			this.logger.removeHandler(handler);
			handler.flush();
			handler.close();
			handler = null;
		}
	}

	private void log(final Meta msg, final boolean inbound) {
		try {
			logger.fine((inbound ? "IN :" : "OUT:")
					+ JOM.getInstance().writeValueAsString(msg));
		} catch (JsonProcessingException e) {
			LOG.log(Level.WARNING, "Couldn't serialize tracemessage", e);
		}
	}

	@Override
	public boolean inbound(Meta msg) {
		log(msg, true);
		// just forwarding...
		return msg.nextIn();
	}

	@Override
	public boolean outbound(Meta msg) {
		log(msg, false);
		// just forwarding...
		return msg.nextOut();
	}

	class MyFormatter extends Formatter {
		/**
		 * Format the given LogRecord.
		 * 
		 * @param record
		 *            the log record to be formatted.
		 * @return a formatted log record
		 */
		public synchronized String format(LogRecord record) {
			StringBuilder sb = new StringBuilder();
			sb.append(record.getMillis());
			sb.append(":");
			sb.append(record.getLoggerName());
			sb.append(":");
			sb.append(record.getThreadID());
			sb.append(":");
			sb.append(record.getMessage());
			sb.append("\n");
			return sb.toString();
		}
	}
}
