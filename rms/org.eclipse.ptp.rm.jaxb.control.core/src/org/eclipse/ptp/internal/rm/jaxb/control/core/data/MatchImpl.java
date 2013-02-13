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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.ptp.internal.rm.jaxb.control.core.IAssign;
import org.eclipse.ptp.internal.rm.jaxb.control.core.messages.Messages;
import org.eclipse.ptp.internal.rm.jaxb.control.core.utils.DebuggingLogger;
import org.eclipse.ptp.rm.jaxb.core.IVariableMap;
import org.eclipse.ptp.rm.jaxb.core.data.MatchType;
import org.eclipse.ptp.rm.jaxb.core.data.RegexType;

/**
 * Wrapper implementation. Consists of a Regular Expression and a reference to a target attribute. When the regex is satisfied, the
 * match applies the list of assign actions to its target.<br>
 * <br>
 * A match can also be set to force its target to the front of the list of targets held by the tokenizer (<code>moveToTop</code>)
 * when the match is successful.
 * 
 * @author arossi
 * 
 */
public class MatchImpl {

	private RegexImpl regex;
	private final TargetImpl target;
	private List<IAssign> assign;
	private final boolean moveToTop;
	private boolean matched;

	/**
	 * @param uuid
	 *            unique id associated with this resource manager operation (can be <code>null</code>).
	 * @param match
	 *            JAXB data element
	 * @param target
	 *            Wrapper for the target to which this match is bound
	 * @param rmVarMap
	 *            resource manager environment
	 */
	public MatchImpl(String uuid, MatchType match, TargetImpl target, IVariableMap rmVarMap) {
		this.target = target;
		this.moveToTop = match.isMoveToTop();
		this.matched = false;

		RegexType r = match.getExpression();
		if (r != null) {
			regex = new RegexImpl(r, uuid, rmVarMap);
		}

		List<Object> assign = match.getAddOrAppendOrPut();
		if (!assign.isEmpty()) {
			this.assign = new ArrayList<IAssign>();
			for (Object o : assign) {
				AbstractAssign.add(uuid, o, this.assign, rmVarMap);
			}
		}
	}

	/**
	 * Executes the regular expression match on the provided segment and applies the assign actions if match is positive.
	 * 
	 * @param sequence
	 *            string segment to match.
	 * @return index of the last character in the match
	 * @throws Throwable
	 */
	public synchronized int doMatch(String sequence) throws Throwable {
		int end = 0;
		String[] tokens = null;

		DebuggingLogger.getLogger().logSegmentInfo(Messages.MatchImpl_0 + sequence);
		DebuggingLogger.getLogger().logSegmentInfo(
				Messages.MatchImpl_1 + regex.getExpression() + Messages.MatchImpl_2 + regex.getFlags());

		if (regex == null) {
			matched = true;
			DebuggingLogger.getLogger().logMatchInfo(Messages.MatchImpl_3);
			return sequence.length();
		} else {
			tokens = regex.getMatched(sequence);
			if (tokens == null) {
				DebuggingLogger.getLogger().logMatchInfo(Messages.MatchImpl_4);
				return end;
			}
			/*
			 * return pos of the unmatched remainder
			 */
			matched = true;
			end = regex.getLastChar();
			DebuggingLogger.getLogger().logMatchInfo(Messages.MatchImpl_5 + Arrays.asList(tokens));
		}

		if (target == null || assign == null) {
			return end;
		}

		int incr = 0;

		for (IAssign a : assign) {
			if (a.isForceNew()) {
				incr++;
			}
			a.incrementIndex(incr);
			Object t = target.getTarget(a);
			a.setTarget(t);
			a.assign(tokens);
		}

		return end;
	}

	/**
	 * @return whether the match succeeded
	 */
	public boolean getMatched() {
		return matched;
	}

	/**
	 * Flag for prioritizing the target bound to this match by promoting it to the head of the ordered list of targets held by the
	 * tokenizer.
	 * 
	 * @see org.eclipse.ptp.internal.rm.jaxb.control.core.core.runnable.command.ConfigurableRegexTokenizer
	 * @see org.eclipse.ptp.internal.rm.jaxb.control.core.core.data.TargetImpl
	 * 
	 * @return whether to set this match as the "selected" one when matched
	 */
	public boolean getMoveToTop() {
		return moveToTop;
	}

	/**
	 * @return the regex wrapper for this match
	 */
	public RegexImpl getRegex() {
		return regex;
	}

	/**
	 * Clears the matched flag.
	 */
	public void reset() {
		matched = false;
	}
}
