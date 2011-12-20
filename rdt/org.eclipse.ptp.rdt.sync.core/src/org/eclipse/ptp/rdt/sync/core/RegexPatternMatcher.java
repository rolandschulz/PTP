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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.ptp.rdt.sync.core.messages.Messages;
import org.eclipse.ui.IMemento;

/**
 * Class for matching a string against a regular expression.
 * Currently, we simply use java's regex engine.
 *
 *
 */
public class RegexPatternMatcher extends PatternMatcher {
	private static final String ATTR_REGEX = "regex"; //$NON-NLS-1$
	private final String regex;
	private final Pattern pattern;
	
	/**
	 * Constructor
	 * Although PatternSyntaxException is unchecked, callers probably should catch this exception, especially if regular
	 * expressions are entered by users.
	 * 
	 * @param r - the regular expression
	 * @throws PatternSyntaxException
	 * 				if the pattern is invalid
	 */
	public RegexPatternMatcher(String r) throws PatternSyntaxException {
		if (r == null) {
			regex = ""; //$NON-NLS-1$
		} else {
			regex = r;
		}
		pattern = Pattern.compile(regex);
	}
	
	/**
	 * Return whether the given string matches the regex pattern.
	 * 
	 * @param candidate string
	 * @return whether the string matches the regex pattern
	 */
	public boolean match(String candidate) {
		Matcher m = pattern.matcher(candidate);
		return m.matches();
	}
	
	/**
	 * Represent a regex pattern textually as just the regex string itself
	 * @return the string
	 */
	public String toString() {
		return regex;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((regex == null) ? 0 : regex.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof RegexPatternMatcher)) {
			return false;
		}
		RegexPatternMatcher other = (RegexPatternMatcher) obj;
		if (regex == null) {
			if (other.regex != null) {
				return false;
			}
		} else if (!regex.equals(other.regex)) {
			return false;
		}
		return true;
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
			throw new NoSuchElementException(Messages.RegexPatternMatcher_0);
		}
		return new RegexPatternMatcher(memento.getString(ATTR_REGEX));
	}
}
