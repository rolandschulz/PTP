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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.cdi.PCDIException;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIFunctionBreakpoint;
import org.eclipse.ptp.debug.core.cdi.model.IPCDILineBreakpoint;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIStackFrame;


public interface IDebugger {

	/* Debugger Initialization/Termination */
	
	/* The debugger must implement startDebugger()
	 * This method will be called by initialize()
	 * protected abstract void startDebugger(IPJob job);
	 * 
	 * The debugger must implement stopDebugger()
	 * This method will be called by exit()
	 * protected abstract void stopDebugger();
	 * 
	 * The debugger must call
	 * public void handleDebugEvent(int eventType, BitList procs, String[] args);
	 * to give notifications about events
	 */
	
	/* Program Information */
	
	public int getDebuggerPort() throws CoreException;
	
	public void connection() throws CoreException;
	public void startDebugger(IPJob job) throws CoreException;
	public void stopDebugger() throws CoreException;
	
	public void listStackFrames(BitList tasks) throws PCDIException;
	public void setCurrentStackFrame(BitList tasks, IPCDIStackFrame frame) throws PCDIException;
	
	/* Data Display and Manipulation */
	public void getAIFValue(BitList tasks, String expr) throws PCDIException;
	public void evaluateExpression(BitList tasks, String expression) throws PCDIException;
	public void getVariableType(BitList tasks, String varName) throws PCDIException;	
	public void listArguments(BitList tasks, IPCDIStackFrame frame) throws PCDIException;
	public void listLocalVariables(BitList tasks, IPCDIStackFrame frame) throws PCDIException;
	public void listGlobalVariables(BitList tasks) throws PCDIException;
	
	/* Execution Control */
	public void stepInto(BitList tasks, int count) throws PCDIException;
	public void stepOver(BitList tasks, int count) throws PCDIException;
	public void stepFinish(BitList tasks, int count) throws PCDIException;
	public void go(BitList tasks) throws PCDIException;
	public void halt(BitList tasks) throws PCDIException;
	public void kill(BitList tasks) throws PCDIException;
	public void run(String[] args) throws PCDIException;
	public void restart() throws PCDIException;
	
	/* Breakpoints */
	public void setLineBreakpoint(BitList tasks, IPCDILineBreakpoint bpt) throws PCDIException;
	public void setFunctionBreakpoint(BitList tasks, IPCDIFunctionBreakpoint bpt) throws PCDIException;
	public void deleteBreakpoint(BitList tasks, int bpid) throws PCDIException;
	public void enableBreakpoint(BitList tasks, int bpid) throws PCDIException;
	public void disableBreakpoint(BitList tasks, int bpid) throws PCDIException;
	public void conditionBreakpoint(BitList tasks, int bpid, String expr) throws PCDIException;
}