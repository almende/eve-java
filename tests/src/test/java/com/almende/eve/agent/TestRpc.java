/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.agent;

import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.junit.Test;

import com.almende.eve.capabilities.CapabilityFactory;
import com.almende.eve.capabilities.handler.SimpleHandler;
import com.almende.eve.transform.rpc.RpcTransform;
import com.almende.eve.transform.rpc.annotation.Access;
import com.almende.eve.transform.rpc.annotation.AccessType;
import com.almende.eve.transform.rpc.annotation.Name;
import com.almende.util.callback.AsyncCallback;
import com.almende.util.jackson.JOM;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class TestRpc.
 */
public class TestRpc extends TestCase {
	private static final Logger	LOG	= Logger.getLogger(TestRpc.class.getName());
	
	/**
	 * Test me.
	 */
	@Test
	public void testRpc() {
		final ObjectNode params = JOM.createObjectNode();
		params.put("class", "com.almende.eve.transform.rpc.RpcService");
		
		final RpcTransform transform = CapabilityFactory.get(params,
				new SimpleHandler<Object>(new MyClass()), RpcTransform.class);
		
		final AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {
			
			@Override
			public void onSuccess(final Boolean result) {
				LOG.warning("Success!");
				assertTrue(result);
			}
			
			@Override
			public void onFailure(final Exception exception) {
				LOG.log(Level.WARNING, "Fail:", exception);
				fail();
			}
			
		};
		
		final ObjectNode parms = JOM.createObjectNode();
		parms.put("parm", true);
		final Object request = transform.buildMsg("testMe", parms, callback);
		
		// transport
		final Object response = transform.invoke(request,
				URI.create("local://me"));
		// transport
		transform.invoke(response, URI.create("local://me"));
		
	}
	
	/**
	 * The Class MyClass.
	 */
	@Access(AccessType.PUBLIC)
	public class MyClass {
		
		/**
		 * Test me.
		 * 
		 * @param test
		 *            the test
		 * @return the boolean
		 */
		public Boolean testMe(@Name("parm") final Boolean test) {
			return test;
		}
		
	}
}
