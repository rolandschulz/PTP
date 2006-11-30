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
package org.eclipse.ptp.debug.external.core.cdi.model;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.cdt.debug.core.cdi.model.ICDIInstruction;
import org.eclipse.cdt.debug.core.cdi.model.ICDIMixedInstruction;
import org.eclipse.cdt.debug.core.cdi.model.ICDIRegister;
import org.eclipse.cdt.debug.core.cdi.model.ICDIRegisterDescriptor;
import org.eclipse.cdt.debug.core.cdi.model.ICDIRegisterGroup;
import org.eclipse.cdt.debug.core.cdi.model.ICDIRuntimeOptions;
import org.eclipse.cdt.debug.core.cdi.model.ICDISharedLibrary;
import org.eclipse.ptp.core.IPProcess;
import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.IAbstractDebugger;
import org.eclipse.ptp.debug.core.PDebugUtils;
import org.eclipse.ptp.debug.core.cdi.IPCDIAddressLocation;
import org.eclipse.ptp.debug.core.cdi.IPCDICondition;
import org.eclipse.ptp.debug.core.cdi.IPCDIFunctionLocation;
import org.eclipse.ptp.debug.core.cdi.IPCDILineLocation;
import org.eclipse.ptp.debug.core.cdi.PCDIException;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIExpression;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIGlobalVariable;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIGlobalVariableDescriptor;
import org.eclipse.ptp.debug.core.cdi.model.IPCDILocation;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIMemoryBlock;
import org.eclipse.ptp.debug.core.cdi.model.IPCDISignal;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIStackFrame;
import org.eclipse.ptp.debug.core.cdi.model.IPCDITarget;
import org.eclipse.ptp.debug.core.cdi.model.IPCDITargetConfiguration;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIThread;
import org.eclipse.ptp.debug.external.core.cdi.ExpressionManager;
import org.eclipse.ptp.debug.external.core.cdi.MemoryManager;
import org.eclipse.ptp.debug.external.core.cdi.Session;
import org.eclipse.ptp.debug.external.core.cdi.SessionObject;
import org.eclipse.ptp.debug.external.core.cdi.SignalManager;
import org.eclipse.ptp.debug.external.core.cdi.VariableManager;
import org.eclipse.ptp.debug.external.core.cdi.event.ThreadCreatedEvent;
import org.eclipse.ptp.debug.external.core.cdi.event.ThreadExitedEvent;
import org.eclipse.ptp.debug.external.core.cdi.model.variable.GlobalVariableDescriptor;
import org.eclipse.ptp.debug.external.core.commands.CLISignalInfoCommand;
import org.eclipse.ptp.debug.external.core.commands.DataEvaluateExpressionCommand;
import org.eclipse.ptp.debug.external.core.commands.GetInfoThreadsCommand;
import org.eclipse.ptp.debug.external.core.commands.GoCommand;
import org.eclipse.ptp.debug.external.core.commands.HaltCommand;
import org.eclipse.ptp.debug.external.core.commands.SetThreadSelectCommand;
import org.eclipse.ptp.debug.external.core.commands.StepIntoCommand;
import org.eclipse.ptp.debug.external.core.commands.StepOverCommand;
import org.eclipse.ptp.debug.external.core.commands.TerminateCommand;
import org.eclipse.ptp.debug.external.core.proxy.ProxyDebugStackframe;

/**
 * @author Clement chu
 *
 */
public class Target extends SessionObject implements IPCDITarget {
	public class Lock {
		java.lang.Thread heldBy;
		int count;

		public Lock() {}
		public synchronized void aquire() {
			if (heldBy == null || heldBy == java.lang.Thread.currentThread()) {
				heldBy = java.lang.Thread.currentThread();
				count++;
			} else {
				while (true) {
					try {
						wait();
					} catch (InterruptedException e) {
					}
					if (heldBy == null) {
						heldBy = java.lang.Thread.currentThread();
						count++;
						return;
					}					
				}
			}
		}

		public synchronized void release() {
			if (heldBy == null || heldBy != java.lang.Thread.currentThread()) {
				throw new IllegalStateException("Thread does not own lock");
			}
			if(--count == 0) {
				heldBy = null;
				notifyAll();
			}
		}
	}

	IPCDITargetConfiguration fConfiguration;
	Thread[] noThreads = new Thread[0];
	Thread[] currentThreads;
	int currentThreadId;
	String fEndian = null;
	boolean suspended = true;
	private int task_id = -1;
	private BitList task = null;
	boolean deferBreakpoints = true;
	Lock lock = new Lock();
	
	public Target(Session session, int task_id) {
		super(session);
		this.task_id = task_id;
		task = session.createBitList(task_id);
		currentThreads = noThreads;
	}
	public void lockTarget() {
		lock.aquire();
	}
	public void releaseTarget() {
		lock.release();
	}
	public IAbstractDebugger getDebugger() {
		return getSession().getDebugger();
	}
	public void setConfiguration(IPCDITargetConfiguration configuration) {
		fConfiguration = configuration;
	}
	public BitList getTask() {
		return task;
	}
	public IPCDITarget getTarget() {
		return this;
	}
	public void setCurrentThread(IPCDIThread pthread) throws PCDIException {
		if (pthread instanceof Thread) {
			setCurrentThread(pthread, true);
		} else {
			throw new PCDIException("Target - Unknown_thread");
		}
	}
	public void setCurrentThread(IPCDIThread pthread, boolean doUpdate) throws PCDIException {
		if (pthread instanceof Thread) {
			setCurrentThread((Thread)pthread, doUpdate);
		} else {
			throw new PCDIException("Target - Unknown_thread");
		}
	}
	public synchronized void setSupended(boolean state) {
		suspended = state;
		notifyAll();
	}
	public void setCurrentThread(Thread pthread, boolean doUpdate) throws PCDIException {
		int id = pthread.getId();
		if (id == 0) {
			return;
		}
		if (currentThreadId != id) {
			SetThreadSelectCommand command = new SetThreadSelectCommand(getTask(), id);
			getDebugger().postCommand(command);
			Object[] objects = command.getThreadInfo();
			if (objects.length != 2) {
				throw new PCDIException("Cannot SetThreadSelectCommand error");
			}
			currentThreadId = ((Integer)objects[0]).intValue();
			ProxyDebugStackframe frame = (ProxyDebugStackframe)objects[1];
			
			int depth = pthread.getStackFrameCount();
			pthread.currentFrame = new StackFrame(pthread, depth - frame.getLevel(), frame.getLocator(), null);

			if (doUpdate) {
				/*
				RegisterManager regMgr = ((Session) getSession()).getRegisterManager();
				if (regMgr.isAutoUpdate()) {
					regMgr.update(target);
				}
				*/
				VariableManager varMgr = ((Session) getSession()).getVariableManager();
				if (varMgr.isAutoUpdate()) {
					varMgr.update(this);
				}
			}
		}
		
		if (currentThreadId != id) {
			getDebugger().fireEvent(new ThreadExitedEvent(getSession(), getTask(), getThread(id), id));
			throw new PCDIException("Cannot switch to thread " + id);
		}
	}
	public synchronized void updateState(int newThreadId) {
		Thread[] oldThreads = currentThreads;
		currentThreadId = newThreadId;		
		try {
			currentThreads = getPThreads();
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
			ThreadCreatedEvent[] events = new ThreadCreatedEvent[cList.size()];
			for (int j=0; j<events.length; j++) {
				int id = ((Integer)cList.get(j)).intValue();
				events[j] = new ThreadCreatedEvent(getSession(), getTask(), getThread(id), id);
			}
			getDebugger().fireEvents(events);
		}
		//Fire destroryd event for old threads
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
			ThreadExitedEvent[] events = new ThreadExitedEvent[dList.size()];
			for (int j=0; j<events.length; j++) {
				int id = ((Integer)dList.get(j)).intValue();
				events[j] = new ThreadExitedEvent(getSession(), getTask(), getThread(id), id);
			}
			getDebugger().fireEvents(events);
		}
	}
	/**
	 * TODO:  no thread name supported 
	 */
	public Thread[] getPThreads() throws PCDIException {
		Thread[] pthreads = noThreads;
		GetInfoThreadsCommand command = new GetInfoThreadsCommand(getTask());
		getDebugger().postCommand(command);
		//first index is current thread id
		String[] ids = command.getThreadIds();
		if (ids.length > 0) {
			pthreads = new Thread[ids.length];
			for (int i=0; i<ids.length; i++) {
				pthreads[i] = new Thread(this, Integer.parseInt(ids[i]));
			}
		}
		else {
			pthreads = new Thread[] { new Thread(this, 0) };
		}
		currentThreadId = pthreads[0].getId();

		/**
		 * FIXME:  If there is no thread selected, choose the first one as a workaround
		 */
		if (currentThreadId == 0 && pthreads.length > 1) {
			currentThreadId = pthreads[1].getId();
		}
		return pthreads;
	}
	public IPCDIThread getCurrentThread() throws PCDIException {
		IPCDIThread[] threads = getThreads();
		for (int i = 0; i < threads.length; i++) {
			Thread pthread = (Thread)threads[i];
			if (pthread.getId() == currentThreadId) {
				return pthread;
			}
		}
		return null;
	}
	public synchronized IPCDIThread[] getThreads() throws PCDIException {
		if (currentThreads.length == 0) {
			currentThreads = getPThreads();
		}
		return currentThreads;
	}
	public IPCDIThread getThread(int tid) {
		Thread th = null;
		if (currentThreads != null) {
			for (int i = 0; i < currentThreads.length; i++) {
				Thread pthread = currentThreads[i];
				if (pthread.getId() == tid) {
					th = pthread;
					break;
				}
			}
		}
		return th;
	}
	public void restart() throws PCDIException {
		throw new PCDIException("Target - restart not implemented yet");
		//getDebugger().restart();
	}
	public void stepInto() throws PCDIException {
		stepInto(1);
	}
	public void stepInto(int count) throws PCDIException {
		getDebugger().postCommand(new StepIntoCommand(getTask(), count));
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
		getDebugger().postCommand(new StepOverCommand(getTask(), count));
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
	public void runUntil(IPCDILocation location) throws PCDIException {
		stepUntil(location);
	}
	public void stepUntil(IPCDILocation location) throws PCDIException {
		String file = "";
		String func = "";
		String addr = "";
		int line = -1;
		if (location instanceof IPCDILineLocation) {
			IPCDILineLocation lineLocation = (IPCDILineLocation)location;
			if (lineLocation.getFile() != null && lineLocation.getFile().length() > 0) {
				file = lineLocation.getFile(); 
				line = lineLocation.getLineNumber();
			}
		}
		else if (location instanceof IPCDIFunctionLocation) {
			IPCDIFunctionLocation funcLocation = (IPCDIFunctionLocation)location;
			if (funcLocation.getFunction() != null && funcLocation.getFunction().length() > 0) {
				func = funcLocation.getFunction();
			}
			if (funcLocation.getFile() != null && funcLocation.getFile().length() > 0) {
				file = funcLocation.getFile();
			}
		}
		else if (location instanceof IPCDIAddressLocation) {
			IPCDIAddressLocation addrLocation = (IPCDIAddressLocation) location;
			if (! addrLocation.getAddress().equals(BigInteger.ZERO)) {
				addr = "*0x" + addrLocation.getAddress().toString(16);
			}
		}
		//TODO - implement step until location
		//getDebugger().stepUntil(file, func, addr, line);
		throw new PCDIException("Not implement yet - stepUntil(location)");
	}
	public void suspend() throws PCDIException {
		getDebugger().postCommand(new HaltCommand(getTask()));
	}
	public void disconnect() throws PCDIException {
		//Do nothing
		throw new PCDIException("Target - disconnect not implemented yet");
	}
	public void resume() throws PCDIException {
		resume(false);
	}
	public void resume(IPCDILocation location) throws PCDIException {
		jump(location);
	}
	public void resume(IPCDISignal signal) throws PCDIException {
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
		getDebugger().postCommand(new GoCommand(getTask()));
	}
	public void jump(IPCDILocation location) throws PCDIException {
		String file = "";
		String func = "";
		String addr = "";
		int line = -1;
		if (location instanceof IPCDILineLocation) {
			IPCDILineLocation lineLocation = (IPCDILineLocation)location;
			if (lineLocation.getFile() != null && lineLocation.getFile().length() > 0) {
				file = lineLocation.getFile(); 
				line = lineLocation.getLineNumber();
			}
		}
		else if (location instanceof IPCDIFunctionLocation) {
			IPCDIFunctionLocation funcLocation = (IPCDIFunctionLocation)location;
			if (funcLocation.getFunction() != null && funcLocation.getFunction().length() > 0) {
				func = funcLocation.getFunction();
			}
			if (funcLocation.getFile() != null && funcLocation.getFile().length() > 0) {
				file = funcLocation.getFile();
			}
		}
		else if (location instanceof IPCDIAddressLocation) {
			IPCDIAddressLocation addrLocation = (IPCDIAddressLocation) location;
			if (! addrLocation.getAddress().equals(BigInteger.ZERO)) {
				addr = "*0x" + addrLocation.getAddress().toString(16);
			}
		}
		//TODO - implement jump location
		//getDebugger().jump(file, func, addr, line);
		throw new PCDIException("Target - Not implement yet - jump(location)");
	}
	public void signal() throws PCDIException {
		getDebugger().postCommand(new CLISignalInfoCommand(getTask(), "0"));
	}
	public void signal(IPCDISignal signal) throws PCDIException {
		getDebugger().postCommand(new CLISignalInfoCommand(getTask(), signal.getName()));
	}
	public String evaluateExpressionToString(IPCDIStackFrame frame, String expressionText) throws PCDIException {
		//TODO - make sure using -data-evaluate-expression or -var-evaluate-expression
		Target target = (Target)frame.getTarget();
		Thread currentThread = (Thread)target.getCurrentThread();
		StackFrame currentFrame = currentThread.getCurrentStackFrame();
		target.setCurrentThread((IPCDIThread)frame.getThread(), false);
		((Thread)frame.getThread()).setCurrentStackFrame((StackFrame)frame, false);
		try {
			/*
			Variable var = ((Session) target.getSession()).getVariableManager().getVariable(target, expressionText);
			if (var != null) {
				IAIFValue value = var.getValue();
				if (value != null) {
					try {
						return value.getValueString();
					} catch (AIFException e) {}
				}
			}
			*/
			//EvaluateExpressionCommand
			DataEvaluateExpressionCommand command = new DataEvaluateExpressionCommand(getTask(), expressionText);
			getDebugger().postCommand(command);
			return command.getExpressionValue();
		} finally {
			target.setCurrentThread(currentThread, false);
			currentThread.setCurrentStackFrame(currentFrame, false);
		}
	}
	public void terminate() throws PCDIException {
		getDebugger().postCommand(new TerminateCommand(getTask()));
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
	public IPCDICondition createCondition(int ignoreCount, String expression) {
		return createCondition(ignoreCount, expression, null);
	}
	public IPCDICondition createCondition(int ignoreCount, String expression, String[] tids) {
		return getSession().getBreakpointManager().createCondition(ignoreCount, expression, tids);
	}
	public IPCDILineLocation createLineLocation(String file, int line) {
		return getSession().getBreakpointManager().createLineLocation(file, line);
	}
	public IPCDIFunctionLocation createFunctionLocation(String file, String function) {
		return getSession().getBreakpointManager().createFunctionLocation(file, function);
	}
	public IPCDIAddressLocation createAddressLocation(BigInteger address) {
		return getSession().getBreakpointManager().createAddressLocation(address);
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
	public IPCDISignal[] getSignals() throws PCDIException {
		SignalManager sigMgr = ((Session)getSession()).getSignalManager();
		return sigMgr.getSignals(this);
	}
	public void setSourcePaths(String[] srcPaths) throws PCDIException {
		//SourceManager srcMgr = ((Session)getSession()).getSourceManager();
		//srcMgr.setSourcePaths(this, srcPaths);
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
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.cdi.model.IPCDIMemoryBlockManagement#createMemoryBlock(java.lang.String, int, int)
	 */
	public IPCDIMemoryBlock createMemoryBlock(String address, int units, int wordSize) throws PCDIException {
		MemoryManager memMgr = ((Session)getSession()).getMemoryManager();
		return memMgr.createMemoryBlock(this, address, units, wordSize);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.cdi.model.IPCDIMemoryBlockManagement#removeBlocks(org.eclipse.ptp.debug.core.cdi.model.IPCDIMemoryBlock[])
	 */
	public void removeBlocks(IPCDIMemoryBlock[] memoryBlocks) throws PCDIException {
		MemoryManager memMgr = ((Session)getSession()).getMemoryManager();
		memMgr.removeBlocks(this, memoryBlocks);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.cdi.model.IPCDIMemoryBlockManagement#removeAllBlocks()
	 */
	public void removeAllBlocks() throws PCDIException {
		MemoryManager memMgr = ((Session)getSession()).getMemoryManager();
		memMgr.removeAllBlocks(this);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.cdi.model.IPCDIMemoryBlockManagement#getMemoryBlocks()
	 */
	public IPCDIMemoryBlock[] getMemoryBlocks() throws PCDIException {
		MemoryManager memMgr = ((Session)getSession()).getMemoryManager();
		return memMgr.getMemoryBlocks(this);
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
	
	public boolean isLittleEndian() throws PCDIException {
		//TODO - not implemented yet
		PDebugUtils.println("---- called isLittleEndian");
		if (fEndian == null) {
			//"le" : "be"
		}
		return true;
	}	
	public void deferBreakpoints(boolean defer) {
		this.deferBreakpoints = defer;
	}

	public boolean areBreakpointsDeferred() {
		return this.deferBreakpoints;
	}
}
