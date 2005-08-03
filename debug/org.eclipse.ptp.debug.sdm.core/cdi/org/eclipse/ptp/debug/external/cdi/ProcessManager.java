/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.ptp.debug.external.cdi;

import java.util.Hashtable;
import java.util.Iterator;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.ptp.debug.core.cdi.model.IPCDITarget;
import org.eclipse.ptp.debug.external.DebugSession;
import org.eclipse.ptp.debug.external.cdi.model.Target;
import org.eclipse.ptp.debug.external.event.EInferiorCreated;

/**
 */
public class ProcessManager extends Manager {

	static final Target[] EMPTY_TARGETS = new Target[0];
	Hashtable currentDebugTargetList;

	public ProcessManager(Session session) {
		super(session, true);
		currentDebugTargetList = new Hashtable();
	}

	public IPCDITarget getCDITarget(int id) {
		return (IPCDITarget) currentDebugTargetList.get(Integer.toString(id));
	}
	
	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIProcessManager#getProcesses()
	 */
	public Target[] getTargets() {
		int size = currentDebugTargetList.size();
		Target[] targets = new Target[size];
		int index = 0;
		
	    Iterator it = currentDebugTargetList.keySet().iterator();
	    while (it.hasNext()) {
	       String targetId =  (String) it.next();
	       Target target = (Target) currentDebugTargetList.get(targetId);
	       targets[index++] = target;
	    }
	    return targets;
	}

	public IPCDITarget[] getCDITargets() {
		int size = currentDebugTargetList.size();
		IPCDITarget[] targets = new IPCDITarget[size];
		int index = 0;
		
	    Iterator it = currentDebugTargetList.keySet().iterator();
	    while (it.hasNext()) {
	       String targetId =  (String) it.next();
	       IPCDITarget target = (IPCDITarget) currentDebugTargetList.get(targetId);
	       targets[index++] = target;
	    }
	    return targets;
	}

	public void addTargets(Target[] targets) {
		EventManager eventManager = (EventManager)getSession().getEventManager();
		for (int i = 0; i < targets.length; ++i) {
			Target target = targets[i];
			DebugSession dSess = target.getDebugSession();
			if (dSess != null) {
				dSess.getDebugger().addDebuggerObserver(eventManager);
				dSess.getDebugger().fireEvent(new EInferiorCreated(dSess));
				if (!currentDebugTargetList.containsKey(target.getTargetId())) {
					currentDebugTargetList.put(target.getTargetId(), target);
				}
			}
		}
		//currentDebugTargetList.trimToSize();
	}

	public void removeTargets(int[] targets) {
		EventManager eventManager = (EventManager)getSession().getEventManager();
		for (int i = 0; i < targets.length; ++i) {
			String targetId = Integer.toString(targets[i]);
			Target target = (Target) currentDebugTargetList.remove(targetId);
			
			DebugSession miSession = target.getDebugSession();
			if (miSession != null) {
				miSession.getDebugger().deleteDebuggerObserver(eventManager);
			}
		}
	}

	public void update(Target target) throws CDIException {
	}

}
