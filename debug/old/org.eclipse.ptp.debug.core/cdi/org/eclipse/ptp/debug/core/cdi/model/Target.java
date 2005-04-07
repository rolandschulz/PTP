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
package org.eclipse.ptp.debug.core.cdi.model;

import java.io.IOException;
import java.lang.Runtime;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDICondition;
import org.eclipse.cdt.debug.core.cdi.ICDILocation;
import org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDIExceptionpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDIExpression;
import org.eclipse.cdt.debug.core.cdi.model.ICDIInstruction;
import org.eclipse.cdt.debug.core.cdi.model.ICDILocationBreakpoint;
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
import org.eclipse.ptp.debug.core.cdi.model.RegisterGroup;
import org.eclipse.ptp.debug.core.cdi.CDIResources;
import org.eclipse.ptp.debug.core.cdi.Session;
import org.eclipse.ptp.debug.core.cdi.SessionObject;

/**
 */
public class Target extends SessionObject implements ICDITarget {

	Thread[] noThreads = new Thread[0];
	Thread[] currentThreads;
	int currentThreadId;
	boolean terminated = false;
	boolean disconnected = false;
	boolean suspended = true;
	private Runtime runtime = Runtime.getRuntime();
	private Process proc;
	private TargetConfiguration fConfiguration;
	
	public Target(Session s) {
		super(s);
		Thread t = new Thread(this, 666);
		currentThreadId = 666;
		currentThreads = new Thread[1];
		currentThreads[0] = t;
		fConfiguration = new TargetConfiguration(this);
		try {
			proc = runtime.exec("true");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIObject#getTarget()
	 */
	public ICDITarget getTarget() {
		System.out.println("Target.getTarget()");
		return this;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#getCurrentThread()
	 */
	public ICDIThread getCurrentThread() throws CDIException {
		System.out.println("Target.getCurrentThread()");
		ICDIThread[] threads = getThreads();
		for (int i = 0; i < threads.length; i++) {
			Thread cthread = (Thread)threads[i];
			if (cthread.getId() == currentThreadId) {
				return cthread;
			}
		}
		return null;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#getThreads()
	 */
	public ICDIThread[] getThreads() throws CDIException {
		System.out.println("Target.getThreads()");
		return currentThreads;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#restart()
	 */
	public void restart() throws CDIException {
		System.out.println("Target.restart()");
		throw new CDIException(CDIResources.getString("cdi.Common.Not_implemented")); //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#stepInto()
	 */
	public void stepInto() throws CDIException {
		System.out.println("Target.stepInto()");
		stepInto(1);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIExecuteStep#stepInto(int)
	 */
	public void stepInto(int count) throws CDIException {
		System.out.println("Target.stepInto(" + count + ")");
		throw new CDIException(CDIResources.getString("cdi.Common.Not_implemented")); //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#stepIntoInstruction()
	 */
	public void stepIntoInstruction() throws CDIException {
		System.out.println("Target.stepIntoInstruction()");
		stepIntoInstruction(1);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIExecuteStep#stepIntoInstruction(int)
	 */
	public void stepIntoInstruction(int count) throws CDIException {
		System.out.println("Target.stepIntoInstruction(" + count + ")");
		throw new CDIException(CDIResources.getString("cdi.Common.Not_implemented")); //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#stepOver()
	 */
	public void stepOver() throws CDIException {
		System.out.println("Target.stepOver()");
		stepOver(1);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIExecuteStep#stepOver(int)
	 */
	public void stepOver(int count) throws CDIException {
		System.out.println("Target.stepOver(" + count + ")");
		throw new CDIException(CDIResources.getString("cdi.Common.Not_implemented")); //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#stepOverInstruction()
	 */
	public void stepOverInstruction() throws CDIException {
		System.out.println("Target.stepOverInstruction()");
		stepOverInstruction(1);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIExecuteStep#stepOverInstruction(int)
	 */
	public void stepOverInstruction(int count) throws CDIException {
		System.out.println("Target.stepOverInstruction(" + count + ")");
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#stepReturn()
	 */
	public void stepReturn() throws CDIException {
		System.out.println("Target.stepReturn()");
		throw new CDIException(CDIResources.getString("cdi.Common.Not_implemented")); //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#runUntil(ICDILocation)
	 */
	public void runUntil(ICDILocation location) throws CDIException {
		System.out.println("Target.runUntil()");
		stepUntil(location);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIExecuteStep#stepUntil(org.eclipse.cdt.debug.core.cdi.ICDILocation)
	 */
	public void stepUntil(ICDILocation location) throws CDIException {
		System.out.println("Target.stepUntil(loc)");
		throw new CDIException(CDIResources.getString("cdi.Common.Not_implemented")); //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#suspend()
	 */
	public void suspend() throws CDIException {
		System.out.println("Target.suspend()");
		throw new CDIException(CDIResources.getString("cdi.Common.Not_implemented")); //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#disconnect()
	 */
	public void disconnect() throws CDIException {
		System.out.println("Target.disconnect()");
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#resume()
	 */
	public void resume() throws CDIException {
		System.out.println("Target.resume()");
		resume(false);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIExecuteResume#resume(org.eclipse.cdt.debug.core.cdi.ICDILocation)
	 */
	public void resume(ICDILocation location) throws CDIException {
		System.out.println("Target.resume(loc)");
		jump(location);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIExecuteResume#resume(org.eclipse.cdt.debug.core.cdi.model.ICDISignal)
	 */
	public void resume(ICDISignal signal) throws CDIException {
		System.out.println("Target.resume(sig)");
		signal(signal);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIExecuteResume#resume(boolean)
	 */
	public void resume(boolean passSignal) throws CDIException {
		System.out.println("Target.resume(" + passSignal + ")");
		throw new CDIException(CDIResources.getString("cdi.Common.Not_implemented")); //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#jump(ICDILocation)
	 */
	public void jump(ICDILocation location) throws CDIException {
		System.out.println("Target.jump(loc)");
		throw new CDIException(CDIResources.getString("cdi.Common.Not_implemented")); //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#signal()
	 */
	public void signal() throws CDIException {
		System.out.println("Target.signal()");
		throw new CDIException(CDIResources.getString("cdi.Common.Not_implemented")); //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#signal(ICDISignal)
	 */
	public void signal(ICDISignal signal) throws CDIException {
		System.out.println("Target.signal(sig)");
	}

	public String evaluateExpressionToString(ICDIStackFrame frame, String expressionText) throws CDIException {
		System.out.println("Target.evaluateExpressionToString()");
		return expressionText;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#terminate()
	 */
	public void terminate() throws CDIException {
		System.out.println("Target.terminate()");
		terminated = true;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#isTerminated()
	 */
	public boolean isTerminated() {
		System.out.println("Target.isTerminated()");
		return terminated;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#isDisconnected()
	 */
	public boolean isDisconnected() {
		System.out.println("Target.isDisconnected()");
		return disconnected;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#isSuspended()
	 */
	public boolean isSuspended() {
		System.out.println("Target.isSuspended()");
		return suspended;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#getProcess()
	 */
	public Process getProcess() {
		System.out.println("Target.getProcess()");
		return proc;
	}

	// Implementaton of ICDIBreapointManagement.

	
	public ICDIBreakpoint[] getBreakpoints() throws CDIException {
		System.out.println("Target.getBreakpoints()");
		return null;
	}

	public ICDILocationBreakpoint setLocationBreakpoint(int type, ICDILocation location,
			ICDICondition condition, boolean deferred) throws CDIException {		
		System.out.println("Target.setLocationBreakpoint()");
		throw new CDIException(CDIResources.getString("cdi.Common.Not_implemented")); //$NON-NLS-1$
	}

	public ICDIWatchpoint setWatchpoint(int type, int watchType, String expression,
			ICDICondition condition) throws CDIException {
		System.out.println("Target.setWatchpoint()");
		throw new CDIException(CDIResources.getString("cdi.Common.Not_implemented")); //$NON-NLS-1$
	}

	public void deleteBreakpoints(ICDIBreakpoint[] breakpoints) throws CDIException {
		System.out.println("Target.deleteBreakpoints()");
		throw new CDIException(CDIResources.getString("cdi.Common.Not_implemented")); //$NON-NLS-1$
	}

	public void deleteAllBreakpoints() throws CDIException {
		System.out.println("Target.deleteAllBreakpoints()");
		throw new CDIException(CDIResources.getString("cdi.Common.Not_implemented")); //$NON-NLS-1$
	}

	public ICDIExceptionpoint setExceptionBreakpoint(String clazz, boolean stopOnThrow, boolean stopOnCatch) throws CDIException {
		System.out.println("Target.setExceptionBreakpoint()");
		throw new CDIException(CDIResources.getString("cdi.Common.Not_implemented")); //$NON-NLS-1$
	}

	 /* 
	  * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#createCondition(int, java.lang.String, String)
	  */
	public ICDICondition createCondition(int ignoreCount, String expression) {
		System.out.println("Target.createCondition()");
		return createCondition(ignoreCount, expression, null);
	}

	 /* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#createCondition(int, java.lang.String, String)
	 */
	public ICDICondition createCondition(int ignoreCount, String expression, String[] tids) {
		System.out.println("Target.createCondition()");
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#createLocation(java.lang.String, java.lang.String, int)
	 */
	public ICDILocation createLocation(String file, String function, int line) {
		System.out.println("Target.createLocation()");
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#createLocation(long)
	 */
	public ICDILocation createLocation(BigInteger address) {
		System.out.println("Target.createLocation()");
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#getRuntimeOptions()
	 */
	public ICDIRuntimeOptions getRuntimeOptions() {
		System.out.println("Target.getRuntimeOptions()");
		return new RuntimeOptions(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIExpressionManagement#createExpression(java.lang.String)
	 */
	public ICDIExpression createExpression(String code) throws CDIException {
		System.out.println("Target.createExpression()");
		throw new CDIException(CDIResources.getString("cdi.Common.Not_implemented")); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIExpressionManagement#getExpressions()
	 */
	public ICDIExpression[] getExpressions() throws CDIException {
		System.out.println("Target.getExpressions()");
		throw new CDIException(CDIResources.getString("cdi.Common.Not_implemented")); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIExpressionManagement#destroyExpression(org.eclipse.cdt.debug.core.cdi.model.ICDIExpression[])
	 */
	public void destroyExpressions(ICDIExpression[] expressions) throws CDIException {
		System.out.println("Target.destroyExpressions()");
		throw new CDIException(CDIResources.getString("cdi.Common.Not_implemented")); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIExpressionManagement#destroyAllExpression()
	 */
	public void destroyAllExpressions() throws CDIException {
		System.out.println("Target.destroyAllExpressions()");
		throw new CDIException(CDIResources.getString("cdi.Common.Not_implemented")); //$NON-NLS-1$
	}

	/**
	 * Returns the array of signals defined for this target.
	 * 
	 * @return the array of signals
	 * @throws CDIException on failure. Reasons include:
	 */
	public ICDISignal[] getSignals() throws CDIException {
		System.out.println("Target.getSignals()");
		throw new CDIException(CDIResources.getString("cdi.Common.Not_implemented")); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDISourceManagement#addSourcePaths(java.lang.String[])
	 */
	public void addSourcePaths(String[] srcPaths) throws CDIException {
		System.out.println("Target.addSourcePaths()");
		throw new CDIException(CDIResources.getString("cdi.Common.Not_implemented")); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDISourceManagement#getSourcePaths()
	 */
	public String[] getSourcePaths() throws CDIException {
		System.out.println("Target.getSourcePaths()");
		throw new CDIException(CDIResources.getString("cdi.Common.Not_implemented")); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDISourceManagement#getInstructions(java.math.BigInteger, java.math.BigInteger)
	 */
	public ICDIInstruction[] getInstructions(BigInteger startAddress, BigInteger endAddress) throws CDIException {
		System.out.println("Target.getInstructions()");
		throw new CDIException(CDIResources.getString("cdi.Common.Not_implemented")); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDISourceManagement#getInstructions(java.lang.String, int)
	 */
	public ICDIInstruction[] getInstructions(String filename, int linenum) throws CDIException {
		System.out.println("Target.getInstructions()");
		throw new CDIException(CDIResources.getString("cdi.Common.Not_implemented")); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDISourceManagement#getInstructions(java.lang.String, int, int)
	 */
	public ICDIInstruction[] getInstructions(String filename, int linenum, int lines) throws CDIException {
		System.out.println("Target.getConfiguration()");
		throw new CDIException(CDIResources.getString("cdi.Common.Not_implemented")); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDISourceManagement#getMixedInstructions(java.math.BigInteger, java.math.BigInteger)
	 */
	public ICDIMixedInstruction[] getMixedInstructions(BigInteger startAddress, BigInteger endAddress) throws CDIException {
		System.out.println("Target.getMixedInstructions()");
		throw new CDIException(CDIResources.getString("cdi.Common.Not_implemented")); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDISourceManagement#getMixedInstructions(java.lang.String, int)
	 */
	public ICDIMixedInstruction[] getMixedInstructions(String filename, int linenum) throws CDIException {
		System.out.println("Target.getMixedInstructions()");
		throw new CDIException(CDIResources.getString("cdi.Common.Not_implemented")); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDISourceManagement#getMixedInstructions(java.lang.String, int, int)
	 */
	public ICDIMixedInstruction[] getMixedInstructions(String filename, int linenum, int lines) throws CDIException {
		System.out.println("Target.getMixedInstructions()");
		throw new CDIException(CDIResources.getString("cdi.Common.Not_implemented")); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIMemoryBlockManagement#createMemoryBlock(java.lang.String, int)
	 */
	public ICDIMemoryBlock createMemoryBlock(String address, int length) throws CDIException {
		System.out.println("Target.createMemoryBlock()");
		throw new CDIException(CDIResources.getString("cdi.Common.Not_implemented")); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIMemoryBlockManagement#createMemoryBlock(java.math.BigInteger, int)
	 */
	public ICDIMemoryBlock createMemoryBlock(BigInteger address, int length) throws CDIException {
		System.out.println("Target.createMemoryBlock()");
		throw new CDIException(CDIResources.getString("cdi.Common.Not_implemented")); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIMemoryBlockManagement#removeBlocks(org.eclipse.cdt.debug.core.cdi.model.ICDIMemoryBlock[])
	 */
	public void removeBlocks(ICDIMemoryBlock[] memoryBlocks) throws CDIException {
		System.out.println("Target.removeBlocks()");
		throw new CDIException(CDIResources.getString("cdi.Common.Not_implemented")); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIMemoryBlockManagement#removeAllBlocks()
	 */
	public void removeAllBlocks() throws CDIException {
		System.out.println("Target.removeAllBlocks()");
		throw new CDIException(CDIResources.getString("cdi.Common.Not_implemented")); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIMemoryBlockManagement#getMemoryBlocks()
	 */
	public ICDIMemoryBlock[] getMemoryBlocks() throws CDIException {
		System.out.println("Target.getMemoryBlocks()");
		throw new CDIException(CDIResources.getString("cdi.Common.Not_implemented")); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDISharedLibraryManagement#getSharedLibraries()
	 */
	public ICDISharedLibrary[] getSharedLibraries() throws CDIException {
		System.out.println("Target.getSharedLibraries()");
		throw new CDIException(CDIResources.getString("cdi.Common.Not_implemented")); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#getGlobalVariableDescriptors(java.lang.String, java.lang.String, java.lang.String)
	 */
	public ICDIVariableDescriptor getGlobalVariableDescriptors(String filename, String function, String name) throws CDIException {
		System.out.println("Target.getGlobalVariableDescriptors()");
		throw new CDIException(CDIResources.getString("cdi.Common.Not_implemented")); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#getRegisterGroups()
	 */
	public ICDIRegisterGroup[] getRegisterGroups() throws CDIException {
		System.out.println("Target.getRegisterGroups()");
		RegisterGroup group = new RegisterGroup(this, "Main"); //$NON-NLS-1$
		return new ICDIRegisterGroup[] { group };
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#getConfiguration()
	 */
	public ICDITargetConfiguration getConfiguration() {
		System.out.println("Target.getConfiguration()");
		return fConfiguration;
	}

}
