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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ptp.rm.jaxb.core.IAssign;
import org.eclipse.ptp.rm.jaxb.core.IJAXBNonNLSConstants;
import org.eclipse.ptp.rm.jaxb.core.data.Match;
import org.eclipse.ptp.rm.jaxb.core.data.Regex;

/**
 * Wrapper implementation.
 * 
 * @author arossi
 * 
 */
public class MatchImpl implements IJAXBNonNLSConstants {

	private RegexImpl regex;
	private final TargetImpl target;
	private List<IAssign> assign;
	private final boolean moveToTop;
	private boolean matched;

	/**
	 * @param uuid
	 *            unique id associated with this resource manager operation (can
	 *            be <code>null</code>).
	 * @param match
	 *            JAXB data element
	 * @param target
	 *            Wrapper for the target to which this match is bound
	 */
	public MatchImpl(String uuid, Match match, TargetImpl target) {
		this.target = target;
		this.moveToTop = match.isMoveToTop();
		this.matched = false;

		Regex r = match.getExpression();
		if (r != null) {
			regex = new RegexImpl(r);
		}

		List<Object> assign = match.getAddOrAppendOrPut();
		if (!assign.isEmpty()) {
			this.assign = new ArrayList<IAssign>();
			for (Object o : assign) {
				AbstractAssign.add(uuid, o, this.assign);
			}
		}
	}

	/**
	 * Executes the regular expression match on the provided segment.
	 * 
	 * @param sequence
	 *            string segment to match.
	 * @return index of the last character in the match
	 * @throws Throwable
	 */
	public synchronized int doMatch(String sequence) throws Throwable {
		int end = 0;
		String[] tokens = null;

		if (regex == null) {
			matched = true;
			return sequence.length();
		} else {
			tokens = regex.getMatched(sequence);
			if (tokens == null) {
				return end;
			}
			/*
			 * return pos of the unmatched remainder
			 */
			matched = true;
			end = regex.getLastChar();
		}

		if (target == null || assign == null) {
			return end;
		}

		for (IAssign a : assign) {
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
	 * Set by the constructor.
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.core.runnable.command.
	 *      ConfigurableRegexTokenizer
	 * @see org.eclipse.ptp.rm.jaxb.core.data.impl.TargetImpl
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
