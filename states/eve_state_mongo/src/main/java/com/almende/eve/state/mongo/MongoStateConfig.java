/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.state.mongo;

import java.util.logging.Logger;

import com.almende.eve.state.StateConfig;
import com.almende.util.jackson.JOM;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class MongoStateConfig.
 */
public class MongoStateConfig extends StateConfig {
        private static final Logger     LOG             = Logger.getLogger(MongoStateConfig.class
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
	 *            
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
                            server.set( "host", this.get("host") );
                        } else {
                            server.put( "host", "localhost" );
                        }
                        
                        if(this.has( "port" )) {
                            server.set( "port", this.get("port") );
                        } else {
                            server.put( "port", 27017 );
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
}
