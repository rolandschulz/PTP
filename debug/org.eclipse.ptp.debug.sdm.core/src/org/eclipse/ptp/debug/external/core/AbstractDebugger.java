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
import org.eclipse.ptp.debug.core.IAbstractDebugger;
import org.eclipse.ptp.debug.core.IDebugCommand;
import org.eclipse.ptp.debug.core.cdi.IPCDISession;
import org.eclipse.ptp.debug.core.cdi.PCDIException;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIDestroyedEvent;
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
import org.eclipse.ptp.debug.external.core.cdi.event.DebuggerDestroyedEvent;
import org.eclipse.ptp.debug.external.core.cdi.event.EndSteppingRangeEvent;
import org.eclipse.ptp.debug.external.core.cdi.event.ErrorEvent;
import org.eclipse.ptp.debug.external.core.cdi.event.InferiorExitedEvent;
import org.eclipse.ptp.debug.external.core.cdi.event.InferiorResumedEvent;
import org.eclipse.ptp.debug.external.core.cdi.event.InferiorSignaledEvent;
import org.eclipse.ptp.debug.external.core.cdi.event.SuspendEvent;
import org.eclipse.ptp.debug.external.core.cdi.model.LineLocation;
import org.eclipse.ptp.debug.external.core.commands.StartDebuggerCommand;
import org.eclipse.ptp.debug.external.core.commands.StopDebuggerCommand;
import org.eclipse.ptp.debug.external.core.commands.TerminateCommand;

public abstract class AbstractDebugger extends Observable implements IAbstractDebugger {
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
	public IDebugCommand getCurrentCommand() {
		return commandQueue.getCurrentCommand();
	}
	public IDebugCommand getInterruptCommand() {
		return commandQueue.getInterruptCommand();
	}
	public void postInterruptCommand(IDebugCommand command) {
		commandQueue.setInterruptCommand(command);
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
		job.setAttribute(TERMINATED_PROC_KEY, new BitList(job.totalProcesses()));
		job.setAttribute(SUSPENDED_PROC_KEY, new BitList(job.totalProcesses()));
		commandQueue = new DebugCommandQueue(this);
		commandQueue.start();
		
		isExited = false;
		eventThread = new EventThread(this);
		procs = job.getSortedProcesses();
		// Initialize state variables
		
		StartDebuggerCommand command = new StartDebuggerCommand(session.createBitList(), job);
		postCommand(command);
		try {
			command.waitForReturn();
		} catch (PCDIException e) {
			if (session != null) {
				setJobFinished(command.getTasks(), IPProcess.ERROR);
			}
			exit();
			throw new CoreException(new Status(IStatus.ERROR, PTPDebugExternalPlugin.getUniqueIdentifier(), IStatus.ERROR, e.getMessage(), null));
		}
	}
	
	public final void exit() throws CoreException {
		if (!isExited) {
			//make sure all processes are finished
			if (session != null && !isJobFinished()) {
				setJobFinished(session.createBitList(), IPProcess.EXITED);
			}
	
			isExited = true;
			eventThread.cancelJob();
			/*
			if (!eventThread.equals(Thread.currentThread())) {			
				// Kill the event Thread.
				try {
					if (eventThread.isAlive()) {
						eventThread.interrupt();
						eventThread.join(5000);
					}
				} catch (InterruptedException e) {}		
			}
			*/
			deleteObservers();
			commandQueue.setTerminated();
			stopDebugger();
		}
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
	public synchronized final void fireEvent(final IPCDIEvent event) {		
		if (event != null) {
			BitList tasks = event.getAllProcesses();
			System.out.println("***** Debugger event: " + event/* + " for tasks: " + showBitList(tasks)*/);
			if (event instanceof IPCDIExitedEvent) {
				setJobFinished(tasks, (((IPCDIExitedEvent)event).getExitStatus()>-1)?IPProcess.EXITED:IPProcess.EXITED_SIGNALLED);
			} else if (event instanceof IPCDIResumedEvent) {
				setSuspendTasks(false, tasks);
				setProcessStatus(tasks.toArray(), IPProcess.RUNNING);
			} else if (event instanceof IPCDIErrorEvent) {
				IPCDIErrorEvent errEvent = (IPCDIErrorEvent)event;
				switch (errEvent.getErrorCode()) {
				case IPCDIErrorEvent.DBG_FATAL:
					setJobFinished(tasks, IPProcess.ERROR);
					postCommand(new StopDebuggerCommand(getSession().createBitList()));
				break;
				case IPCDIErrorEvent.DBG_WARNING:
					if (!session.getJob().isAllStop()) {
						setJobFinished(tasks, IPProcess.ERROR);
						postCommand(new TerminateCommand(tasks));
					}
				break;
				case IPCDIErrorEvent.DBG_NORMAL:
					session.unregisterTargets(tasks.copy(), true);
				break;
				}
			} else if (event instanceof IPCDISuspendedEvent) {
				setSuspendTasks(true, tasks);
				setProcessStatus(tasks.toArray(), IPProcess.STOPPED);
			}
			else if (event instanceof IPCDIExitedEvent) {
				if (isJobFinished()) {
					postCommand(new StopDebuggerCommand(getSession().createBitList()));
				}
			}
			//FIXME - add item here or??
			eventThread.fireDebugEvent(event);
		}
	}	
	public final void notifyObservers(Object arg) {
		setChanged();
		super.notifyObservers(arg);
	}
	public final boolean isExited() {
		return isExited;
	}
	protected void setJobFinished(BitList tasks, String status) {
		if (tasks == null || tasks.isEmpty()) {
			tasks = session.createBitList();
		}
		setSuspendTasks(false, tasks);
		setTerminateTasks(true, tasks);
		setProcessStatus(tasks.toArray(), status);
		session.unregisterTargets(tasks, true);
	}
	protected void setTerminateTasks(boolean isAdd, BitList tasks) {
		BitList terminatedTasks = getTerminatedProc();
		if (isAdd)
			terminatedTasks = addTasks(terminatedTasks, tasks);
		else
			removeTasks(terminatedTasks, tasks);
	}
	protected void setSuspendTasks(boolean isAdd, BitList tasks) {
		BitList suspendedTasks = getSuspendedProc();
		if (isAdd)
			suspendedTasks = addTasks(suspendedTasks, tasks);			
		else
			removeTasks(suspendedTasks, tasks);
	}	
	protected synchronized void setProcessStatus(int[] tasks, String state) {
		for (int i = 0; i < tasks.length; i++) {
			getProcess(tasks[i]).setStatus(state);
		}
	}
	public BitList getSuspendedProc() {
		return (BitList) job.getAttribute(IAbstractDebugger.SUSPENDED_PROC_KEY);
	}
	public BitList getTerminatedProc() {
		return (BitList) job.getAttribute(IAbstractDebugger.TERMINATED_PROC_KEY);		
	}

	//event
	public void handleStopDebuggerEvent() {
		fireEvent(new DebuggerDestroyedEvent(getSession(), new BitList(0)));
	}
	public void handleBreakpointCreatedEvent(BitList tasks, IPCDIBreakpoint cdiBpt) {
		fireEvent(new BreakpointCreatedEvent(getSession(), tasks, cdiBpt));
	}
	public void handleBreakpointHitEvent(BitList tasks, int bpid, int thread_id, String[] varchanges) {
		IPCDIBreakpoint bpt = ((Session)getSession()).getBreakpointManager().findCDIBreakpoint(bpid);
		if (bpt != null) {
			fireEvent(new BreakpointHitEvent(getSession(), tasks, bpt, thread_id, varchanges));
		}
	}
	public void handleSuspendEvent(BitList tasks, IPCDILocator locator, int thread_id, String[] varchanges) {
		fireEvent(new SuspendEvent(getSession(), tasks, locator, thread_id, varchanges));
	}
	public void handleEndSteppingEvent(BitList tasks, int lineNumber, String filename, int thread_id, String[] varchanges) {
		LineLocation loc = new LineLocation(filename, lineNumber);
		fireEvent(new EndSteppingRangeEvent(getSession(), tasks, loc, thread_id, varchanges));
	}
	public void handleProcessSignaledEvent(BitList tasks, IPCDILocator locator, int thread_id, String[] varchanges) {
		fireEvent(new InferiorSignaledEvent(getSession(), tasks, locator, thread_id, varchanges));
	}
	public void handleProcessResumedEvent(BitList tasks, int type) {
		fireEvent(new InferiorResumedEvent(getSession(), tasks, type));
	}
	public void handleProcessTerminatedEvent(BitList tasks, int exitStatus) {
		fireEvent(new InferiorExitedEvent(getSession(), tasks, exitStatus));
	}
	public void handleProcessTerminatedEvent(BitList tasks, String signalName, String signalMeaning) {
		fireEvent(new InferiorExitedEvent(getSession(), tasks, signalName, signalMeaning));
	}
	public void handleErrorEvent(BitList tasks, String errMsg, int errCode) {
		System.err.println("----- debugger error: " + errMsg + " on Tasks: " + showBitList(tasks) +" ------------");
		if (tasks == null || tasks.isEmpty() || errCode == IPCDIErrorEvent.DBG_FATAL) {
			tasks = session.createBitList();
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

	//filter process
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
		return (terminatedTasks.cardinality() == job.totalProcesses());
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

	/* debug purpose */
	public static String showBitList(BitList tasks) {
		if (tasks == null) {
			return "";
		}
		int[] array = tasks.toArray();
		if (array.length == 0)
			return "";
		
		String msg = "";
		int preTask = array[0];
		msg += preTask;
		boolean isContinue = false;
		for (int i = 1; i < array.length; i++) {
			if (preTask == (array[i] - 1)) {
				preTask = array[i];
				isContinue = true;
				if (i == (array.length - 1)) {
					msg += "-" + array[i];
					break;
				}
				continue;
			}
			if (isContinue)
				msg += "-" + preTask;
			msg += "," + array[i];
			isContinue = false;
			preTask = array[i];
		}
		return msg;
	}
}
