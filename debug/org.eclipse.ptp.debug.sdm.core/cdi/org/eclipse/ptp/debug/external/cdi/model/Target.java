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
package org.eclipse.ptp.debug.external.cdi.model;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.cdt.debug.core.cdi.ICDIAddressLocation;
import org.eclipse.cdt.debug.core.cdi.ICDICondition;
import org.eclipse.cdt.debug.core.cdi.ICDIFunctionLocation;
import org.eclipse.cdt.debug.core.cdi.ICDILineLocation;
import org.eclipse.cdt.debug.core.cdi.ICDILocation;
import org.eclipse.cdt.debug.core.cdi.model.ICDIInstruction;
import org.eclipse.cdt.debug.core.cdi.model.ICDIMemoryBlock;
import org.eclipse.cdt.debug.core.cdi.model.ICDIMixedInstruction;
import org.eclipse.cdt.debug.core.cdi.model.ICDIRegister;
import org.eclipse.cdt.debug.core.cdi.model.ICDIRegisterDescriptor;
import org.eclipse.cdt.debug.core.cdi.model.ICDIRegisterGroup;
import org.eclipse.cdt.debug.core.cdi.model.ICDIRuntimeOptions;
import org.eclipse.cdt.debug.core.cdi.model.ICDISharedLibrary;
import org.eclipse.cdt.debug.core.cdi.model.ICDISignal;
import org.eclipse.ptp.core.IPProcess;
import org.eclipse.ptp.debug.core.aif.IAIF;
import org.eclipse.ptp.debug.core.cdi.PCDIException;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIAddressBreakpoint;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIBreakpoint;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIExceptionpoint;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIExpression;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIFunctionBreakpoint;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIGlobalVariable;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIGlobalVariableDescriptor;
import org.eclipse.ptp.debug.core.cdi.model.IPCDILineBreakpoint;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIStackFrame;
import org.eclipse.ptp.debug.core.cdi.model.IPCDITarget;
import org.eclipse.ptp.debug.core.cdi.model.IPCDITargetConfiguration;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIThread;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIWatchpoint;
import org.eclipse.ptp.debug.external.IAbstractDebugger;
import org.eclipse.ptp.debug.external.cdi.BreakpointManager;
import org.eclipse.ptp.debug.external.cdi.ExpressionManager;
import org.eclipse.ptp.debug.external.cdi.Session;
import org.eclipse.ptp.debug.external.cdi.SessionObject;
import org.eclipse.ptp.debug.external.cdi.VariableManager;
import org.eclipse.ptp.debug.external.cdi.model.variable.GlobalVariableDescriptor;
import org.eclipse.ptp.debug.external.cdi.model.variable.Variable;
import org.eclipse.ptp.debug.external.commands.EvaluteExpressionCommand;
import org.eclipse.ptp.debug.external.commands.GoCommand;
import org.eclipse.ptp.debug.external.commands.HaltCommand;
import org.eclipse.ptp.debug.external.commands.KillCommand;
import org.eclipse.ptp.debug.external.commands.StepIntoCommand;
import org.eclipse.ptp.debug.external.commands.StepOverCommand;

/**
 * @author Clement chu
 *
 */
public class Target extends SessionObject implements IPCDITarget {
	IPCDITargetConfiguration fConfiguration;
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
	public void setConfiguration(IPCDITargetConfiguration configuration) {
		fConfiguration = configuration;
	}
	public IPCDITarget getTarget() {
		return this;
	}
	public void setCurrentThread(IPCDIThread cthread) throws PCDIException {
		if (cthread instanceof Thread) {
			setCurrentThread(cthread, true);
		} else {
			throw new PCDIException("Target - Unknown_thread");
		}
	}
	public void setCurrentThread(IPCDIThread cthread, boolean doUpdate) throws PCDIException {
		if (cthread instanceof Thread) {
			try {
				setCurrentThread((Thread)cthread, doUpdate);
			} catch (PCDIException e) {
				throw new PCDIException(e.getMessage());
			}
		} else {
			throw new PCDIException("Target - Unknown_thread");
		}
	}
	public synchronized void setSupended(boolean state) {
		suspended = state;
		notifyAll();
	}
	public void setCurrentThread(Thread cthread, boolean doUpdate) throws PCDIException {
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
			throw new PCDIException("Cannot switch to thread " + id);
		}
	}
	public synchronized void updateState(int newThreadId) {
		Thread[] oldThreads = currentThreads;
		currentThreadId = newThreadId;
		try {
			currentThreads = getCThreads();
		} catch (PCDIException e) {
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
	private Thread[] getCThreads() throws PCDIException {
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
	public IPCDIThread getCurrentThread() throws PCDIException {
		IPCDIThread[] threads = getThreads();
		for (int i = 0; i < threads.length; i++) {
			Thread cthread = (Thread)threads[i];
			if (cthread.getId() == currentThreadId) {
				return cthread;
			}
		}
		return null;
	}
	public synchronized IPCDIThread[] getThreads() throws PCDIException {
		if (currentThreads.length == 0) {
			currentThreads = getCThreads();
		}
		return currentThreads;
	}
	public IPCDIThread getThread(int tid) throws PCDIException {
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
	public void restart() throws PCDIException {
		getDebugger().restart();
	}
	public void stepInto() throws PCDIException {
		stepInto(1);
	}
	public void stepInto(int count) throws PCDIException {
		getDebugger().postCommand(new StepIntoCommand(((Session)getSession()).createBitList(getTargetID()), count));
		//getDebugger().steppingInto(((Session)getSession()).createBitList(getTargetID()), count);
	}
	public void stepIntoInstruction() throws PCDIException {
		stepIntoInstruction(1);
	}
	public void stepIntoInstruction(int count) throws PCDIException {
		//TODO - implement step into instruction
		//getDebugger().stepIntoInstrunction(count);
		throw new PCDIException("Not implement yet - Target: stepIntoInstruction");
	}
	public void stepOver() throws PCDIException {
		stepOver(1);
	}
	public void stepOver(int count) throws PCDIException {
		getDebugger().postCommand(new StepOverCommand(((Session)getSession()).createBitList(getTargetID()), count));
		//getDebugger().steppingOver(((Session)getSession()).createBitList(getTargetID()), count);
	}
	public void stepOverInstruction() throws PCDIException {
		stepOverInstruction(1);
	}
	public void stepOverInstruction(int count) throws PCDIException {
		//TODO - implement step over instruction
		//getDebugger().stepOverInstrunction(count);
		throw new PCDIException("Not implement yet - Target: stepOverInstruction");
	}
	public void stepReturn() throws PCDIException {
		((Thread)getCurrentThread()).getCurrentStackFrame().stepReturn();
	}
	public void runUntil(ICDILocation location) throws PCDIException {
		stepUntil(location);
	}
	public void stepUntil(ICDILocation location) throws PCDIException {
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
		throw new PCDIException("Not implement yet - stepUntil(location)");
	}
	public void suspend() throws PCDIException {
		getDebugger().postCommand(new HaltCommand(((Session)getSession()).createBitList(getTargetID())));
		//getDebugger().suspend(((Session)getSession()).createBitList(getTargetID()));
	}
	public void disconnect() throws PCDIException {
		//Do nothing
	}
	public void resume() throws PCDIException {
		resume(false);
	}
	public void resume(ICDILocation location) throws PCDIException {
		resume(location);
	}
	public void resume(ICDISignal signal) throws PCDIException {
		signal(signal);
	}
	public void resume(boolean passSignal) throws PCDIException {
		String state = getPProcess().getStatus();
		if (state.equals(IPProcess.RUNNING)) {
			throw new PCDIException("The process is already running");
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
	public void continuation() throws PCDIException {
		getDebugger().postCommand(new GoCommand(((Session)getSession()).createBitList(getTargetID())));
		//getDebugger().resume(((Session)getSession()).createBitList(getTargetID()));
	}
	public void jump(ICDILocation location) throws PCDIException {
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
		throw new PCDIException("Not implement yet - jump(location)");
	}
	public void signal() throws PCDIException {
		//TODO - implement signal
		//getDebugger().singal();
		throw new PCDIException("Not implement yet - signal");
	}
	public void signal(ICDISignal signal) throws PCDIException {
		//TODO - implement signal(ICDISignal)
		//getDebugger().singal(signal.getName());
		throw new PCDIException("Not implement yet - signal(ICDISignal)");
	}
	public String evaluateExpressionToString(IPCDIStackFrame frame, String expressionText) throws PCDIException {
		Target target = (Target)frame.getTarget();
		Thread currentThread = (Thread)target.getCurrentThread();
		StackFrame currentFrame = currentThread.getCurrentStackFrame();
		target.setCurrentThread((IPCDIThread)frame.getThread(), false);
		((Thread)frame.getThread()).setCurrentStackFrame((StackFrame)frame, false);
		try {
			Session session = (Session) target.getSession();
			Variable var = session.getVariableManager().getVariable(target, expressionText);
			if (var == null) {
				return null;
			}
			IAIF aif = var.getAIF();
			if (aif != null) {
				return aif.getValue().toString();
			}
			EvaluteExpressionCommand command = new EvaluteExpressionCommand(session.createBitList(target.getTargetID()), expressionText);
			session.getDebugger().postCommand(command);
			return command.getExpressionValue();
		} finally {
			target.setCurrentThread(currentThread, false);
			currentThread.setCurrentStackFrame(currentFrame, false);
		}
	}
	public void terminate() throws PCDIException {
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
	public IPCDILineBreakpoint setLineBreakpoint(int type, ICDILineLocation location, ICDICondition condition, boolean deferred) throws PCDIException {
		Session session = (Session)getSession();
		BreakpointManager bMgr = session.getBreakpointManager();
		return bMgr.setLineBreakpoint(session.createBitList(getTargetID()), type, location, condition, deferred);
	}
	public IPCDIFunctionBreakpoint setFunctionBreakpoint(int type, ICDIFunctionLocation location, ICDICondition condition, boolean deferred) throws PCDIException {		
		Session session = (Session)getSession();
		BreakpointManager bMgr = session.getBreakpointManager();
		return bMgr.setFunctionBreakpoint(session.createBitList(getTargetID()), type, location, condition, deferred);
	}
	public IPCDIAddressBreakpoint setAddressBreakpoint(int type, ICDIAddressLocation location, ICDICondition condition, boolean deferred) throws PCDIException {
		Session session = (Session)getSession();
		BreakpointManager bMgr = session.getBreakpointManager();
		return bMgr.setAddressBreakpoint(session.createBitList(getTargetID()), type, location, condition, deferred);
	}
	public IPCDIWatchpoint setWatchpoint(int type, int watchType, String expression, ICDICondition condition) throws PCDIException {
		Session session = (Session)getSession();
		BreakpointManager bMgr = session.getBreakpointManager();
		return bMgr.setWatchpoint(session.createBitList(getTargetID()), type, watchType, expression, condition);
	}
	public IPCDIExceptionpoint setExceptionBreakpoint(String clazz, boolean stopOnThrow, boolean stopOnCatch) throws PCDIException {
		throw new PCDIException("Not implemented yet setExceptionBreakpoint");
	}
	public IPCDIBreakpoint[] getBreakpoints() throws PCDIException {
		throw new PCDIException("Not implemented yet - Target: getBreakpoints");
	}
	public void deleteBreakpoints(IPCDIBreakpoint[] breakpoints) throws PCDIException {
		throw new PCDIException("Not implemented yet - Target: deleteBreakpoints");
	}
	public void deleteAllBreakpoints() throws PCDIException {
		throw new PCDIException("Not implemented yet - Target: deleteAllBreakpoints");
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
	public IPCDIExpression createExpression(String code) throws PCDIException {
		ExpressionManager expMgr = ((Session)getSession()).getExpressionManager();
		return expMgr.createExpression(this, code);
	}
	public IPCDIExpression[] getExpressions() throws PCDIException {
		ExpressionManager expMgr = ((Session)getSession()).getExpressionManager();
		return expMgr.getExpressions(this);
	}
	public void destroyExpressions(IPCDIExpression[] expressions) throws PCDIException {
		ExpressionManager expMgr = ((Session)getSession()).getExpressionManager();
		expMgr.destroyExpressions(this, (IPCDIExpression[])expressions);
	}	
	public void destroyAllExpressions() throws PCDIException {
		ExpressionManager expMgr = ((Session)getSession()).getExpressionManager();
		expMgr.destroyAllExpressions(this);
	}
	public ICDISignal[] getSignals() throws PCDIException {
		throw new PCDIException("Not implemented yet - Target: getSignals");
	}
	public void setSourcePaths(String[] srcPaths) throws PCDIException {
		//throw new PCDIException("Not implemented yet - Target: setSourcePaths");
	}
	public String[] getSourcePaths() throws PCDIException {
		throw new PCDIException("Not implemented yet - Target: getSourcePaths");
	}
	public ICDIInstruction[] getInstructions(BigInteger startAddress, BigInteger endAddress) throws PCDIException {
		throw new PCDIException("Not implemented yet - Target: getInstructions");
	}
	public ICDIInstruction[] getInstructions(String filename, int linenum) throws PCDIException {
		throw new PCDIException("Not implemented yet - Target: getInstructions");
	}
	public ICDIInstruction[] getInstructions(String filename, int linenum, int lines) throws PCDIException {
		throw new PCDIException("Not implemented yet - Target: getInstructions");
	}
	public ICDIMixedInstruction[] getMixedInstructions(BigInteger startAddress, BigInteger endAddress) throws PCDIException {
		throw new PCDIException("Not implemented yet - Target: getMixedInstructions");
	}
	public ICDIMixedInstruction[] getMixedInstructions(String filename, int linenum) throws PCDIException {
		throw new PCDIException("Not implemented yet - Target: getMixedInstructions");
	}
	public ICDIMixedInstruction[] getMixedInstructions(String filename, int linenum, int lines) throws PCDIException {
		throw new PCDIException("Not implemented yet - Target: getMixedInstructions");
	}
	public ICDIMemoryBlock createMemoryBlock(String address, int units, int wordSize) throws PCDIException {
		throw new PCDIException("Not implemented yet - Target: createMemoryBlock");
	}
	public void removeBlocks(ICDIMemoryBlock[] memoryBlocks) throws PCDIException {
		throw new PCDIException("Not implemented yet - Target: removeBlocks");
	}
	public void removeAllBlocks() throws PCDIException {
		throw new PCDIException("Not implemented yet - Target: removeAllBlocks");
	}
	public ICDIMemoryBlock[] getMemoryBlocks() throws PCDIException {
		throw new PCDIException("Not implemented yet - Target: getMemoryBlocks");
	}
	public ICDISharedLibrary[] getSharedLibraries() throws PCDIException {
		throw new PCDIException("Not implemented yet - Target: getSharedLibraries");
	}
	public IPCDIGlobalVariableDescriptor getGlobalVariableDescriptors(String filename, String function, String name) throws PCDIException {
		//VariableManager varMgr = ((Session)getSession()).getVariableManager();
		//return varMgr.getGlobalVariableDescriptor(this, filename, function, name);
		throw new PCDIException("Not implemented yet - Target: getGlobalVariableDescriptors");
	}

	public ICDIRegisterGroup[] getRegisterGroups() throws PCDIException {
		throw new PCDIException("Not implemented yet - Target: getRegisterGroups");
	}
	public IPCDITargetConfiguration getConfiguration() {
		if (fConfiguration == null) {
			fConfiguration = new TargetConfiguration(this);				
		}		
		return fConfiguration;
	}
	public IPCDIGlobalVariable createGlobalVariable(IPCDIGlobalVariableDescriptor varDesc) throws PCDIException {
		if (varDesc instanceof GlobalVariableDescriptor) {
			VariableManager varMgr = ((Session)getSession()).getVariableManager();
			return varMgr.createGlobalVariable((GlobalVariableDescriptor)varDesc);
		}
		return null;
	}
	public ICDIRegister createRegister(ICDIRegisterDescriptor varDesc) throws PCDIException {
		throw new PCDIException("Not implemented yet - Target: createRegister");
	}
	
	public IPProcess getPProcess() {
		return getDebugger().getProcess(task_id);
	}
	public int getTargetID() {
		return task_id;
	}
}
