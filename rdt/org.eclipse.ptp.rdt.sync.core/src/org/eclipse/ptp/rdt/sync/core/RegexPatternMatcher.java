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

import java.util.NoSuchElementException;

import org.eclipse.ui.IMemento;

/**
 * Class for matching a string against a regular expression.
 *
 *
 */
public class RegexPatternMatcher extends PatternMatcher {
	private static final String ATTR_REGEX = "regex"; //$NON-NLS-1$
	private final String regex;
	
	public RegexPatternMatcher(String r) {
		regex = r;
	}
	
	public boolean match(String candidate) {
		return regexCompare(regex, candidate);
	}
	
	public String toString() {
		return regex;
	}
	
	/**
	 * Compare a regular expression to a candidate string. Since this functionality may be generally useful, this function
	 * is both public and static.
	 * 
	 * @param regex
	 * @param candidate
	 * @return whether or not candidate matches the regular expression
	 */
	public static boolean regexCompare(String regex, String candidate) {
		return candidate.startsWith(regex);
	}
	
	/**
	 * Place needed data for recreating inside the memento
	 */
	@Override
	public void savePattern(IMemento memento) {
		super.savePattern(memento);
		memento.putString(ATTR_REGEX, regex);
	}
	
	/**
	 * Recreate instance from memento
	 * 
	 * @param memento
	 * @return the recreated instance
	 * @throws NoSuchElementException
	 * 				if expected data is not in the memento.
	 */
	public static PatternMatcher loadPattern(IMemento memento) throws NoSuchElementException {
		String r = memento.getString(ATTR_REGEX);
		if (r == null) {
			throw new NoSuchElementException("Regex pattern not found in memento"); //$NON-NLS-1$
		}
		return new RegexPatternMatcher(memento.getString(ATTR_REGEX));
	}
}
