package org.eclipse.ptp.rm.jaxb.core.data.impl;

import java.util.List;

import org.eclipse.ptp.rm.jaxb.core.IMatchable;
import org.eclipse.ptp.rm.jaxb.core.data.And;
import org.eclipse.ptp.rm.jaxb.core.data.Or;
import org.eclipse.ptp.rm.jaxb.core.data.Target;

public class OrImpl implements IMatchable {

	private List<IMatchable> matchTargets;

	public OrImpl(String uuid, Or or) {
		List<Object> targets = or.getOrOrAndOrTarget();
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

	public synchronized boolean doMatch(StringBuffer segment) throws Throwable {
		for (IMatchable t : matchTargets) {
			if (t.doMatch(segment)) {
				return true;
			}
		}
		return false;
	}

	public void postProcess() throws Throwable {
		for (IMatchable t : matchTargets) {
			t.postProcess();
		}
	}
}
