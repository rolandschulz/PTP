package org.eclipse.ptp.rm.jaxb.core.data.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.rm.jaxb.core.IJAXBNonNLSConstants;
import org.eclipse.ptp.rm.jaxb.core.data.Match;
import org.eclipse.ptp.rm.jaxb.core.data.Read;
import org.eclipse.ptp.rm.jaxb.core.messages.Messages;
import org.eclipse.ptp.rm.jaxb.core.utils.CoreExceptionUtils;

public class ReadImpl implements IJAXBNonNLSConstants {

	private static final byte xOR = 0;
	private static final byte xAND = 1;

	private final char delim;
	private final List<MatchImpl> matches;
	private final int maxLen;
	private final Integer save;
	private final byte mode;
	private final boolean all;
	private final boolean includeDelim;
	private final char[] chars;
	private final LinkedList<String> saved;
	private final StringBuffer segment;
	private final int total;

	private int nMatches;
	private boolean looking;

	public ReadImpl(Read read) {
		String d = read.getDelim();
		if (d != null) {
			delim = d.charAt(0);
			includeDelim = read.isIncludeDelim();
		} else {
			delim = 0;
			includeDelim = false;
		}

		maxLen = read.getMaxMatchLen();
		if (maxLen != 0) {
			chars = new char[maxLen];
		} else {
			chars = new char[1];
		}

		String md = read.getMode();
		if (md != null) {
			if (md.equalsIgnoreCase(OR)) {
				mode = xOR;
			} else if (md.equalsIgnoreCase(AND)) {
				mode = xAND;
			} else {
				mode = xOR;
			}
		} else {
			mode = xOR;
		}

		save = read.getSave();
		if (save != null) {
			saved = new LinkedList<String>();
		} else {
			saved = null;
		}

		all = read.isAll();

		this.matches = initializeMatches(read);
		total = this.matches.size();
		segment = new StringBuffer();
	}

	/**
	 * 
	 * @param in
	 * @param out
	 * @return whether EOS has been encountered.
	 * @throws CoreException
	 */
	public boolean read(BufferedReader in, BufferedWriter out) throws CoreException {
		nMatches = 0;
		looking = true;
		while (looking) {
			String s = findNextSegment(in, out);

			if (all) {
				if (save != null) {
					if (s != null) {
						while (saved.size() >= save) {
							saved.removeFirst();
						}
						saved.addLast(s);
					}
				}
				if (s == null) {
					break;
				}
				reset(s.length());
				continue;
			}

			if (s == null) {
				return true;
			}

			reset(findMatches(s));
		}

		if (all) {
			while (looking && !saved.isEmpty()) {
				findMatches(saved.removeFirst());
			}
		}

		clear();
		return segment == null || all;
	}

	private void clear() {
		for (MatchImpl m : matches) {
			m.clear();
		}
	}

	private int findMatches(String segment) throws CoreException {
		int end = 0;
		for (MatchImpl m : matches) {
			if (!m.getMatched()) {
				try {
					end = m.doMatch(segment);
				} catch (Throwable t) {
					throw CoreExceptionUtils.newException(Messages.IllegalFailedMatch + m.getRegex().getExpression() + CO + SP
							+ segment, t);
				}
			} else {
				continue;
			}

			if (m.getMatched()) {
				nMatches++;
				switch (mode) {
				case xOR:
					if (nMatches > 0) {
						looking = false;
					}
					break;
				case xAND:
					if (nMatches == total) {
						looking = false;
					}
					break;
				}
				break;
			}
		}

		return end;
	}

	private String findNextSegment(BufferedReader in, BufferedWriter out) throws CoreException {
		boolean endOfStream = false;
		try {
			while (true) {
				int read = 0;
				try {
					read = in.read(chars);
					if (read == EOF) {
						endOfStream = true;
						break;
					}
				} catch (EOFException eof) {
					endOfStream = true;
					break;
				} catch (IOException t) {
					throw CoreExceptionUtils.newException(Messages.ReadSegmentError, t);
				}

				try {
					if (out != null) {
						out.write(chars, 0, read);
					}
				} catch (IOException t) {
					t.printStackTrace();
				}

				if (chars.length == 1) {
					if (chars[0] == delim) {
						if (includeDelim) {
							if (segment != null) {
								segment.append(delim);
							}
						}
						break;
					}
					segment.append(chars[0]);
				} else {
					segment.append(chars, 0, read);
					break;
				}
			}

			if (endOfStream) {
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

		return segment.toString();
	}

	private List<MatchImpl> initializeMatches(Read read) {
		List<Match> matches = read.getMatch();
		List<MatchImpl> mimpls = new ArrayList<MatchImpl>();
		TargetImpl last = null;
		TargetImpl current = null;
		for (Match m : matches) {
			MatchImpl mimpl = new MatchImpl(m);
			current = mimpl.getTarget();
			if (current != null) {
				last = current;
			} else {
				mimpl.setTarget(last);
			}
			mimpls.add(mimpl);
		}
		return mimpls;
	}

	private void reset(int end) {
		if (chars.length == 1) {
			this.segment.setLength(0);
		} else {
			if (end > 0) {
				this.segment.delete(0, end);
			} else if (this.segment.length() > chars.length) {
				this.segment.delete(0, chars.length);
			}
		}
	}
}
