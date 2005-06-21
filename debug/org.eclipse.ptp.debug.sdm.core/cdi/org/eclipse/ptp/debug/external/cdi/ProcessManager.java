/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.ptp.debug.external.cdi;

import java.util.Vector;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.ptp.debug.core.cdi.model.IPCDITarget;
import org.eclipse.ptp.debug.external.DebugSession;
import org.eclipse.ptp.debug.external.cdi.model.Target;
import org.eclipse.ptp.debug.external.event.EInferiorCreated;

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
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIProcessManager#getProcesses()
	 */
	public Target[] getTargets() {
		return (Target[]) debugTargetList.toArray(new Target[debugTargetList.size()]);
	}

	public IPCDITarget[] getCDITargets() {
		return (IPCDITarget[]) debugTargetList.toArray(new IPCDITarget[debugTargetList.size()]);
	}

	public void addTargets(Target[] targets) {
		EventManager eventManager = (EventManager)getSession().getEventManager();
		for (int i = 0; i < targets.length; ++i) {
			Target target = targets[i];
			DebugSession miSession = target.getDebugSession();
			if (miSession != null) {
				miSession.addDebuggerObserver(eventManager);
				
				//int procsNum = target.getProcesses().length;
				//for (int j = 0; j < procsNum; j++)
				//	miSession.getDebugger().fireEvent(new EInferiorCreated(miSession));
				
				miSession.getDebugger().fireEvent(new EInferiorCreated(miSession));
							
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
			DebugSession miSession = target.getDebugSession();
			if (miSession != null) {
				miSession.deleteDebuggerObserver(eventManager);
			}
			debugTargetList.remove(target);
		}
		debugTargetList.trimToSize();
	}

	public Target getTarget(DebugSession miSession) {
		synchronized(debugTargetList) {
			for (int i = 0; i < debugTargetList.size(); ++i) {
				Target target = (Target)debugTargetList.get(i);
				DebugSession mi = target.getDebugSession();
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
