package org.eclipse.ptp.rm.jaxb.core.data.impl;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ptp.rm.jaxb.core.data.And;
import org.eclipse.ptp.rm.jaxb.core.data.Or;
import org.eclipse.ptp.rm.jaxb.core.data.Target;

public class OrImpl {

	private final String uuid;
	private final List<OrImpl> ors;
	private final List<AndImpl> ands;
	private final List<TargetImpl> targets;

	public OrImpl(String uuid, Or or) {
		this.uuid = uuid;
		ors = new ArrayList<OrImpl>();
		List<Or> odata = or.getOr();
		for (Or o : odata) {
			ors.add(new OrImpl(uuid, o));
		}
		ands = new ArrayList<AndImpl>();
		List<And> adata = or.getAnd();
		for (And a : adata) {
			ands.add(new AndImpl(uuid, a));
		}
		targets = new ArrayList<TargetImpl>();
		List<Target> tdata = or.getTarget();
		for (Target t : tdata) {
			targets.add(new TargetImpl(uuid, t));
		}
	}

	public synchronized void doMatch(StringBuffer segment) throws Throwable {

	}
}
