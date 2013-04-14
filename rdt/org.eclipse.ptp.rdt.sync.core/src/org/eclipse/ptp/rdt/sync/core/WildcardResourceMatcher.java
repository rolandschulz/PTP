/*******************************************************************************
 * Copyright (c) 2013 IBM Corp. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Beth Tibbitts - initial implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.sync.core;

import java.util.NoSuchElementException;
import java.util.regex.PatternSyntaxException;

import org.eclipse.core.resources.IResource;
import org.eclipse.ptp.internal.rdt.sync.core.messages.Messages;
import org.osgi.service.prefs.Preferences;

/**
 * Match a string against a simple wildcard (globbing pattern) syntax
 * e.g. *.o
 */
public class WildcardResourceMatcher extends ResourceMatcher {
	private static final String ATTR_WILDCARD = "wildcard"; //$NON-NLS-1$
	private final String wildcard;
	private final boolean DEBUG = false;

	/**
	 * Constructor
	 * 
	 * @param r
	 *            - the regular expression
	 * @throws PatternSyntaxException
	 *             if the pattern is invalid
	 */
	public WildcardResourceMatcher(String r) {
		if (r == null) {
			wildcard = ""; //$NON-NLS-1$
		} else {
			wildcard = r;
		}
	}

	/**
	 * Return whether the given string matches the wildcard pattern.
	 * 
	 * @param candidate
	 *            string
	 * @return whether the given source (its string name, presumably) matches the wildcard pattern
	 */
	@Override
	public boolean match(IResource candidate) {
		if (candidate == null) {
			return false;
		}
		if (DEBUG) {
			System.out.println("Wildcard match: candidate: " + candidate + " - " + candidate.getProjectRelativePath().toOSString()); //$NON-NLS-1$//$NON-NLS-2$
		}
		boolean result = matches(candidate.getProjectRelativePath().toOSString(), wildcard);

		if (DEBUG) {
			System.out.println("       to pattern: " + wildcard + " is: " + result); //$NON-NLS-1$//$NON-NLS-2$
		}
		return result;
	}

	/**
	 * Determine if a string matches a wildcard pattern
	 * 
	 * @param text
	 * @param wildcardPattern
	 * @return
	 */
	private boolean matches(String text, String wildcardPattern) {
		String remainder = null;
		int pos = wildcardPattern.indexOf('*');
		if (pos != -1) {
			remainder = wildcardPattern.substring(pos + 1);
			wildcardPattern = wildcardPattern.substring(0, pos);
		}

		if (wildcardPattern.length() > text.length()) {
			return false;
		}

		// first compute up to the first *
		for (int i = 0; i < wildcardPattern.length(); i++) {
			if (wildcardPattern.charAt(i) != '?' && !wildcardPattern.substring(i, i + 1).equalsIgnoreCase(text.substring(i, i + 1))) {
				return false;
			}
		}

		// recurse for the part after the first *, if any
		if (remainder == null) {
			return wildcardPattern.length() == text.length();
		} else {
			for (int i = wildcardPattern.length(); i <= text.length(); i++) {
				if (matches(text.substring(i), remainder)) {
					return true;
				}
			}
			return false;
		}
	}

	/**
	 * Represent a wildcard pattern textually as just the wildcard pattern string itself
	 * 
	 * @return the string
	 */
	@Override
	public String toString() {
		return wildcard;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return wildcard.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof WildcardResourceMatcher)) {
			return false;
		}
		WildcardResourceMatcher other = (WildcardResourceMatcher) obj;
		if (!wildcard.equals(other.wildcard)) {
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
		prefRootNode.put(ATTR_WILDCARD, wildcard);
	}

	/**
	 * Recreate instance from preference node
	 * 
	 * @param preference
	 *            node
	 * @return the recreated instance
	 * @throws NoSuchElementException
	 *             if expected data is not in the preference node.
	 */
	public static ResourceMatcher loadMatcher(Preferences prefRootNode) throws NoSuchElementException {
		String r = prefRootNode.get(ATTR_WILDCARD, null);
		if (r == null) {
			throw new NoSuchElementException(Messages.WildcardResourceMatcher_Wildcard_pattern_not_found_in_preference_node);
		}
		return new WildcardResourceMatcher(r);
	}
}