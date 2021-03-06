/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.state.mongo;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import com.almende.eve.state.StateConfig;
import com.almende.util.jackson.JOM;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.MongoCredential;

/**
 * The Class MongoStateConfig.
 */
public class MongoStateConfig extends StateConfig {
	private static final Logger	LOG		= Logger.getLogger(MongoStateConfig.class
												.getName());
	private static final String	BUILDER	= MongoStateBuilder.class.getName();

	protected MongoStateConfig() {
		super();
	}

	/**
	 * Instantiates a new mongoDB state config.
	 *
	 * @return the mongo state config
	 */
	public static MongoStateConfig create() {
		final MongoStateConfig res = new MongoStateConfig();
		res.setBuilder(BUILDER);
		return res;
	}

	/**
	 * Instantiates a new mongoDB state config.
	 *
	 * @param node
	 *            the node
	 * @return the mongo state config
	 */
	public static MongoStateConfig decorate(final ObjectNode node) {
		final MongoStateConfig res = new MongoStateConfig();
		res.extend(node);
		return res;
	}

	/**
	 * Gets the key.
	 * 
	 * @return the key
	 */
	@JsonIgnore
	public String getKey() {
		return getHost() + ":" + getPort() + "/" + getDatabase() + "#"
				+ getCollection();
	}

	/**
	 * Sets the host.
	 * 
	 * @param host
	 *            the new host
	 * @deprecated Please use setHosts(hosts[])
	 */
	@Deprecated
	public void setHost(final String host) {
		this.put("host", host);
	}

	/**
	 * Gets the url.
	 * 
	 * @deprecated Please use getHosts()
	 * @return the url
	 */
	@Deprecated
	@JsonIgnore
	public String getHost() {
		if (this.has("host")) {
			return this.get("host").asText();
		}
		return "localhost";
	}

	/**
	 * Sets the port.
	 * 
	 * @param port
	 *            the new port
	 * @deprecated Please use setHosts(hosts[])
	 */
	@Deprecated
	public void setPort(final int port) {
		this.put("port", port);
	}

	/**
	 * Gets the port.
	 * 
	 * @deprecated Please use getHosts(hosts[])
	 * @return the port
	 */
	@Deprecated
	@JsonIgnore
	public int getPort() {
		if (this.has("port")) {
			return this.get("port").asInt();
		}
		return 27017;
	}

	/**
	 * Gets the host configurations.
	 * 
	 * @return the host configurations.
	 */
	public ArrayNode getHosts() {
		final JsonNode res = this.get("hosts");
		if (res == null && this.has("host")) {
			LOG.warning("This configuration is deprecated! Host should be renamed to 'hosts' and needs to be an array.");
			final ArrayNode other = JOM.createArrayNode();
			final ObjectNode server = JOM.createObjectNode();

			if (this.has("host")) {
				server.set("host", this.get("host"));
			} else {
				server.put("host", "localhost");
			}

			if (this.has("port")) {
				server.set("port", this.get("port"));
			} else {
				server.put("port", 27017);
			}

			other.add(server);

			return other;
		}
		return (ArrayNode) res;
	}

	/**
	 * Sets the list of host configurations.
	 * 
	 * @param hosts
	 *            the new transport config
	 */
	public void setHosts(final ArrayNode hosts) {
		this.set("hosts", hosts);
	}

	/**
	 * Sets the database.
	 * 
	 * @param database
	 *            the new database
	 */
	public void setDatabase(final String database) {
		this.put("database", database);
	}

	/**
	 * Gets the database.
	 * 
	 * @return the database
	 */
	public String getDatabase() {
		if (this.has("database")) {
			return this.get("database").asText();
		}
		return "eve";
	}

	/**
	 * Sets the collection.
	 * 
	 * @param collection
	 *            the new collection
	 */
	public void setCollection(final String collection) {
		this.put("collection", collection);
	}

	/**
	 * Gets the collection.
	 * 
	 * @return the collection
	 */
	public String getCollection() {
		if (this.has("collection")) {
			return this.get("collection").asText();
		}
		return "agents";
	}

	/**
	 * Gets the credentials for the mongoDb from the config
	 * 
	 * @return An ArrayNode of credentials. Where each credential is an
	 *         objectNode
	 */
	public ArrayNode getCredentials() {

		if (this.has("credentials")
				&& this.get("credentials") instanceof ArrayNode) {
			return (ArrayNode) this.get("credentials");
		}
		return null;
	}

	/**
	 * Sets the credentials to the current config.
	 *
	 * @param credentials
	 *            the new credentials
	 */
	public void setCredentials(ArrayNode credentials) {
		this.set("credentials", credentials);
	}

	/**
	 * If this agent has to have its own unique MongoClient instance, this
	 * field should
	 * be set in eve.yaml. Else it will load a single instance of
	 * MongoClient
	 *
	 * @return the mongo client label
	 */
	public String getMongoClientLabel() {

		if (this.has("mongoClientKey")) {
			return this.get("mongoClientKey").asText();
		}
		return null;
	}

	/**
	 * Sets the mongoClientLabel to the current config.
	 *
	 * @param mongoClientKey
	 *            the new mongo client label
	 */
	public void setMongoClientLabel(String mongoClientKey) {
		this.put("mongoClientKey", mongoClientKey);
	}

	/**
	 * Helper method to fetch the list of Mongocredentials based on the
	 * config.
	 *
	 * @return the mongo credentials
	 */
	@JsonIgnore
	public List<MongoCredential> getMongoCredentials() {

		ArrayNode credentials = getCredentials();
		List<MongoCredential> mongoCredentials = null;
		if (credentials != null) {
			mongoCredentials = new ArrayList<MongoCredential>(1);
			for (JsonNode credential : credentials) {
				if (credential.has("user") && credential.has("database")
						&& credential.has("password")) {
					mongoCredentials.add(MongoCredential
							.createMongoCRCredential(credential.get("user")
									.asText(), credential.get("database")
									.asText(), credential.get("password")
									.asText().toCharArray()));
				}
			}
		}
		return mongoCredentials;
	}
}
