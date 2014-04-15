/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.transport.zmq;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.zeromq.ZMQ.Socket;

import com.almende.eve.capabilities.handler.Handler;
import com.almende.eve.transport.AbstractTransport;
import com.almende.eve.transport.Receiver;
import com.almende.eve.transport.TransportService;
import com.almende.eve.transport.tokens.TokenRet;
import com.almende.eve.transport.tokens.TokenStore;
import com.almende.util.ObjectCache;
import com.almende.util.callback.AsyncCallback;
import com.almende.util.callback.AsyncCallbackQueue;
import com.almende.util.callback.SyncCallback;
import com.almende.util.jackson.JOM;
import com.almende.util.threads.ThreadPool;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * The Class ZmqTransport.
 */
public class ZmqTransport extends AbstractTransport {
	private static final Logger						LOG					= Logger.getLogger(ZmqTransport.class
																				.getCanonicalName());
	private String									zmqUrl;
	private Thread									listeningThread;
	private boolean									doesAuthentication	= false;
	private boolean									doDisconnect		= false;
	private static final AsyncCallbackQueue<String>	callbacks			= new AsyncCallbackQueue<String>();
	
	/**
	 * Instantiates a new zmq transport.
	 * 
	 * @param params
	 *            the params
	 * @param handle
	 *            the handle
	 * @param service
	 *            the service
	 */
	public ZmqTransport(JsonNode params, Handler<Receiver> handle,
			TransportService service) {
		super(URI.create(params.get("address").asText()), handle, service);
		zmqUrl = super.getAddress().toString().replaceFirst("^zmq:/?/?", "");
		
	}
	
	/**
	 * Send async.
	 * 
	 * @param zmqType
	 *            the zmq type
	 * @param token
	 *            the token
	 * @param receiverUrl
	 *            the receiver url
	 * @param message
	 *            the message
	 * @param tag
	 *            the tag
	 */
	public void sendAsync(final byte[] zmqType, final String token,
			final URI receiverUrl, final byte[] message, final String tag) {
		final String senderUrl = super.getAddress().toString();
		ThreadPool.getPool().execute(new Runnable() {
			@Override
			public void run() {
				final String addr = receiverUrl.toString().replaceFirst(
						"zmq:/?/?", "");
				final Socket socket = ZMQ.getSocket(org.zeromq.ZMQ.PUSH);
				LOG.warning("trying to connect to:" + addr);
				try {
					socket.connect(addr);
					socket.send(zmqType, org.zeromq.ZMQ.SNDMORE);
					socket.send(senderUrl, org.zeromq.ZMQ.SNDMORE);
					socket.send(token, org.zeromq.ZMQ.SNDMORE);
					socket.send(message, 0);
					
				} catch (final Exception e) {
					LOG.log(Level.WARNING, "Failed to send JSON through ZMQ", e);
				}
				socket.setTCPKeepAlive(-1);
				socket.setLinger(-1);
				socket.close();
			}
		});
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.almende.eve.transport.Transport#send(java.net.URI,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public void send(URI receiverUri, String message, String tag)
			throws IOException {
		sendAsync(ZMQ.NORMAL, TokenStore.create().toString(), receiverUri,
				message.getBytes(), tag);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.almende.eve.transport.Transport#send(java.net.URI, byte[],
	 * java.lang.String)
	 */
	@Override
	public void send(final URI receiverUri, final byte[] message,
			final String tag) throws IOException {
		sendAsync(ZMQ.NORMAL, TokenStore.create().toString(), receiverUri,
				message, tag);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.almende.eve.transport.Transport#connect()
	 */
	@Override
	public void connect() throws IOException {
		if (listeningThread != null) {
			disconnect();
		}
		listen();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.almende.eve.transport.Transport#disconnect()
	 */
	@Override
	public void disconnect() {
		doDisconnect = true;
		listeningThread.interrupt();
	}
	
	/**
	 * Gets the request.
	 * 
	 * @param socket
	 *            the socket
	 * @return the request
	 */
	private ByteBuffer[] getRequest(final Socket socket) {
		final byte[] res = socket.recv();
		final ByteBuffer[] result = new ByteBuffer[4];
		if (res != null) {
			result[0] = ByteBuffer.wrap(res);
			result[1] = ByteBuffer.wrap(socket.recv());
			result[2] = ByteBuffer.wrap(socket.recv());
			result[3] = ByteBuffer.wrap(socket.recv());
		}
		return result;
		
	}
	
	/**
	 * process an incoming zmq message.
	 * If the message contains a valid JSON-RPC request or response,
	 * the message will be processed.
	 * 
	 */
	public void listen() {
		listeningThread = new Thread(new Runnable() {
			@Override
			public void run() {
				Socket socket = ZMQ.getSocket(org.zeromq.ZMQ.PULL);
				socket.bind(zmqUrl);
				while (true) {
					try {
						final ByteBuffer[] msg = getRequest(socket);
						
						if (msg[0] != null) {
							handleMsg(msg);
							continue;
						}
						if (doDisconnect) {
							socket.disconnect(zmqUrl);
							doDisconnect = false;
							return;
						}
					} catch (final Exception e) {
						LOG.log(Level.SEVERE, "Caught error:", e);
					}
				}
			}
		});
		listeningThread.start();
	}
	
	/**
	 * Handle msg.
	 * 
	 * @param msg
	 *            the msg
	 * @throws ClassNotFoundException
	 *             the class not found exception
	 * @throws InstantiationException
	 *             the instantiation exception
	 * @throws IllegalAccessException
	 *             the illegal access exception
	 * @throws InvocationTargetException
	 *             the invocation target exception
	 * @throws NoSuchMethodException
	 *             the no such method exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws URISyntaxException
	 */
	private void handleMsg(final ByteBuffer[] msg)
			throws ClassNotFoundException, InstantiationException,
			IllegalAccessException, InvocationTargetException,
			NoSuchMethodException, IOException, URISyntaxException {
		
		// Receive
		// ZMQ.NORMAL|senderUrl|tokenJson|body
		// ZMQ.HANDSHAKE|senderUrl|tokenJson|timestamp
		// ZMQ.HANDSHAKE_RESPONSE|senderUrl|tokenJson|null
		
		final URI senderUrl = new URI(new String(msg[1].array()));
		final TokenRet token = JOM.getInstance().readValue(msg[2].array(),
				TokenRet.class);
		final String body = new String(msg[3].array());
		final String key = senderUrl + ":" + token.getToken();
		
		if (Arrays.equals(msg[0].array(), ZMQ.HANDSHAKE)) {
			// Reply token corresponding to timestamp.
			final String res = TokenStore.get(body);
			sendAsync(ZMQ.HANDSHAKE_RESPONSE, res, senderUrl, res.getBytes(),
					null);
			return;
		} else if (Arrays.equals(msg[0].array(), ZMQ.HANDSHAKE_RESPONSE)) {
			// post response to callback for handling by other thread
			AsyncCallback<String> callback = callbacks.pull(key);
			if (callback != null) {
				callback.onSuccess(body);
			} else {
				LOG.warning("Received ZMQ.HANDSHAKE_RESPONSE for unknown handshake..."
						+ senderUrl + " : " + token);
			}
			return;
		} else {
			final ObjectCache sessionCache = ObjectCache.get("ZMQSessions");
			if (!sessionCache.containsKey(key) && doesAuthentication) {
				SyncCallback<String> callback = new SyncCallback<String>();
				callbacks.push(key, "", callback);
				sendAsync(ZMQ.HANDSHAKE, token.toString(), senderUrl, token
						.getTime().getBytes(), null);
				
				String retToken = null;
				try {
					retToken = callback.get();
				} catch (Exception e) {
				}
				if (token.getToken().equals(retToken)) {
					sessionCache.put(key, true);
				} else {
					LOG.warning("Failed to complete handshake!");
					return;
				}
			}
		}
		
		if (body != null) {
			super.getHandle().get().receive(body, senderUrl, null);
		}
	}
	
}
