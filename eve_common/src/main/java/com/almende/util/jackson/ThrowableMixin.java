/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.util.jackson;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * The Class ThrowableMixin.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS,
include = JsonTypeInfo.As.WRAPPER_ARRAY)
abstract public class ThrowableMixin {
	
	/** The cause. */
	@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS,
			include = JsonTypeInfo.As.WRAPPER_ARRAY)
	Throwable	cause;
}
