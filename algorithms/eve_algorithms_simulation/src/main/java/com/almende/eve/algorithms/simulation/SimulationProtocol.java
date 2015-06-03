/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.algorithms.simulation;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.almende.eve.capabilities.handler.Handler;
import com.almende.eve.protocol.Meta;
import com.almende.eve.protocol.jsonrpc.RpcBasedProtocol;
import com.almende.eve.protocol.jsonrpc.formats.Caller;
import com.almende.eve.protocol.jsonrpc.formats.JSONMessage;
import com.almende.eve.protocol.jsonrpc.formats.JSONRequest;
import com.almende.eve.protocol.jsonrpc.formats.JSONResponse;
import com.almende.eve.protocol.jsonrpc.formats.Params;
import com.almende.util.TypeUtil;
import com.almende.util.callback.AsyncCallback;
import com.almende.util.callback.SyncCallback;
import com.almende.util.jackson.JOM;
import com.almende.util.threads.ThreadPool;
import com.almende.util.uuid.UUID;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class InboxProtocol, provides an easy way to get a single threaded agent,
 * only one inbound message in a single thread at a time.
 */
public class SimulationProtocol implements RpcBasedProtocol {
	private static final Logger				LOG					= Logger.getLogger(SimulationProtocol.class
																		.getName());
	private static final TypeUtil<Tracer>	TRACER				= new TypeUtil<Tracer>() {};
	private SimulationProtocolConfig		params				= null;

	private Set<Tracer>						outboundTracers		= new HashSet<Tracer>();
	private Set<Tracer>						inboundTracers		= new HashSet<Tracer>();
	private Map<String, Boolean>			inboundRequests		= new HashMap<String, Boolean>();
	private Handler<Caller>					caller				= null;

	private boolean							strongConsistency	= true;
	private SimulationInbox					inbox				= null;
	private Integer[]						counter				= new Integer[1];
	private List<SyncCallback<?>>			outboundCallbacks	= new ArrayList<SyncCallback<?>>();

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
		this.params = SimulationProtocolConfig.decorate(params);

		this.strongConsistency = this.params.isStrongConsistency();
		if (this.strongConsistency) {
			inbox = new SimulationInbox(this.params.getId());
			counter[0] = 0;
			heartbeatCheck();
		}
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
		return !inboundRequests.isEmpty();
	}

	private Collection<Tracer> checkTracers() {
		if (outboundTracers.isEmpty() && inboundRequests.isEmpty()) {
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

	@Override
	public boolean inbound(final Meta msg) {
		JSONMessage message = JSONMessage.jsonConvert(msg.getMsg());
		if (message != null) {
			// Parse inbound message, check for tracer.
			msg.setMsg(message);
			if (message.getExtra() != null) {
				final ObjectNode extra = message.getExtra();
				if (message.isRequest()) {
					// If tracer found, make sure we have id, if not add one.
					// Store tracer
					final JSONRequest request = (JSONRequest) message;
					if (!"scheduler.receiveTracerReport".equals(request
							.getMethod())) {
						final Object tracerObj = extra.remove("@simtracer");
						if (tracerObj != null) {
							synchronized (inboundRequests) {
								Tracer tracer = TRACER.inject(tracerObj);
								storeInboundTracer(tracer);
								if (request.getId() != null
										&& !request.getId().isNull()) {
									inboundRequests.put(request.getId()
											.asText(), false);
								} else {
									request.setId(JOM.getInstance()
											.valueToTree(tracer.getId()));
									inboundRequests.put(request.getId()
											.asText(), true);
								}
							}

						}
					}
				}
				final JsonNode report = extra.remove("@simtracerreport");
				if (report != null) {
					final Tracer tracer = TRACER.inject(report);
					synchronized (outboundTracers) {
						if (outboundTracers.contains(tracer)) {
							receiveTracerReport(tracer);
							sendReports(checkTracers(), null, msg.getPeer());
							if (message.isRequest()) {
								return false;
							}
						} else if (message.isRequest()) {
							// skip counting for strong consistency This one
							// may always proceed
							return msg.nextIn();
						}
					}
				}
			}
		}
		if (strongConsistency) {
			// TODO: what is id is null, what order would messages then get?
			// Still is random now!
			final UUID uuid = message.getId() != null ? new UUID(message
					.getId().asText()) : new UUID();
			inbox.add(msg, uuid);
			return false;
		} else {
			return msg.nextIn();
		}
	}

	private void heartbeatCheck() {
		ThreadPool.getPool().execute(new Runnable() {
			@Override
			public void run() {
				while (true) {
					synchronized (counter) {
						if (counter[0] == 0) {
							try {
								counter.wait(100);
							} catch (InterruptedException e) {}
							continue;
						}
					}
					synchronized (inboundRequests) {
						if (inboundRequests.isEmpty()) {
							synchronized (counter) {
								if (counter[0] > 0) {
									if (inbox.heartBeat(Integer
											.valueOf(counter[0]))) {
										counter[0] = 0;
									}
								}
							}
							continue;
						}
						if (outboundCallbacks.size() == 0) {
							continue;
						}
						final Iterator<SyncCallback<?>> iter = outboundCallbacks
								.iterator();
						int count = 0;
						while (iter.hasNext()) {
							final SyncCallback<?> callback = iter.next();
							if (callback.isWaiting()) {
								count++;
							} else if (callback.isDone()) {
								iter.remove();
							}
						}
						if (inboundRequests.size() == count) {
							synchronized (counter) {
								if (counter[0] > 0) {
									if (inbox.heartBeat(Integer
											.valueOf(counter[0]))) {
										counter[0] = 0;
									}
								}
							}
						}
					}
				}
			}
		});
	}

	private void incCounter() {
		if (strongConsistency) {
			synchronized (counter) {
				counter[0]++;
				counter.notify();
			}
		}
	}

	@Override
	public boolean outbound(final Meta msg) {
		if (doTracer()) {
			final JSONMessage message = JSONMessage.jsonConvert(msg.getMsg());
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
						if (strongConsistency) {
							AsyncCallback<?> callback = request.getCallback();
							if (callback != null
									&& callback instanceof SyncCallback<?>) {
								outboundCallbacks
										.add((SyncCallback<?>) callback);
							}
							incCounter();
						}
					}
				} else {
					final JSONResponse response = (JSONResponse) message;
					Boolean drop = null;
					synchronized (inboundRequests) {
						drop = inboundRequests.remove(response.getId()
								.textValue());
						if (drop != null && drop && msg.getTag() == null) {
							sendReports(checkTracers(), null, msg.getPeer());
							// skip forwarding
							return false;
						} else {
							sendReports(checkTracers(), response, msg.getPeer());
							incCounter();
						}
					}
				}
			}
		}
		return msg.nextOut();
	}
}
