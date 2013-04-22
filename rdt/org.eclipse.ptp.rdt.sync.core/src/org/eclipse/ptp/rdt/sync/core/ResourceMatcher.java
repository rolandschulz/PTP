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

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.IResource;
import org.eclipse.ptp.internal.rdt.sync.core.messages.Messages;
import org.osgi.service.prefs.Preferences;

/**
 * Abstract class to be inherited to support various ways of testing strings.
 * Subclasses must implement either a public static "loadMatcher" method that takes a preference node or a default constructor to
 * support persistence. Otherwise, calling PatternMatcher.loadMatcher(Preferences) for a saved instance will throw an exception.
 * 
 * @since 3.0
 */
public abstract class ResourceMatcher {
	private static final String ATTR_CLASS_NAME = "class-name"; //$NON-NLS-1$

	public abstract boolean match(IResource candidate);

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public abstract String toString();

	/**
	 * Returns a string describing the type of matcher
	 * 
	 * @return type string
	 */
	public abstract String getType();

	/**
	 * Create a clone of the matcher using the new pattern
	 * 
	 * @param pattern
	 *            pattern to use for the new matcher
	 * @return copy of matcher
	 */
	public abstract ResourceMatcher clone(String pattern);

	/*
	 * Ideally, these two methods should identify two matchers as equal if and only if the set of matching resources is identical.
	 * This is used to remove duplicate matchers from the user's list. Matchers should be considered unequal if it is unknown
	 * whether they match the same set of resources. (This can happen with regular expressions, for example.)
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public abstract boolean equals(Object o);

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public abstract int hashCode();

	/**
	 * Save pattern to the given Preference node. Subclasses may override, first calling super.savePattern(), if they need to save
	 * additional data beyond the class type.
	 * 
	 * @param preference
	 *            node
	 */
	public void saveMatcher(Preferences prefRootNode) {
		prefRootNode.put(ATTR_CLASS_NAME, this.getClass().getName());
	}

	/**
	 * Restore a pattern matcher from a given preference node.
	 * Supporting the loading of subclasses requires reflection, which makes this code somewhat involved because it must handle
	 * several possible exceptions.
	 * 
	 * @param preference
	 *            node of pattern matcher to restore
	 * @return new pattern matcher instance
	 * 
	 * @throws InvocationTargetException
	 *             if an exception occurs inside subclass's static "loadMatcher" method.
	 * @throws ParserConfigurationException
	 *             for various problems while parsing and attempting to instantiate the matcher class.
	 */
	public static ResourceMatcher loadMatcher(Preferences prefRootNode) throws InvocationTargetException,
			ParserConfigurationException {
		String className = prefRootNode.get(ATTR_CLASS_NAME, null);
		if (className == null) {
			throw new ParserConfigurationException(Messages.ResourceMatcher_1);
		}

		try {
			// Sanity check on the retrieved class
			if (!ResourceMatcher.class.isAssignableFrom(Class.forName(className))
					|| Modifier.isAbstract(Class.forName(className).getModifiers())) {
				throw new ParserConfigurationException(Messages.ResourceMatcher_2);
			}

			// First, try to invoke the subclass's "loadMatcher" method
			boolean hasMethod = true;
			Method subClassMethod = null;
			try {
				subClassMethod = Class.forName(className).getDeclaredMethod("loadMatcher", Preferences.class); //$NON-NLS-1$
			} catch (SecurityException e) {
				hasMethod = false; // Treat as if method does not exist
			} catch (NoSuchMethodException e) {
				hasMethod = false;
			}

			if (hasMethod && subClassMethod != null) {
				try {
					return (ResourceMatcher) subClassMethod.invoke(Class.forName(className), prefRootNode);
				} catch (IllegalArgumentException e) {
					assert (false); // This should never happen
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
					return (ResourceMatcher) subClassConstructor.newInstance();
				} catch (IllegalArgumentException e) {
					assert (false); // This should never happen
				} catch (InstantiationException e) {
					// Treat as if method does not exist
				} catch (IllegalAccessException e) {
					// Treat as if method does not exist
				}
			}
		} catch (ClassNotFoundException e) {
			throw new ParserConfigurationException(Messages.ResourceMatcher_0 + className);
		}

		throw new ParserConfigurationException(Messages.ResourceMatcher_3);
	}
}