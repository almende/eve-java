package com.almende.util;

import com.almende.util.NamespaceUtil.CallTuple;


/**
 * @author Almende
 *
 */
public interface RPCCallCache {
	/**
	 * @param path
	 * @return Calltuple
	 */
	CallTuple getCallTuple(String path);
	
	/**
	 * @param path
	 * @param tuple
	 */
	void putCallTuple(String path, CallTuple tuple);
}
