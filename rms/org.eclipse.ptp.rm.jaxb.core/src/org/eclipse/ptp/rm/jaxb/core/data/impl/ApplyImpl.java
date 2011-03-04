package org.eclipse.ptp.rm.jaxb.core.data.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.rm.jaxb.core.IJAXBNonNLSConstants;
import org.eclipse.ptp.rm.jaxb.core.data.Apply;
import org.eclipse.ptp.rm.jaxb.core.data.Match;
import org.eclipse.ptp.rm.jaxb.core.messages.Messages;
import org.eclipse.ptp.rm.jaxb.core.runnable.ConfigurableRegexTokenizer;
import org.eclipse.ptp.rm.jaxb.core.utils.CoreExceptionUtils;

public class ApplyImpl implements IJAXBNonNLSConstants {

	private List<ApplyImpl> subApply;
	private List<MatchImpl> match;
	private final boolean includeDelim;
	private final int count;
	private final StringBuffer buffer;
	private char delim;
	private char until;

	public ApplyImpl(ConfigurableRegexTokenizer tokenizer, Apply apply) {
		List<Apply> appList = apply.getApply();

		if (!appList.isEmpty()) {
			this.subApply = new ArrayList<ApplyImpl>();
			for (Apply a : appList) {
				this.subApply.add(new ApplyImpl(tokenizer, a));
			}
		} else {
			List<Match> matchList = apply.getMatch();
			if (!matchList.isEmpty()) {
				this.match = new ArrayList<MatchImpl>();
				for (Match m : matchList) {
					this.match.add(new MatchImpl(tokenizer, m));
				}
			}
		}

		String v = apply.getDelim();
		if (v != null) {
			delim = v.charAt(0);
		} else {
			delim = 0;
		}
		v = apply.getUntil();
		if (v != null) {
			until = v.charAt(0);
		} else {
			until = 0;
		}
		if (apply.getFor() == null) {
			count = 0;
		} else {
			count = apply.getFor();
		}
		includeDelim = apply.isIncludeDelim();
		buffer = new StringBuffer();
	}

	public void doApply(BufferedReader in, BufferedWriter out) throws CoreException {
		int iterator = 0;

		while (true) {
			if (subApply != null) {
				for (ApplyImpl a : subApply) {
					a.doApply(in, out);
				}
			} else {
				String segment = findNextSegment(in, out);
				if (segment == null) {
					break;
				}

				for (MatchImpl m : match) {
					try {
						if (m.doMatch(segment)) {
							break;
						}
					} catch (Throwable t) {
						throw CoreExceptionUtils.newException(Messages.IllegalFailedMatch + m.getRegex().getExpression() + CO + SP
								+ segment, t);
					}
				}
			}

			if (count > 0 && ++iterator >= count) {
				break;
			}
		}
	}

	private String findNextSegment(BufferedReader in, BufferedWriter out) throws CoreException {
		buffer.setLength(0);

		char[] chars = new char[1];

		try {
			while (true) {
				try {
					int read = in.read(chars);
					if (read == EOF) {
						break;
					}
				} catch (EOFException eof) {
					break;
				} catch (IOException t) {
					throw CoreExceptionUtils.newException(Messages.DoApplyError, t);
				}

				try {
					if (out != null) {
						out.write(chars);
					}
				} catch (IOException t) {
					t.printStackTrace();
				}

				if (chars[0] == delim) {
					if (includeDelim) {
						buffer.append(delim);
					}
					break;
				}

				if (until != 0 && chars[0] == until) {
					buffer.setLength(0);
					break;
				}

				buffer.append(chars[0]);
			}

			if (buffer.length() == 0) {
				return null;
			}
		} finally {
			if (out != null) {
				try {
					out.flush();
				} catch (IOException t) {
					t.printStackTrace();
				}
			}
		}

		return buffer.toString();
	}
}
