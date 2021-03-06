/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.control.internal.runnable.command;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.core.util.CoreExceptionUtils;
import org.eclipse.ptp.rm.jaxb.control.JAXBControlConstants;
import org.eclipse.ptp.rm.jaxb.control.data.RegexImpl;
import org.eclipse.ptp.rm.jaxb.control.internal.IMatchable;
import org.eclipse.ptp.rm.jaxb.control.internal.IStreamParserTokenizer;
import org.eclipse.ptp.rm.jaxb.control.internal.data.TargetImpl;
import org.eclipse.ptp.rm.jaxb.control.internal.messages.Messages;
import org.eclipse.ptp.rm.jaxb.control.internal.utils.TokenizerLogger;
import org.eclipse.ptp.rm.jaxb.core.IVariableMap;
import org.eclipse.ptp.rm.jaxb.core.data.RegexType;
import org.eclipse.ptp.rm.jaxb.core.data.TargetType;
import org.eclipse.ptp.rm.jaxb.core.data.TokenizerType;

/**
 * Stream tokenizer (parser) which can be attached to process streams to
 * retrieve data returned by the command execution; for instance: job id, job
 * status, or system-specific properties.<br>
 * <br>
 * The tokenizer reads from the character stream and attempts to apply regex
 * matches for a set of Property or Attribute targets; a successful match
 * triggers one or more assignment operations; at the end of the tokenization,
 * an additional set of conditional assignment actions can be run on each target
 * based on values on the target or in the resource manager environment.<br>
 * <br>
 * While the configurable tokenizer is not completely general, it has been
 * written to provide maximum flexibility within certain limits. Examples of how
 * to configure the tokenizer are provided in
 * org.eclipse.ptp.rm.jaxb.tests/data/tokenizer-examples.xml.<br>
 * <br>
 * For the two ways to segment the stream, see
 * {@link #findNextSegment(BufferedReader)}. For the two modes of reading, see
 * {@link #read(BufferedReader)}.
 * 
 * @see org.eclipse.ptp.rm.jaxb.control.internal.data.TargetImpl
 * @see org.eclipse.ptp.rm.jaxb.control.internal.data.MatchImpl
 * @see org.eclipse.ptp.rm.jaxb.control.data.RegexImpl
 * @see org.eclipse.ptp.rm.jaxb.control.internal.data.AddImpl
 * @see org.eclipse.ptp.rm.jaxb.control.internal.data.AppendImpl
 * @see org.eclipse.ptp.rm.jaxb.control.internal.data.PutImpl
 * @see org.eclipse.ptp.rm.jaxb.control.internal.data.SetImpl
 * @see org.eclipse.ptp.rm.jaxb.control.internal.data.TestImpl
 * @see org.eclipse.ptp.rm.jaxb.control.internal.data.ThrowImpl
 * 
 * @author arossi
 * 
 */
public class ConfigurableRegexTokenizer implements IStreamParserTokenizer, Runnable {

	public static final String EXT_ID = "org.eclipse.ptp.rm.jaxb.configurableRegexTokenizer"; //$NON-NLS-1$

	/**
	 * Compensation for escaping.
	 * 
	 * @param delim
	 * @return the non-escaped char
	 */
	private static char getChar(String delim) {
		if (delim.indexOf(JAXBControlConstants.LNESC) >= 0) {
			return JAXBControlConstants.LN.charAt(0);
		}
		if (delim.indexOf(JAXBControlConstants.RTESC) >= 0) {
			return JAXBControlConstants.RT.charAt(0);
		}
		if (delim.indexOf(JAXBControlConstants.TBESC) >= 0) {
			return JAXBControlConstants.TAB.charAt(0);
		}
		return delim.charAt(0);
	}

	private final TokenizerType tokenizer;
	private int maxLen;
	private Integer save;
	private boolean all;
	private boolean applyToAll;
	private List<IMatchable> toMatch;
	private IProgressMonitor commandMonitor;
	private StringBuffer segment;

	private char delim;
	private RegexImpl exitOn;
	private RegexImpl exitAfter;
	private boolean includeDelim;
	private Throwable error;
	private InputStream in;
	private char[] chars;
	private LinkedList<String> saved;
	private boolean endOfStream;

	/**
	 * @param tokenizer
	 *            JAXB data element
	 */
	public ConfigurableRegexTokenizer(TokenizerType tokenizer) {
		this.tokenizer = tokenizer;
	}

	/**
	 * @return error generated during thread execution
	 */
	public Throwable getInternalError() {
		return error;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.jaxb.control.internal.IStreamParserTokenizer#initialize
	 * (java.lang.String, org.eclipse.ptp.rm.jaxb.core.IVariableMap,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void initialize(String uuid, IVariableMap rmVarMap, IProgressMonitor commandMonitor) {
		this.commandMonitor = commandMonitor;
		String d = tokenizer.getDelim();
		if (d != null) {
			delim = getChar(d);
			includeDelim = tokenizer.isIncludeDelim();
		} else {
			delim = 0;
			includeDelim = false;
		}

		maxLen = tokenizer.getMaxMatchLen();
		if (maxLen != 0) {
			chars = new char[2 * maxLen];
		} else {
			chars = new char[1];
		}

		save = tokenizer.getSave();
		if (save != null) {
			saved = new LinkedList<String>();
		} else {
			saved = null;
		}

		all = tokenizer.isAll();
		applyToAll = tokenizer.isApplyToAll();

		toMatch = new ArrayList<IMatchable>();
		List<TargetType> targets = tokenizer.getTarget();
		for (TargetType target : targets) {
			toMatch.add(new TargetImpl(uuid, target, rmVarMap));
		}

		RegexType reg = tokenizer.getExitOn();
		if (reg != null) {
			exitOn = new RegexImpl(reg, uuid, rmVarMap);
		}
		reg = tokenizer.getExitAfter();
		if (reg != null) {
			exitAfter = new RegexImpl(reg, uuid, rmVarMap);
		}

		segment = new StringBuffer();
	}

	/**
	 * Calls {@link #read(BufferedReader)}.
	 */
	public void run() {
		TokenizerLogger.initialize();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(in));
			read(br);
		} catch (Throwable t) {
			error = t;
			/*
			 * attempt to stop any waiting or joining on the executing process
			 */
			if (commandMonitor != null) {
				commandMonitor.setCanceled(true);
			}
			/*
			 * we do not close the out here because it probably is a standard
			 * stream
			 */
		}
	}

	/**
	 * @param stream
	 *            from process
	 */
	public void setInputStream(InputStream stream) {
		in = stream;
	}

	/**
	 * A regular expression can be provided for an exit condition (either on the
	 * match or after parsing and processing the segment containing the match).
	 * 
	 * @param regex
	 *            defining the pattern which when encountered should trigger an
	 *            exit from the tokenizer
	 * @return true if the pattern has been encountered
	 */
	private boolean checkExit(RegexImpl regex) {
		if (regex != null) {
			if (null != regex.getMatched(segment.toString())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * There are two ways of segmenting the stream. The first is to provide a
	 * single-character delimiter (most commonly the line break); the second is
	 * to define a buffer size. In the latter case, one needs to be assured that
	 * the expansion of any regex pattern being sought on the stream cannot
	 * exceed that buffer size, else the match will be lost.
	 * 
	 * @param in
	 *            the reader for the stream
	 * @throws CoreException
	 */
	private void findNextSegment(BufferedReader in) throws CoreException {
		endOfStream = false;
		int len = chars.length == 1 ? 1 : chars.length - segment.length();
		char[] lookAhead = new char[1];
		while (true) {
			int read = 0;
			int peek = 0;
			try {
				read = in.read(chars, 0, len);
				if (read == JAXBControlConstants.EOF) {
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
					if (delim == '\r') {
						try {
							in.mark(1);
							peek = in.read(lookAhead, 0, 1);
							if (peek == 1 && lookAhead[0] == '\n') {
								if (includeDelim) {
									segment.append('\n');
								}
							} else {
								in.reset();
							}
						} catch (IOException t) {
							throw CoreExceptionUtils.newException(Messages.ReadSegmentError, t);
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
	}

	/**
	 * Applies the matches contained in each target to the target. If a matched
	 * target is also selected by one of its matches, that target is promoted to
	 * the head of the list of targets.
	 * 
	 * @throws Throwable
	 */
	private void matchTargets() throws Throwable {
		IMatchable selected = null;
		for (IMatchable m : toMatch) {
			if (m.doMatch(segment) && !applyToAll) {
				if (m.isSelected()) {
					selected = m;
				}
				break;
			}
		}

		if (selected != null) {
			for (Iterator<IMatchable> i = toMatch.iterator(); i.hasNext();) {
				IMatchable m = i.next();
				m.setSelected(false);
				if (m == selected) {
					i.remove();
				}
			}
			toMatch.add(0, selected);
		}
	}

	/**
	 * Runs merge and tests on each of the targets.
	 * 
	 * @throws Throwable
	 */
	private void postProcessTargets() throws Throwable {
		for (IMatchable m : toMatch) {
			m.postProcess();
		}
	}

	/**
	 * There are two modes of reading. The default is simply forward, a segment
	 * at a time. If <code>all</code> is set to true, however, the read
	 * operation will not apply the matches until it has consumed the entire
	 * stream. In this case one needs to define a <code>save</code> buffer
	 * (number of segments). The buffer is cycled so that what is left are the
	 * last N segments read. These are then applied to the targets one at a
	 * time.
	 * 
	 * @param in
	 *            the input stream reader
	 * @throws Throwable
	 */
	private void read(BufferedReader in) throws Throwable {
		boolean exit = false;

		while (!exit) {
			findNextSegment(in);

			if (checkExit(exitOn)) {
				break;
			}

			if (checkExit(exitAfter)) {
				exit = true;
			}

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

			matchTargets();
			reset();

			if (endOfStream) {
				break;
			}
		}

		if (all) {
			while (!saved.isEmpty()) {
				segment.append(saved.removeFirst());
				matchTargets();
				reset();
			}
		}

		postProcessTargets();
	}

	/**
	 * Adjusts the buffer. If the read is delimited, the buffer is cleared. Else
	 * the end of the buffer (with capacity 2X the maximum length indicated by
	 * the configuration) is shifted forward so that then next
	 * <code>maxLen</code> characters can be read.
	 */
	private void reset() {
		if (chars.length == 1) {
			this.segment.setLength(0);
		} else if (segment.length() == chars.length) {
			segment.delete(0, maxLen);
		}
	}
}
