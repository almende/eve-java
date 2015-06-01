/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.algorithms.simulation;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.almende.eve.capabilities.Config;
import com.almende.eve.capabilities.handler.Handler;
import com.almende.eve.protocol.Meta;
import com.almende.eve.protocol.jsonrpc.RpcBasedProtocol;
import com.almende.eve.protocol.jsonrpc.formats.Caller;
import com.almende.eve.protocol.jsonrpc.formats.JSONMessage;
import com.almende.eve.protocol.jsonrpc.formats.JSONRequest;
import com.almende.eve.protocol.jsonrpc.formats.JSONResponse;
import com.almende.eve.protocol.jsonrpc.formats.Params;
import com.almende.util.TypeUtil;
import com.almende.util.jackson.JOM;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class InboxProtocol, provides an easy way to get a single threaded agent,
 * only one inbound message in a single thread at a time.
 */
public class SimulationProtocol implements RpcBasedProtocol {
	private static final Logger				LOG				= Logger.getLogger(SimulationProtocol.class
																	.getName());
	private static final TypeUtil<Tracer>	TRACER			= new TypeUtil<Tracer>() {};
	private Config							params			= null;

	private Set<Tracer>						outboundTracers	= new HashSet<Tracer>();
	private Set<Tracer>						inboundTracers	= new HashSet<Tracer>();
	private Handler<Caller>					caller			= null;
	private int								seenResponse	= 0;

	/**
	 * Instantiates a new inbox protocol.
	 *
	 * @param params
	 *            the params
	 * @param handle
	 *            the handle
	 */
	public SimulationProtocol(final ObjectNode params,
			final Handler<Object> handle) {
		this.params = Config.decorate(params);
	}

	@Override
	public ObjectNode getParams() {
		return this.params;
	}

	@Override
	public void setCaller(Handler<Caller> caller) {
		this.caller = caller;
	}

	@Override
	public void delete() {}

	private synchronized void inc() {
		seenResponse++;
	}

	private synchronized void dec() {
		seenResponse--;
	}

	private void receiveTracerReport(final Tracer tracer) {
		synchronized (outboundTracers) {
			outboundTracers.remove(tracer);
		}
	}

	private void storeOutboundTracer(final Tracer tracer) {
		synchronized (outboundTracers) {
			outboundTracers.add(tracer);
		}
	}

	private void storeInboundTracer(final Tracer tracer) {
		synchronized (inboundTracers) {
			inboundTracers.add(tracer);
		}
	}

	private boolean doTracer() {
		return !inboundTracers.isEmpty();
	}

	private Collection<Tracer> checkTracers() {
		if (outboundTracers.isEmpty() && seenResponse <= 0) {
			return inboundTracers;
		}
		return null;
	}

	private Tracer createTracer() {
		final Tracer tracer = new Tracer();
		tracer.setOwner(caller.get().getSenderUrls().get(0));
		return tracer;
	}

	private void sendReports(final Collection<Tracer> tracers,
			JSONResponse resp, final URI peer) {
		if (tracers == null) {
			return;
		}
		synchronized (tracers) {
			final Iterator<Tracer> iter = tracers.iterator();
			while (iter.hasNext()) {
				final Tracer tracer = iter.next();
				final ObjectNode extra = JOM.createObjectNode();
				extra.set("@simtracerreport",
						JOM.getInstance().valueToTree(tracer));
				if (resp != null && tracer.getOwner().equals(peer)) {
					if (resp.getExtra() == null) {
						resp.setExtra(extra);
					} else {
						resp.getExtra().setAll(extra);
					}
					resp = null;
				} else {
					final Params params = new Params();
					params.set("tracer", JOM.getInstance().valueToTree(tracer));
					final JSONRequest message = new JSONRequest(
							"scheduler.receiveTracerReport", params);
					message.setExtra(extra);
					try {
						caller.get().call(tracer.getOwner(), message);
					} catch (IOException e) {
						LOG.log(Level.WARNING, "Failed to send tracerreport", e);
					}
				}
				iter.remove();
			}
		}
	}

	/*
	 * trace on inbound call, add tag, keep in list.
	 * outbound call, send new tracers, in list.
	 * When report on tracer, remove tracer from list;
	 * If seen my response (throught tag garanteed) and tracers empty, clear
	 * tracer.
	 * When all tracers gone, send report to trace owner. (Requires caller!)
	 */

	@Override
	public boolean inbound(final Meta msg) {

		JSONMessage message = JSONMessage.jsonConvert(msg.getResult());
		if (message != null) {
			// Parse inbound message, check for tracer.
			msg.setResult(message);
			if (message.getExtra() != null) {
				final ObjectNode extra = message.getExtra();
				if (message.isRequest()) {
					// If tracer found, tag message with my tag
					// Store tracer
					final JSONRequest request = (JSONRequest) message;
					if (!"scheduler.receiveTracerReport".equals(request
							.getMethod())) {
						final Object tracerObj = extra.remove("@simtracer");
						if (tracerObj != null) {
							Tracer tracer = TRACER.inject(tracerObj);
							msg.setTag("tracer");
							storeInboundTracer(tracer);
							inc();
						}
					}
				}
				final JsonNode report = extra.remove("@simtracerreport");
				if (report != null) {
					Tracer tracer = TRACER.inject(report);
					if (outboundTracers.contains(tracer)) {
						receiveTracerReport(tracer);
						sendReports(checkTracers(), null, msg.getPeer());
					} else {
						//Onwards to scheduler.
						return msg.nextIn();
					}
				}
			}
		}
		return msg.nextIn();
	}

	@Override
	public boolean outbound(final Meta msg) {
		// if my tag on message, check if "empty" message, if so, drop. Else
		// forward.
		// Mark tracer as done for response
		if ("tracer".equals(msg.getTag())) {
			dec();
			msg.setTag(null);
			final JSONResponse message = (JSONResponse) msg.getResult();
			if (message.getResult() == null || message.getResult().isNull()) {
				sendReports(checkTracers(), null, msg.getPeer());
				// skip forwarding
				return false;
			} else {
				sendReports(checkTracers(), message, msg.getPeer());
			}
		} else if (doTracer()) {
			final JSONMessage message = JSONMessage
					.jsonConvert(msg.getResult());
			if (message != null) {
				if (message.isRequest()) {
					final JSONRequest request = (JSONRequest) message;
					if (!"scheduler.receiveTracerReport".equals(request
							.getMethod())) {
						final Tracer tracer = createTracer();
						final ObjectNode extra = JOM.createObjectNode();
						extra.set("@simtracer",
								JOM.getInstance().valueToTree(tracer));
						if (message.getExtra() == null) {
							message.setExtra(extra);
						} else {
							message.getExtra().setAll(extra);
						}
						storeOutboundTracer(tracer);
					} else {
						return msg.nextOut();

					}
				}
			}
		}
		return msg.nextOut();
	}
}
