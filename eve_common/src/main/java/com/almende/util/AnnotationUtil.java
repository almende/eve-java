/**
 * @file AnnotationUtil.java
 * 
 *       AnnotationUtil is a utility to get all annotations of a class, its
 *       methods,
 *       and the method parameters. Returned annotations include all annotations
 *       of
 *       the classes interfaces and super classes.
 *       Requested classes are cached, so requesting a classes annotations
 *       repeatedly
 *       is fast.
 * 
 *       Example usage:
 * 
 *       AnnotatedClass annotatedClass = AnnotationUtil.get(MyClass.class);
 *       List<AnnotatedMethod> methods = annotatedClass.getMethods();
 *       for (AnnotatedMethod method : methods) {
 *       System.out.println("Method: " + method.getName());
 *       List<Annotation> annotations = method.getAnnotations();
 *       for (Annotation annotation : annotations) {
 *       System.out.println("    Annotation: " + annotation.toString());
 *       }
 *       }
 * 
 * @brief
 *        AnnotationUtil is a utility to retrieve merged annotations from a
 *        class
 *        including all its superclasses and interfaces.
 * 
 * @license
 *          Licensed under the Apache License, Version 2.0 (the "License"); you
 *          may not
 *          use this file except in compliance with the License. You may obtain
 *          a copy
 *          of the License at
 * 
 *          http://www.apache.org/licenses/LICENSE-2.0
 * 
 *          Unless required by applicable law or agreed to in writing, software
 *          distributed under the License is distributed on an "AS IS" BASIS,
 *          WITHOUT
 *          WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 *          the
 *          License for the specific language governing permissions and
 *          limitations under
 *          the License.
 * 
 *          Copyright (c) 2013 Almende B.V.
 * 
 * @author Jos de Jong, <jos@almende.org>
 * @date 2013-01-21
 */
package com.almende.util;

import java.lang.annotation.Annotation;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.WrongMethodTypeException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The Class AnnotationUtil.
 */
public final class AnnotationUtil {
	private static final Logger					LOG						= Logger.getLogger(AnnotationUtil.class
																				.getName());
	private static Map<String, AnnotatedClass>	cache					= new ConcurrentHashMap<String, AnnotatedClass>();
	
	private AnnotationUtil() {};
	
	/**
	 * Get all annotations of a class, methods, and parameters.
	 * Returned annotations include all annotations of the classes interfaces
	 * and super classes (excluding java.lang.Object).
	 * 
	 * @param clazz
	 *            the clazz
	 * @return annotatedClazz
	 */
	public static AnnotatedClass get(final Class<?> clazz) {
		AnnotatedClass annotatedClazz = cache.get(clazz.getName());
		if (annotatedClazz == null) {
			annotatedClazz = new AnnotatedClass(clazz);
			cache.put(clazz.getName(), annotatedClazz);
		}
		return annotatedClazz;
	}
	
	/**
	 * AnnotatedClass describes a class, its annotations, and its methods.
	 */
	public static class AnnotatedClass {
		
		/** The clazz. */
		private Class<?>					clazz		= null;
		
		//TODO: convert to Maps for indexed searches:
		/** The annotations. */
		private final List<Annotation>		annotations	= new ArrayList<Annotation>();
		
		/** The methods. */
		private final List<AnnotatedMethod>	methods		= new ArrayList<AnnotatedMethod>();
		
		/** The fields. */
		private final List<AnnotatedField>	fields		= new ArrayList<AnnotatedField>();
		
		/**
		 * Create a new AnnotatedClass.
		 *
		 * @param clazz
		 *            the clazz
		 */
		public AnnotatedClass(final Class<?> clazz) {
			this.clazz = clazz;
			merge(clazz);
		}
		
		/**
		 * Recursively merge a class into this AnnotatedClass.
		 * The method loops over all the classess interfaces and superclasses
		 * Methods with will be merged.
		 * 
		 * @param clazz
		 *            the clazz
		 * @param includeObject
		 *            if true, superclass java.lang.Object will
		 *            be included too.
		 */
		private void merge(final Class<?> clazz) {
			Class<?> c = clazz;
			while (c != null && c != Object.class) {
				// merge the annotations
				AnnotationUtil.merge(annotations, c.getDeclaredAnnotations());
				
				// merge the methods
				AnnotationUtil.merge(methods, c.getDeclaredMethods());
				AnnotationUtil.merge(fields, c.getDeclaredFields());
				
				// merge all interfaces and the superclasses of the interfaces
				for (final Class<?> i : c.getInterfaces()) {
					merge(i);
				}
				
				// ok now again for the superclass
				c = c.getSuperclass();
			}
		}
		
		/**
		 * Get the actual Java class described by this AnnotatedClass.
		 * 
		 * @return clazz
		 */
		public Class<?> getActualClass() {
			return clazz;
		}
		
		/**
		 * Get all methods including methods declared in superclasses.
		 * 
		 * @return methods
		 */
		public List<AnnotatedMethod> getMethods() {
			return methods;
		}
		
		/**
		 * Get all methods including methods declared in superclasses, filtered
		 * by name.
		 * 
		 * @param name
		 *            the name
		 * @return filteredMethods
		 */
		public List<AnnotatedMethod> getMethods(final String name) {
			final List<AnnotatedMethod> filteredMethods = new ArrayList<AnnotatedMethod>();
			for (final AnnotatedMethod method : methods) {
				if (method.getName().equals(name)) {
					filteredMethods.add(method);
				}
			}
			return filteredMethods;
		}
		
		/**
		 * Get all methods including methods declared in superclasses, filtered
		 * by annotation.
		 * 
		 * @param <T>
		 *            the generic type
		 * @param annotation
		 *            the annotation
		 * @return filteredMethods
		 */
		public <T> List<AnnotatedMethod> getAnnotatedMethods(
				final Class<T> annotation) {
			final List<AnnotatedMethod> filteredMethods = new ArrayList<AnnotatedMethod>();
			for (final AnnotatedMethod method : methods) {
				if (method.getAnnotation(annotation) != null) {
					filteredMethods.add(method);
				}
			}
			return filteredMethods;
		}
		
		/**
		 * Get all fields including fields declared in superclasses, filtered
		 * by annotation.
		 * 
		 * @param <T>
		 *            the generic type
		 * @param annotation
		 *            the annotation
		 * @return filteredMethods
		 */
		public <T> List<AnnotatedField> getAnnotatedFields(
				final Class<T> annotation) {
			final List<AnnotatedField> filteredFields = new ArrayList<AnnotatedField>();
			for (final AnnotatedField field : fields) {
				if (field.getAnnotation(annotation) != null) {
					filteredFields.add(field);
				}
			}
			return filteredFields;
		}
		
		/**
		 * Get all annotations defined on this class, its superclasses, and its
		 * interfaces.
		 * 
		 * @return annotations
		 */
		public List<Annotation> getAnnotations() {
			return annotations;
		}
		
		/**
		 * Get an annotation of this class by type.
		 * Returns null if not available.
		 * 
		 * @param <T>
		 *            the generic type
		 * @param type
		 *            the type
		 * @return annotation
		 */
		@SuppressWarnings("unchecked")
		public <T> T getAnnotation(final Class<T> type) {
			for (final Annotation annotation : annotations) {
				if (annotation.annotationType() == type) {
					return (T) annotation;
				}
			}
			return null;
		}
	}
	
	/**
	 * The Class AnnotatedField.
	 */
	public static class AnnotatedField {
		
		/** The field. */
		private Field					field		= null;
		
		/** The name. */
		private String					name		= null;
		
		/** The type. */
		private Type					type		= null;
		
		/** The annotations. */
		private final List<Annotation>	annotations	= new ArrayList<Annotation>();
		
		/**
		 * Instantiates a new annotated field.
		 * 
		 * @param field
		 *            the field
		 */
		public AnnotatedField(final Field field) {
			this.field = field;
			name = field.getName();
			type = field.getType();
			
			merge(field);
		}
		
		/**
		 * Merge a java method into this Annotated method.
		 * Annotations and parameter annotations will be merged.
		 * 
		 * @param field
		 *            the field
		 */
		private void merge(final Field field) {
			AnnotationUtil.merge(annotations, field.getDeclaredAnnotations());
		}
		
		/**
		 * Gets the field.
		 * 
		 * @return the field
		 */
		public Field getField() {
			return field;
		}
		
		/**
		 * Gets the name.
		 * 
		 * @return the name
		 */
		public String getName() {
			return name;
		}
		
		/**
		 * Gets the type.
		 * 
		 * @return the type
		 */
		public Type getType() {
			return type;
		}
		
		/**
		 * Gets the annotations.
		 * 
		 * @return the annotations
		 */
		public List<Annotation> getAnnotations() {
			return annotations;
		}
		
		/**
		 * Get an annotation of this field by type.
		 * Returns null if not available.
		 * 
		 * @param <T>
		 *            the generic type
		 * @param type
		 *            the type
		 * @return annotation
		 */
		@SuppressWarnings("unchecked")
		public <T> T getAnnotation(final Class<T> type) {
			for (final Annotation annotation : annotations) {
				if (annotation.annotationType() == type) {
					return (T) annotation;
				}
			}
			return null;
		}
	}
	
	/**
	 * AnnotatedMethod describes a method and its parameters.
	 */
	public static class AnnotatedMethod {
		
		/** The method. */
		private final Method				method;
		
		/** The name. */
		private String						name				= null;
		
		/** The return type. */
		private Class<?>					returnType			= null;
		
		/** The generic return type. */
		private Type						genericReturnType	= null;
		
		/** The annotations. */
		private final List<Annotation>		annotations			= new ArrayList<Annotation>();
		
		/** The parameters. */
		private final List<AnnotatedParam>	parameters			= new ArrayList<AnnotatedParam>();
		
		private MethodHandle				methodHandle;
		
		/**
		 * Instantiates a new annotated method.
		 * 
		 * @param method
		 *            the method
		 * @throws IllegalAccessException
		 *             the illegal access exception
		 */
		public AnnotatedMethod(final Method method)
				throws IllegalAccessException {
			this.method = method;
			method.setAccessible(true);
			name = method.getName();
			returnType = method.getReturnType();
			genericReturnType = method.getGenericReturnType();
			merge(method);
			
			if (Defines.HASMETHODHANDLES) {
				MethodType newType;
				if (Modifier.isStatic(method.getModifiers())) {
					newType = MethodType.genericMethodType(parameters.size(),
							method.isVarArgs());
				} else {
					newType = MethodType.genericMethodType(
							parameters.size() + 1, method.isVarArgs());
				}
				if (method.getReturnType() == Void.class) {
					newType = newType.changeReturnType(void.class);
				}
				try {
				methodHandle = new ConstantCallSite(MethodHandles.lookup()
						.unreflect(method).asType(newType)
						.asSpreader(Object[].class, newType.parameterCount()))
						.dynamicInvoker();
				} catch (WrongMethodTypeException e){
					final IllegalAccessException res = new IllegalAccessException();
					res.initCause(e);
					throw res;
				}
			}
		}
		
		/**
		 * Merge a java method into this Annotated method.
		 * Annotations and parameter annotations will be merged.
		 * 
		 * @param method
		 *            the method
		 */
		private void merge(final Method method) {
			// merge the annotations
			AnnotationUtil.merge(annotations, method.getDeclaredAnnotations());
			
			// merge the params
			final Annotation[][] params = method.getParameterAnnotations();
			final Class<?>[] types = method.getParameterTypes();
			final Type[] genericTypes = method.getGenericParameterTypes();
			for (int i = 0; i < params.length; i++) {
				if (i > parameters.size() - 1) {
					parameters.add(new AnnotatedParam(params[i], types[i],
							genericTypes[i]));
				} else {
					parameters.get(i).merge(params[i]);
				}
			}
		}
		
		/**
		 * Get the actual method as MethodHandler.
		 * 
		 * @return methodHandle
		 */
		public MethodHandle getMethodHandle() {
			return methodHandle;
		}
		
		/**
		 * Get the actual Java method described by this AnnotatedMethod.
		 * 
		 * @return method
		 */
		public Method getActualMethod() {
			return method;
		}
		
		/**
		 * Get the method name.
		 * 
		 * @return name
		 */
		public String getName() {
			return name;
		}
		
		/**
		 * Get the return type of the method.
		 * 
		 * @return returnType
		 */
		public Class<?> getReturnType() {
			return returnType;
		}
		
		/**
		 * Get the generic return type of the method.
		 * 
		 * @return genericType
		 */
		public Type getGenericReturnType() {
			return genericReturnType;
		}
		
		/**
		 * Get all annotations of this method, defined in all implementations
		 * and interfaces of the class.
		 * 
		 * @return annotations
		 */
		public List<Annotation> getAnnotations() {
			return annotations;
		}
		
		/**
		 * Get an annotation of this method by type.
		 * Returns null if not available.
		 * 
		 * @param <T>
		 *            the generic type
		 * @param type
		 *            the type
		 * @return annotation
		 */
		@SuppressWarnings("unchecked")
		public <T> T getAnnotation(final Class<T> type) {
			for (final Annotation annotation : annotations) {
				if (annotation.annotationType() == type) {
					return (T) annotation;
				}
			}
			return null;
		}
		
		/**
		 * Get all parameter annotations of this method, defined in all
		 * implementations and interfaces of the methods declaring class.
		 * 
		 * @return params
		 */
		public List<AnnotatedParam> getParams() {
			return parameters;
		}
	}
	
	/**
	 * AnnotatedParam describes all annotations of a parameter.
	 */
	public static class AnnotatedParam {
		
		/** The annotations. */
		private final List<Annotation>	annotations	= new ArrayList<Annotation>();
		
		/** The type. */
		private Class<?>				type		= null;
		
		/** The generic type. */
		private Type					genericType	= null;
		
		/**
		 * Instantiates a new annotated param.
		 */
		public AnnotatedParam() {
		}
		
		/**
		 * Instantiates a new annotated param.
		 * 
		 * @param annotations
		 *            the annotations
		 * @param type
		 *            the type
		 * @param genericType
		 *            the generic type
		 */
		public AnnotatedParam(final Annotation[] annotations,
				final Class<?> type, final Type genericType) {
			this.type = type;
			this.genericType = genericType;
			
			merge(annotations);
		}
		
		/**
		 * Merge.
		 * 
		 * @param annotations
		 *            the annotations
		 */
		private void merge(final Annotation[] annotations) {
			// merge the annotations
			AnnotationUtil.merge(this.annotations, annotations);
		}
		
		/**
		 * Get all annotations of this parameter, defined in all implementations
		 * and interfaces of the class.
		 * 
		 * @return annotations
		 */
		public List<Annotation> getAnnotations() {
			return annotations;
		}
		
		/**
		 * Get an annotation of this parameter by type.
		 * Returns null if not available.
		 * 
		 * @param <T>
		 *            the generic type
		 * @param type
		 *            the type
		 * @return annotation
		 */
		@SuppressWarnings("unchecked")
		public <T> T getAnnotation(final Class<T> type) {
			for (final Annotation annotation : annotations) {
				if (annotation.annotationType() == type) {
					return (T) annotation;
				}
			}
			return null;
		}
		
		/**
		 * Get the type of the parameter.
		 * 
		 * @return type
		 */
		public Class<?> getType() {
			return type;
		}
		
		/**
		 * Get the generic type of the parameter.
		 * 
		 * @return genericType
		 */
		public Type getGenericType() {
			return genericType;
		}
	}
	
	/**
	 * Merge an array with annotations (listB) into a list with
	 * annotations (listA).
	 * 
	 * @param listA
	 *            the list a
	 * @param listB
	 *            the list b
	 */
	private static void merge(final List<Annotation> listA,
			final Annotation[] listB) {
		for (final Annotation b : listB) {
			boolean found = false;
			for (final Annotation a : listA) {
				if (a.getClass() == b.getClass()) {
					found = true;
					break;
				}
			}
			if (!found) {
				listA.add(b);
			}
		}
	}
	
	/**
	 * Merge an array of methods (listB) into a list with method
	 * annotations (listA).
	 * 
	 * @param listA
	 *            the list a
	 * @param listB
	 *            the list b
	 * @throws IllegalAccessException
	 */
	private static void merge(final List<AnnotatedMethod> listA,
			final Method[] listB) {
		for (final Method b : listB) {
			AnnotatedMethod methodAnnotations = null;
			for (final AnnotatedMethod a : listA) {
				if (areEqual(a.method, b)) {
					methodAnnotations = a;
					break;
				}
			}
			
			if (methodAnnotations != null) {
				methodAnnotations.merge(b);
			} else {
				try {
					listA.add(new AnnotatedMethod(b));
				} catch (IllegalAccessException e) {
					LOG.log(Level.SEVERE, "Failed to obtain AnnotatedMethod:"
							+ b.getName(), e);
				}
			}
		}
	}
	
	/**
	 * Merge an array with annotations (listB) into a list with
	 * annotations (listA).
	 * 
	 * @param listA
	 *            the list a
	 * @param listB
	 *            the list b
	 */
	private static void merge(final List<AnnotatedField> listA,
			final Field[] listB) {
		for (final Field b : listB) {
			AnnotatedField fieldAnnotations = null;
			for (final AnnotatedField a : listA) {
				if (areEqual(a.field, b)) {
					fieldAnnotations = a;
					break;
				}
			}
			
			if (fieldAnnotations != null) {
				fieldAnnotations.merge(b);
			} else {
				listA.add(new AnnotatedField(b));
			}
		}
	}
	
	/**
	 * Test if two methods have equal names, return type, param count,
	 * and param types.
	 * 
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 * @return true, if successful
	 */
	private static boolean areEqual(final Method a, final Method b) {
		// http://stackoverflow.com/q/10062957/1262753
		if (!a.getName().equals(b.getName())) {
			return false;
		}
		if (a.getReturnType() != b.getReturnType()) {
			return false;
		}
		
		final Class<?>[] paramsa = a.getParameterTypes();
		final Class<?>[] paramsb = b.getParameterTypes();
		if (paramsa.length != paramsb.length) {
			return false;
		}
		for (int i = 0; i < paramsa.length; i++) {
			if (paramsa[i] != paramsb[i]) {
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Test if two fields have equal names and types.
	 * 
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 * @return true, if successful
	 */
	private static boolean areEqual(final Field a, final Field b) {
		// http://stackoverflow.com/q/10062957/1262753
		if (!a.getName().equals(b.getName())) {
			return false;
		}
		if (a.getType() != b.getType()) {
			return false;
		}
		return true;
	}
}
