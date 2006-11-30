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
import org.eclipse.ptp.debug.core.PDebugUtils;
import org.eclipse.ptp.debug.core.PTPDebugCorePlugin;
import org.eclipse.ptp.debug.core.cdi.PCDIException;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIErrorEvent;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIFunctionBreakpoint;
import org.eclipse.ptp.debug.core.cdi.model.IPCDILineBreakpoint;
import org.eclipse.ptp.debug.core.cdi.model.IPCDISignal;
import org.eclipse.ptp.debug.core.cdi.model.IPCDITarget;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIWatchpoint;
import org.eclipse.ptp.debug.external.core.AbstractDebugger;
import org.eclipse.ptp.debug.external.core.cdi.model.Signal;
import org.eclipse.ptp.debug.external.core.cdi.model.Target;
import org.eclipse.ptp.debug.external.core.proxy.ProxyDebugClient;
import org.eclipse.ptp.debug.external.core.proxy.ProxyDebugSignal;
import org.eclipse.ptp.debug.external.core.proxy.event.IProxyDebugEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.IProxyDebugEventListener;
import org.eclipse.ptp.debug.external.core.proxy.event.ProxyDebugArgsEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.ProxyDebugBreakpointHitEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.ProxyDebugBreakpointSetEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.ProxyDebugDataEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.ProxyDebugDataExpValueEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.ProxyDebugErrorEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.ProxyDebugExitEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.ProxyDebugInfoThreadsEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.ProxyDebugInitEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.ProxyDebugMemoryInfoEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.ProxyDebugPartialAIFEvent;
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
	private ProxyDebugClient	proxy;
	private int					bpId = 0;

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
	public void listStackFrames(BitList tasks, int low, int high) throws PCDIException {
		try {
			proxy.debugListStackframes(tasks, low, high);
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
	
	public void getAIF(BitList tasks, String expr) throws PCDIException {
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
	public void listLocalVariables(BitList tasks) throws PCDIException {
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
	public void listArguments(BitList tasks, int low, int high) throws PCDIException {
		try {
			proxy.debugListArguments(tasks, low, high);
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
	/** new function **/
	public void dataEvaluateExpression(BitList tasks, String expression) throws PCDIException {
		try {
			proxy.debugDataEvaluateExpression(tasks, expression);
		} catch (IOException e) {
			throw new PCDIException(e.getMessage());
		}
	}
	public void getPartialAIF(BitList tasks, String expr, boolean listChildren, boolean express) throws PCDIException {
		try {
			proxy.debugGetPartialAIF(tasks, expr, listChildren, express);
		} catch (IOException e) {
			throw new PCDIException(e.getMessage());
		}
	}
	public void variableDelete(BitList tasks, String varname) throws PCDIException {
		try {
			proxy.debugVariableDelete(tasks, varname);
		} catch (IOException e) {
			throw new PCDIException(e.getMessage());
		}
	}
	
	protected int getErrorCode(int internalErrorCode) {
		switch (internalErrorCode) {
		case IParallelDebuggerConstants.DBGERR_NOBACKEND:
		case IParallelDebuggerConstants.DBGERR_DEBUGGER:
		case IParallelDebuggerConstants.DBGERR_NOFILEDIR:
		case IParallelDebuggerConstants.DBGERR_CHDIR:
			return IPCDIErrorEvent.DBG_FATAL;
		//case IParallelDebuggerConstants.DBGERR_INPROGRESS:
		//case IParallelDebuggerConstants.DBGERR_UNKNOWN_TYPE:
		//case IParallelDebuggerConstants.DBGERR_UNKNOWN_VARIABLE:
		case IParallelDebuggerConstants.DBGERR_NOFILE:
		case IParallelDebuggerConstants.DBGERR_NOBP:
			return IPCDIErrorEvent.DBG_NORMAL;
		default:
			return IPCDIErrorEvent.DBG_WARNING;
		}
	}
	
	/************************************
	 * Debug handle event
	 ***********************************/
	public synchronized void handleEvent(IProxyDebugEvent e) {
		PDebugUtils.println("got debug event: " + e.toString());
		switch (e.getEventID()) {
		case IProxyDebugEvent.EVENT_DBG_OK:
			completeCommand(e.getBitSet(), IDebugCommand.RETURN_OK);
			break;
			
		case IProxyDebugEvent.EVENT_DBG_INIT:
			int numServers = ((ProxyDebugInitEvent)e).getNumServers();
			PDebugUtils.println("num servers = " + numServers);
			completeCommand(e.getBitSet(), IDebugCommand.RETURN_OK);
			break;
		
		case IProxyDebugEvent.EVENT_DBG_ERROR:
			ProxyDebugErrorEvent errEvent = (ProxyDebugErrorEvent)e;
			String errMsg = errEvent.getErrorMessage();
			if (errMsg == null || errMsg.length() ==0)
				errMsg = "Unknown Error";
			
			int err_code = getErrorCode(errEvent.getErrorCode());
			completeCommand(e.getBitSet(), new PCDIException(errMsg, err_code));
			//handleErrorEvent(e.getBitSet(), errMsg, err_code);
			//handleException(e.getBitSet(), err_code);
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
				
				IDebugCommand cmd = getInterruptCommand();
				if (cmd != null) {
					cmd.setReturn(e.getBitSet(), IDebugCommand.RETURN_OK);
				}
				else {
					//send signal if the current command is not terminate command.
					handleProcessSignaledEvent(e.getBitSet(), sigEvent.getLocator(), sigEvent.getThreadId(), sigEvent.getChangedVars());
				}
			}
			break;	
			
		case IProxyDebugEvent.EVENT_DBG_BPSET:
			completeCommand(e.getBitSet(), ((ProxyDebugBreakpointSetEvent)e).getBreakpoint());
			break;
			
		case IProxyDebugEvent.EVENT_DBG_FRAMES:
			completeCommand(e.getBitSet(), ((ProxyDebugStackframeEvent)e).getFrames());
			//completeCommand(e.getBitSet(), (IPCDIStackFrame[]) frameList.toArray(new IPCDIStackFrame[0]));
			break;

		case IProxyDebugEvent.EVENT_DBG_TYPE:
			completeCommand(e.getBitSet(), ((ProxyDebugTypeEvent)e).getType());
			break;
			
		case IProxyDebugEvent.EVENT_DBG_DATA:
			completeCommand(e.getBitSet(), ((ProxyDebugDataEvent)e).getData());
			break;
			
		case IProxyDebugEvent.EVENT_DBG_VARS:
			completeCommand(e.getBitSet(), ((ProxyDebugVarsEvent)e).getVariables());			
			break;
			
		case IProxyDebugEvent.EVENT_DBG_ARGS:
			completeCommand(e.getBitSet(), ((ProxyDebugArgsEvent)e).getVariables());			
			break;

		case IProxyDebugEvent.EVENT_DBG_EXIT_SIGNAL:
			PDebugUtils.println("======================= EVENT_DBG_EXIT_SIGNAL ====================");
			ProxyDebugSignalExitEvent exitSigEvent = (ProxyDebugSignalExitEvent)e;
			handleProcessTerminatedEvent(e.getBitSet(), exitSigEvent.getSignalName(), exitSigEvent.getSignalMeaning());
			break;

		case IProxyDebugEvent.EVENT_DBG_EXIT:
			PDebugUtils.println("======================= EVENT_DBG_EXIT ====================");
			ProxyDebugExitEvent exitEvent = (ProxyDebugExitEvent)e;
			handleProcessTerminatedEvent(e.getBitSet(), exitEvent.getExitStatus());
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
			completeCommand(e.getBitSet(), ((ProxyDebugInfoThreadsEvent)e).getThreadIds());			
			break;
			
		case IProxyDebugEvent.EVENT_DBG_THREAD_SELECT:
			ProxyDebugSetThreadSelectEvent setThreadSelectEvent = (ProxyDebugSetThreadSelectEvent)e;
			Object[] objects = new Object[2];
			objects[0] = new Integer(setThreadSelectEvent.getThreadId());
			objects[1] = setThreadSelectEvent.getFrame();
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
			
		case IProxyDebugEvent.EVENT_DBG_DATA_EVA_EX:
			completeCommand(e.getBitSet(), (ProxyDebugDataExpValueEvent)e);
			break;

		case IProxyDebugEvent.EVENT_DBG_PARTIAL_AIF:
			completeCommand(e.getBitSet(), (ProxyDebugPartialAIFEvent)e);
			break;
		}
	}	

	private int newBreakpointId() {
		return this.bpId++;
	}
}