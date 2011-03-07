package org.eclipse.ptp.rm.jaxb.core.data.impl;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ptp.rm.jaxb.core.IAssign;
import org.eclipse.ptp.rm.jaxb.core.IJAXBNonNLSConstants;
import org.eclipse.ptp.rm.jaxb.core.data.Add;
import org.eclipse.ptp.rm.jaxb.core.data.Append;
import org.eclipse.ptp.rm.jaxb.core.data.Match;
import org.eclipse.ptp.rm.jaxb.core.data.Put;
import org.eclipse.ptp.rm.jaxb.core.data.Regex;
import org.eclipse.ptp.rm.jaxb.core.data.Set;
import org.eclipse.ptp.rm.jaxb.core.data.Target;
import org.eclipse.ptp.rm.jaxb.core.exceptions.UnsatisfiedRegexMatchException;

public class MatchImpl implements IJAXBNonNLSConstants {

	private RegexImpl regex;
	private TargetImpl target;
	private List<IAssign> assign;
	private final boolean errorOnMiss;
	private boolean matched;

	public MatchImpl(Match match) {
		Regex r = match.getRegex();
		if (r != null) {
			regex = new RegexImpl(r);
		}
		Target t = match.getTarget();
		if (t != null) {
			target = new TargetImpl(t);
		}

		List<Object> actions = match.getSetOrAddOrPut();
		if (!actions.isEmpty()) {
			assign = new ArrayList<IAssign>();
			for (Object o : actions) {
				if (o instanceof Add) {
					assign.add(new AddImpl((Add) o));
				} else if (o instanceof Append) {
					assign.add(new AppendImpl((Append) o));
				} else if (o instanceof Put) {
					assign.add(new PutImpl((Put) o));
				} else if (o instanceof Set) {
					assign.add(new SetImpl((Set) o));
				}
			}
		}

		errorOnMiss = match.isErrorOnMiss();
	}

	public synchronized void clear() {
		matched = false;
		if (target != null) {
			target.clear();
		}
	}

	public synchronized int doMatch(String sequence) throws Throwable {
		int end = 0;
		if (matched) {
			return end;
		}

		String[] tokens = null;

		if (regex == null) {
			matched = true;
		} else {
			tokens = regex.getMatched(sequence);
			if (tokens == null) {
				if (errorOnMiss) {
					throw new UnsatisfiedRegexMatchException(regex.getExpression());
				}
				return end;
			}
			matched = true;
			/*
			 * return pos of the unmatched remainder
			 */
			end = regex.getLastChar();
		}

		if (target == null || assign == null) {
			return end;
		}

		Object value = target.getTarget(tokens);

		for (IAssign a : assign) {
			a.setTarget(value);
			a.assign(tokens);
		}

		return end;
	}

	public synchronized boolean getMatched() {
		return matched;
	}

	public RegexImpl getRegex() {
		return regex;
	}

	public TargetImpl getTarget() {
		return target;
	}

	public void setTarget(TargetImpl target) {
		this.target = target;
	}
}
