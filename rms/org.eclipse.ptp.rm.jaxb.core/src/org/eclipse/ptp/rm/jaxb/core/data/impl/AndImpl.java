package org.eclipse.ptp.rm.jaxb.core.data.impl;

import java.util.List;

import org.eclipse.ptp.rm.jaxb.core.IMatchable;
import org.eclipse.ptp.rm.jaxb.core.data.And;
import org.eclipse.ptp.rm.jaxb.core.data.Or;
import org.eclipse.ptp.rm.jaxb.core.data.Target;

public class AndImpl implements IMatchable {

	private List<IMatchable> matchTargets;

	public AndImpl(String uuid, And and) {
		List<Object> targets = and.getOrOrAndOrTarget();
		for (Object t : targets) {
			if (t instanceof Or) {
				matchTargets.add(new OrImpl(uuid, (Or) t));
			} else if (t instanceof And) {
				matchTargets.add(new AndImpl(uuid, (And) t));
			} else if (t instanceof Target) {
				matchTargets.add(new TargetImpl(uuid, (Target) t));
			}
		}
	}

	/*
	 * This is not a real logical and, but more like an OR that does not stop at
	 * the first match, but applies the segment to all children.
	 * 
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.jaxb.core.IMatchable#doMatch(java.lang.StringBuffer)
	 */
	public synchronized boolean doMatch(StringBuffer segment) throws Throwable {
		boolean matched = false;
		for (IMatchable t : matchTargets) {
			if (t.doMatch(segment)) {
				matched = true;
			}
		}
		return matched;
	}

	public void postProcess() throws Throwable {
		for (IMatchable t : matchTargets) {
			t.postProcess();
		}
	}
}
