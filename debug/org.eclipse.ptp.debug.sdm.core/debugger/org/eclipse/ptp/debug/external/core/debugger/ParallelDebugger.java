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
package org.eclipse.ptp.debug.external.core.debugger;

import java.io.IOException;
import java.math.BigInteger;
import org.eclipse.cdt.debug.core.cdi.ICDICondition;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.core.IPProcess;
import org.eclipse.ptp.core.PreferenceConstants;
import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.ExtFormat;
import org.eclipse.ptp.debug.core.IDebugCommand;
import org.eclipse.ptp.debug.core.IDebugger;
import org.eclipse.ptp.debug.core.PTPDebugCorePlugin;
import org.eclipse.ptp.debug.core.cdi.PCDIException;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIErrorEvent;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIArgument;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIFunctionBreakpoint;
import org.eclipse.ptp.debug.core.cdi.model.IPCDILineBreakpoint;
import org.eclipse.ptp.debug.core.cdi.model.IPCDILocalVariable;
import org.eclipse.ptp.debug.core.cdi.model.IPCDISignal;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIStackFrame;
import org.eclipse.ptp.debug.core.cdi.model.IPCDITarget;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIWatchpoint;
import org.eclipse.ptp.debug.external.core.AbstractDebugger;
import org.eclipse.ptp.debug.external.core.cdi.model.Signal;
import org.eclipse.ptp.debug.external.core.cdi.model.StackFrame;
import org.eclipse.ptp.debug.external.core.cdi.model.Target;
import org.eclipse.ptp.debug.external.core.cdi.model.variable.Argument;
import org.eclipse.ptp.debug.external.core.cdi.model.variable.LocalVariable;
import org.eclipse.ptp.debug.external.core.commands.TerminateCommand;
import org.eclipse.ptp.debug.external.core.proxy.ProxyDebugClient;
import org.eclipse.ptp.debug.external.core.proxy.ProxyDebugSignal;
import org.eclipse.ptp.debug.external.core.proxy.ProxyDebugStackframe;
import org.eclipse.ptp.debug.external.core.proxy.event.IProxyDebugEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.IProxyDebugEventListener;
import org.eclipse.ptp.debug.external.core.proxy.event.ProxyDebugArgsEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.ProxyDebugBreakpointHitEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.ProxyDebugBreakpointSetEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.ProxyDebugDataEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.ProxyDebugErrorEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.ProxyDebugExitEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.ProxyDebugInfoThreadsEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.ProxyDebugInitEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.ProxyDebugMemoryInfoEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.ProxyDebugSetThreadSelectEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.ProxyDebugSignalEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.ProxyDebugSignalExitEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.ProxyDebugSignalsEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.ProxyDebugStackInfoDepthEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.ProxyDebugStackframeEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.ProxyDebugStepEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.ProxyDebugSuspendEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.ProxyDebugTypeEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.ProxyDebugVarsEvent;


public class ParallelDebugger extends AbstractDebugger implements IDebugger, IProxyDebugEventListener {
	/**
	private HashMap				bpMap = new HashMap();
	private ArrayList			bpArray = new ArrayList();
	private class BreakpointMapping {
		private IPCDIBreakpoint		bpObject;
		private BitList				bpSet;
		private int					bpId;
		public BreakpointMapping(int bpid, BitList set, IPCDIBreakpoint bpt) {
			this.bpId = bpid;
			this.bpSet = set;
			this.bpObject = bpt;
		}
		public int getBreakpointId() {
			return this.bpId;
		}
		public BitList getBreakpointProcs() {
			return this.bpSet;
		}
		public IPCDIBreakpoint getBreakpoint() {
			return this.bpObject;
		}	
		public void updateProcs(BitList set) {
			if (this.bpSet != null) {
				this.bpSet.or(set);
			} else {
				this.bpSet = set;
			}
		}
	}
	*/
	
	private ProxyDebugClient	proxy;
	private int					numServers;
	private int					bpId = 0;
	private IPCDIStackFrame		currFrame = null;

	private String join(String[] strs, String delim) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < strs.length; i++) {
			if (i > 0)
				buf.append(delim);
			buf.append(strs[i]);
		}
		return buf.toString();
	}
	public int getDebuggerPort() throws CoreException {
		proxy = new ProxyDebugClient();
		try {
			proxy.sessionCreate();
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, PTPDebugCorePlugin.getUniqueIdentifier(), IStatus.ERROR, "could not create proxy", null));
		}
		return proxy.getSessionPort();
	}
	
	public void connection() throws CoreException {
	}
	
	public void startDebugger(IPJob job) throws CoreException {
		try {
			String app = (String) job.getAttribute(PreferenceConstants.JOB_APP_NAME);
			String path = (String) job.getAttribute(PreferenceConstants.JOB_APP_PATH);
			String dir = (String) job.getAttribute(PreferenceConstants.JOB_WORK_DIR);
			String[] args = (String[]) job.getAttribute(PreferenceConstants.JOB_ARGS);
			proxy.waitForConnect();
			proxy.addEventListener(this);
			proxy.debugStartSession(app, path, dir, args);
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, PTPDebugCorePlugin.getUniqueIdentifier(), IStatus.ERROR, "Cannot start debugger", e));
		}
	}
	public void stopDebugger() throws CoreException {
		if (proxy != null) {
			try {
				proxy.sessionFinish();
				proxy = null;
			} catch (IOException e) {
				throw new CoreException(new Status(IStatus.ERROR, PTPDebugCorePlugin.getUniqueIdentifier(), IStatus.ERROR, "Cannot stop debugger", e));
			}
		}
	}
	public Process getDebuggerProcess() {
		return null;
	}
	public void restart() throws PCDIException {
		throw new PCDIException(PCDIException.NOT_IMPLEMENTED, "restart");
	}
	public void run(String[] args) throws PCDIException {
		throw new PCDIException(PCDIException.NOT_IMPLEMENTED, "run");		
	}

	public void go(BitList tasks) throws PCDIException {
		try {
			proxy.debugGo(tasks);
		} catch (IOException e) {
			// TODO deal with IOException (maybe should be dealt with in ProxyClient?)
			throw new PCDIException(e.getMessage());
		}
	}
	public void kill(BitList tasks) throws PCDIException {
		//halt(tasks);
		try {
			proxy.debugTerminate(tasks);
		} catch (IOException e) {
			// TODO deal with IOException (maybe should be dealt with in ProxyClient?)
			throw new PCDIException(e.getMessage());
		}
	}
	public void halt(BitList tasks) throws PCDIException {
		try {
			proxy.debugInterrupt(tasks);
		} catch (IOException e) {
			// TODO deal with IOException (maybe should be dealt with in ProxyClient?)
			throw new PCDIException(e.getMessage());
		}
	}
	public void stepInto(BitList tasks, int count) throws PCDIException {
		try {
			proxy.debugStep(tasks, count, ProxyDebugClient.STEP_INTO);
		} catch (IOException e) {
			throw new PCDIException(e.getMessage());
		}
	}
	public void stepOver(BitList tasks, int count) throws PCDIException {
		try {
			proxy.debugStep(tasks, count, ProxyDebugClient.STEP_OVER);
		} catch (IOException e) {
			throw new PCDIException(e.getMessage());
		}
	}
	public void stepFinish(BitList tasks, int count) throws PCDIException {
		try {
			proxy.debugStep(tasks, count, ProxyDebugClient.STEP_FINISH);
		} catch (IOException e) {
			throw new PCDIException(e.getMessage());
		}
	}
	public void setLineBreakpoint(BitList tasks, IPCDILineBreakpoint bpt) throws PCDIException {
		try {
			ICDICondition condition = bpt.getCondition();
			proxy.debugSetLineBreakpoint(tasks, newBreakpointId(), bpt.isTemporary(), bpt.isHardware(), bpt.getLocator().getFile(), bpt.getLocator().getLineNumber(), (condition!=null?condition.getExpression():""), (condition!=null?condition.getIgnoreCount():0), 0);
		} catch (IOException e) {
			throw new PCDIException(e.getMessage());
		}
	}
	public void setFunctionBreakpoint(BitList tasks, IPCDIFunctionBreakpoint bpt) throws PCDIException {
		try {
			ICDICondition condition = bpt.getCondition();
			proxy.debugSetFuncBreakpoint(tasks, newBreakpointId(), bpt.isTemporary(), bpt.isHardware(), bpt.getLocator().getFile(), bpt.getLocator().getFunction(), (condition!=null?condition.getExpression():""), (condition!=null?condition.getIgnoreCount():0), 0);
		} catch (IOException e) {
			throw new PCDIException(e.getMessage());
		}
	}
	public void setWatchpoint(BitList tasks, IPCDIWatchpoint bpt) throws PCDIException {
		try {
			String expression = bpt.getWatchExpression();
			boolean access = bpt.isReadType() && bpt.isWriteType(); 
			boolean read = ! bpt.isWriteType() && bpt.isReadType(); 
			
			ICDICondition condition = bpt.getCondition();
			proxy.debugSetWatchpoint(tasks, newBreakpointId(), expression, access, read, (condition!=null?condition.getExpression():""), (condition!=null?condition.getIgnoreCount():0));
		} catch (IOException e) {
			throw new PCDIException(e.getMessage());
		}
	}
	public void deleteBreakpoint(BitList tasks, int bpid)  throws PCDIException {
		try {
			proxy.debugDeleteBreakpoint(tasks, bpid);
		} catch (IOException e) {
			throw new PCDIException(e.getMessage());
		}
	}
	public void enableBreakpoint(BitList tasks, int bpid) throws PCDIException {
		try {
			proxy.debugEnableBreakpoint(tasks, bpid);
		} catch (IOException e) {
			throw new PCDIException(e.getMessage());
		}
	}
	public void disableBreakpoint(BitList tasks, int bpid) throws PCDIException {
		try {
			proxy.debugDisableBreakpoint(tasks, bpid);
		} catch (IOException e) {
			throw new PCDIException(e.getMessage());
		}
	}
	public void conditionBreakpoint(BitList tasks, int bpid, String expr) throws PCDIException {
		try {
			proxy.debugConditionBreakpoint(tasks, bpid, expr);
		} catch (IOException e) {
			throw new PCDIException(e.getMessage());
		}
	}
	
	/**
	 * list stack frames for first process in procs
	 */
	public void listStackFrames(BitList tasks) throws PCDIException {
		try {
			proxy.debugListStackframes(tasks, 0);
		} catch (IOException e) {
			throw new PCDIException(e.getMessage());
		}
	}
	public void setCurrentStackFrame(BitList tasks, int level) throws PCDIException {
		try {
			proxy.debugSetCurrentStackframe(tasks, level);
		} catch (IOException e) {
			throw new PCDIException(e.getMessage());
		}
	}
	/**
	 * evaluate expression for first process in procs
	 */
	public void evaluateExpression(BitList tasks, String expr) throws PCDIException {
		try {
			proxy.debugEvaluateExpression(tasks, expr);
		} catch (IOException e) {
			throw new PCDIException(e.getMessage());
		}
	}
	
	public void getAIFValue(BitList tasks, String expr) throws PCDIException {
		evaluateExpression(tasks, expr);
	}
	/**
	 * get variable type for first process in procs
	 */
	public void getVariableType(BitList tasks, String varName) throws PCDIException {
		try {
			proxy.debugGetType(tasks, varName);
		} catch (IOException e) {
			throw new PCDIException(e.getMessage());
		}
	}
	/**
	 * list local variables for first process in procs
	 */
	public void listLocalVariables(BitList tasks, IPCDIStackFrame frame) throws PCDIException {
		this.currFrame = frame;
		try {
			proxy.debugListLocalVariables(tasks);
		} catch (IOException e) {
			throw new PCDIException(e.getMessage());
		}
	}
	/**
	 * list global variables for first process in procs
	 */
	public void listGlobalVariables(BitList tasks) throws PCDIException {
		throw new PCDIException(PCDIException.NOT_IMPLEMENTED, "listGlobalVariables");
	}
	/**
	 * list arguments for first process in procs
	 */
	public void listArguments(BitList tasks, IPCDIStackFrame frame, int depth) throws PCDIException {
		this.currFrame = frame;
		try {
			proxy.debugListArguments(tasks, depth-frame.getLevel());
		} catch (IOException e) {
			throw new PCDIException(e.getMessage());
		}
	}
	/**
	 * thread
	 */
	public void getInfothreads(BitList tasks) throws PCDIException {
		try {
			proxy.debugListInfoThreads(tasks);
		} catch (IOException e) {
			throw new PCDIException(e.getMessage());
		}
	}
	public void setThreadSelect(BitList tasks, int threadNum) throws PCDIException {
		try {
			proxy.debugSetThreadSelect(tasks, threadNum);
		} catch (IOException e) {
			throw new PCDIException(e.getMessage());
		}
	}
	private String createFormat(int wordFormat) {
		switch (wordFormat) {
		case ExtFormat.UNSIGNED :
			return "u";
		case ExtFormat.FLOAT :
			return "f";
		case ExtFormat.ADDRESS :
			return "a";
		case ExtFormat.INSTRUCTION :
			return "i";
		case ExtFormat.CHAR :
			return "c";
		case ExtFormat.STRING :
			return "s";
		case ExtFormat.DECIMAL :
			return "d";
		case ExtFormat.BINARY :
			return "t";
		case ExtFormat.OCTAL :
			return "o";
		case ExtFormat.HEXADECIMAL:
		default :
			return "x";
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.IDebugger#setDataReadMemoryCommand(org.eclipse.ptp.core.util.BitList, long, java.lang.String, int, int, int, int, java.lang.Character)
	 */
	public void setDataReadMemoryCommand(BitList tasks, long offset, String address, int wordFormat, int wordSize, int rows, int cols, Character asChar) throws PCDIException {
		try {
			proxy.setDataReadMemoryCommand(tasks, offset, address, createFormat(wordFormat), wordSize, rows, cols, asChar);
		} catch (IOException e) {
			throw new PCDIException(e.getMessage());
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.IDebugger#setDataWriteMemoryCommand(org.eclipse.ptp.core.util.BitList, long, java.lang.String, int, int, java.lang.String)
	 */
	public void setDataWriteMemoryCommand(BitList tasks, long offset, String address, int wordFormat, int wordSize, String value) throws PCDIException {
		try {
			proxy.setDataWriteMemoryCommand(tasks, offset, address, createFormat(wordFormat), wordSize, value);
		} catch (IOException e) {
			throw new PCDIException(e.getMessage());
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.IDebugger#getStackInfoDepth(org.eclipse.ptp.core.util.BitList)
	 */
	public void getStackInfoDepth(BitList tasks) throws PCDIException {
		try {
			proxy.debugStackInfoDepth(tasks);
		} catch (IOException e) {
			// TODO deal with IOException (maybe should be dealt with in ProxyClient?)
			throw new PCDIException(e.getMessage());
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.IDebugger#getListSignals(org.eclipse.ptp.core.util.BitList, java.lang.String)
	 */
	public void getListSignals(BitList tasks, String name) throws PCDIException {
		try {
			proxy.debugListSignals(tasks, name);
		} catch (IOException e) {
			throw new PCDIException(e.getMessage());
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.IDebugger#getSignalInfo(org.eclipse.ptp.core.util.BitList, java.lang.String)
	 */
	public void getSignalInfo(BitList tasks, String arg) throws PCDIException {
		try {
			proxy.debugSignalInfo(tasks, arg);
		} catch (IOException e) {
			throw new PCDIException(e.getMessage());
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.IDebugger#cliHandle(org.eclipse.ptp.core.util.BitList, java.lang.String)
	 */
	public void cliHandle(BitList tasks, String arg) throws PCDIException {
		try {
			proxy.debugCLIHandle(tasks, arg);
		} catch (IOException e) {
			throw new PCDIException(e.getMessage());
		}
	}
	
	public synchronized void handleEvent(IProxyDebugEvent e) {
		System.out.println("got debug event: " + e.toString());
		switch (e.getEventID()) {
		case IProxyDebugEvent.EVENT_DBG_OK:
			completeCommand(e.getBitSet(), IDebugCommand.RETURN_OK);
			break;
			
		case IProxyDebugEvent.EVENT_DBG_INIT:
			numServers = ((ProxyDebugInitEvent)e).getNumServers();
			System.out.println("num servers = " + numServers);
			completeCommand(e.getBitSet(), IDebugCommand.RETURN_OK);
			break;
			
		case IProxyDebugEvent.EVENT_DBG_SUSPEND:
			if (e instanceof ProxyDebugBreakpointHitEvent) {
				ProxyDebugBreakpointHitEvent bptHitEvent = (ProxyDebugBreakpointHitEvent)e;
				handleBreakpointHitEvent(e.getBitSet(), bptHitEvent.getBreakpointId(), bptHitEvent.getThreadId(), bptHitEvent.getChangedVars());
			} else if (e instanceof ProxyDebugSuspendEvent) {
				ProxyDebugSuspendEvent suspendEvent = (ProxyDebugSuspendEvent)e;
				handleSuspendEvent(e.getBitSet(), suspendEvent.getLocator(), suspendEvent.getThreadId(), suspendEvent.getChangedVars());
			} else if (e instanceof ProxyDebugStepEvent) {
				ProxyDebugStepEvent stepEvent = (ProxyDebugStepEvent)e;
				handleEndSteppingEvent(e.getBitSet(), stepEvent.getFrame().getLocator().getLineNumber(), stepEvent.getFrame().getLocator().getFile(), stepEvent.getThreadId(), stepEvent.getChangedVars());
			} else if (e instanceof ProxyDebugSignalEvent) {
				ProxyDebugSignalEvent sigEvent = (ProxyDebugSignalEvent)e;
				IDebugCommand cmd = getCurrentCommand();
				if (cmd != null && cmd instanceof TerminateCommand) {
					completeCommand(e.getBitSet(), IDebugCommand.RETURN_OK);
				}
				else {
					//send signal if the current command is not terminate command.
					handleProcessSignaledEvent(e.getBitSet(), sigEvent.getLocator(), sigEvent.getThreadId(), sigEvent.getChangedVars());
				}
			}
			break;	
			
		case IProxyDebugEvent.EVENT_DBG_BPSET:
			ProxyDebugBreakpointSetEvent bpEvt = (ProxyDebugBreakpointSetEvent)e;
			completeCommand(e.getBitSet(), bpEvt.getBreakpoint());
			//updateBreakpointInfo(bpEvt.getBreakpointId(), e.getBitSet(), bpEvt.getBreakpoint());
			break;
			
		case IProxyDebugEvent.EVENT_DBG_FRAMES:
			ProxyDebugStackframeEvent frameEvent = (ProxyDebugStackframeEvent)e;

			IPCDIStackFrame[] pcdiFrames = new IPCDIStackFrame[0];
			IPProcess[] frameProcs = getProcesses(e.getBitSet());
			if (frameProcs.length > 0) {
				ProxyDebugStackframe[] frames = frameEvent.getFrames();
				pcdiFrames = new IPCDIStackFrame[frames.length];
				int taskId = frameProcs[0].getTaskId();
				IPCDITarget target = getSession().getTarget(taskId);
				for (int j = 0; j < frames.length; j++) {
					int level = frames[j].getLevel();
					String file = frames[j].getLocator().getFile();
					String func = frames[j].getLocator().getFunction();
					int line = frames[j].getLocator().getLineNumber();
					BigInteger addr = frames[j].getLocator().getAddress();
//System.out.println("frame " + level + " " + file + " " + func + " " + line + " " + addr);
					pcdiFrames[j] = new StackFrame((Target)target, level, file, func, line, addr);
				}
			}
			completeCommand(e.getBitSet(), pcdiFrames);
			//completeCommand(e.getBitSet(), (IPCDIStackFrame[]) frameList.toArray(new IPCDIStackFrame[0]));
			break;

		case IProxyDebugEvent.EVENT_DBG_TYPE:
			ProxyDebugTypeEvent type = (ProxyDebugTypeEvent)e;
			completeCommand(e.getBitSet(), type.getType());
			break;
			
		case IProxyDebugEvent.EVENT_DBG_DATA:
			ProxyDebugDataEvent data = (ProxyDebugDataEvent)e;
			completeCommand(e.getBitSet(), data.getData());
			break;
			
		case IProxyDebugEvent.EVENT_DBG_VARS:
			if (this.currFrame == null) {
				completeCommand(e.getBitSet(), new PCDIException("No stack frame selected", IPCDIErrorEvent.DBG_WARNING));
				break;
			}

			ProxyDebugVarsEvent varsEvent = (ProxyDebugVarsEvent)e;
			IPCDILocalVariable[] pcdiVars = new IPCDILocalVariable[0];
			IPProcess[] varProcs = getProcesses(varsEvent.getBitSet());
			if(varProcs.length > 0) {
				int taskId = varProcs[0].getTaskId();
				IPCDITarget target = getSession().getTarget(taskId);
				String vars[] = varsEvent.getVariables();
				pcdiVars = new IPCDILocalVariable[vars.length];
				for (int j = 0; j < vars.length; j++) {
					pcdiVars[j] = new LocalVariable((Target) target, null, (StackFrame) this.currFrame, vars[j], vars[j], vars.length - j, this.currFrame.getLevel(), null);
				}
			}
			completeCommand(e.getBitSet(), pcdiVars);			
			//completeCommand(e.getBitSet(), (IPCDILocalVariable[])varList.toArray(new IPCDILocalVariable[0]));			
			break;
			
		case IProxyDebugEvent.EVENT_DBG_ARGS:
			if (this.currFrame == null) {
				completeCommand(e.getBitSet(), new PCDIException("No stack frame selected", IPCDIErrorEvent.DBG_WARNING));
				break;
			}

			ProxyDebugArgsEvent argsEvent = (ProxyDebugArgsEvent)e;
			IPCDIArgument[] pcdiArgs = new IPCDIArgument[0];
			IPProcess[] argProcs = getProcesses(argsEvent.getBitSet());
			if(argProcs.length > 0) {
				int taskId = argProcs[0].getTaskId();
				IPCDITarget target = getSession().getTarget(taskId);
				String args[] = argsEvent.getVariables();
				pcdiArgs = new IPCDIArgument[args.length];
				for (int j = 0; j < args.length; j++) {
					pcdiArgs[j] = new Argument((Target) target, null, (StackFrame) this.currFrame, args[j], args[j], args.length - j, this.currFrame.getLevel(), null);
				}
			}
			completeCommand(e.getBitSet(), pcdiArgs);			
			//completeCommand(e.getBitSet(), (IPCDIArgument[]) argList.toArray(new IPCDIArgument[0]));			
			break;
			
		case IProxyDebugEvent.EVENT_DBG_EXIT:
			if (e instanceof ProxyDebugExitEvent) {
				System.out.println("======================= EVENT_DBG_EXIT ====================");
				ProxyDebugExitEvent exitEvent = (ProxyDebugExitEvent)e;
				handleProcessTerminatedEvent(e.getBitSet(), exitEvent.getExitStatus());
			} else if (e instanceof ProxyDebugSignalExitEvent) {
				System.out.println("======================= EVENT_DBG_EXIT_SIGNAL ====================");
				ProxyDebugSignalExitEvent exitSigEvent = (ProxyDebugSignalExitEvent)e;
				handleProcessTerminatedEvent(e.getBitSet(), exitSigEvent.getSignalName(), exitSigEvent.getSignalMeaning());
			}
			break;
			
		case IProxyDebugEvent.EVENT_DBG_SIGNALS:
			ProxyDebugSignalsEvent signalsEvent = (ProxyDebugSignalsEvent)e;
			IPCDISignal[] pcdiSignals = new IPCDISignal[0];
			IPProcess[] sigProcs = getProcesses(e.getBitSet());
			if (sigProcs.length > 0) {
				int taskId = sigProcs[0].getTaskId();
				IPCDITarget target = getSession().getTarget(taskId);
				ProxyDebugSignal[] signals = signalsEvent.getSignals();
				pcdiSignals = new IPCDISignal[signals.length];
				for (int j=0; j<signals.length; j++) {
					pcdiSignals[j] = new Signal((Target)target, signals[j].getName(), signals[j].isStop(), signals[j].isPrint(), signals[j].isPass(), signals[j].getDescription()); 
				}
			}
			completeCommand(e.getBitSet(), pcdiSignals);
			break;
		
		case IProxyDebugEvent.EVENT_DBG_THREADS:
			ProxyDebugInfoThreadsEvent infoThreadsEvent = (ProxyDebugInfoThreadsEvent)e;
			completeCommand(e.getBitSet(), infoThreadsEvent.getThreadIds());			
			break;
			
		case IProxyDebugEvent.EVENT_DBG_THREAD_SELECT:
			ProxyDebugSetThreadSelectEvent setThreadSelectEvent = (ProxyDebugSetThreadSelectEvent)e;
			Object[] objects = new Object[2];
			objects[0] = new Integer(setThreadSelectEvent.getThreadId());
			
			IPProcess[] threadProcs = getProcesses(e.getBitSet());
			if (threadProcs.length > 0) {
				int taskId = threadProcs[0].getTaskId();
				IPCDITarget target = getSession().getTarget(taskId);
				ProxyDebugStackframe frame = setThreadSelectEvent.getFrame();
				int level = frame.getLevel();
				String file = frame.getLocator().getFile();
				String func = frame.getLocator().getFunction();
				int line = frame.getLocator().getLineNumber();
				BigInteger addr = frame.getLocator().getAddress();
				objects[1] = new StackFrame((Target)target, level, file, func, line, addr);
			}
			completeCommand(e.getBitSet(), objects);
			break;
			
		case IProxyDebugEvent.EVENT_DBG_STACK_INFO_DEPTH:
			ProxyDebugStackInfoDepthEvent stackInfoDepthEvent = (ProxyDebugStackInfoDepthEvent)e;
			completeCommand(e.getBitSet(), new Integer(stackInfoDepthEvent.getDepth()));			
			break;
			
		case IProxyDebugEvent.EVENT_DBG_DATA_READ_MEMORY:
			ProxyDebugMemoryInfoEvent memoryInfoEvent = (ProxyDebugMemoryInfoEvent)e;
			completeCommand(e.getBitSet(), memoryInfoEvent.getMemoryInfo());			
			break;
			
		case IProxyDebugEvent.EVENT_DBG_ERROR:
			ProxyDebugErrorEvent errEvent = (ProxyDebugErrorEvent)e;
			String errMsg = errEvent.getErrorMessage();
			if (errMsg == null || errMsg.length() ==0)
				errMsg = "Unknown Error";
			
			completeCommand(e.getBitSet(), new PCDIException(errMsg, getErrorCode(errEvent.getErrorCode())));
			//completeCommand(e.getBitSet(), new PCDIException(errMsg, getErrorCode(errEvent.getErrorCode())));
			break;
		}
	}
	private int getErrorCode(int internalErrorCode) {
		switch (internalErrorCode) {
		case IParallelDebuggerConstants.DBGERR_DEBUGGER:
		case IParallelDebuggerConstants.DBGERR_NOFILEDIR:
		case IParallelDebuggerConstants.DBGERR_CHDIR:
			return IPCDIErrorEvent.DBG_FATAL;
		case IParallelDebuggerConstants.DBGERR_INPROGRESS:
		case IParallelDebuggerConstants.DBGERR_UNKNOWN_TYPE:
		case IParallelDebuggerConstants.DBGERR_UNKNOWN_VARIABLE:
			return IPCDIErrorEvent.DBG_NORMAL;
		default:
			return IPCDIErrorEvent.DBG_WARNING;
		}
	}
	
	/*
	 * Keep two data structures:
	 * 
	 * 1. A HashMap that is indexed by the actual breakpoint object that contains lists of
	 * breakpoint id/procset tuples associated with the object. This is used to delete a breakpoint 
	 * based on a breakpoint object. See the deleteBreakpoint() method.
	 * 
	 * 2. An ArrayList that is indexed by breakpoint id that contains the breakpoint 
	 * object/procset combination associated with the breakpoint id. This is used to look up the breakpoint
	 * object when a BPHIT event arrives.
	 * 
	 * ASSUMPTIONS: There is a one-to-one mapping between breakpoint id and breakpoint object. This is
	 * achieved by generating a new breakpoint id every time a breakpoint is set.
	 * 
	 */
	/*
	private void updateBreakpointInfo(int bpid, BitList bpset, IPCDIBreakpoint bpt) {
		BreakpointMapping bp = (BreakpointMapping) bpMap.get(bpt);
		
		if (bp == null) {
			bp = new BreakpointMapping(bpid, bpset, bpt);
			bpMap.put(bpt, bp);
		} else
			bp.updateProcs(bpset);
		
		if (bpid >= bpArray.size() || bpArray.get(bpid) == null) {
			bpArray.add(bpid, bp);
		}
	}
	private BreakpointMapping findBreakpointInfo(int bpid) {
		return (BreakpointMapping)bpArray.get(bpid);
	}
	private BreakpointMapping findBreakpointInfo(IPCDIBreakpoint bpt) {
		return (BreakpointMapping)bpMap.get(bpt);
	}
	*/

	private int newBreakpointId() {
		return this.bpId++;
	}
}