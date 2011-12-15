/*******************************************************************************
 * Copyright (c) 2011 Oak Ridge National Laboratory and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Eblen - initial implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.sync.core;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.eclipse.ptp.rdt.sync.core.messages.Messages;
import org.eclipse.ui.IMemento;

/**
 * Abstract class to be inherited to support various ways of testing strings.
 * Subclasses must implement either a public static "loadPattern" method that takes a memento or a default constructor to
 * support persistence. Otherwise, calling PatternMatcher.loadPattern(IMemento) for a saved instance will throw an exception.
 *
 *
 */
public abstract class PatternMatcher {
	private static final String ATTR_CLASS_NAME = "class-name"; //$NON-NLS-1$
	public abstract boolean match(String candidate);
	public abstract String toString();
	public abstract boolean equals(Object o);
	public abstract int hashCode();
	
	/**
	 * Save pattern to the given memento. Subclasses may override, first calling super.savePattern(), if they need to save
	 * additional data beyond the class type.
	 * @param memento
	 */
	public void savePattern(IMemento memento) {
		memento.putString(ATTR_CLASS_NAME, this.getClass().getName());
	}

	/**
	 * Restore a pattern matcher from a given memento. Supporting the loading of subclasses requires reflection, which makes
	 * this code somewhat involved because it must handle several possible exceptions.
	 * 
	 * @param memento of pattern matcher to restore
	 * @return new pattern matcher instance
	 *
	 * @throws InvocationTargetException
	 * 					if an exception occurs inside subclass's static "loadPattern" method.
	 * @throws NoSuchMethodException
	 * 					if subclass does not contain either a static "loadPattern" method or a default constructor.
	 * 					if the class name is not in the memento, is not known, or is not a PatternMatcher.
	 */
	public static PatternMatcher loadPattern(IMemento memento) throws InvocationTargetException, NoSuchMethodException, ClassNotFoundException {
		String className = memento.getString(ATTR_CLASS_NAME);
		if (className == null) {
			throw new ClassNotFoundException(Messages.PatternMatcher_0);
		}

		// Sanity check on the retrieved class
		if (!PatternMatcher.class.isAssignableFrom(Class.forName(className)) ||
				Modifier.isAbstract(Class.forName(className).getModifiers())) {
			throw new ClassNotFoundException(Messages.PatternMatcher_1);
		}

		// First, try to invoke the subclass's "loadPattern" method
		boolean hasMethod = true;
		Method subClassMethod = null;
		try {
			subClassMethod = Class.forName(className).getDeclaredMethod("loadPattern", IMemento.class); //$NON-NLS-1$
		} catch (SecurityException e) {
			hasMethod = false; // Treat as if method does not exist
		} catch (NoSuchMethodException e) {
			hasMethod = false;
		}
		
		if (hasMethod && subClassMethod != null) {
			try {
				return (PatternMatcher) subClassMethod.invoke(Class.forName(className), memento);
			} catch (IllegalArgumentException e) {
				assert(false); // This should never happen
			} catch (IllegalAccessException e) {
				// Treat as if method does not exist
			}
		}

		// Next, try to instantiate from subclass's default constructor (code is very similar to the above).
		hasMethod = true;
		Constructor<? extends Object> subClassConstructor = null;
		try {
			subClassConstructor = Class.forName(className).getConstructor();
		} catch (SecurityException e) {
			hasMethod = false; // Treat as if method does not exist
		} catch (NoSuchMethodException e) {
			hasMethod = false;
		}
		
		if (hasMethod && subClassConstructor != null) {
			try {
				return (PatternMatcher) subClassConstructor.newInstance();
			} catch (IllegalArgumentException e) {
				assert(false); // This should never happen
			} catch (InstantiationException e) {
				// Treat as if method does not exist
			} catch (IllegalAccessException e) {
				// Treat as if method does not exist
			}
		}
		
		throw new NoSuchMethodException(Messages.PatternMatcher_2);
	}
}
