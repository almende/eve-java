/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.transport.amqp;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.codec.binary.Base64;

import com.almende.eve.capabilities.handler.Handler;
import com.almende.eve.transport.AbstractTransport;
import com.almende.eve.transport.Receiver;
import com.almende.eve.transport.amqp.AmqpTransportBuilder.AmqpService;
import com.almende.eve.transport.envelop.JSONEnvelop;
import com.almende.util.URIUtil;
import com.almende.util.callback.AsyncCallback;
import com.almende.util.threads.ThreadPool;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

/**
 * The Class AmqpTransport.
 */
public class AmqpTransport extends AbstractTransport {
	private static final Logger	LOG			= Logger.getLogger(AmqpTransport.class
													.getName());
	private ConnectionFactory	factory		= null;
	private Connection			connection	= null;
	private Channel				channel		= null;
	private String				myId		= "";

	/**
	 * Instantiates a new AMQP transport.
	 *
	 * @param config
	 *            the config
	 * @param newHandle
	 *            the new handle
	 * @param amqpService
	 *            the amqp service
	 */
	public AmqpTransport(AmqpTransportConfig config,
			Handler<Receiver> newHandle, AmqpService amqpService) {
		super(URIUtil.create("amqp:" + config.getId()), newHandle, amqpService,
				config);
		myId = config.getId();
		factory = new ConnectionFactory();
		try {
			factory.setUri(config.getHostUri());
		} catch (KeyManagementException | NoSuchAlgorithmException
				| URISyntaxException e) {
			LOG.log(Level.WARNING, "AMQP initialisation problem", e);
		}

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
		if (channel != null && channel.isOpen()) {
			final String to = receiverUri.getRawSchemeSpecificPart();
			final String msg = JSONEnvelop.wrapAsString(myId, to, message);
			LOG.warning("Sending '" + msg + "' to:" + to);
			channel.basicPublish("", to, null, msg.getBytes());
		} else {
			throw new IOException("Amqp transport not connected!");
		}
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
		connection = factory.newConnection();
		channel = connection.createChannel();
		channel.queueDeclare(myId, true, true, true, null);

		Consumer consumer = new DefaultConsumer(channel) {
			@Override
			public void handleDelivery(final String consumerTag,
					final Envelope envelope,
					final AMQP.BasicProperties properties, final byte[] body)
					throws IOException {
				final String message = new String(body, "UTF-8");
				final JSONEnvelop.Envelop res = JSONEnvelop.unwrap(message);
				if (myId.equals(res.getTo())) {
					ThreadPool.getPool().execute(new Runnable() {
						@Override
						public void run() {
							getHandle().get().receive(res.getMessage(),
									URIUtil.create("amqp:" + res.getFrom()),
									null);
						}
					});
				}
			}
		};
		channel.basicConsume(myId, true, consumer);
	}

	/*
	 * (non-Javadoc)
	 * @see com.almende.eve.transport.Transport#disconnect()
	 */
	@Override
	public void disconnect() {
		try {
			channel.close();
			channel = null;

			connection.close();
			connection = null;
		} catch (IOException e) {
			// ignore
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.almende.eve.transport.Transport#getProtocols()
	 */
	@Override
	public List<String> getProtocols() {
		return Arrays.asList("amqp");
	}

}
