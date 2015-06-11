/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.algorithms.clustering;

import java.net.URI;
import java.util.Map;

import com.almende.eve.agent.Agent;
import com.almende.eve.protocol.jsonrpc.annotation.Access;
import com.almende.eve.protocol.jsonrpc.annotation.AccessType;

/**
 * The Class GlobalAddressMapperAgent.
 */
@Access(AccessType.PUBLIC)
public class GlobalAddressMapperAgent extends Agent {
	
	/**
	 * Gets the mappings.
	 *
	 * @return the mappings
	 */
	public Map<String,URI> getMappings(){
		return GlobalAddressMapper.get();
	}
}
