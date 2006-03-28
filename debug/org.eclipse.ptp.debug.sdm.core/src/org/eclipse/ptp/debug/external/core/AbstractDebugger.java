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
package org.eclipse.ptp.debug.external.core;

import java.util.Observable;
import java.util.Observer;
import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.core.IPProcess;
import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.core.util.Queue;
import org.eclipse.ptp.debug.core.IAbstractDebugger;
import org.eclipse.ptp.debug.core.IDebugCommand;
import org.eclipse.ptp.debug.core.cdi.IPCDISession;
import org.eclipse.ptp.debug.core.cdi.PCDIException;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIErrorEvent;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIEvent;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIExitedEvent;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIResumedEvent;
import org.eclipse.ptp.debug.core.cdi.event.IPCDISuspendedEvent;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIBreakpoint;
import org.eclipse.ptp.debug.core.cdi.model.IPCDILocator;
import org.eclipse.ptp.debug.core.launch.IPLaunch;
import org.eclipse.ptp.debug.external.core.cdi.Session;
import org.eclipse.ptp.debug.external.core.cdi.event.BreakpointCreatedEvent;
import org.eclipse.ptp.debug.external.core.cdi.event.BreakpointHitEvent;
import org.eclipse.ptp.debug.external.core.cdi.event.DebuggerExitedEvent;
import org.eclipse.ptp.debug.external.core.cdi.event.EndSteppingRangeEvent;
import org.eclipse.ptp.debug.external.core.cdi.event.ErrorEvent;
import org.eclipse.ptp.debug.external.core.cdi.event.InferiorExitedEvent;
import org.eclipse.ptp.debug.external.core.cdi.event.InferiorResumedEvent;
import org.eclipse.ptp.debug.external.core.cdi.event.InferiorSignaledEvent;
import org.eclipse.ptp.debug.external.core.cdi.event.SuspendEvent;
import org.eclipse.ptp.debug.external.core.cdi.model.LineLocation;
import org.eclipse.ptp.debug.external.core.commands.KillCommand;
import org.eclipse.ptp.debug.external.core.commands.StartDebuggerCommand;
import org.eclipse.ptp.debug.external.core.commands.StopDebuggerCommand;

public abstract class AbstractDebugger extends Observable implements IAbstractDebugger {
	protected Queue eventQueue = null;
	protected EventThread eventThread = null;
	protected IPCDISession session = null;
	protected IPProcess[] procs;
	protected boolean isExited = false;
	protected IPJob job = null;
	protected DebugCommandQueue commandQueue = null;
	
	public IPCDISession createDebuggerSession(IPLaunch launch, IBinaryObject exe, int timeout, IProgressMonitor monitor) throws CoreException {
		IPJob job = launch.getPJob();
		session = new Session(this, job, launch, exe);
		initialize(job, timeout);
		return session;
	}	
	
	public void postCommand(IDebugCommand command) {
		if (!isExited)
			commandQueue.addCommand(command);
	}
	public void completeCommand(BitList tasks, Object result) {
		commandQueue.setCommandReturn(tasks, result);
	}

	public final void initialize(IPJob job, int timeout) throws CoreException {
		this.job = job;
		job.setAttribute(TERMINATED_PROC_KEY, new BitList(job.size()));
		job.setAttribute(SUSPENDED_PROC_KEY, new BitList(job.size()));
		commandQueue = new DebugCommandQueue(this, timeout);
		commandQueue.start();
		
		isExited = false;
		eventQueue = new Queue();
		eventThread = new EventThread(this);
		eventThread.start();
		procs = job.getSortedProcesses();
		// Initialize state variables
		
		StartDebuggerCommand command = new StartDebuggerCommand(job);
		postCommand(command);
		try {
			command.waitFnish();
		} catch (PCDIException e) {
			exit();
			throw new CoreException(new Status(IStatus.ERROR, PTPDebugExternalPlugin.getUniqueIdentifier(), IStatus.ERROR, e.getMessage(), null));
		}
	}
	
	public final void exit() throws CoreException {
		//make sure all processes are finished
		if (session != null && !isJobFinished()) {
			setJobFinished(session.createBitList(), IPProcess.EXITED);
		}

		isExited = true;
		if (!eventThread.equals(Thread.currentThread())) {			
			// Kill the event Thread.
			try {
				if (eventThread.isAlive()) {
					eventThread.interrupt();
					eventThread.join(5000);
				}
			} catch (InterruptedException e) {}		
		}
		deleteObservers();
		commandQueue.setTerminated();
		stopDebugger();
	}
	public final IPCDISession getSession() {
		return session;
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
	private synchronized void setProcessStatus(int[] tasks, String state) {
		for (int i = 0; i < tasks.length; i++) {
			getProcess(tasks[i]).setStatus(state);
		}
	}
	public synchronized final void fireEvent(final IPCDIEvent event) {		
		if (event != null) {
			//FIXME - add item here or??
			eventQueue.addItem(event);
			System.out.println("    --- Abs debugger: " + event);
			BitList tasks = event.getAllProcesses();
			if (event instanceof IPCDIExitedEvent) {
				setJobFinished(tasks, IPProcess.EXITED);
			} else if (event instanceof IPCDIResumedEvent) {
				setSuspendTasks(false, tasks);
				setProcessStatus(tasks.toArray(), IPProcess.RUNNING);
			} else if (event instanceof IPCDIErrorEvent) {
				IPCDIErrorEvent errEvent = (IPCDIErrorEvent)event;
				switch (errEvent.getErrorCode()) {
				case IPCDIErrorEvent.DBG_FATAL:
					setJobFinished(tasks, IPProcess.ERROR);
					postCommand(new StopDebuggerCommand());
				break;
				case IPCDIErrorEvent.DBG_ERROR:
					setJobFinished(tasks, IPProcess.ERROR);
					postCommand(new KillCommand(tasks));
				break;
				case IPCDIErrorEvent.DBG_WARNING:
					session.unregisterTargets(tasks.toArray(), true);
				break;
				}
			} else if (event instanceof IPCDISuspendedEvent) {
				setSuspendTasks(true, tasks);
				setProcessStatus(tasks.toArray(), IPProcess.STOPPED);
			}
			//eventQueue.addItem(event);
			if (event instanceof IPCDIExitedEvent) {
				if (isJobFinished()) {
					postCommand(new StopDebuggerCommand());
				}
			}
		}
	}
	private void setJobFinished(BitList tasks, String status) {
		if (tasks == null) {
			tasks = session.createBitList();
		}
		setSuspendTasks(false, tasks);
		setTerminateTasks(true, tasks);
		session.unregisterTargets(tasks.toArray(), true);
		setProcessStatus(tasks.toArray(), status);
	}
	public final void notifyObservers(Object arg) {
		setChanged();
		super.notifyObservers(arg);
	}
	public final Queue getEventQueue() {
		return eventQueue;
	}
	public final boolean isExited() {
		return isExited;
	}

	//event
	public void handleStopDebuggerEvent() {
		eventQueue.addItem(new DebuggerExitedEvent(getSession(), new BitList(0)));
		session.shutdown();
	}
	//not used breakpoint created event
	public void handleBreakpointCreatedEvent(BitList tasks) {
		fireEvent(new BreakpointCreatedEvent(getSession(), tasks));		
	}
	public void handleBreakpointHitEvent(BitList tasks, int bpid, int thread_id) {
		IPCDIBreakpoint bpt = ((Session)getSession()).getBreakpointManager().findCDIBreakpoint(bpid);
		if (bpt != null) {
			fireEvent(new BreakpointHitEvent(getSession(), tasks, bpt, thread_id));
		}
	}
	public void handleSuspendEvent(BitList tasks, IPCDILocator locator, int thread_id) {
		fireEvent(new SuspendEvent(getSession(), tasks, locator, thread_id));
	}
	public void handleEndSteppingEvent(BitList tasks, int lineNumber, String filename, int thread_id) {
		LineLocation loc = new LineLocation(filename, lineNumber);
		fireEvent(new EndSteppingRangeEvent(getSession(), tasks, loc, thread_id));
	}
	public void handleProcessResumedEvent(BitList tasks) {
		fireEvent(new InferiorResumedEvent(getSession(), tasks));
	}
	public void handleProcessTerminatedEvent(BitList tasks) {
		fireEvent(new InferiorExitedEvent(getSession(), tasks));
	}
	public void handleProcessSignaledEvent(BitList tasks, IPCDILocator locator, int thread_id) {
		fireEvent(new InferiorSignaledEvent(getSession(), tasks, locator, thread_id));
	}
	public void handleErrorEvent(BitList tasks, String errMsg, int errCode) {
		System.err.println("----- debugger error: " + errMsg + " ------------");
		if (tasks == null || tasks.cardinality() == 0 || errCode == IPCDIErrorEvent.DBG_FATAL) {
			tasks = ((Session)session).createBitList();
		}
		fireEvent(new ErrorEvent(getSession(), tasks, errMsg, errCode));
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

	public BitList filterRunningTasks(BitList tasks) {//get suspend tasks
		removeTasks(tasks, (BitList) job.getAttribute(TERMINATED_PROC_KEY));
		BitList suspendedTasks = (BitList) job.getAttribute(SUSPENDED_PROC_KEY);
		//if the case is in startup, there is no suspended tasks
		if (suspendedTasks.cardinality() > 0)
			tasks.and(suspendedTasks);
		return tasks;
	}
	public BitList filterSuspendTasks(BitList tasks) {//get running tasks
		removeTasks(tasks, (BitList) job.getAttribute(TERMINATED_PROC_KEY));
		removeTasks(tasks, (BitList) job.getAttribute(SUSPENDED_PROC_KEY));
		return tasks;
	}
	public BitList filterTerminateTasks(BitList tasks) {//get not terminate tasks
		removeTasks(tasks, (BitList) job.getAttribute(TERMINATED_PROC_KEY));
		return tasks;
	}
	public boolean isJobFinished() {
		BitList terminatedTasks = (BitList) job.getAttribute(TERMINATED_PROC_KEY);
		return (terminatedTasks.cardinality() == job.size());
	}

	//internal functions
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
}
