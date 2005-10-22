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
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;
import org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpoint;
import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.core.IPProcess;
import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.core.util.Queue;
import org.eclipse.ptp.debug.core.cdi.IPCDISession;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIEvent;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIExitedEvent;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIResumedEvent;
import org.eclipse.ptp.debug.core.cdi.event.IPCDISuspendedEvent;
import org.eclipse.ptp.debug.external.cdi.PCDIException;
import org.eclipse.ptp.debug.external.cdi.breakpoints.LineBreakpoint;
import org.eclipse.ptp.debug.external.cdi.event.BreakpointHitEvent;
import org.eclipse.ptp.debug.external.cdi.event.EndSteppingRangeEvent;
import org.eclipse.ptp.debug.external.cdi.event.ErrorEvent;
import org.eclipse.ptp.debug.external.cdi.event.InferiorExitedEvent;
import org.eclipse.ptp.debug.external.cdi.event.InferiorResumedEvent;
import org.eclipse.ptp.debug.external.cdi.model.LineLocation;

/**
 * @author donny
 * 
 */
public abstract class AbstractDebugger extends Observable implements IAbstractDebugger {
	protected Queue eventQueue = null;
	protected EventThread eventThread = null;
	protected ArrayList userDefinedProcessSetList = null;
	protected IPCDISession session = null;
	protected IPProcess[] procs;
	protected boolean isExitingFlag = false; /* Checked by the eventThread */
	private HashMap pseudoProcesses;

	public final void initialize(IPJob job) {
		eventQueue = new Queue();
		eventThread = new EventThread(this);
		eventThread.start();
		userDefinedProcessSetList = new ArrayList();
		procs = job.getSortedProcesses();
		pseudoProcesses = new HashMap();
		// Initialize state variables
		startDebugger(job);
	}
	public final void exit() {
		isExitingFlag = true;
		stopDebugger();
		// Allow (10 secs) for the EventThread to finish processing the queue.
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
	private void setProcessStatus(int[] tasks, String state) {
		for (int i = 0; i < tasks.length; i++) {
			getProcess(tasks[i]).setStatus(state);
		}
	}
	public final void fireEvent(IPCDIEvent event) {
		if (event != null) {
			int[] tasks = event.getAllProcesses().toArray();
			System.out.println("     **Task: "+ tasks.length+ ", --- Abs debugger: " + event);
			if (event instanceof IPCDIExitedEvent) {
				setProcessStatus(tasks, IPProcess.EXITED);
			} else if (event instanceof IPCDIResumedEvent) {
				setProcessStatus(tasks, IPProcess.RUNNING);
			} else if (event instanceof IPCDISuspendedEvent) {
				if (event instanceof ErrorEvent) {
					setProcessStatus(tasks, IPProcess.ERROR);
				} else {
					setProcessStatus(tasks, IPProcess.STOPPED);
				}
			}
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
	public Process getPseudoProcess(IPProcess proc) {
		if (!pseudoProcesses.containsKey(proc.getElementName()))
			pseudoProcesses.put(proc.getElementName(), new PseudoProcess(proc));
		return (Process) pseudoProcesses.get(proc.getElementName());
	}
	public void removePseudoProcess(IPProcess proc) {
		PseudoProcess p = (PseudoProcess) pseudoProcesses.remove(proc.getElementName());
		p.destroy();
	}
	public void handleBreakpointHitEvent(BitList procs, int lineNumber, String filename) {
		LineLocation loc = new LineLocation(filename, lineNumber);
		LineBreakpoint bpt = new LineBreakpoint(ICDIBreakpoint.REGULAR, loc, null);
		fireEvent(new BreakpointHitEvent(getSession(), procs, bpt));
	}
	public void handleEndSteppingEvent(BitList procs, int lineNumber, String filename) {
		LineLocation loc = new LineLocation(filename, lineNumber);
		fireEvent(new EndSteppingRangeEvent(getSession(), procs, loc));
	}
	public void handleProcessResumedEvent(BitList procs) {
		fireEvent(new InferiorResumedEvent(getSession(), procs));
	}
	public void handleProcessTerminatedEvent(BitList procs) {
		fireEvent(new InferiorExitedEvent(getSession(), procs));
	}
	public void goAction(BitList tasks) throws PCDIException {
		handleProcessResumedEvent(tasks);
		go(tasks);
	}
	public void stepIntoAction(BitList tasks, int count) throws PCDIException {
		handleProcessResumedEvent(tasks);
		stepInto(tasks, count);
	}
	public void stepOverAction(BitList tasks, int count) throws PCDIException {
		handleProcessResumedEvent(tasks);
		stepOver(tasks, count);
	}
	public void stepFinishAction(BitList tasks, int count) throws PCDIException {
		handleProcessResumedEvent(tasks);
		stepFinish(tasks, count);
	}
	public void haltAction(BitList tasks) throws PCDIException {
		halt(tasks);
	}
	public void killAction(BitList tasks) throws PCDIException {
		kill(tasks);
	}
	public void runAction(String[] args) throws PCDIException {
		run(args);
	}
	public void restartAction() throws PCDIException {
		restart();
	}
	public IPProcess getProcess(int number) {
		return procs[number];
	}
	public IPProcess[] getProcesses(BitList tasks) {
		int[] taskArray = tasks.toArray();
		IPProcess[] processes = new IPProcess[taskArray.length];
		for (int i = 0; i < taskArray.length; i++) {
			processes[i] = getProcess(taskArray[i]);
		}
		return processes;
	}

	public abstract void stopDebugger();
	public abstract void startDebugger(IPJob job);
}