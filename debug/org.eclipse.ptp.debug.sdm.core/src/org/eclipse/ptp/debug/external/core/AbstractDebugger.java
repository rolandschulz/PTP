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
import org.eclipse.ptp.core.attributes.IntegerAttribute;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.elements.IPProcess;
import org.eclipse.ptp.core.elements.attributes.JobAttributes;
import org.eclipse.ptp.core.elements.attributes.ProcessAttributes;
import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.IAbstractDebugger;
import org.eclipse.ptp.debug.core.IDebugCommand;
import org.eclipse.ptp.debug.core.PDebugUtils;
import org.eclipse.ptp.debug.core.cdi.IPCDISession;
import org.eclipse.ptp.debug.core.cdi.PCDIException;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIDebugDestroyedEvent;
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
	protected BitList terminatedProcs = null;
	protected BitList suspendedProcs = null;
	protected int jobSize = 0;
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.IAbstractDebugger#createDebuggerSession(org.eclipse.ptp.debug.core.launch.IPLaunch, org.eclipse.cdt.core.IBinaryParser.IBinaryObject, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IPCDISession createDebuggerSession(IPLaunch launch, IBinaryObject exe, IProgressMonitor monitor) throws CoreException {
		IPJob job = launch.getPJob();
		/*
		 * Find number of processes in job. If the attribute does not exist, assume one process.
		 */
		IntegerAttribute numProcAttr = (IntegerAttribute)job.getAttribute(JobAttributes.getNumberOfProcessesAttributeDefinition());
		if (numProcAttr != null) {
			this.jobSize = numProcAttr.getValue();
		} else {
			this.jobSize = 1;
		}
		session = new Session(this, job, jobSize, launch, exe);
		initialize(job, jobSize, monitor);
		this.job = job;
		return session;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.IAbstractDebugger#getCurrentCommand()
	 */
	public IDebugCommand getCurrentCommand() {
		return commandQueue.getCurrentCommand();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.IAbstractDebugger#getInterruptCommand()
	 */
	public IDebugCommand getInterruptCommand() {
		return commandQueue.getInterruptCommand();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.IAbstractDebugger#postInterruptCommand(org.eclipse.ptp.debug.core.IDebugCommand)
	 */
	public void postInterruptCommand(IDebugCommand command) {
		commandQueue.setInterruptCommand(command);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.IAbstractDebugger#postCommand(org.eclipse.ptp.debug.core.IDebugCommand)
	 */
	public void postCommand(IDebugCommand command) {
		if (!isExited) {
			commandQueue.addCommand(command);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.IAbstractDebugger#completeCommand(org.eclipse.ptp.core.util.BitList, java.lang.Object)
	 */
	public void completeCommand(BitList tasks, Object result) {
		commandQueue.setCommandReturn(tasks, result);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.IAbstractDebugger#initialize(org.eclipse.ptp.core.elements.IPJob, int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public final void initialize(IPJob job, int jobSize, IProgressMonitor monitor) throws CoreException {
		monitor.subTask("Connecting to debug server...");
		boolean connected = waitForConnection(monitor);
		monitor.worked(5);
		if (!connected) {
			monitor.done();
			throw new CoreException(Status.CANCEL_STATUS);
		}
		
		monitor.subTask("Starting debugger...");
		terminatedProcs = new BitList(jobSize);
		suspendedProcs = new BitList(jobSize);
		commandQueue = new DebugCommandQueue(this);
		isExited = false;
		eventThread = new EventThread(this);
		procs = job.getSortedProcesses();
		monitor.worked(10);
		// Initialize state variables
		IDebugCommand command = new StartDebuggerCommand(session.createBitList(), job);
		postCommand(command);
		monitor.worked(10);
		try {
			command.waitForReturn();
		} catch (PCDIException e) {
			if (session != null) {
				setJobFinished(command.getTasks(), ProcessAttributes.State.ERROR);
			}
			exit();
			throw new CoreException(new Status(IStatus.ERROR, PTPDebugExternalPlugin.getUniqueIdentifier(), IStatus.ERROR, e.getMessage(), null));
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.IAbstractDebugger#exit()
	 */
	public final void exit() throws CoreException {
		if (!isExited) {
			// make sure all processes are finished
			if (session != null && !isJobFinished()) {
				setJobFinished(session.createBitList(), ProcessAttributes.State.EXITED);
			}
			try {
				stopDebugger();
			} finally {
				isExited = true;
				deleteObservers();
				eventThread.cancelJob();
				commandQueue.setTerminated();
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.IAbstractDebugger#getSession()
	 */
	public final IPCDISession getSession() {
		return session;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.IAbstractDebugger#addDebuggerObserver(java.util.Observer)
	 */
	public final void addDebuggerObserver(Observer obs) {
		this.addObserver(obs);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.IAbstractDebugger#deleteDebuggerObserver(java.util.Observer)
	 */
	public final void deleteDebuggerObserver(Observer obs) {
		this.deleteObserver(obs);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.IAbstractDebugger#deleteAllObservers()
	 */
	public final void deleteAllObservers() {
		this.deleteObservers();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.IAbstractDebugger#fireEvents(org.eclipse.ptp.debug.core.cdi.event.IPCDIEvent[])
	 */
	public synchronized final void fireEvents(IPCDIEvent[] events) {
		if (events != null && events.length > 0) {
			for (int i = 0; i < events.length; i++) {
				fireEvent(events[i]);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.IAbstractDebugger#fireEvent(org.eclipse.ptp.debug.core.cdi.event.IPCDIEvent)
	 */
	public synchronized final void fireEvent(final IPCDIEvent event) {
		if (event != null) {
			BitList tasks = event.getAllProcesses();
			PDebugUtils.println("***** Debugger event: " + event/* + " for tasks: " + showBitList(tasks) */);
			if (event instanceof IPCDIDebugDestroyedEvent) {
				commandQueue.setTerminated();
			} else if (event instanceof IPCDIExitedEvent) {
				setJobFinished(tasks, (((IPCDIExitedEvent) event).getExitStatus() > -1) ? ProcessAttributes.State.EXITED : ProcessAttributes.State.EXITED_SIGNALLED);
				if (isJobFinished()) {
					postStopDebugger();
				}
			} else if (event instanceof IPCDIResumedEvent) {
				setSuspendTasks(false, tasks);
				setProcessStatus(tasks.toArray(), ProcessAttributes.State.RUNNING);
			} else if (event instanceof IPCDIErrorEvent) {
				handleException(tasks, ((IPCDIErrorEvent) event).getErrorCode());
			} else if (event instanceof IPCDISuspendedEvent) {
				setSuspendTasks(true, tasks);
				setProcessStatus(tasks.toArray(), ProcessAttributes.State.STOPPED);
			}
			// FIXME - add item here or??
			eventThread.fireDebugEvent(event);
		}
	}
	
	protected void handleException(BitList tasks, int err_code) {
		switch (err_code) {
		case IPCDIErrorEvent.DBG_FATAL:
			setJobFinished(tasks, ProcessAttributes.State.ERROR);
			postStopDebugger();
			break;
		case IPCDIErrorEvent.DBG_WARNING:
			if (!session.getJob().isTerminated()) {
				setJobFinished(tasks, ProcessAttributes.State.ERROR);
				postCommand(new TerminateCommand(tasks));
			}
			break;
		// case IPCDIErrorEvent.DBG_NORMAL:
		// session.unregisterTargets(tasks.copy(), true);
		// break;
		}
	}
	
	private void postStopDebugger() {
		commandQueue.cleanup(true);
		postCommand(new StopDebuggerCommand(getSession().createBitList()));
		commandQueue.setStopAddCommand(true);
	}
	
	/* (non-Javadoc)
	 * @see java.util.Observable#notifyObservers(java.lang.Object)
	 */
	public final void notifyObservers(Object arg) {
		setChanged();
		super.notifyObservers(arg);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.IAbstractDebugger#isExited()
	 */
	public final boolean isExited() {
		return isExited;
	}
	
	protected synchronized void setJobFinished(BitList tasks, ProcessAttributes.State status) {
		if (tasks == null || tasks.isEmpty()) {
			tasks = session.createBitList();
		}
		setSuspendTasks(false, tasks);
		setTerminateTasks(true, tasks);
		setProcessStatus(tasks.toArray(), status);
		session.unregisterTargets(tasks.copy(), true);
	}
	
	protected synchronized void setTerminateTasks(boolean isAdd, BitList tasks) {
		BitList terminatedTasks = getTerminatedProc();
		if (isAdd)
			terminatedTasks = addTasks(terminatedTasks, tasks);
		else
			removeTasks(terminatedTasks, tasks);
	}
	
	protected synchronized void setSuspendTasks(boolean isAdd, BitList tasks) {
		BitList suspendedTasks = getSuspendedProc();
		if (isAdd)
			suspendedTasks = addTasks(suspendedTasks, tasks);
		else
			removeTasks(suspendedTasks, tasks);
	}
	
	protected void setProcessStatus(int[] tasks, ProcessAttributes.State state) {
		for (int i = 0; i < tasks.length; i++) {
			getProcess(tasks[i]).setState(state);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.IAbstractDebugger#getSuspendedProc()
	 */
	public synchronized BitList getSuspendedProc() {
		return suspendedProcs;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.IAbstractDebugger#getTerminatedProc()
	 */
	public synchronized BitList getTerminatedProc() {
		return terminatedProcs;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.IAbstractDebugger#handleStopDebuggerEvent()
	 */
	public void handleStopDebuggerEvent() {
		fireEvent(new DebuggerDestroyedEvent(getSession()));
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.IAbstractDebugger#handleBreakpointCreatedEvent(org.eclipse.ptp.core.util.BitList, org.eclipse.ptp.debug.core.cdi.model.IPCDIBreakpoint)
	 */
	public void handleBreakpointCreatedEvent(BitList tasks, IPCDIBreakpoint cdiBpt) {
		fireEvent(new BreakpointCreatedEvent(getSession(), tasks, cdiBpt));
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.IAbstractDebugger#handleBreakpointHitEvent(org.eclipse.ptp.core.util.BitList, int, int, java.lang.String[])
	 */
	public void handleBreakpointHitEvent(BitList tasks, int bpid, int thread_id, String[] varchanges) {
		IPCDIBreakpoint bpt = ((Session) getSession()).getBreakpointManager().findCDIBreakpoint(bpid);
		if (bpt != null) {
			fireEvent(new BreakpointHitEvent(getSession(), tasks, bpt, thread_id, varchanges));
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.IAbstractDebugger#handleSuspendEvent(org.eclipse.ptp.core.util.BitList, org.eclipse.ptp.debug.core.cdi.model.IPCDILocator, int, java.lang.String[])
	 */
	public void handleSuspendEvent(BitList tasks, IPCDILocator locator, int thread_id, String[] varchanges) {
		fireEvent(new SuspendEvent(getSession(), tasks, locator, thread_id, varchanges));
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.IAbstractDebugger#handleEndSteppingEvent(org.eclipse.ptp.core.util.BitList, int, java.lang.String, int, java.lang.String[])
	 */
	public void handleEndSteppingEvent(BitList tasks, int lineNumber, String filename, int thread_id, String[] varchanges) {
		LineLocation loc = new LineLocation(filename, lineNumber);
		fireEvent(new EndSteppingRangeEvent(getSession(), tasks, loc, thread_id, varchanges));
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.IAbstractDebugger#handleProcessSignaledEvent(org.eclipse.ptp.core.util.BitList, org.eclipse.ptp.debug.core.cdi.model.IPCDILocator, int, java.lang.String[])
	 */
	public void handleProcessSignaledEvent(BitList tasks, IPCDILocator locator, int thread_id, String[] varchanges) {
		fireEvent(new InferiorSignaledEvent(getSession(), tasks, locator, thread_id, varchanges));
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.IAbstractDebugger#handleProcessResumedEvent(org.eclipse.ptp.core.util.BitList, int)
	 */
	public void handleProcessResumedEvent(BitList tasks, int type) {
		fireEvent(new InferiorResumedEvent(getSession(), tasks, type));
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.IAbstractDebugger#handleProcessTerminatedEvent(org.eclipse.ptp.core.util.BitList, int)
	 */
	public void handleProcessTerminatedEvent(BitList tasks, int exitStatus) {
		fireEvent(new InferiorExitedEvent(getSession(), tasks, exitStatus));
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.IAbstractDebugger#handleProcessTerminatedEvent(org.eclipse.ptp.core.util.BitList, java.lang.String, java.lang.String)
	 */
	public void handleProcessTerminatedEvent(BitList tasks, String signalName, String signalMeaning) {
		fireEvent(new InferiorExitedEvent(getSession(), tasks, signalName, signalMeaning));
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.IAbstractDebugger#handleErrorEvent(org.eclipse.ptp.core.util.BitList, java.lang.String, int)
	 */
	public void handleErrorEvent(BitList tasks, String errMsg, int errCode) {		
		PDebugUtils.println("----- debugger error: " + errMsg + " on Tasks: " + showBitList(tasks) + " ------------");
		if (errCode == IPCDIErrorEvent.DBG_IGNORE)
			return;

		if (tasks == null || tasks.isEmpty() || errCode == IPCDIErrorEvent.DBG_FATAL) {
			tasks = session.createBitList();
		}
		fireEvent(new ErrorEvent(getSession(), tasks, errMsg, errCode));
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.IAbstractDebugger#getProcess(int)
	 */
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
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.IAbstractDebugger#isSuspended(org.eclipse.ptp.core.util.BitList)
	 */
	public synchronized boolean isSuspended(BitList tasks) {
		removeTasks(tasks, getSuspendedProc());
		return tasks.isEmpty();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.IAbstractDebugger#isTerminated(org.eclipse.ptp.core.util.BitList)
	 */
	public synchronized boolean isTerminated(BitList tasks) {
		removeTasks(tasks, getTerminatedProc());
		return tasks.isEmpty();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.IAbstractDebugger#filterRunningTasks(org.eclipse.ptp.core.util.BitList)
	 */
	public synchronized BitList filterRunningTasks(BitList tasks) {// get suspend tasks
		removeTasks(tasks, getTerminatedProc());
		BitList suspendedTasks = getSuspendedProc();
		// if the case is in startup, there is no suspended tasks
		if (suspendedTasks.cardinality() > 0)
			tasks.and(suspendedTasks);
		return tasks;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.IAbstractDebugger#filterSuspendTasks(org.eclipse.ptp.core.util.BitList)
	 */
	public synchronized BitList filterSuspendTasks(BitList tasks) {// get running tasks
		removeTasks(tasks, getTerminatedProc());
		removeTasks(tasks, getSuspendedProc());
		return tasks;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.IAbstractDebugger#filterTerminateTasks(org.eclipse.ptp.core.util.BitList)
	 */
	public synchronized BitList filterTerminateTasks(BitList tasks) {// get not terminate tasks
		removeTasks(tasks, getTerminatedProc());
		return tasks;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.IAbstractDebugger#isJobFinished()
	 */
	public synchronized boolean isJobFinished() {
		return (getTerminatedProc().cardinality() == jobSize);
	}
	
	// internal functions
	private synchronized BitList addTasks(BitList curTasks, BitList newTasks) {
		if (curTasks.size() < newTasks.size()) {
			newTasks.or(curTasks);
			return newTasks.copy();
		}
		curTasks.or(newTasks);
		return curTasks;
	}
	
	private synchronized void removeTasks(BitList curTasks, BitList newTasks) {
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
