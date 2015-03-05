/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.protocol.jsonrpc.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for parameter requirement.
 * 
 * @deprecated Please use @Optional instead! Will be removed in 3.2.x.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.PARAMETER)
@Deprecated
@Documented
public @interface Required {
	
	/**
	 * Value.
	 * 
	 * @return true, if successful
	 */
	boolean value();
}
