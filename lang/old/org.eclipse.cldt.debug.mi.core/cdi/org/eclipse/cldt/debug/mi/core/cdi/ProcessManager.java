/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cldt.debug.mi.core.cdi;

import java.util.Vector;

import org.eclipse.cldt.debug.core.cdi.CDIException;
import org.eclipse.cldt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cldt.debug.mi.core.MISession;
import org.eclipse.cldt.debug.mi.core.cdi.model.Target;
import org.eclipse.cldt.debug.mi.core.event.MIInferiorCreatedEvent;

/**
 */
public class ProcessManager extends Manager {

	static final Target[] EMPTY_TARGETS = new Target[0];
	Vector debugTargetList;

	public ProcessManager(Session session) {
		super(session, true);
		debugTargetList = new Vector(1);
	}

	/**
	 * @see org.eclipse.cldt.debug.core.cdi.ICDIProcessManager#getProcesses()
	 */
	public Target[] getTargets() {
		return (Target[]) debugTargetList.toArray(new Target[debugTargetList.size()]);
	}

	public ICDITarget[] getCDITargets() {
		return (ICDITarget[]) debugTargetList.toArray(new ICDITarget[debugTargetList.size()]);
	}

	public void addTargets(Target[] targets) {
		EventManager eventManager = (EventManager)getSession().getEventManager();
		for (int i = 0; i < targets.length; ++i) {
			Target target = targets[i];
			MISession miSession = target.getMISession();
			if (miSession != null) {
				miSession.addObserver(eventManager);
				miSession.fireEvent(new MIInferiorCreatedEvent(miSession, 0));
				if (!debugTargetList.contains(target)) {
					debugTargetList.add(target);
				}
			}
		}
		debugTargetList.trimToSize();
	}

	public void removeTargets(Target[] targets) {
		EventManager eventManager = (EventManager)getSession().getEventManager();
		for (int i = 0; i < targets.length; ++i) {
			Target target = targets[i];
			MISession miSession = target.getMISession();
			if (miSession != null) {
				miSession.deleteObserver(eventManager);
			}
			debugTargetList.remove(target);
		}
		debugTargetList.trimToSize();
	}

	public Target getTarget(MISession miSession) {
		synchronized(debugTargetList) {
			for (int i = 0; i < debugTargetList.size(); ++i) {
				Target target = (Target)debugTargetList.get(i);
				MISession mi = target.getMISession();
				if (mi.equals(miSession)) {
					return target;
				}
			}
		}
		// ASSERT: it should not happen.
		return null;
	}

	public void update(Target target) throws CDIException {
	}

}
