/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.internal.rm.jaxb.control.core.data;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.ptp.internal.rm.jaxb.control.core.JAXBControlConstants;
import org.eclipse.ptp.rm.jaxb.core.IVariableMap;
import org.eclipse.ptp.rm.jaxb.core.data.RegexType;

/**
 * Wrapper implementation.
 * 
 * @author arossi
 * 
 */
public class RegexImpl {
	private String expression;
	private final boolean split;
	private final Pattern pattern;
	private final String flags;
	private int lastChar;

	/**
	 * Wraps the Java implementation of regular expressions.
	 * 
	 * @param regex
	 *            JAXB data element
	 */
	public RegexImpl(RegexType regex) {
		this(regex, null, null);
	}

	/**
	 * Wraps the Java implementation of regular expressions.
	 * 
	 * @param regex
	 *            JAXB data element
	 * @param uuid
	 *            of job (can be <code>null</code>)
	 * @param map
	 *            of environment (can be <code>null</code>)
	 */
	public RegexImpl(RegexType regex, String uuid, IVariableMap map) {
		expression = regex.getExpression();
		if (expression == null) {
			expression = regex.getContent();
		}
		if (map != null) {
			expression = map.getString(uuid, expression);
		}
		split = regex.isSplit();
		flags = regex.getFlags();
		pattern = Pattern.compile(expression, getFlags(flags));
	}

	/**
	 * @return the regex pattern
	 */
	public String getExpression() {
		return expression;
	}

	public String getFlags() {
		return flags;
	}

	/**
	 * @return the index of the last character in the match
	 */
	public int getLastChar() {
		return lastChar;
	}

	/**
	 * @param sequence
	 *            the segment to match
	 * @return array of substrings either from a split using the regex, or the
	 *         regex groups (including group 0).
	 */
	public String[] getMatched(String sequence) {
		String[] result = null;
		if (split) {
			result = pattern.split(sequence);
		} else {
			Matcher m = pattern.matcher(sequence);
			if (m.matches()) {
				int count = m.groupCount();
				result = new String[count + 1];
				for (int i = 0; i < result.length; i++) {
					result[i] = m.group(i);
				}
				lastChar = m.end(count);
			}
		}
		return result;
	}

	/**
	 * Translates the string representation of the flags into the corresponding
	 * Java int value. String can represent an or'd set, e.g.,
	 * "CASE_INSENTIVE | DOTALL".
	 * 
	 * @param flags
	 *            string representing the or'd flags.
	 * @return corresponding internal value
	 */
	private int getFlags(String flags) {
		if (flags == null) {
			return 0;
		}
		int f = 0;
		String[] split = flags.split(JAXBControlConstants.REGPIP);
		for (String s : split) {
			if (JAXBControlConstants.CASE_INSENSITIVE.equals(s.trim())) {
				f |= Pattern.CASE_INSENSITIVE;
			} else if (JAXBControlConstants.MULTILINE.equals(s.trim())) {
				f |= Pattern.MULTILINE;
			} else if (JAXBControlConstants.DOTALL.equals(s.trim())) {
				f |= Pattern.DOTALL;
			} else if (JAXBControlConstants.UNICODE_CASE.equals(s.trim())) {
				f |= Pattern.UNICODE_CASE;
			} else if (JAXBControlConstants.CANON_EQ.equals(s.trim())) {
				f |= Pattern.CANON_EQ;
			} else if (JAXBControlConstants.LITERAL.equals(s.trim())) {
				f |= Pattern.LITERAL;
			} else if (JAXBControlConstants.COMMENTS.equals(s.trim())) {
				f |= Pattern.COMMENTS;
			} else if (JAXBControlConstants.UNIX_LINES.equals(s.trim())) {
				f |= Pattern.UNIX_LINES;
			}
		}
		return f;
	}
}
