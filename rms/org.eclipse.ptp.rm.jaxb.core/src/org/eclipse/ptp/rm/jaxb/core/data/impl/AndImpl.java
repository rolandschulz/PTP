package org.eclipse.ptp.rm.jaxb.core.data.impl;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ptp.rm.jaxb.core.data.And;
import org.eclipse.ptp.rm.jaxb.core.data.Or;
import org.eclipse.ptp.rm.jaxb.core.data.Target;

public class AndImpl {

	private final List<OrImpl> ors;
	private final List<AndImpl> ands;
	private final List<TargetImpl> targets;

	public AndImpl(String uuid, And and) {
		ors = new ArrayList<OrImpl>();
		List<Or> odata = and.getOr();
		for (Or o : odata) {
			ors.add(new OrImpl(uuid, o));
		}
		ands = new ArrayList<AndImpl>();
		List<And> adata = and.getAnd();
		for (And a : adata) {
			ands.add(new AndImpl(uuid, a));
		}
		targets = new ArrayList<TargetImpl>();
		List<Target> tdata = and.getTarget();
		for (Target t : tdata) {
			targets.add(new TargetImpl(uuid, t));
		}
	}

	public synchronized void doMatch(StringBuffer segment) throws Throwable {

	}

}
