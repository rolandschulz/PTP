/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.core.runnable.command;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.rm.jaxb.core.IJAXBNonNLSConstants;
import org.eclipse.ptp.rm.jaxb.core.IMatchable;
import org.eclipse.ptp.rm.jaxb.core.IStreamParserTokenizer;
import org.eclipse.ptp.rm.jaxb.core.data.And;
import org.eclipse.ptp.rm.jaxb.core.data.Or;
import org.eclipse.ptp.rm.jaxb.core.data.Target;
import org.eclipse.ptp.rm.jaxb.core.data.Tokenizer;
import org.eclipse.ptp.rm.jaxb.core.data.impl.AndImpl;
import org.eclipse.ptp.rm.jaxb.core.data.impl.OrImpl;
import org.eclipse.ptp.rm.jaxb.core.data.impl.TargetImpl;
import org.eclipse.ptp.rm.jaxb.core.messages.Messages;
import org.eclipse.ptp.rm.jaxb.core.utils.CoreExceptionUtils;

public class ConfigurableRegexTokenizer implements IStreamParserTokenizer, IJAXBNonNLSConstants, Runnable {

	public static final String EXT_ID = "org.eclipse.ptp.rm.jaxb.configurableRegexTokenizer"; //$NON-NLS-1$

	private char delim;
	private int maxLen;
	private Integer save;
	private boolean all;
	private boolean includeDelim;
	private IMatchable toMatch;

	private Throwable error;
	private InputStream in;

	private char[] chars;
	private LinkedList<String> saved;
	private StringBuffer segment;

	private boolean endOfStream;

	public ConfigurableRegexTokenizer() {
	}

	public ConfigurableRegexTokenizer(String uuid, Tokenizer t) {
		String d = t.getDelim();
		if (d != null) {
			delim = getChar(d);
			includeDelim = t.isIncludeDelim();
		} else {
			delim = 0;
			includeDelim = false;
		}

		maxLen = t.getMaxMatchLen();
		if (maxLen != 0) {
			maxLen = maxLen * 2;
			chars = new char[maxLen];
		} else {
			chars = new char[1];
		}

		save = t.getSave();
		if (save != null) {
			saved = new LinkedList<String>();
		} else {
			saved = null;
		}

		all = t.isAll();

		Target target = t.getTarget();
		if (target != null) {
			toMatch = new TargetImpl(uuid, target);
		} else {
			Or o = t.getOr();
			if (o != null) {
				toMatch = new OrImpl(uuid, o);
			} else {
				And a = t.getAnd();
				if (a != null) {
					toMatch = new AndImpl(uuid, a);
				}
			}
		}
	}

	public Throwable getInternalError() {
		return error;
	}

	public void run() {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(in));
			boolean endOfStream = false;
			while (!endOfStream) {
				if (read(br)) {
					break;
				}
			}
		} catch (Throwable t) {
			error = t;
			// we do not close the out here because it probably is stdout
		}
	}

	public void setInputStream(InputStream stream) {
		in = stream;
	}

	private void findNextSegment(BufferedReader in) throws CoreException {
		endOfStream = false;
		int len = chars.length == 1 ? 1 : chars.length - segment.length();

		while (true) {
			int read = 0;
			try {
				read = in.read(chars, 0, len);
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

			if (chars.length == 1) {
				if (chars[0] == delim) {
					if (includeDelim) {
						segment.append(delim);
					}
					break;
				}
				segment.append(chars[0]);
			} else {
				segment.append(chars, 0, read);
				break;
			}
		}
	}

	private boolean read(BufferedReader in) throws Throwable {
		while (true) {
			findNextSegment(in);
			if (all) {
				if (save != null) {
					if (segment.length() > 0) {
						while (saved.size() >= save) {
							saved.removeFirst();
						}
						saved.addLast(segment.toString());
					}
					reset();
				}

				if (endOfStream) {
					break;
				}

				continue;
			}

			toMatch.doMatch(segment);
			reset();

			if (endOfStream) {
				return true;
			}
		}

		if (all) {
			while (!saved.isEmpty()) {
				segment.append(saved.removeFirst());
				toMatch.doMatch(segment);
				reset();
			}
		}

		toMatch.postProcess();
		return endOfStream || all;
	}

	private void reset() {
		if (chars.length == 1) {
			this.segment.setLength(0);
		}
	}

	private static char getChar(String delim) {
		if (delim.indexOf(RTESC) >= 0 || delim.indexOf(LNESC) >= 0) {
			return LINE_SEP.charAt(0);
		} else if (delim.indexOf(TBESC) >= 0) {
			return TAB.charAt(0);
		}
		return delim.charAt(0);
	}
}
