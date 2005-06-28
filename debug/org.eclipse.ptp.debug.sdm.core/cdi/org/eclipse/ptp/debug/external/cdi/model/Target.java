/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.debug.external.cdi.model;

import java.math.BigInteger;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDIAddressLocation;
import org.eclipse.cdt.debug.core.cdi.ICDICondition;
import org.eclipse.cdt.debug.core.cdi.ICDIFunctionLocation;
import org.eclipse.cdt.debug.core.cdi.ICDILineLocation;
import org.eclipse.cdt.debug.core.cdi.ICDILocation;
import org.eclipse.cdt.debug.core.cdi.model.ICDIAddressBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDIExceptionpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDIExpression;
import org.eclipse.cdt.debug.core.cdi.model.ICDIFunctionBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDIInstruction;
import org.eclipse.cdt.debug.core.cdi.model.ICDILineBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDIMemoryBlock;
import org.eclipse.cdt.debug.core.cdi.model.ICDIMixedInstruction;
import org.eclipse.cdt.debug.core.cdi.model.ICDIRegisterGroup;
import org.eclipse.cdt.debug.core.cdi.model.ICDIRuntimeOptions;
import org.eclipse.cdt.debug.core.cdi.model.ICDISharedLibrary;
import org.eclipse.cdt.debug.core.cdi.model.ICDISignal;
import org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDITargetConfiguration;
import org.eclipse.cdt.debug.core.cdi.model.ICDIThread;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariableDescriptor;
import org.eclipse.cdt.debug.core.cdi.model.ICDIWatchpoint;
import org.eclipse.ptp.debug.core.cdi.model.IPCDITarget;
import org.eclipse.ptp.debug.external.DebugSession;
import org.eclipse.ptp.debug.external.cdi.BreakpointManager;
import org.eclipse.ptp.debug.external.cdi.RegisterManager;
import org.eclipse.ptp.debug.external.cdi.Session;
import org.eclipse.ptp.debug.external.cdi.SessionObject;

/**
 */
public class Target extends SessionObject implements IPCDITarget {
	
	private TargetConfiguration fConfiguration;
	private DebugSession dSession;
	Thread[] noThreads = new Thread[0];
	Thread[] currentThreads;
	int currentThreadId;
	
	public Target(Session s, DebugSession dS) {
		super(s);
		dSession = dS;
		fConfiguration = new TargetConfiguration(this);
		currentThreads = noThreads;
	}

	public DebugSession getDebugSession() {
		System.out.println("Target.getDebugSession()");
		return dSession;
	}
	
	public Process[] getProcesses() {
		System.out.println("Target.getProcesses()");
		return dSession.getDebugger().getProcesses();
	}

	public Process getProcess(int num) {
		System.out.println("Target.getProcess()");
		return dSession.getDebugger().getProcess(num);
	}
	
	public Process getProcess() {
		System.out.println("Target.getProcess()");
		return getProcess(0);
	}

	public ICDITargetConfiguration getConfiguration() {
		System.out.println("Target.getConfiguration()");
		return fConfiguration;
	}

	public String evaluateExpressionToString(ICDIStackFrame context, String expressionText) throws CDIException {
		// Auto-generated method stub
		System.out.println("Target.evaluateExpressionToString()");
		return null;
	}

	public ICDIVariableDescriptor getGlobalVariableDescriptors(String filename, String function, String name) throws CDIException {
		// Auto-generated method stub
		System.out.println("Target.getGlobalVariableDescriptors()");
		return null;
	}

	public ICDIRegisterGroup[] getRegisterGroups() throws CDIException {
		// Auto-generated method stub
		System.out.println("Target.getRegisterGroups()");
		RegisterManager regMgr = ((Session)getSession()).getRegisterManager();
		return regMgr.getRegisterGroups(this);
	}

	public boolean isTerminated() {
		// Auto-generated method stub
		System.out.println("Target.isTerminated()");
		return false;
	}

	public void terminate() throws CDIException {
		// Auto-generated method stub
		System.out.println("Target.terminate()");
		dSession.getDebugger().terminate();
	}

	public boolean isDisconnected() {
		// Auto-generated method stub
		System.out.println("Target.isDisconnected()");
		return false;
	}

	public void disconnect() throws CDIException {
		// Auto-generated method stub
		System.out.println("Target.disconnect()");
		dSession.getDebugger().disconnect();
	}

	public void restart() throws CDIException {
		// Auto-generated method stub
		System.out.println("Target.restart()");
		dSession.getDebugger().restart();
	}

	public void resume() throws CDIException {
		// Auto-generated method stub
		System.out.println("Target.resume()");
		resume(false);
	}

	public void stepOver() throws CDIException {
		// Auto-generated method stub
		System.out.println("Target.stepOver()");
		
	}

	public void stepInto() throws CDIException {
		// Auto-generated method stub
		System.out.println("Target.stepInto()");
		
	}

	public void stepOverInstruction() throws CDIException {
		// Auto-generated method stub
		System.out.println("Target.stepOverInstruction()");
		
	}

	public void stepIntoInstruction() throws CDIException {
		// Auto-generated method stub
		System.out.println("Target.stepIntoInstruction()");
		
	}

	public void runUntil(ICDILocation location) throws CDIException {
		// Auto-generated method stub
		System.out.println("Target.runUntil()");
		
	}

	public void jump(ICDILocation location) throws CDIException {
		// Auto-generated method stub
		System.out.println("Target.jump()");
		
	}

	public void signal() throws CDIException {
		// Auto-generated method stub
		System.out.println("Target.signal()");
		
	}

	public void signal(ICDISignal signal) throws CDIException {
		// Auto-generated method stub
		System.out.println("Target.signal()");
		
	}

	public ICDIRuntimeOptions getRuntimeOptions() {
		// Auto-generated method stub
		System.out.println("Target.getRuntimeOptions()");
		return null;
	}

	public ICDICondition createCondition(int ignoreCount, String expression) {
		// Auto-generated method stub
		System.out.println("Target.createCondition()");
		return null;
	}

	public ICDICondition createCondition(int ignoreCount, String expression, String[] threadIds) {
		// Auto-generated method stub
		System.out.println("Target.createCondition()");
		return null;
	}
	public ICDIThread[] getThreads() throws CDIException {
		// Auto-generated method stub
		System.out.println("Target.getThreads()");
		if (currentThreads.length == 0) {
			currentThreads = new Thread[]{new Thread(this, 0)};
			currentThreadId = currentThreads[0].getId();
		}
		return currentThreads;
	}

	public ICDIThread getCurrentThread() throws CDIException {
		// Auto-generated method stub
		System.out.println("Target.getCurrentThread()");
		return null;
	}

	public ICDIBreakpoint[] getBreakpoints() throws CDIException {
		// Auto-generated method stub
		System.out.println("Target.getBreakpoints()");
		return null;
	}

	public ICDIWatchpoint setWatchpoint(int type, int watchType, String expression, ICDICondition condition) throws CDIException {
		// Auto-generated method stub
		System.out.println("Target.setWatchpoint()");
		return null;
	}

	public void deleteBreakpoints(ICDIBreakpoint[] breakpoints) throws CDIException {
		// Auto-generated method stub
		System.out.println("Target.deleteBreakpoints()");
		
	}

	public void deleteAllBreakpoints() throws CDIException {
		// Auto-generated method stub
		System.out.println("Target.deleteAllBreakpoints()");
		
	}

	public ICDIExceptionpoint setExceptionBreakpoint(String clazz, boolean stopOnThrow, boolean stopOnCatch) throws CDIException {
		// Auto-generated method stub
		System.out.println("Target.setExceptionBreakpoint()");
		return null;
	}

	public void stepOver(int count) throws CDIException {
		// Auto-generated method stub
		System.out.println("Target.stepOver()");
		
	}

	public void stepOverInstruction(int count) throws CDIException {
		// Auto-generated method stub
		System.out.println("Target.stepOverInstruction()");
		
	}

	public void stepInto(int count) throws CDIException {
		// Auto-generated method stub
		System.out.println("Target.stepInto()");
		
	}

	public void stepIntoInstruction(int count) throws CDIException {
		// Auto-generated method stub
		System.out.println("Target.stepIntoInstruction()");
		
	}

	public void stepUntil(ICDILocation location) throws CDIException {
		// Auto-generated method stub
		System.out.println("Target.stepUntil()");
		
	}

	public void resume(boolean passSignal) throws CDIException {
		// Auto-generated method stub
		System.out.println("Target.resume()");
		dSession.getDebugger().resume();
	}

	public void resume(ICDILocation location) throws CDIException {
		// Auto-generated method stub
		System.out.println("Target.resume()");
		
	}

	public void resume(ICDISignal signal) throws CDIException {
		// Auto-generated method stub
		System.out.println("Target.resume()");
		
	}

	public void suspend() throws CDIException {
		// Auto-generated method stub
		System.out.println("Target.suspend()");
		dSession.getDebugger().suspend();
	}

	public boolean isSuspended() {
		// Auto-generated method stub
		System.out.println("Target.isSuspended()");
		return false;
	}

	public ICDISignal[] getSignals() throws CDIException {
		// Auto-generated method stub
		System.out.println("Target.getSignals()");
		return null;
	}

	public ICDITarget getTarget() {
		// Auto-generated method stub
		System.out.println("Target.getTarget()");
		return this;
	}

	public ICDIExpression createExpression(String code) throws CDIException {
		// Auto-generated method stub
		System.out.println("Target.createExpression()");
		return null;
	}

	public ICDIExpression[] getExpressions() throws CDIException {
		// Auto-generated method stub
		System.out.println("Target.getExpressions()");
		return null;
	}

	public void destroyExpressions(ICDIExpression[] expressions) throws CDIException {
		// Auto-generated method stub
		System.out.println("Target.destroyExpressions()");
		
	}

	public void destroyAllExpressions() throws CDIException {
		// Auto-generated method stub
		System.out.println("Target.destroyAllExpressions()");
		
	}

	public void addSourcePaths(String[] srcPaths) throws CDIException {
		// Auto-generated method stub
		System.out.println("Target.addSourcePaths()");
		
	}

	public String[] getSourcePaths() throws CDIException {
		// Auto-generated method stub
		System.out.println("Target.getSourcePaths()");
		return null;
	}

	public ICDIInstruction[] getInstructions(BigInteger startAddress, BigInteger endAddress) throws CDIException {
		// Auto-generated method stub
		System.out.println("Target.getInstructions()");
		return null;
	}

	public ICDIInstruction[] getInstructions(String filename, int linenum) throws CDIException {
		// Auto-generated method stub
		System.out.println("Target.getInstructions()");
		return null;
	}

	public ICDIInstruction[] getInstructions(String filename, int linenum, int lines) throws CDIException {
		// Auto-generated method stub
		System.out.println("Target.getInstructions()");
		return null;
	}

	public ICDIMixedInstruction[] getMixedInstructions(BigInteger startAddress, BigInteger endAddress) throws CDIException {
		// Auto-generated method stub
		System.out.println("Target.getMixedInstructions()");
		return null;
	}

	public ICDIMixedInstruction[] getMixedInstructions(String filename, int linenum) throws CDIException {
		// Auto-generated method stub
		System.out.println("Target.getMixedInstructions()");
		return null;
	}

	public ICDIMixedInstruction[] getMixedInstructions(String filename, int linenum, int lines) throws CDIException {
		// Auto-generated method stub
		System.out.println("Target.getMixedInstructions()");
		return null;
	}

	public ICDISharedLibrary[] getSharedLibraries() throws CDIException {
		// Auto-generated method stub
		System.out.println("Target.getSharedLibraries()");
		return null;
	}

	public ICDIMemoryBlock createMemoryBlock(String address, int units, int wordSize) throws CDIException {
		// Auto-generated method stub
		System.out.println("Target.createMemoryBlock()");
		return null;
	}

	public void removeBlocks(ICDIMemoryBlock[] memoryBlocks) throws CDIException {
		// Auto-generated method stub
		System.out.println("Target.removeBlocks()");
		
	}

	public void removeAllBlocks() throws CDIException {
		// Auto-generated method stub
		System.out.println("Target.removeAllBlocks()");
		
	}

	public ICDIMemoryBlock[] getMemoryBlocks() throws CDIException {
		// Auto-generated method stub
		System.out.println("Target.getMemoryBlocks()");
		return null;
	}

	public ICDILineLocation createLineLocation(String file, int line) {
		// Auto-generated method stub
		System.out.println("Target.createLineLocation()");
		BreakpointManager bMgr = ((Session)getSession()).getBreakpointManager();
		return bMgr.createLineLocation(file, line);
	}

	public ICDIFunctionLocation createFunctionLocation(String file, String function) {
		// Auto-generated method stub
		System.out.println("Target.createFunctionLocation()");
		BreakpointManager bMgr = ((Session)getSession()).getBreakpointManager();
		return bMgr.createFunctionLocation(file, function);
	}

	public ICDIAddressLocation createAddressLocation(BigInteger address) {
		// Auto-generated method stub
		System.out.println("Target.createAddressLocation()");
		BreakpointManager bMgr = ((Session)getSession()).getBreakpointManager();
		return bMgr.createAddressLocation(address);
	}

	public ICDILineBreakpoint setLineBreakpoint(int type, ICDILineLocation location, ICDICondition condition, boolean deferred) throws CDIException {
		// Auto-generated method stub
		System.out.println("Target.setLineBreakpoint()");
		BreakpointManager bMgr = ((Session)getSession()).getBreakpointManager();
		return bMgr.setLineBreakpoint(this, type, location, condition, deferred);
	}

	public ICDIFunctionBreakpoint setFunctionBreakpoint(int type, ICDIFunctionLocation location, ICDICondition condition, boolean deferred) throws CDIException {
		// Auto-generated method stub
		System.out.println("Target.setFunctionBreakpoint()");
		BreakpointManager bMgr = ((Session)getSession()).getBreakpointManager();
		return bMgr.setFunctionBreakpoint(this, type, location, condition, deferred);
	}

	public ICDIAddressBreakpoint setAddressBreakpoint(int type, ICDIAddressLocation location, ICDICondition condition, boolean deferred) throws CDIException {
		// Auto-generated method stub
		System.out.println("Target.setAddressBreakpoint()");
		BreakpointManager bMgr = ((Session)getSession()).getBreakpointManager();
		return bMgr.setAddressBreakpoint(this, type, location, condition, deferred);
	}

	public void setSourcePaths(String[] srcPaths) throws CDIException {
		// Auto-generated method stub
		System.out.println("Target.setSourcePaths()");
		
	}

}
