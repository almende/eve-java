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
import java.util.List;
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
import com.almende.util.URIUtil;
import com.almende.util.callback.AsyncCallback;
import com.almende.util.callback.AsyncCallbackStore;
import com.almende.util.callback.SyncCallback;
import com.almende.util.jackson.JOM;
import com.almende.util.threads.ThreadPool;

/**
 * The Class ZmqTransport.
 */
public class ZmqTransport extends AbstractTransport {
	private static final Logger						LOG					= Logger.getLogger(ZmqTransport.class
																				.getCanonicalName());
	private final String							zmqUrl;
	private Thread									listeningThread;
	private boolean									doesAuthentication	= false;
	private boolean									doDisconnect		= false;
	private static final AsyncCallbackStore<String>	CALLBACKS			= new AsyncCallbackStore<String>(
																				"ZMQ");
	private final TokenStore						tokenstore			= new TokenStore();
	private final List<String>						protocols			= Arrays.asList("zmq");

	/**
	 * Instantiates a new zmq transport.
	 * 
	 * @param config
	 *            the config
	 * @param handle
	 *            the handle
	 * @param service
	 *            the service
	 */
	public ZmqTransport(final ZmqTransportConfig config,
			final Handler<Receiver> handle, final TransportService service) {
		super(config.getAddress(), handle, service, config);
		zmqUrl = super.getAddress().toString().replaceFirst("^zmq:/?/?", "");
		doesAuthentication = config.getDoAuthentication();
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
	 * @see com.almende.eve.transport.Transport#send(java.net.URI,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public void send(final URI receiverUri, final String message,
			final String tag) throws IOException {
		// Check and deliver local shortcut.
		if (sendLocal(receiverUri, message)) {
			return;
		}
		sendAsync(ZMQ.NORMAL, tokenstore.create().toString(), receiverUri,
				message.getBytes(), tag);
	}

	/*
	 * (non-Javadoc)
	 * @see com.almende.eve.transport.Transport#send(java.net.URI, byte[],
	 * java.lang.String)
	 */
	@Override
	public void send(final URI receiverUri, final byte[] message,
			final String tag) throws IOException {
		if (sendLocal(receiverUri, message)) {
			return;
		}
		sendAsync(ZMQ.NORMAL, tokenstore.create().toString(), receiverUri,
				message, tag);
	}

	/*
	 * (non-Javadoc)
	 * @see com.almende.eve.transport.Transport#connect()
	 */
	@Override
	public void connect() throws IOException {
		if (listeningThread != null) {
			return;
		}
		listen();
	}

	/*
	 * (non-Javadoc)
	 * @see com.almende.eve.transport.Transport#disconnect()
	 */
	@Override
	public void disconnect() {
		doDisconnect = true;
		listeningThread.interrupt();
	}

	/*
	 * (non-Javadoc)
	 * @see com.almende.eve.transport.Transport#getProtocols()
	 */
	@Override
	public List<String> getProtocols() {
		return protocols;
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
	 */
	public void listen() {
		listeningThread = new Thread(new Runnable() {
			@Override
			public void run() {
				final Socket socket = ZMQ.getSocket(org.zeromq.ZMQ.PULL);
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

		final URI senderUrl = URIUtil.parse(new String(msg[1].array()));
		final TokenRet token = JOM.getInstance().readValue(msg[2].array(),
				TokenRet.class);
		final String body = new String(msg[3].array());
		final String key = senderUrl + ":" + token.getToken();

		if (Arrays.equals(msg[0].array(), ZMQ.HANDSHAKE)) {
			// Reply token corresponding to timestamp.
			final String res = tokenstore.get(body);
			sendAsync(ZMQ.HANDSHAKE_RESPONSE, res, senderUrl, res.getBytes(),
					null);
			return;
		} else if (Arrays.equals(msg[0].array(), ZMQ.HANDSHAKE_RESPONSE)) {
			// post response to callback for handling by other thread
			final AsyncCallback<String> callback = CALLBACKS.get(key);
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
				final SyncCallback<String> callback = new SyncCallback<String>() {};
				CALLBACKS.put(key, "", callback);
				sendAsync(ZMQ.HANDSHAKE, token.toString(), senderUrl, token
						.getTime().getBytes(), null);

				String retToken = null;
				try {
					retToken = callback.get();
				} catch (final Exception e) {}
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
