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

import org.eclipse.core.resources.IResource;
import org.eclipse.ptp.internal.rdt.sync.core.messages.Messages;
import org.osgi.service.prefs.Preferences;

/**
 * Class for matching a string against a regular expression.
 * Currently, we simply use java's regex engine.
 */
public class RegexResourceMatcher extends ResourceMatcher {
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
	public RegexResourceMatcher(String r) throws PatternSyntaxException {
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
	public boolean match(IResource candidate) {
		if (candidate == null) {
			return false;
		}
		Matcher m = pattern.matcher(candidate.getProjectRelativePath().toOSString());
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
		return regex.hashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof RegexResourceMatcher)) {
			return false;
		}
		RegexResourceMatcher other = (RegexResourceMatcher) obj;
		if (!regex.equals(other.regex)) {
			return false;
		}
		return true;
	}

	/**
	 * Place needed data for recreating inside the preference node
	 */
	@Override
	public void saveMatcher(Preferences prefRootNode) {
		super.saveMatcher(prefRootNode);
		prefRootNode.put(ATTR_REGEX, regex);
	}
	
	/**
	 * Recreate instance from preference node
	 * 
	 * @param preference node
	 * @return the recreated instance
	 * @throws NoSuchElementException
	 * 				if expected data is not in the preference node.
	 */
	public static ResourceMatcher loadMatcher(Preferences prefRootNode) throws NoSuchElementException {
		String r = prefRootNode.get(ATTR_REGEX, null);
		if (r == null) {
			throw new NoSuchElementException(Messages.RegexResourceMatcher_0);
		}
		return new RegexResourceMatcher(r);
	}
}
