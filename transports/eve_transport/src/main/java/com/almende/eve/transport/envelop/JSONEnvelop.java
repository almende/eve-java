/*
 * Copyright: Almende B.V. (2015), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.transport.envelop;

import java.io.IOException;

import org.json.JSONObject;

import com.almende.util.jackson.JOM;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class JSONEnvelop.
 */
public final class JSONEnvelop {

	private JSONEnvelop() {}

	/**
	 * Wrap as Jackson ObjectNode.
	 *
	 * @param from
	 *            the from
	 * @param to
	 *            the to
	 * @param message
	 *            the message
	 * @return the object node
	 */
	public static ObjectNode wrapAsObjectNode(final String from,
			final String to, final String message) {
		final ObjectNode envelop = JOM.createObjectNode();
		envelop.put("to", to);
		envelop.put("from", from);
		envelop.put("message", message);
		return envelop;
	}

	/**
	 * Wrap as String.
	 *
	 * @param from
	 *            the from
	 * @param to
	 *            the to
	 * @param message
	 *            the message
	 * @return the string
	 */
	public static String wrapAsString(final String from, final String to,
			final String message) {
		return wrapAsObjectNode(from, to, message).toString();
	}

	/**
	 * Wrap as org.json.JSONObject.
	 *
	 * @param from
	 *            the from
	 * @param to
	 *            the to
	 * @param message
	 *            the message
	 * @return the JSON object
	 * @throws JsonProcessingException
	 *             the json processing exception
	 */
	public static JSONObject wrapAsJSONObject(final String from,
			final String to, final String message)
			throws JsonProcessingException {
		return JOM.getInstance().treeToValue(
				wrapAsObjectNode(from, to, message), JSONObject.class);
	}

	/**
	 * Unwrap.
	 *
	 * @param envelop
	 *            the envelop
	 * @return the envelop
	 */
	public static Envelop unwrap(final JSONObject envelop){
		return JOM.getInstance().convertValue(envelop, Envelop.class);
	}
	
	/**
	 * Unwrap.
	 *
	 * @param envelop
	 *            the envelop
	 * @return the envelop
	 * @throws JsonProcessingException
	 *             the json processing exception
	 */
	public static Envelop unwrap(final ObjectNode envelop) throws JsonProcessingException{
		return JOM.getInstance().treeToValue(envelop, Envelop.class);
	}
	
	/**
	 * Unwrap.
	 *
	 * @param envelop
	 *            the envelop
	 * @return the envelop
	 * @throws JsonParseException
	 *             the json parse exception
	 * @throws JsonMappingException
	 *             the json mapping exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static Envelop unwrap(final String envelop) throws JsonParseException, JsonMappingException, IOException{
		return JOM.getInstance().readValue(envelop, Envelop.class);
	}
	/**
	 * The Class Envelop.
	 */
	public static class Envelop {
		private String from=null;
		private String to= null;
		private String message=null;
		
		/**
		 * Instantiates a new envelop.
		 */
		public Envelop(){}
		
		/**
		 * Instantiates a new envelop.
		 *
		 * @param from
		 *            the from
		 * @param to
		 *            the to
		 * @param message
		 *            the message
		 */
		public Envelop(final String from, final String to, final String message){
			this.from=from;
			this.to=to;
			this.message=message;
		}
		
		/**
		 * Gets the from.
		 *
		 * @return the from
		 */
		public String getFrom() {
			return from;
		}
		
		/**
		 * Sets the from.
		 *
		 * @param from
		 *            the new from
		 */
		public void setFrom(String from) {
			this.from = from;
		}
		
		/**
		 * Gets the to.
		 *
		 * @return the to
		 */
		public String getTo() {
			return to;
		}
		
		/**
		 * Sets the to.
		 *
		 * @param to
		 *            the new to
		 */
		public void setTo(String to) {
			this.to = to;
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
		 * Sets the message.
		 *
		 * @param message
		 *            the new message
		 */
		public void setMessage(String message) {
			this.message = message;
		}
	}
}
