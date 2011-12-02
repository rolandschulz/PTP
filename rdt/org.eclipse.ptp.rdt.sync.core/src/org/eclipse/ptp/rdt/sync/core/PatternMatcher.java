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

import org.eclipse.ui.IMemento;

/**
 * Interface for a class that tests strings against some pattern. This pattern could be a regular expression or some other
 * property of interest.
 *
 *
 */
public abstract class PatternMatcher {
	private static final String ATTR_CLASS_NAME = "class-name"; //$NON-NLS-1$
	public abstract boolean match(String candidate);
//	public static abstract PatternMatcher loadPattern(IMemento memento);
//	public PatternMatcher loadUnknownPattern(IMemento memento) {
//		String className = memento.getString(ATTR_CLASS_NAME);
//		assert (className != null);
//		Object patternMatcherClass = Class.forName(className);
//		((PatternMatcher) patternMatcherClass).loadPattern(memento);
//	}
}
