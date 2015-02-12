/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.dht;

/**
 * The Class Constants.
 */
public class Constants {

	/** The Constant BITLENGTH. */
	public static final int		BITLENGTH	= 160;

	/** The Constant K. */
	public static final int		K			= 20;

	/** The Constant A. */
	public static final int		A			= 5;

	/** The Constant EXPIRE. */
	public static final long	EXPIRE		= 24 * 60 * 60 * 1000 + ((int)Math.floor(Math.random()*1000));

	/** The Constant REFRESH. */
	public static final long	REFRESH		= 60 * 60 * 1000 - ((int)Math.floor(Math.random()*1000));

}
