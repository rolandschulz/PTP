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
import org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpoint;
import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.core.IPProcess;
import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.core.util.Queue;
import org.eclipse.ptp.debug.core.IAbstractDebugger;
import org.eclipse.ptp.debug.core.cdi.IPCDISession;
import org.eclipse.ptp.debug.core.cdi.PCDIException;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIEvent;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIExitedEvent;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIResumedEvent;
import org.eclipse.ptp.debug.core.cdi.event.IPCDISuspendedEvent;
import org.eclipse.ptp.debug.external.cdi.breakpoints.LineBreakpoint;
import org.eclipse.ptp.debug.external.cdi.event.BreakpointHitEvent;
import org.eclipse.ptp.debug.external.cdi.event.DebuggerExitedEvent;
import org.eclipse.ptp.debug.external.cdi.event.EndSteppingRangeEvent;
import org.eclipse.ptp.debug.external.cdi.event.ErrorEvent;
import org.eclipse.ptp.debug.external.cdi.event.InferiorExitedEvent;
import org.eclipse.ptp.debug.external.cdi.event.InferiorResumedEvent;
import org.eclipse.ptp.debug.external.cdi.model.LineLocation;

public abstract class AbstractDebugger extends Observable implements IAbstractDebugger {
	protected Queue eventQueue = null;
	protected EventThread eventThread = null;
	protected ArrayList userDefinedProcessSetList = null;
	protected IPCDISession session = null;
	protected IPProcess[] procs;
	protected boolean isExitingFlag = false;
	private IPJob job = null;

	public final void initialize(IPJob job) {
		this.job = job;
		job.setAttribute(TERMINATED_PROC_KEY, new BitList(job.size()));
		job.setAttribute(SUSPENDED_PROC_KEY, new BitList(job.size()));
		
		eventQueue = new Queue();
		eventThread = new EventThread(this);
		eventThread.start();
		userDefinedProcessSetList = new ArrayList();
		procs = job.getSortedProcesses();
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
	public final void deleteAllObservers() {
		this.deleteObservers();
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
			BitList tasks = event.getAllProcesses();
			System.out.println("    --- Abs debugger: " + event);
			if (event instanceof IPCDIExitedEvent) {
				setSuspendTasks(false, tasks);
				setTerminateTasks(true, tasks);
				setProcessStatus(tasks.toArray(), IPProcess.EXITED);
			} else if (event instanceof IPCDIResumedEvent) {
				setSuspendTasks(false, tasks);
				setProcessStatus(tasks.toArray(), IPProcess.RUNNING);
			} else if (event instanceof ErrorEvent) {
				setProcessStatus(tasks.toArray(), IPProcess.ERROR);
			} else if (event instanceof IPCDISuspendedEvent) {
				setSuspendTasks(true, tasks);				
				setProcessStatus(tasks.toArray(), IPProcess.STOPPED);
			}
			eventQueue.addItem(event);
			//check if the job finished
			if (event instanceof IPCDIExitedEvent) {
				if (isJobFinished()) {
					eventQueue.addItem(new DebuggerExitedEvent(getSession(), new BitList(0)));
					exit();
					//remove all observers when the job is finished
					deleteAllObservers();
				}
			}
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
	public void resume(BitList tasks) throws PCDIException {
		filterRunningTasks(tasks);
		handleProcessResumedEvent(tasks);
		go(tasks);
	}
	public void steppingInto(BitList tasks, int count) throws PCDIException {
		filterRunningTasks(tasks);
		handleProcessResumedEvent(tasks);
		stepInto(tasks, count);
	}
	public void steppingInto(BitList tasks) throws PCDIException {
		steppingInto(tasks, 1);
	}
	public void steppingOver(BitList tasks, int count) throws PCDIException {
		filterRunningTasks(tasks);
		handleProcessResumedEvent(tasks);
		stepOver(tasks, 1);
	}
	public void steppingOver(BitList tasks) throws PCDIException {
		steppingOver(tasks, 1);
	}
	public void steppingReturn(BitList tasks) throws PCDIException {
		filterRunningTasks(tasks);
		handleProcessResumedEvent(tasks);
		stepFinish(tasks, 0);
	}
	public void suspend(BitList tasks) throws PCDIException {
		filterSuspendTasks(tasks);
		halt(tasks);
	}
	public void stop(BitList tasks) throws PCDIException {
		filterTerminateTasks(tasks);
		kill(tasks);
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
		
	private BitList addTasks(BitList curTasks, BitList newTasks) {
		if (curTasks.size() < newTasks.size()) {
			newTasks.or(curTasks);
			return newTasks.copy();
		}
		curTasks.or(newTasks);
		return curTasks;
	}
	private void removeTasks(BitList curTasks, BitList newTasks) {
		curTasks.andNot(newTasks);
	}
	
	private void setTerminateTasks(boolean isAdd, BitList tasks) {
		BitList terminatedTasks = (BitList) job.getAttribute(TERMINATED_PROC_KEY);
		if (isAdd)
			terminatedTasks = addTasks(terminatedTasks, tasks);
		else
			removeTasks(terminatedTasks, tasks);
	}
	private void setSuspendTasks(boolean isAdd, BitList tasks) {
		BitList suspendedTasks = (BitList) job.getAttribute(IAbstractDebugger.SUSPENDED_PROC_KEY);
		if (isAdd)
			suspendedTasks = addTasks(suspendedTasks, tasks);			
		else
			removeTasks(suspendedTasks, tasks);
	}
	private BitList filterRunningTasks(BitList tasks) {//get suspend tasks
		removeTasks(tasks, (BitList) job.getAttribute(TERMINATED_PROC_KEY));
		BitList suspendedTasks = (BitList) job.getAttribute(SUSPENDED_PROC_KEY);
		//if the case is in startup, there is no suspended tasks
		if (suspendedTasks.cardinality() > 0)
			tasks.and(suspendedTasks);
		return tasks;
	}
	private BitList filterSuspendTasks(BitList tasks) {//get running tasks
		removeTasks(tasks, (BitList) job.getAttribute(TERMINATED_PROC_KEY));
		removeTasks(tasks, (BitList) job.getAttribute(SUSPENDED_PROC_KEY));
		return tasks;
	}
	private BitList filterTerminateTasks(BitList tasks) {//get not terminate tasks
		removeTasks(tasks, (BitList) job.getAttribute(TERMINATED_PROC_KEY));
		return tasks;
	}
	private boolean isJobFinished() {
		BitList terminatedTasks = (BitList) job.getAttribute(TERMINATED_PROC_KEY);
		return (terminatedTasks.cardinality() == job.size());
	}
}
