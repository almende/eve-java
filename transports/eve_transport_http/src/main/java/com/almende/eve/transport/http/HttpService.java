/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.transport.http;

import com.almende.eve.capabilities.handler.Handler;
import com.almende.eve.transport.Transport;
import com.almende.eve.transport.TransportService;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * The Class HttpService.
 */
public class HttpService implements TransportService {
	
	/**
	 * Gets the instance by params.
	 * 
	 * @param params
	 *            the params
	 * @return the instance by params
	 */
	public static HttpService getInstanceByParams(final JsonNode params) {
		//TODO: return an instance mapped by servlet URL part. Create such Servlet and launch servlet container if needed.
		//In the case the servlet pre-exists (through web.xml) we need to have a mechanism to let the servlet find us back.nu
		return null;
	}
	
	/* (non-Javadoc)
	 * @see com.almende.eve.capabilities.Capability#get(com.fasterxml.jackson.databind.JsonNode, com.almende.eve.capabilities.handler.Handler, java.lang.Class)
	 */
	@Override
	public <T, V> T get(JsonNode params, Handler<V> handle, Class<T> type) {
		// TODO Auto-generated method stub
		return null;
	}
	
	/* (non-Javadoc)
	 * @see com.almende.eve.transport.TransportService#delete(com.almende.eve.transport.Transport)
	 */
	@Override
	public void delete(Transport instance) {
		// TODO Auto-generated method stub
		
	}
	
}
