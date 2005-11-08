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
import java.util.ArrayList;
import java.util.List;
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
import org.eclipse.ptp.core.IPProcess;
import org.eclipse.ptp.debug.core.cdi.model.IPCDITarget;
import org.eclipse.ptp.debug.external.IAbstractDebugger;
import org.eclipse.ptp.debug.external.cdi.BreakpointManager;
import org.eclipse.ptp.debug.external.cdi.ExpressionManager;
import org.eclipse.ptp.debug.external.cdi.Session;
import org.eclipse.ptp.debug.external.cdi.SessionObject;
import org.eclipse.ptp.debug.external.cdi.VariableManager;
import org.eclipse.ptp.debug.external.cdi.model.variable.GlobalVariableDescriptor;
import org.eclipse.ptp.debug.external.commands.EvaluteExpressionCommand;
import org.eclipse.ptp.debug.external.commands.GoCommand;
import org.eclipse.ptp.debug.external.commands.HaltCommand;
import org.eclipse.ptp.debug.external.commands.KillCommand;
import org.eclipse.ptp.debug.external.commands.StepIntoCommand;
import org.eclipse.ptp.debug.external.commands.StepOverCommand;

public class Target extends SessionObject implements IPCDITarget {
	ICDITargetConfiguration fConfiguration;
	Thread[] noThreads = new Thread[0];
	Thread[] currentThreads;
	int currentThreadId;
	String fEndian = null;
	boolean suspended = true;
	private int task_id = -1;
	
	public Target(Session session, int task_id) {
		super(session);
		this.task_id = task_id;
		currentThreads = noThreads;
	}
	
	public IAbstractDebugger getDebugger() {
		return ((Session)getSession()).getDebugger();
	}
	public void setConfiguration(ICDITargetConfiguration configuration) {
		fConfiguration = configuration;
	}
	public ICDITarget getTarget() {
		return this;
	}
	public void setCurrentThread(ICDIThread cthread) throws CDIException {
		if (cthread instanceof Thread) {
			setCurrentThread(cthread, true);
		} else {
			throw new CDIException("Target - Unknown_thread");
		}
	}
	public void setCurrentThread(ICDIThread cthread, boolean doUpdate) throws CDIException {
		if (cthread instanceof Thread) {
			setCurrentThread((Thread)cthread, doUpdate);
		} else {
			throw new CDIException("Target - Unknown_thread");
		}
	}
	public synchronized void setSupended(boolean state) {
		suspended = state;
		notifyAll();
	}
	public void setCurrentThread(Thread cthread, boolean doUpdate) throws CDIException {
		int id = cthread.getId();
		if (id == 0) {
			return;
		}
		if (currentThreadId != id) {
			currentThreadId = cthread.getStackFrameCount();
			if (doUpdate) {
				Session session = (Session)getSession();
				VariableManager varMgr = session.getVariableManager();
				if (varMgr.isAutoUpdate()) {
					varMgr.update(this);
				}
			}
		}
		if (currentThreadId != id) {
			throw new CDIException("Cannot switch to thread " + id);
		}
	}
	public synchronized void updateState(int newThreadId) {
		Thread[] oldThreads = currentThreads;
		currentThreadId = newThreadId;
		try {
			currentThreads = getCThreads();
		} catch (CDIException e) {
			currentThreads = noThreads;
		}

		List cList = new ArrayList(currentThreads.length);
		for (int i = 0; i < currentThreads.length; i++) {
			boolean found = false;
			for (int j = 0; j < oldThreads.length; j++) {
				if (currentThreads[i].getId() == oldThreads[j].getId()) {
					oldThreads[j].clearState();
					currentThreads[i] = oldThreads[j];
					found = true;
					break;
				}
			}
			if (!found) {
				cList.add(new Integer(currentThreads[i].getId()));
			}
		}
		if (!cList.isEmpty()) {
			//TODO - thread created event?
		}
		List dList = new ArrayList(oldThreads.length);
		for (int i = 0; i < oldThreads.length; i++) {
			boolean found = false;
			for (int j = 0; j < currentThreads.length; j++) {
				if (currentThreads[j].getId() == oldThreads[i].getId()) {
					found = true;
					break;
				}
			}
			if (!found) {
				dList.add(new Integer(oldThreads[i].getId()));
			}
		}
		if (!dList.isEmpty()) {
			//TODO - thread created event?
		}
	}
	private Thread[] getCThreads() throws CDIException {
		Thread[] cthreads = noThreads;
		//TODO - implement list threads
		//Session session = (Sessoin)getSession();
		//cthreads = debugger.listTheads(session.createBitList(getTargetID()));
		cthreads = new Thread[]{new Thread(this, 0)};
		if (currentThreadId == 0 && cthreads.length > 0) {
			currentThreadId = cthreads[0].getId();
		}
		return cthreads;
	}
	public ICDIThread getCurrentThread() throws CDIException {
		ICDIThread[] threads = getThreads();
		for (int i = 0; i < threads.length; i++) {
			Thread cthread = (Thread)threads[i];
			if (cthread.getId() == currentThreadId) {
				return cthread;
			}
		}
		return null;
	}
	public synchronized ICDIThread[] getThreads() throws CDIException {
		if (currentThreads.length == 0) {
			currentThreads = getCThreads();
		}
		return currentThreads;
	}
	public ICDIThread getThread(int tid) throws CDIException {
		Thread th = null;
		if (currentThreads != null) {
			for (int i = 0; i < currentThreads.length; i++) {
				Thread cthread = currentThreads[i];
				if (cthread.getId() == tid) {
					th = cthread;
					break;
				}
			}
		}
		return th;
	}
	public void restart() throws CDIException {
		getDebugger().restart();
	}
	public void stepInto() throws CDIException {
		stepInto(1);
	}
	public void stepInto(int count) throws CDIException {
		getDebugger().postCommand(new StepIntoCommand(((Session)getSession()).createBitList(getTargetID()), count));
		//getDebugger().steppingInto(((Session)getSession()).createBitList(getTargetID()), count);
	}
	public void stepIntoInstruction() throws CDIException {
		stepIntoInstruction(1);
	}
	public void stepIntoInstruction(int count) throws CDIException {
		//TODO - implement step into instruction
		//getDebugger().stepIntoInstrunction(count);
		throw new CDIException("Not implement yet - Target: stepIntoInstruction");
	}
	public void stepOver() throws CDIException {
		stepOver(1);
	}
	public void stepOver(int count) throws CDIException {
		getDebugger().postCommand(new StepOverCommand(((Session)getSession()).createBitList(getTargetID()), count));
		//getDebugger().steppingOver(((Session)getSession()).createBitList(getTargetID()), count);
	}
	public void stepOverInstruction() throws CDIException {
		stepOverInstruction(1);
	}
	public void stepOverInstruction(int count) throws CDIException {
		//TODO - implement step over instruction
		//getDebugger().stepOverInstrunction(count);
		throw new CDIException("Not implement yet - Target: stepOverInstruction");
	}
	public void stepReturn() throws CDIException {
		((Thread)getCurrentThread()).getCurrentStackFrame().stepReturn();
	}
	public void runUntil(ICDILocation location) throws CDIException {
		stepUntil(location);
	}
	public void stepUntil(ICDILocation location) throws CDIException {
		String file = "";
		String func = "";
		String addr = "";
		int line = -1;
		if (location instanceof ICDILineLocation) {
			ICDILineLocation lineLocation = (ICDILineLocation)location;
			if (lineLocation.getFile() != null && lineLocation.getFile().length() > 0) {
				file = lineLocation.getFile(); 
				line = lineLocation.getLineNumber();
			}
		}
		else if (location instanceof ICDIFunctionLocation) {
			ICDIFunctionLocation funcLocation = (ICDIFunctionLocation)location;
			if (funcLocation.getFunction() != null && funcLocation.getFunction().length() > 0) {
				func = funcLocation.getFunction();
			}
			if (funcLocation.getFile() != null && funcLocation.getFile().length() > 0) {
				file = funcLocation.getFile();
			}
		}
		else if (location instanceof ICDIAddressLocation) {
			ICDIAddressLocation addrLocation = (ICDIAddressLocation) location;
			if (! addrLocation.getAddress().equals(BigInteger.ZERO)) {
				addr = "*0x" + addrLocation.getAddress().toString(16);
			}
		}
		//TODO - implement step until location
		//getDebugger().stepUntil(file, func, addr, line);
		throw new CDIException("Not implement yet - stepUntil(location)");
	}
	public void suspend() throws CDIException {
		getDebugger().postCommand(new HaltCommand(((Session)getSession()).createBitList(getTargetID())));
		//getDebugger().suspend(((Session)getSession()).createBitList(getTargetID()));
	}
	public void disconnect() throws CDIException {
		//Do nothing
	}
	public void resume() throws CDIException {
		resume(false);
	}
	public void resume(ICDILocation location) throws CDIException {
		jump(location);
	}
	public void resume(ICDISignal signal) throws CDIException {
		signal(signal);
	}
	public void resume(boolean passSignal) throws CDIException {
		String state = getPProcess().getStatus();
		if (state.equals(IPProcess.RUNNING)) {
			throw new CDIException("The process is already running");
		}
		else if (state.equals(IPProcess.STOPPED)) {
			if (passSignal) {
				signal();
			} else {
				continuation();
			}
		} else if (state.equals(IPProcess.EXITED)) {
			restart();
		} else {
			restart();
		}
	}
	public void continuation() throws CDIException {
		getDebugger().postCommand(new GoCommand(((Session)getSession()).createBitList(getTargetID())));
		//getDebugger().resume(((Session)getSession()).createBitList(getTargetID()));
	}
	public void jump(ICDILocation location) throws CDIException {
		String file = "";
		String func = "";
		String addr = "";
		int line = -1;
		if (location instanceof ICDILineLocation) {
			ICDILineLocation lineLocation = (ICDILineLocation)location;
			if (lineLocation.getFile() != null && lineLocation.getFile().length() > 0) {
				file = lineLocation.getFile(); 
				line = lineLocation.getLineNumber();
			}
		}
		else if (location instanceof ICDIFunctionLocation) {
			ICDIFunctionLocation funcLocation = (ICDIFunctionLocation)location;
			if (funcLocation.getFunction() != null && funcLocation.getFunction().length() > 0) {
				func = funcLocation.getFunction();
			}
			if (funcLocation.getFile() != null && funcLocation.getFile().length() > 0) {
				file = funcLocation.getFile();
			}
		}
		else if (location instanceof ICDIAddressLocation) {
			ICDIAddressLocation addrLocation = (ICDIAddressLocation) location;
			if (! addrLocation.getAddress().equals(BigInteger.ZERO)) {
				addr = "*0x" + addrLocation.getAddress().toString(16);
			}
		}
		//TODO - implement jump location
		//getDebugger().jump(file, func, addr, line);
		throw new CDIException("Not implement yet - jump(location)");
	}
	public void signal() throws CDIException {
		//TODO - implement signal
		//getDebugger().singal();
		throw new CDIException("Not implement yet - signal");
	}
	public void signal(ICDISignal signal) throws CDIException {
		//TODO - implement signal(ICDISignal)
		//getDebugger().singal(signal.getName());
		throw new CDIException("Not implement yet - signal(ICDISignal)");
	}
	public String evaluateExpressionToString(ICDIStackFrame frame, String expressionText) throws CDIException {
		Target target = (Target)frame.getTarget();
		Thread currentThread = (Thread)target.getCurrentThread();
		StackFrame currentFrame = currentThread.getCurrentStackFrame();
		target.setCurrentThread(frame.getThread(), false);
		((Thread)frame.getThread()).setCurrentStackFrame((StackFrame)frame, false);
		try {
			Session session = (Session) target.getSession();
			EvaluteExpressionCommand command = new EvaluteExpressionCommand(session.createBitList(target.getTargetID()), expressionText);
			session.getDebugger().postCommand(command);
			return command.getExpressionValue();
		} finally {
			target.setCurrentThread(currentThread, false);
			currentThread.setCurrentStackFrame(currentFrame, false);
		}
	}
	public void terminate() throws CDIException {
		getDebugger().postCommand(new KillCommand(((Session)getSession()).createBitList(getTargetID())));
	}
	public boolean isTerminated() {
		return getPProcess().isTerminated();
	}
	public boolean isDisconnected() {
		return isTerminated();
	}
	public boolean isSuspended() {
		return getPProcess().getStatus().equals(IPProcess.STOPPED);
	}
	public boolean isRunning() {
		return getPProcess().getStatus().equals(IPProcess.RUNNING);
	}
	public Process getProcess() {
		return null;
	}
	public ICDILineBreakpoint setLineBreakpoint(int type, ICDILineLocation location, ICDICondition condition, boolean deferred) throws CDIException {
		Session session = (Session)getSession();
		BreakpointManager bMgr = session.getBreakpointManager();
		return bMgr.setLineBreakpoint(session.createBitList(getTargetID()), type, location, condition, deferred);
	}
	public ICDIFunctionBreakpoint setFunctionBreakpoint(int type, ICDIFunctionLocation location, ICDICondition condition, boolean deferred) throws CDIException {		
		Session session = (Session)getSession();
		BreakpointManager bMgr = session.getBreakpointManager();
		return bMgr.setFunctionBreakpoint(session.createBitList(getTargetID()), type, location, condition, deferred);
	}
	public ICDIAddressBreakpoint setAddressBreakpoint(int type, ICDIAddressLocation location, ICDICondition condition, boolean deferred) throws CDIException {
		Session session = (Session)getSession();
		BreakpointManager bMgr = session.getBreakpointManager();
		return bMgr.setAddressBreakpoint(session.createBitList(getTargetID()), type, location, condition, deferred);
	}
	public ICDIWatchpoint setWatchpoint(int type, int watchType, String expression, ICDICondition condition) throws CDIException {
		Session session = (Session)getSession();
		BreakpointManager bMgr = session.getBreakpointManager();
		return bMgr.setWatchpoint(session.createBitList(getTargetID()), type, watchType, expression, condition);
	}
	public ICDIExceptionpoint setExceptionBreakpoint(String clazz, boolean stopOnThrow, boolean stopOnCatch) throws CDIException {
		throw new CDIException("Not implemented yet setExceptionBreakpoint");
	}
	public ICDIBreakpoint[] getBreakpoints() throws CDIException {
		throw new CDIException("Not implemented yet - Target: getBreakpoints");
	}
	public void deleteBreakpoints(ICDIBreakpoint[] breakpoints) throws CDIException {
		throw new CDIException("Not implemented yet - Target: deleteBreakpoints");
	}
	public void deleteAllBreakpoints() throws CDIException {
		throw new CDIException("Not implemented yet - Target: deleteAllBreakpoints");
	}
	public ICDICondition createCondition(int ignoreCount, String expression) {
		return createCondition(ignoreCount, expression, null);
	}
	public ICDICondition createCondition(int ignoreCount, String expression, String[] tids) {
		BreakpointManager bMgr = ((Session)getSession()).getBreakpointManager();
		return bMgr.createCondition(ignoreCount, expression, tids);
	}
	public ICDILineLocation createLineLocation(String file, int line) {
		BreakpointManager bMgr = ((Session)getSession()).getBreakpointManager();
		return bMgr.createLineLocation(file, line);
	}
	public ICDIFunctionLocation createFunctionLocation(String file, String function) {
		BreakpointManager bMgr = ((Session)getSession()).getBreakpointManager();
		return bMgr.createFunctionLocation(file, function);
	}
	public ICDIAddressLocation createAddressLocation(BigInteger address) {
		BreakpointManager bMgr = ((Session)getSession()).getBreakpointManager();
		return bMgr.createAddressLocation(address);
	}
	public ICDIRuntimeOptions getRuntimeOptions() {
		//TODO implement later
		//return new RuntimeOptions(this);
		return null;
	}
	public ICDIExpression createExpression(String code) throws CDIException {
		ExpressionManager expMgr = ((Session)getSession()).getExpressionManager();
		return expMgr.createExpression(this, code);
	}
	public ICDIExpression[] getExpressions() throws CDIException {
		ExpressionManager expMgr = ((Session)getSession()).getExpressionManager();
		return expMgr.getExpressions(this);
	}
	public void destroyExpressions(ICDIExpression[] expressions) throws CDIException {
		ExpressionManager expMgr = ((Session)getSession()).getExpressionManager();
		expMgr.destroyExpressions(this, expressions);
	}	
	public void destroyAllExpressions() throws CDIException {
		ExpressionManager expMgr = ((Session)getSession()).getExpressionManager();
		expMgr.destroyAllExpressions(this);
	}
	public ICDISignal[] getSignals() throws CDIException {
		throw new CDIException("Not implemented yet - Target: getSignals");
	}
	public void setSourcePaths(String[] srcPaths) throws CDIException {
		//throw new CDIException("Not implemented yet - Target: setSourcePaths");
	}
	public String[] getSourcePaths() throws CDIException {
		throw new CDIException("Not implemented yet - Target: getSourcePaths");
	}
	public ICDIInstruction[] getInstructions(BigInteger startAddress, BigInteger endAddress) throws CDIException {
		throw new CDIException("Not implemented yet - Target: getInstructions");
	}
	public ICDIInstruction[] getInstructions(String filename, int linenum) throws CDIException {
		throw new CDIException("Not implemented yet - Target: getInstructions");
	}
	public ICDIInstruction[] getInstructions(String filename, int linenum, int lines) throws CDIException {
		throw new CDIException("Not implemented yet - Target: getInstructions");
	}
	public ICDIMixedInstruction[] getMixedInstructions(BigInteger startAddress, BigInteger endAddress) throws CDIException {
		throw new CDIException("Not implemented yet - Target: getMixedInstructions");
	}
	public ICDIMixedInstruction[] getMixedInstructions(String filename, int linenum) throws CDIException {
		throw new CDIException("Not implemented yet - Target: getMixedInstructions");
	}
	public ICDIMixedInstruction[] getMixedInstructions(String filename, int linenum, int lines) throws CDIException {
		throw new CDIException("Not implemented yet - Target: getMixedInstructions");
	}
	public ICDIMemoryBlock createMemoryBlock(String address, int units, int wordSize) throws CDIException {
		throw new CDIException("Not implemented yet - Target: createMemoryBlock");
	}
	public void removeBlocks(ICDIMemoryBlock[] memoryBlocks) throws CDIException {
		throw new CDIException("Not implemented yet - Target: removeBlocks");
	}
	public void removeAllBlocks() throws CDIException {
		throw new CDIException("Not implemented yet - Target: removeAllBlocks");
	}
	public ICDIMemoryBlock[] getMemoryBlocks() throws CDIException {
		throw new CDIException("Not implemented yet - Target: getMemoryBlocks");
	}
	public ICDISharedLibrary[] getSharedLibraries() throws CDIException {
		throw new CDIException("Not implemented yet - Target: getSharedLibraries");
	}
	public ICDIGlobalVariableDescriptor getGlobalVariableDescriptors(String filename, String function, String name) throws CDIException {
		//VariableManager varMgr = ((Session)getSession()).getVariableManager();
		//return varMgr.getGlobalVariableDescriptor(this, filename, function, name);
		throw new CDIException("Not implemented yet - Target: getGlobalVariableDescriptors");
	}
	public ICDIRegisterGroup[] getRegisterGroups() throws CDIException {
		throw new CDIException("Not implemented yet - Target: getRegisterGroups");
	}
	public ICDITargetConfiguration getConfiguration() {
		if (fConfiguration == null) {
			fConfiguration = new TargetConfiguration(this);				
		}		
		return fConfiguration;
	}
	public ICDIGlobalVariable createGlobalVariable(ICDIGlobalVariableDescriptor varDesc) throws CDIException {
		if (varDesc instanceof GlobalVariableDescriptor) {
			VariableManager varMgr = ((Session)getSession()).getVariableManager();
			return varMgr.createGlobalVariable((GlobalVariableDescriptor)varDesc);
		}
		return null;
	}
	public ICDIRegister createRegister(ICDIRegisterDescriptor varDesc) throws CDIException {
		throw new CDIException("Not implemented yet - Target: createRegister");
	}
	
	public IPProcess getPProcess() {
		return getDebugger().getProcess(task_id);
	}
	public int getTargetID() {
		return task_id;
	}
}
