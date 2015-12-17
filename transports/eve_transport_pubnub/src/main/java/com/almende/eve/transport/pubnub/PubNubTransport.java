/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.transport.pubnub;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.codec.binary.Base64;

import com.almende.eve.capabilities.handler.Handler;
import com.almende.eve.transport.AbstractTransport;
import com.almende.eve.transport.Receiver;
import com.almende.eve.transport.envelop.JSONEnvelop;
import com.almende.eve.transport.pubnub.PubNubTransportBuilder.PubNubService;
import com.almende.util.URIUtil;
import com.almende.util.callback.AsyncCallback;
import com.almende.util.jackson.JOM;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubError;
import com.pubnub.api.PubnubException;

/**
 * The Class PubNubTransport.
 */
public class PubNubTransport extends AbstractTransport {
	private static final Logger	LOG				= Logger.getLogger(PubNubTransport.class
														.getName());

	private Pubnub				pubnub			= null;
	private String				publishKey		= null;
	private String				subscribeKey	= null;
	private boolean				useSSL			= true;

	private String				myChannel		= null;

	/**
	 * Instantiates a new PubNub transport.
	 *
	 * @param config
	 *            the config
	 * @param newHandle
	 *            the new handle
	 * @param pnService
	 *            the pn service
	 */
	public PubNubTransport(PubNubTransportConfig config,
			Handler<Receiver> newHandle, PubNubService pnService) {
		super(config.getAddress(), newHandle, pnService, config);
		publishKey = config.getPublishKey();
		subscribeKey = config.getSubscribeKey();
		useSSL = config.isUseSSL();
		myChannel = super.getAddress().getSchemeSpecificPart();
	}

	/*
	 * (non-Javadoc)
	 * @see com.almende.eve.transport.Transport#send(java.net.URI,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public <T> void send(final URI receiverUri, final String message,
			final String tag, final AsyncCallback<T> callback)
			throws IOException {

		// Check and deliver local shortcut.
		if (sendLocal(receiverUri, message)) {
			return;
		}

		final Callback pubnubCB = new Callback() {
			public void successCallback(String channel, Object response) {
				LOG.warning("successfully send:" + message + " to:" + channel
						+ " response:" + response);
			}

			public void errorCallback(String channel, PubnubError error) {
				LOG.warning("PubNub returned an error for:" + message + " to:"
						+ channel + " response:" + error);
				if (callback != null) {
					callback.onFailure(new IOException(error.getErrorString()));
				}
			}
		};
		pubnub.publish(receiverUri.getSchemeSpecificPart(), JSONEnvelop
				.wrapAsJSONObject(getAddress().getSchemeSpecificPart(),
						receiverUri.getSchemeSpecificPart(), message), pubnubCB);
	}

	/*
	 * (non-Javadoc)
	 * @see com.almende.eve.transport.Transport#send(java.net.URI, byte[],
	 * java.lang.String)
	 */
	@Override
	public <T> void send(final URI receiverUri, final byte[] message,
			final String tag, final AsyncCallback<T> callback)
			throws IOException {
		send(receiverUri, Base64.encodeBase64String(message), tag, callback);
	}

	/*
	 * (non-Javadoc)
	 * @see com.almende.eve.transport.Transport#connect()
	 */
	@Override
	public void connect() throws IOException {
		LOG.warning("pubnub connect:" + publishKey + " " + subscribeKey + " "
				+ useSSL);
		pubnub = new Pubnub(publishKey, subscribeKey, useSSL);
		try {
			subscribe();
		} catch (PubnubException e) {
			throw new IOException(e);
		}

	}

	/*
	 * (non-Javadoc)
	 * @see com.almende.eve.transport.Transport#disconnect()
	 */
	@Override
	public void disconnect() {
		if (pubnub != null) {
			pubnub.shutdown();
		}
		pubnub = null;
	}

	/*
	 * (non-Javadoc)
	 * @see com.almende.eve.transport.Transport#getProtocols()
	 */
	@Override
	public List<String> getProtocols() {
		return Arrays.asList("pubnub");
	}

	private void subscribe() throws PubnubException {
		LOG.warning("pubnub subscribe to:" + myChannel);
		final Handler<Receiver> handler = super.getHandle();
		pubnub.subscribe(myChannel, new Callback() {

			@Override
			public void connectCallback(String channel, Object message) {
				LOG.fine("SUBSCRIBE : CONNECT on channel:" + channel + " : "
						+ message.getClass() + " : " + message.toString());
			}

			@Override
			public void disconnectCallback(String channel, Object message) {
				LOG.fine("SUBSCRIBE : DISCONNECT on channel:" + channel + " : "
						+ message.getClass() + " : " + message.toString());
			}

			public void reconnectCallback(String channel, Object message) {
				LOG.fine("SUBSCRIBE : RECONNECT on channel:" + channel + " : "
						+ message.getClass() + " : " + message.toString());
			}

			@Override
			public void successCallback(String channel, Object message) {
				LOG.fine("SUBSCRIBE : " + channel + " : " + message.getClass()
						+ " : " + message.toString());
				// Received a message!
				if (message.toString().equals("")) {
					LOG.warning("received empty message!");
					return;
				}
				if (message.toString().startsWith("{")) {
					ObjectNode msg = JOM.getInstance().valueToTree(message);
					if (myChannel.equals(msg.get("to").asText())) {
						handler.get().receive(
								msg.get("message").asText(),
								URIUtil.create("pubnub:"
										+ msg.get("from").asText()), null);
					} else {
						LOG.fine("Received message for someone else, ignoring!");
					}
				} else {
					LOG.warning("Received non-json message:" + message);
				}
			}

			@Override
			public void errorCallback(String channel, PubnubError error) {
				LOG.warning("SUBSCRIBE : ERROR on channel " + channel + " : "
						+ error.toString());
			}
		});
	}

}
