/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
/*
 * Created on Feb 18, 2005
 *
 */
package org.eclipse.ptp.debug.external;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.core.IPProcess;
import org.eclipse.ptp.debug.core.cdi.IPCDISession;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIEvent;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIDebugProcess;
import org.eclipse.ptp.debug.core.utils.BitList;
import org.eclipse.ptp.debug.core.utils.Queue;
import org.eclipse.ptp.debug.external.cdi.model.DebugProcess;

/**
 * @author donny
 *
 */
public abstract class AbstractDebugger extends Observable implements IDebugger, IDebuggerEvent {
	protected Queue eventQueue = null;
	protected EventThread eventThread = null;

	protected ArrayList userDefinedProcessSetList = null;
	
	protected IPCDISession session = null;
	
	protected IPProcess[] procs;
	
	protected boolean isExitingFlag = false; /* Checked by the eventThread */

	protected abstract void startDebugger(IPJob job);
	
	public final void initialize(IPJob job) {
		eventQueue = new Queue();
		eventThread = new EventThread(this);
		eventThread.start();
		
		userDefinedProcessSetList = new ArrayList();
		
		procs = job.getSortedProcesses();
		
		// Initialize state variables
		startDebugger(job);
	}
	
	protected abstract void stopDebugger();
	
	public final void exit() {
		isExitingFlag = true;
		stopDebugger();
		
		// Allow (10 secs) for the EventThread  to finish processing the queue.
		for (int i = 0; !eventQueue.isEmpty() && i < 5; i++) {
			try {
				java.lang.Thread.sleep(2000);
			} catch (InterruptedException e) {
			}
		}
		
		// Kill the event Thread.
		try {
			if (eventThread.isAlive()) {
				eventThread.interrupt();
				eventThread.join(); // Should use a timeout ?
			}
		} catch (InterruptedException e) {
		}		
	}

	public final IPCDISession getSession() {
		return session;
	}
	
	public final void setSession(IPCDISession s) {
		session = s;
	}

	public final void addDebuggerObserver(Observer obs) {
		this.addObserver(obs);
	}

	public final void deleteDebuggerObserver(Observer obs) {
		this.deleteObserver(obs);
	}
	
	public final void fireEvents(IPCDIEvent[] events) {
		if (events != null && events.length > 0) {
			for (int i = 0; i < events.length; i++) {
				fireEvent(events[i]);
			}
		}
	}

	public final void fireEvent(IPCDIEvent event) {
		if (event != null) {
			eventQueue.addItem(event);
		}
	}
	
	public final void notifyObservers(Object arg) {
		setChanged();
		super.notifyObservers(arg);
	}

	public final Queue getEventQueue() {
		return eventQueue;
	}
	
	public final boolean isExiting() {
		return isExitingFlag;
	}
	
	public final IPCDIDebugProcess getProcess(int number) {
		IPCDIDebugProcess proc = new DebugProcess(session, procs[number], (Process) procs[number]);
		return proc;
	}
	
	public final IPCDIDebugProcess getProcess() {
		return getProcess(0);
	}
	
	public final IPCDIDebugProcess[] getProcesses() {
		IPCDIDebugProcess[] list = new IPCDIDebugProcess[procs.length];
		
		for (int i = 0; i < procs.length; i++) {
			IPCDIDebugProcess proc = new DebugProcess(session, procs[i], (Process) procs[i]);
			list[i] = proc;
		}
		
		return list;
	}
	
	public void handleDebugEvent(int eventType, BitList procs, String[] args) {
		IPCDIEvent event = null;
		
		if (eventType == IDBGEV_BPHIT) {
			event = handleBreakpointHitEvent(procs, args);
		} else if (eventType == IDBGEV_ENDSTEPPING) {
			event = handleEndSteppingEvent(procs, args);
		} else if (eventType == IDBGEV_PROCESSRESUMED) {
			event = handleProcessResumedEvent(procs, args);
		} else if (eventType == IDBGEV_PROCESSTERMINATED) {
			event = handleProcessTerminatedEvent(procs, args);
		}
		
		fireEvent(event);
	}
}