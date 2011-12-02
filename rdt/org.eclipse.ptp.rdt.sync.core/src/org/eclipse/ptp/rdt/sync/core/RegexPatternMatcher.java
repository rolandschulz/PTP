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

/**
 * Class for matching a string against a regular expression.
 *
 *
 */
public class RegexPatternMatcher extends PatternMatcher {
	String regex;
	
	public RegexPatternMatcher(String r) {
		regex = r;
	}
	
	public boolean match(String candidate) {
		return regexCompare(regex, candidate);
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
}
