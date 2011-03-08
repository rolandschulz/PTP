/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.core.data.impl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.ptp.rm.jaxb.core.IJAXBNonNLSConstants;
import org.eclipse.ptp.rm.jaxb.core.data.Regex;

public class RegexImpl implements IJAXBNonNLSConstants {

	private static final String FLAG_SEP = OPENSQ + PIP + CLOSSQ;

	private String expression;
	private final boolean split;
	private final Pattern pattern;
	private int lastChar;

	public RegexImpl(Regex regex) {
		expression = regex.getExpression();
		if (expression == null) {
			expression = regex.getContent();
		}
		split = regex.isSplit();
		pattern = Pattern.compile(expression, getFlags(regex.getFlags()));
	}

	public String getExpression() {
		return expression;
	}

	public int getLastChar() {
		return lastChar;
	}

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

	private int getFlags(String flags) {
		if (flags == null) {
			return 0;
		}
		int f = 0;
		String[] split = flags.split(FLAG_SEP);
		for (String s : split) {
			if (CASE_INSENSITIVE.equals(s.trim())) {
				f |= Pattern.CASE_INSENSITIVE;
			} else if (MULTILINE.equals(s.trim())) {
				f |= Pattern.MULTILINE;
			} else if (DOTALL.equals(s.trim())) {
				f |= Pattern.DOTALL;
			} else if (UNICODE_CASE.equals(s.trim())) {
				f |= Pattern.UNICODE_CASE;
			} else if (CANON_EQ.equals(s.trim())) {
				f |= Pattern.CANON_EQ;
			} else if (LITERAL.equals(s.trim())) {
				f |= Pattern.LITERAL;
			} else if (COMMENTS.equals(s.trim())) {
				f |= Pattern.COMMENTS;
			}
		}
		return f;
	}
}
