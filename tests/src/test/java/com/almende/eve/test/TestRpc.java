/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.test;

import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.junit.Test;

import com.almende.eve.capabilities.handler.SimpleHandler;
import com.almende.eve.protocol.jsonrpc.JSONRpcProtocol;
import com.almende.eve.protocol.jsonrpc.JSONRpcProtocolBuilder;
import com.almende.eve.protocol.jsonrpc.JSONRpcProtocolConfig;
import com.almende.eve.protocol.jsonrpc.annotation.Access;
import com.almende.eve.protocol.jsonrpc.annotation.AccessType;
import com.almende.eve.protocol.jsonrpc.annotation.Name;
import com.almende.eve.protocol.jsonrpc.annotation.Namespace;
import com.almende.eve.protocol.jsonrpc.formats.JSONRequest;
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
		final ObjectNode params = new JSONRpcProtocolConfig();
		
		final JSONRpcProtocol protocol = new JSONRpcProtocolBuilder().withConfig(params).withHandle(
				new SimpleHandler<Object>(new MyClass())).build();
		
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
		Object request = new JSONRequest("testMe", parms, callback);
		
		// transport
		Object response = protocol.invoke(request,
				URI.create("local://me"));
		// transport
		protocol.invoke(response, URI.create("local://me"));
		
		request = new JSONRequest("test.testMe", parms, callback);
		
		// transport
		response = protocol.invoke(request,
						URI.create("local://me"));
		// transport
		protocol.invoke(response, URI.create("local://me"));
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
	
		/**
		 * The Class MyClass.
		 */
		@Access(AccessType.PUBLIC)
		class MySubClass {
			
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
		
		/**
		 * Gets the sub.
		 *
		 * @return the sub
		 */
		@Namespace("test")
		public MySubClass getSub(){
			return new MySubClass();
		}
		
	}
	
}
