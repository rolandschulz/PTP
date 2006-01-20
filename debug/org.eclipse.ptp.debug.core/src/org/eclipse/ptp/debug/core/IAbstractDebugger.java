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
package org.eclipse.ptp.debug.core;

import java.util.Observer;
import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.debug.core.cdi.ICDILocator;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.core.IPProcess;
import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.core.util.Queue;
import org.eclipse.ptp.debug.core.cdi.IPCDISession;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIEvent;
import org.eclipse.ptp.debug.core.launch.IPLaunch;

public interface IAbstractDebugger extends IDebugger {
	public final static String TERMINATED_PROC_KEY = "terminated";
	public final static String SUSPENDED_PROC_KEY = "suspended";
	
	public IPCDISession createDebuggerSession(IPLaunch launch, IBinaryObject exe, IProgressMonitor monitor) throws CoreException;
	
	/* Debugger Initialization/Termination */
	public void initialize(IPJob job) throws CoreException;
	public void exit() throws CoreException;
	
	public void addDebuggerObserver(Observer obs);
	public void deleteDebuggerObserver(Observer obs);
	public void deleteAllObservers();
	public void fireEvents(IPCDIEvent[] events);
	public void fireEvent(IPCDIEvent event);
	public void notifyObservers(Object arg);
	public Queue getEventQueue();
	
	/* Miscellaneous */
	public IPCDISession getSession();
	public void setSession(IPCDISession session);
	public boolean isExited();

	public IPProcess getProcess(int number);
	
	/* event */
	public void handleStopDebuggerEvent();
	public void handleBreakpointCreatedEvent(BitList tasks);
	public void handleBreakpointHitEvent(BitList tasks, int bpid);
	public void handleEndSteppingEvent(BitList tasks, int lineNumber, String filename);
	public void handleProcessResumedEvent(BitList tasks);
	public void handleProcessTerminatedEvent(BitList tasks);
	public void handleProcessSignaledEvent(BitList tasks, ICDILocator locator);
	public void handleErrorEvent(BitList tasks, String errMsg);
	public void handleErrorEvent(BitList tasks, String errMsg, int errCode);
	
	/* others */
	public BitList filterRunningTasks(BitList tasks);
	public BitList filterSuspendTasks(BitList tasks);
	public BitList filterTerminateTasks(BitList tasks);
	public boolean isJobFinished();
	
	/* Command */
	public void postCommand(IDebugCommand command);
	public void postCommandAndWait(IDebugCommand command);
	public void completeCommand(Object result);	
}