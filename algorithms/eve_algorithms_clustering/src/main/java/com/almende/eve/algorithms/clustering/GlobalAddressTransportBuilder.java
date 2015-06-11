/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.algorithms.clustering;

import java.net.URI;
import java.util.logging.Logger;

import com.almende.eve.capabilities.AbstractCapabilityBuilder;
import com.almende.eve.transport.Transport;
import com.almende.util.URIUtil;

/**
 * The Class LocalTransportBuilder.
 */
public class GlobalAddressTransportBuilder extends AbstractCapabilityBuilder<Transport> {
	private static final Logger					LOG			= Logger.getLogger(GlobalAddressTransportBuilder.class
																	.getName());

	@Override
	public Transport build() {
		final GlobalAddressTransportConfig config = GlobalAddressTransportConfig
				.decorate(getParams());
		final String id = config.getId();
		if (id == null) {
			LOG.warning("Parameter 'id' is required!");
			return null;
		}
		if (config.getRealAddressPattern() == null){
			LOG.warning("Parameter 'realAddressPattern' is required!");
			return null;
		}
		
		final String addr = "eve:" + config.getId();
		final URI address = URIUtil.create(addr);
		return new GlobalAddressTransport(address,null,null,getParams());
	}
}