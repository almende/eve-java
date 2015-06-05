/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.util;


/**
 * The Class ClassUtil.
 */
public final class ClassUtil {
	
	/**
	 * Instantiates a new class util.
	 */
	private ClassUtil() {
	};
	
	/**
	 * Check if checkClass has implemented interfaceClass.
	 * 
	 * @param checkClass
	 *            the check class
	 * @param interfaceClass
	 *            the interface class
	 * @return true, if successful
	 */
	public static boolean hasInterface(final Class<?> checkClass,
			final Class<?> interfaceClass) {
		final String name = interfaceClass.getName();
		Class<?> s = checkClass;
		while (s != null) {
			final Class<?>[] interfaces = s.getInterfaces();
			for (final Class<?> i : interfaces) {
				if (i.getName().equals(name)) {
					return true;
				}
				if (hasInterface(s, i)) {
					return true;
				}
			}
			
			s = s.getSuperclass();
		}
		
		return false;
	}
	
	/**
	 * Check if checkClass extends superClass.
	 * 
	 * @param checkClass
	 *            the check class
	 * @param superClass
	 *            the super class
	 * @return true, if successful
	 */
	public static boolean hasSuperClass(final Class<?> checkClass,
			final Class<?> superClass) {
		// TODO: replace with return (checkClass instanceof superClass); ?
		final String name = superClass.getName();
		Class<?> s = (checkClass != null) ? checkClass.getSuperclass() : null;
		while (s != null) {
			if (s.getName().equals(name)) {
				return true;
			}
			s = s.getSuperclass();
		}
		
		return false;
	}
}
