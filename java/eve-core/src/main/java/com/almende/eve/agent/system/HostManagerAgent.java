package com.almende.eve.agent.system;

import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.almende.eve.agent.Agent;
import com.almende.eve.rpc.jsonrpc.JSONMessage;
import com.almende.eve.rpc.jsonrpc.JSONRPCException;
import com.almende.eve.rpc.jsonrpc.JSONResponse;

/**
 * @author Almende
 * 
 */
public class HostManagerAgent extends Agent {
	private static final Logger	LOG	= Logger.getLogger(HostManagerAgent.class
											.getName());
	
	
	/**
	 * Utility method that is called by the agentHost to report original
	 * receiver is missing.
	 * 
	 * 
	 * @param msg
	 * @param agentId
	 * @param senderUrl
	 * @param tag
	 */
	public void reportMissingAgent(final String agentId, final Object msg,
			final URI senderUrl, final String tag) {
		final JSONRPCException jsonError = new JSONRPCException(
				JSONRPCException.CODE.NOT_FOUND, "Agent " + agentId
						+ " is not found on this host.");
		final JSONResponse response = new JSONResponse(jsonError);
		
		final JSONMessage jsonMsg = jsonConvert(msg);
		if (jsonMsg != null && jsonMsg.getId() != null) {
			response.setId(jsonMsg.getId());
		}
		try {
			send(response, senderUrl, null, tag);
		} catch (final Exception e) {
			LOG.log(Level.WARNING, getId()
					+ ": failed to send missing agent error to remote agent.",
					e);
		}
		
	}
}
