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
import org.eclipse.cdt.debug.core.cdi.model.ICDIGlobalVariable;
import org.eclipse.cdt.debug.core.cdi.model.ICDIGlobalVariableDescriptor;
import org.eclipse.cdt.debug.core.cdi.model.ICDIInstruction;
import org.eclipse.cdt.debug.core.cdi.model.ICDILineBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDIMemoryBlock;
import org.eclipse.cdt.debug.core.cdi.model.ICDIMixedInstruction;
import org.eclipse.cdt.debug.core.cdi.model.ICDIRegister;
import org.eclipse.cdt.debug.core.cdi.model.ICDIRegisterDescriptor;
import org.eclipse.cdt.debug.core.cdi.model.ICDIRegisterGroup;
import org.eclipse.cdt.debug.core.cdi.model.ICDIRuntimeOptions;
import org.eclipse.cdt.debug.core.cdi.model.ICDISharedLibrary;
import org.eclipse.cdt.debug.core.cdi.model.ICDISignal;
import org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDITargetConfiguration;
import org.eclipse.cdt.debug.core.cdi.model.ICDIThread;
import org.eclipse.cdt.debug.core.cdi.model.ICDIWatchpoint;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIDebugProcess;
import org.eclipse.ptp.debug.core.cdi.model.IPCDITarget;
import org.eclipse.ptp.debug.external.IAbstractDebugger;
import org.eclipse.ptp.debug.external.PTPDebugExternalPlugin;
import org.eclipse.ptp.debug.external.cdi.ExpressionManager;
import org.eclipse.ptp.debug.external.cdi.Session;
import org.eclipse.ptp.debug.external.cdi.SessionObject;
import org.eclipse.ptp.debug.external.cdi.VariableManager;
import org.eclipse.ptp.debug.external.cdi.model.variable.GlobalVariableDescriptor;

public class Target extends SessionObject implements IPCDITarget {
	
	private TargetConfiguration fConfiguration;
	private IAbstractDebugger fDebugger;
	
	Thread[] currentThreads;
	int currentThreadId;
	boolean suspended = true;
	
	int targetId; /* synonymous with the process number/id */
	IPCDIDebugProcess debugProcess;
	
	public Target(Session session, IAbstractDebugger debugger, int tId) {
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
		PTPDebugExternalPlugin.getDefault().getLogger().finer("");
		// Target target = (Target) frame.getTarget();
		// Session session = (Session) target.getSession();
		// IDebugger debugger = session.getDebugger();
		// DebugProcessSet newSet = new DebugProcessSet(session, target.getTargetId());

		return createExpression(expressionText).getValue(frame).getValueString();
	}

	public ICDIGlobalVariableDescriptor getGlobalVariableDescriptors(String filename, String function, String name) throws CDIException {
		PTPDebugExternalPlugin.getDefault().getLogger().finer("");
		VariableManager varMgr = ((Session)getSession()).getVariableManager();
		return varMgr.getGlobalVariableDescriptor(this, filename, function, name);
	}
	
	public ICDIRegisterGroup[] getRegisterGroups() throws CDIException {
		PTPDebugExternalPlugin.getDefault().getLogger().finer("");
		return null;
	}

	public boolean isTerminated() {
		PTPDebugExternalPlugin.getDefault().getLogger().finer("");
		return getDebugProcess().isTerminated();
	}

	public void terminate() throws CDIException {
		PTPDebugExternalPlugin.getDefault().getLogger().finer("");
		fDebugger.killAction(((Session) getSession()).createBitList(getTargetId()));
	}

	public boolean isDisconnected() {
		PTPDebugExternalPlugin.getDefault().getLogger().finer("");
		return false;
	}

	public void disconnect() throws CDIException {
		PTPDebugExternalPlugin.getDefault().getLogger().finer("");
		//fDebugger.detach(null);
		fDebugger.killAction(((Session) getSession()).createBitList(getTargetId()));
	}

	public void restart() throws CDIException {
		PTPDebugExternalPlugin.getDefault().getLogger().finer("");
		fDebugger.restartAction();
	}

	public void resume() throws CDIException {
		PTPDebugExternalPlugin.getDefault().getLogger().finer("");
		resume(false);
	}

	public void stepOver() throws CDIException {
		stepOver(1);
	}

	public void stepInto() throws CDIException {
		stepInto(1);
	}

	public void stepOverInstruction() throws CDIException {
		PTPDebugExternalPlugin.getDefault().getLogger().finer("");
	}

	public void stepIntoInstruction() throws CDIException {
		PTPDebugExternalPlugin.getDefault().getLogger().finer("");
	}

	public void runUntil(ICDILocation location) throws CDIException {
		PTPDebugExternalPlugin.getDefault().getLogger().finer("");
	}

	public void jump(ICDILocation location) throws CDIException {
		PTPDebugExternalPlugin.getDefault().getLogger().finer("");
	}

	public void signal() throws CDIException {
		PTPDebugExternalPlugin.getDefault().getLogger().finer("");
	}

	public void signal(ICDISignal signal) throws CDIException {
		PTPDebugExternalPlugin.getDefault().getLogger().finer("");
	}

	public ICDIRuntimeOptions getRuntimeOptions() {
		PTPDebugExternalPlugin.getDefault().getLogger().finer("");
		return null;
	}

	public ICDICondition createCondition(int ignoreCount, String expression) {
		PTPDebugExternalPlugin.getDefault().getLogger().finer("");
		return null;
	}

	public ICDICondition createCondition(int ignoreCount, String expression, String[] threadIds) {
		PTPDebugExternalPlugin.getDefault().getLogger().finer("");
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
		PTPDebugExternalPlugin.getDefault().getLogger().finer("");
		fDebugger.stepOverAction(((Session) getSession()).createBitList(getTargetId()), count);
	}

	public void stepOverInstruction(int count) throws CDIException {
		PTPDebugExternalPlugin.getDefault().getLogger().finer("");
	}

	public void stepInto(int count) throws CDIException {
		PTPDebugExternalPlugin.getDefault().getLogger().finer("");
		fDebugger.stepIntoAction(((Session) getSession()).createBitList(getTargetId()), count);
	}

	public void stepIntoInstruction(int count) throws CDIException {
		PTPDebugExternalPlugin.getDefault().getLogger().finer("");
	}

	public void stepUntil(ICDILocation location) throws CDIException {
		PTPDebugExternalPlugin.getDefault().getLogger().finer("");
		fDebugger.stepFinishAction(((Session) getSession()).createBitList(getTargetId()), 0);
	}

	public void resume(boolean passSignal) throws CDIException {
		PTPDebugExternalPlugin.getDefault().getLogger().finer("");
		fDebugger.goAction(((Session) getSession()).createBitList(getTargetId()));
	}

	public void resume(ICDILocation location) throws CDIException {
		PTPDebugExternalPlugin.getDefault().getLogger().finer("");
	}

	public void resume(ICDISignal signal) throws CDIException {
		PTPDebugExternalPlugin.getDefault().getLogger().finer("");
	}

	public void suspend() throws CDIException {
		PTPDebugExternalPlugin.getDefault().getLogger().finer("");
		fDebugger.haltAction(null);
	}

	public boolean isSuspended() {
		PTPDebugExternalPlugin.getDefault().getLogger().finer("");
		return getDebugProcess().isSuspended();
	}

	public ICDISignal[] getSignals() throws CDIException {
		PTPDebugExternalPlugin.getDefault().getLogger().finer("");
		return null;
	}

	public ICDITarget getTarget() {
		PTPDebugExternalPlugin.getDefault().getLogger().finer("");
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
		PTPDebugExternalPlugin.getDefault().getLogger().finer("");
		ExpressionManager expMgr = ((Session)getSession()).getExpressionManager();
		return expMgr.getExpressions(this);
	}

	public void destroyExpressions(ICDIExpression[] expressions) throws CDIException {
		PTPDebugExternalPlugin.getDefault().getLogger().finer("");
		ExpressionManager expMgr = ((Session)getSession()).getExpressionManager();
		expMgr.destroyExpressions(this, expressions);
	}

	public void destroyAllExpressions() throws CDIException {
		PTPDebugExternalPlugin.getDefault().getLogger().finer("");
		ExpressionManager expMgr = ((Session)getSession()).getExpressionManager();
		expMgr.destroyAllExpressions(this);
	}

	public void addSourcePaths(String[] srcPaths) throws CDIException {
		PTPDebugExternalPlugin.getDefault().getLogger().finer("");
	}

	public String[] getSourcePaths() throws CDIException {
		PTPDebugExternalPlugin.getDefault().getLogger().finer("");
		return null;
	}

	public ICDIInstruction[] getInstructions(BigInteger startAddress, BigInteger endAddress) throws CDIException {
		PTPDebugExternalPlugin.getDefault().getLogger().finer("");
		return null;
	}

	public ICDIInstruction[] getInstructions(String filename, int linenum) throws CDIException {
		PTPDebugExternalPlugin.getDefault().getLogger().finer("");
		return null;
	}

	public ICDIInstruction[] getInstructions(String filename, int linenum, int lines) throws CDIException {
		PTPDebugExternalPlugin.getDefault().getLogger().finer("");
		return null;
	}

	public ICDIMixedInstruction[] getMixedInstructions(BigInteger startAddress, BigInteger endAddress) throws CDIException {
		PTPDebugExternalPlugin.getDefault().getLogger().finer("");
		return null;
	}

	public ICDIMixedInstruction[] getMixedInstructions(String filename, int linenum) throws CDIException {
		PTPDebugExternalPlugin.getDefault().getLogger().finer("");
		return null;
	}

	public ICDIMixedInstruction[] getMixedInstructions(String filename, int linenum, int lines) throws CDIException {
		PTPDebugExternalPlugin.getDefault().getLogger().finer("");
		return null;
	}

	public ICDISharedLibrary[] getSharedLibraries() throws CDIException {
		PTPDebugExternalPlugin.getDefault().getLogger().finer("");
		return null;
	}

	public ICDIMemoryBlock createMemoryBlock(String address, int units, int wordSize) throws CDIException {
		PTPDebugExternalPlugin.getDefault().getLogger().finer("");
		return null;
	}

	public void removeBlocks(ICDIMemoryBlock[] memoryBlocks) throws CDIException {
		PTPDebugExternalPlugin.getDefault().getLogger().finer("");
	}

	public void removeAllBlocks() throws CDIException {
		PTPDebugExternalPlugin.getDefault().getLogger().finer("");
	}

	public ICDIMemoryBlock[] getMemoryBlocks() throws CDIException {
		PTPDebugExternalPlugin.getDefault().getLogger().finer("");
		return null;
	}

	public void setSourcePaths(String[] srcPaths) throws CDIException {
		PTPDebugExternalPlugin.getDefault().getLogger().finer("");
	}

	
	
	/* Unused methods, see org.eclipse.ptp.debug.external.cdi.Session */
	
	public ICDILineLocation createLineLocation(String file, int line) {
		PTPDebugExternalPlugin.getDefault().getLogger().finer("");
		return null;
	}

	public ICDIFunctionLocation createFunctionLocation(String file, String function) {
		PTPDebugExternalPlugin.getDefault().getLogger().finer("");
		return null;
	}

	public ICDIAddressLocation createAddressLocation(BigInteger address) {
		PTPDebugExternalPlugin.getDefault().getLogger().finer("");
		return null;
	}

	public ICDILineBreakpoint setLineBreakpoint(int type, ICDILineLocation location, ICDICondition condition, boolean deferred) throws CDIException {
		PTPDebugExternalPlugin.getDefault().getLogger().finer("");
		return null;
	}

	public ICDIFunctionBreakpoint setFunctionBreakpoint(int type, ICDIFunctionLocation location, ICDICondition condition, boolean deferred) throws CDIException {
		PTPDebugExternalPlugin.getDefault().getLogger().finer("");
		return null;
	}

	public ICDIAddressBreakpoint setAddressBreakpoint(int type, ICDIAddressLocation location, ICDICondition condition, boolean deferred) throws CDIException {
		PTPDebugExternalPlugin.getDefault().getLogger().finer("");
		return null;
	}

	public ICDIWatchpoint setWatchpoint(int type, int watchType, String expression, ICDICondition condition) throws CDIException {
		PTPDebugExternalPlugin.getDefault().getLogger().finer("");
		return null;
	}

	public ICDIExceptionpoint setExceptionBreakpoint(String clazz, boolean stopOnThrow, boolean stopOnCatch) throws CDIException {
		PTPDebugExternalPlugin.getDefault().getLogger().finer("");
		return null;
	}

	public ICDIBreakpoint[] getBreakpoints() throws CDIException {
		PTPDebugExternalPlugin.getDefault().getLogger().finer("");
		return null;
	}

	public void deleteBreakpoints(ICDIBreakpoint[] breakpoints) throws CDIException {
		PTPDebugExternalPlugin.getDefault().getLogger().finer("");
	}

	public void deleteAllBreakpoints() throws CDIException {
		PTPDebugExternalPlugin.getDefault().getLogger().finer("");
	}

	public ICDIGlobalVariable createGlobalVariable(ICDIGlobalVariableDescriptor varDesc) throws CDIException {
		PTPDebugExternalPlugin.getDefault().getLogger().finer("");
		if (varDesc instanceof GlobalVariableDescriptor) {
			VariableManager varMgr = ((Session)getSession()).getVariableManager();
			return varMgr.createGlobalVariable((GlobalVariableDescriptor)varDesc);
		}
		return null;
	}

	public ICDIRegister createRegister(ICDIRegisterDescriptor varDesc) throws CDIException {
		PTPDebugExternalPlugin.getDefault().getLogger().finer("");
		return null;
	}

}
