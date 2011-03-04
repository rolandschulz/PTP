package org.eclipse.ptp.rm.jaxb.core.data.impl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.ptp.rm.jaxb.core.IJAXBNonNLSConstants;
import org.eclipse.ptp.rm.jaxb.core.data.Regex;

public class RegexImpl implements IJAXBNonNLSConstants {

	private final String expression;
	private final boolean split;
	private final Pattern pattern;

	public RegexImpl(Regex regex) {
		this.expression = regex.getContent();
		this.split = regex.isSplit();
		this.pattern = Pattern.compile(expression, getFlags(regex.getFlags()));
	}

	public String getExpression() {
		return expression;
	}

	public String[] getMatched(String sequence) {
		String[] result = null;
		if (split) {
			result = pattern.split(sequence);
		} else {
			Matcher m = pattern.matcher(sequence);
			if (m.matches()) {
				result = new String[m.groupCount() + 1];
				for (int i = 0; i < result.length; i++) {
					result[i] = m.group(i);
				}
			}
		}
		return result;
	}

	private int getFlags(String flags) {
		if (flags == null) {
			return 0;
		}
		int f = 0;
		String[] split = flags.split(PIP);
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
