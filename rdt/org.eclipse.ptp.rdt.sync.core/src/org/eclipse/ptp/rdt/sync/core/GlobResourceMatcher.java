/*******************************************************************************
 * Copyright (c) 2013 Oak Ridge National Laboratory and others.
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
import org.eclipse.ptp.rdt.sync.core.messages.Messages;
import org.osgi.service.prefs.Preferences;

/**
 * Class for matching a string using a glob pattern.
 * Code uses Java regex internally, so the code is very similar to {@code RegexResourceMatcher}, except for the added function to
 * convert a glob to a Java regex before compiling the pattern.
 */
public class GlobResourceMatcher extends ResourceMatcher {
	private static final String ATTR_GLOB = "glob"; //$NON-NLS-1$
	private final String glob;
	private final Pattern pattern;
	
	/**
	 * Constructor
	 * Although PatternSyntaxException is unchecked, callers probably should catch this exception, especially if regular
	 * expressions are entered by users.
	 * 
	 * @param g - the glob
	 * @throws PatternSyntaxException
	 * 				if the pattern is invalid
	 */
	public GlobResourceMatcher(String g) throws PatternSyntaxException {
		if (g == null) {
			glob = ""; //$NON-NLS-1$
		} else {
			glob = g;
		}
		pattern = Pattern.compile(glob);
	}
	
	/**
	 * Return whether the given string matches the glob pattern.
	 * 
	 * @param candidate string
	 * @return whether the string matches the glob pattern
	 */
	public boolean match(IResource candidate) {
		if (candidate == null) {
			return false;
		}
		Matcher m = pattern.matcher(candidate.getProjectRelativePath().toOSString());
		return m.matches();
	}
	
	/**
	 * Represent a glob pattern textually as just the glob string itself
	 * @return the string
	 */
	public String toString() {
		return glob;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return glob.hashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof GlobResourceMatcher)) {
			return false;
		}
		GlobResourceMatcher other = (GlobResourceMatcher) obj;
		if (!glob.equals(other.glob)) {
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
		prefRootNode.put(ATTR_GLOB, glob);
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
		String g = prefRootNode.get(ATTR_GLOB, null);
		if (g == null) {
			throw new NoSuchElementException(Messages.GlobResourceMatcher_0);
		}
		return new GlobResourceMatcher(g);
	}
	
	public static String globToRegex(String glob) {
		String regex = glob;
		return regex;
	}
}
