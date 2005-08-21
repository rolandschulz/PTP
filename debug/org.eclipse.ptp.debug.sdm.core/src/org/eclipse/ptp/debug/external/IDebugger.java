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

import java.util.Observer;

import org.eclipse.cdt.debug.core.cdi.model.ICDIArgument;
import org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDIFunctionBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDIGlobalVariable;
import org.eclipse.cdt.debug.core.cdi.model.ICDILineBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDILocalVariable;
import org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariable;
import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.debug.core.cdi.IPCDISession;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIEvent;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIDebugProcess;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIDebugProcessSet;
import org.eclipse.ptp.debug.core.utils.Queue;
import org.eclipse.ptp.debug.external.cdi.PCDIException;

/**
 * @author donny
 *
 */
public interface IDebugger {

	/* Debugger Initialization/Termination */
	public void initialize(IPJob job);
	/* The debugger must implement startDebugger()
	 * This method will be called by initialize()
	 * protected abstract void startDebugger(IPJob job);
	 */
	public void exit() throws PCDIException;
	/* The debugger must implement stopDebugger()
	 * This method will be called by exit()
	 * protected abstract void stopDebugger();
	 */
	
	/* Program Information */
	public abstract ICDIStackFrame[] listStackFrames(IPCDIDebugProcessSet procs) throws PCDIException;
	public abstract void setCurrentStackFrame(IPCDIDebugProcessSet procs, ICDIStackFrame frame) throws PCDIException;
	
	/* Data Display and Manipulation */
	public abstract String evaluateExpression(IPCDIDebugProcessSet procs, ICDIVariable var) throws PCDIException;
	public abstract ICDIArgument[] listArguments(IPCDIDebugProcessSet procs, ICDIStackFrame frame) throws PCDIException;
	public abstract ICDILocalVariable[] listLocalVariables(IPCDIDebugProcessSet procs, ICDIStackFrame frame) throws PCDIException;
	public abstract ICDIGlobalVariable[] listGlobalVariables(IPCDIDebugProcessSet procs) throws PCDIException;
	
	/* Execution Control */
	public abstract void stepInto(IPCDIDebugProcessSet procs, int count) throws PCDIException;
	public abstract void stepOver(IPCDIDebugProcessSet procs, int count) throws PCDIException;
	public abstract void stepFinish(IPCDIDebugProcessSet procs, int count) throws PCDIException;
	public abstract void go(IPCDIDebugProcessSet procs) throws PCDIException;
	public abstract void halt(IPCDIDebugProcessSet procs) throws PCDIException;
	public abstract void kill(IPCDIDebugProcessSet procs) throws PCDIException;
	public abstract void run(String[] args) throws PCDIException;
	public abstract void restart() throws PCDIException;
	
	/* Breakpoints */
	public abstract void setLineBreakpoint(IPCDIDebugProcessSet procs, ICDILineBreakpoint bpt) throws PCDIException;
	public abstract void setFunctionBreakpoint(IPCDIDebugProcessSet procs, ICDIFunctionBreakpoint bpt) throws PCDIException;
	public abstract void deleteBreakpoints(ICDIBreakpoint[] bp) throws PCDIException;
	
	/* Events */
	public void addDebuggerObserver(Observer obs);
	public void deleteDebuggerObserver(Observer obs);
	public void fireEvents(IPCDIEvent[] events);
	public void fireEvent(IPCDIEvent event);
	public void notifyObservers(Object arg);
	public Queue getEventQueue();
	
	/* Miscellaneous */
	public IPCDISession getSession();
	public void setSession(IPCDISession session);
	public boolean isExiting();
	public IPCDIDebugProcess getProcess(int number);
	public IPCDIDebugProcess getProcess();
	public IPCDIDebugProcess[] getProcesses();
	public abstract Process getDebuggerProcess();

}