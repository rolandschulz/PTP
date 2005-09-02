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
import org.eclipse.ptp.debug.core.cdi.model.IPCDIDebugProcess;
import org.eclipse.ptp.debug.core.cdi.model.IPCDITarget;
import org.eclipse.ptp.debug.external.IDebugger;
import org.eclipse.ptp.debug.external.cdi.ExpressionManager;
import org.eclipse.ptp.debug.external.cdi.Session;
import org.eclipse.ptp.debug.external.cdi.SessionObject;
import org.eclipse.ptp.debug.external.cdi.VariableManager;

public class Target extends SessionObject implements IPCDITarget {
	
	private TargetConfiguration fConfiguration;
	private IDebugger fDebugger;
	
	Thread[] currentThreads;
	int currentThreadId;
	boolean suspended = true;
	
	int targetId; /* synonymous with the process number/id */
	IPCDIDebugProcess debugProcess;
	
	public Target(Session session, IDebugger debugger, int tId) {
		super(session);
		fDebugger = debugger;
		targetId = tId;
		debugProcess = session.getModelManager().getProcess(targetId);
		
		fConfiguration = new TargetConfiguration(this);
		currentThreads = new Thread[0];
	}
	
	public IPCDIDebugProcess getDebugProcess() {
		return debugProcess;
	}
	
	public int getTargetId() {
		return targetId;
	}
	
	public Process getProcess() {
		return ((Session) getSession()).getProcess(targetId);
	}

	public ICDITargetConfiguration getConfiguration() {
		return fConfiguration;
	}

	public String evaluateExpressionToString(ICDIStackFrame frame, String expressionText) throws CDIException {
		// Auto-generated method stub
		System.out.println("Target.evaluateExpressionToString()");
		
		Target target = (Target) frame.getTarget();
		Session session = (Session) target.getSession();
		IDebugger debugger = session.getDebugger();
		DebugProcessSet newSet = new DebugProcessSet(session, target.getTargetId());

		return createExpression(expressionText).getValue(frame).getValueString();
	}

	public ICDIVariableDescriptor getGlobalVariableDescriptors(String filename, String function, String name) throws CDIException {
		// Auto-generated method stub
		System.out.println("Target.getGlobalVariableDescriptors()");
		VariableManager varMgr = ((Session)getSession()).getVariableManager();
		return varMgr.getGlobalVariableDescriptor(this, filename, function, name);
	}
	
	public ICDIRegisterGroup[] getRegisterGroups() throws CDIException {
		// Auto-generated method stub
		System.out.println("Target.getRegisterGroups()");
		return null;
	}

	public boolean isTerminated() {
		// Auto-generated method stub
		System.out.println("Target.isTerminated()");
		return false;
	}

	public void terminate() throws CDIException {
		// Auto-generated method stub
		System.out.println("Target.terminate()");
		fDebugger.kill(null);
	}

	public boolean isDisconnected() {
		// Auto-generated method stub
		System.out.println("Target.isDisconnected()");
		return false;
	}

	public void disconnect() throws CDIException {
		// Auto-generated method stub
		System.out.println("Target.disconnect()");
		//fDebugger.detach(null);
		fDebugger.kill(null);
	}

	public void restart() throws CDIException {
		// Auto-generated method stub
		System.out.println("Target.restart()");
		fDebugger.restart();
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
	
	public ICDIThread getThread(int threadId) throws CDIException {
		if (currentThreads.length == 0)
			getThreads();
		
		return currentThreads[threadId];
	}
	
	/**
	 * Called when stopping because of breakpoints etc ..
	 */
	public void updateState() {
		// get the new Threads.
		
		for (int i = 0; i < currentThreads.length; i++) {
			currentThreads[i].clearState();
		}
	}
	
	public ICDIThread[] getThreads() throws CDIException {
		/* Currently the debug external interface doesn't support thread
		 */
		if (currentThreads.length == 0) {
			currentThreads = new Thread[1];
			currentThreads[0] = new Thread(this, 0);
			currentThreadId = 0;
		}
		return currentThreads;
	}
	
	public ICDIThread getCurrentThread() throws CDIException {
		return currentThreads[currentThreadId];
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
		fDebugger.go(null);
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
		fDebugger.halt(null);
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
	
	public synchronized void setSuspended(boolean state) {
		suspended = state;
		notifyAll();
	}

	public ICDIExpression createExpression(String code) throws CDIException {
		ExpressionManager expMgr = ((Session)getSession()).getExpressionManager();
		return expMgr.createExpression(this, code);
	}

	public ICDIExpression[] getExpressions() throws CDIException {
		// Auto-generated method stub
		System.out.println("Target.getExpressions()");
		ExpressionManager expMgr = ((Session)getSession()).getExpressionManager();
		return expMgr.getExpressions(this);
	}

	public void destroyExpressions(ICDIExpression[] expressions) throws CDIException {
		// Auto-generated method stub
		System.out.println("Target.destroyExpressions()");
		ExpressionManager expMgr = ((Session)getSession()).getExpressionManager();
		expMgr.destroyExpressions(this, expressions);
	}

	public void destroyAllExpressions() throws CDIException {
		// Auto-generated method stub
		System.out.println("Target.destroyAllExpressions()");
		ExpressionManager expMgr = ((Session)getSession()).getExpressionManager();
		expMgr.destroyAllExpressions(this);
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

	public void setSourcePaths(String[] srcPaths) throws CDIException {
		// Auto-generated method stub
		System.out.println("Target.setSourcePaths()");
	}

	
	
	/* Unused methods, see org.eclipse.ptp.debug.external.cdi.Session */
	
	public ICDILineLocation createLineLocation(String file, int line) {
		// Auto-generated method stub
		System.out.println("Target.createLineLocation()");
		return null;
	}

	public ICDIFunctionLocation createFunctionLocation(String file, String function) {
		// Auto-generated method stub
		System.out.println("Target.createFunctionLocation()");
		return null;
	}

	public ICDIAddressLocation createAddressLocation(BigInteger address) {
		// Auto-generated method stub
		System.out.println("Target.createAddressLocation()");
		return null;
	}

	public ICDILineBreakpoint setLineBreakpoint(int type, ICDILineLocation location, ICDICondition condition, boolean deferred) throws CDIException {
		// Auto-generated method stub
		System.out.println("Target.setLineBreakpoint()");
		return null;
	}

	public ICDIFunctionBreakpoint setFunctionBreakpoint(int type, ICDIFunctionLocation location, ICDICondition condition, boolean deferred) throws CDIException {
		// Auto-generated method stub
		System.out.println("Target.setFunctionBreakpoint()");
		return null;
	}

	public ICDIAddressBreakpoint setAddressBreakpoint(int type, ICDIAddressLocation location, ICDICondition condition, boolean deferred) throws CDIException {
		// Auto-generated method stub
		System.out.println("Target.setAddressBreakpoint()");
		return null;
	}

	public ICDIWatchpoint setWatchpoint(int type, int watchType, String expression, ICDICondition condition) throws CDIException {
		// Auto-generated method stub
		System.out.println("Target.setWatchpoint()");
		return null;
	}

	public ICDIExceptionpoint setExceptionBreakpoint(String clazz, boolean stopOnThrow, boolean stopOnCatch) throws CDIException {
		// Auto-generated method stub
		System.out.println("Target.setExceptionBreakpoint()");
		return null;
	}

	public ICDIBreakpoint[] getBreakpoints() throws CDIException {
		// Auto-generated method stub
		System.out.println("Target.getBreakpoints()");
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

}
