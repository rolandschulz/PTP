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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ptp.core.attributes.ArrayAttribute;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.elements.IPProcess;
import org.eclipse.ptp.core.elements.attributes.JobAttributes;
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
import org.eclipse.ptp.debug.external.core.proxy.event.IProxyDebugArgsEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.IProxyDebugBreakpointHitEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.IProxyDebugBreakpointSetEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.IProxyDebugDataEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.IProxyDebugDataExpValueEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.IProxyDebugErrorEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.IProxyDebugEventListener;
import org.eclipse.ptp.debug.external.core.proxy.event.IProxyDebugExitEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.IProxyDebugInfoThreadsEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.IProxyDebugInitEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.IProxyDebugMemoryInfoEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.IProxyDebugOKEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.IProxyDebugPartialAIFEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.IProxyDebugSetThreadSelectEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.IProxyDebugSignalEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.IProxyDebugSignalExitEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.IProxyDebugSignalsEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.IProxyDebugStackInfoDepthEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.IProxyDebugStackframeEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.IProxyDebugStepEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.IProxyDebugSuspendEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.IProxyDebugTypeEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.IProxyDebugVarsEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.ProxyDebugInitEvent;


public class ParallelDebugger extends AbstractDebugger implements IDebugger, IProxyDebugEventListener {
	private ProxyDebugClient	proxy;
	private int					bpId = 0;

	public int getDebuggerPort() throws CoreException {
		proxy = new ProxyDebugClient();
		proxy.addProxyDebugEventListener(this);
		try {
			proxy.sessionCreate();
		} catch (IOException e) {
			proxy.removeProxyDebugEventListener(this);
			throw new CoreException(new Status(IStatus.ERROR, PTPDebugCorePlugin.getUniqueIdentifier(), IStatus.ERROR, "could not create proxy", null));
		}
		return proxy.getSessionPort();
	}
	
	public void connection(IProgressMonitor monitor) throws CoreException {
		try {
			//using checkConnection() instead of waitForConnect()
			//proxy.checkConnection();
			if (proxy.waitForConnect(monitor)) {
				proxy.sessionHandleEvents();
			} else {
				proxy.removeProxyDebugEventListener(this);
			}
		} catch (IOException e) {
			proxy.removeProxyDebugEventListener(this);
			throw new CoreException(new Status(IStatus.ERROR, PTPDebugCorePlugin.getUniqueIdentifier(), IStatus.ERROR, e.getMessage(), null));
		}
	}
	
	public void disconnection(IProgressMonitor monitor) throws CoreException {
		try {
			if (proxy != null) {
				proxy.removeProxyDebugEventListener(this);
				proxy.closeConnection();
			}
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, PTPDebugCorePlugin.getUniqueIdentifier(), IStatus.ERROR, e.getMessage(), null));
		} finally {
			proxy = null;
		}
	}
	
	public void startDebugger(IPJob job) throws CoreException {
		try {
			String app = job.getAttribute(JobAttributes.getExecutableNameAttributeDefinition()).getValueAsString();
			String path = job.getAttribute(JobAttributes.getExecutablePathAttributeDefinition()).getValueAsString();
			String dir = job.getAttribute(JobAttributes.getWorkingDirectoryAttributeDefinition()).getValueAsString();
			String[] args = (String[]) (((ArrayAttribute)job.getAttribute(JobAttributes.getProgramArgumentsAttributeDefinition())).getValue());
			proxy.debugStartSession(app, path, dir, args);
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, PTPDebugCorePlugin.getUniqueIdentifier(), IStatus.ERROR, "Cannot start debugger", e));
		}
	}
	
	public void stopDebugger() throws CoreException {
		if (proxy != null) {
			try {
				proxy.sessionFinish();
			} catch (IOException e) {
				throw new CoreException(new Status(IStatus.ERROR, PTPDebugCorePlugin.getUniqueIdentifier(), IStatus.ERROR, e.getMessage(), null));
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
	
	public void getPartialAIF(BitList tasks, String expr, String key, boolean listChildren, boolean express) throws PCDIException {
		try {
			proxy.debugGetPartialAIF(tasks, expr, key, listChildren, express);
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
		case IParallelDebuggerConstants.DBGERR_UNKNOWN_TYPE:
		case IParallelDebuggerConstants.DBGERR_NOFILE:
		case IParallelDebuggerConstants.DBGERR_NOBP:
			return IPCDIErrorEvent.DBG_NORMAL;
		case IParallelDebuggerConstants.DBGERR_UNKNOWN_VARIABLE:
			return IPCDIErrorEvent.DBG_IGNORE;
		default:
			return IPCDIErrorEvent.DBG_WARNING;
		}
	}
	
	private int newBreakpointId() {
		return this.bpId++;
	}

	public void handleProxyDebugArgsEvent(IProxyDebugArgsEvent e) {
		completeCommand(e.getBitSet(),e.getVariables());			
	}

	public void handleProxyDebugBreakpointHitEvent(IProxyDebugBreakpointHitEvent e) {
		handleBreakpointHitEvent(e.getBitSet(), e.getBreakpointId(), e.getThreadId(), e.getChangedVars());
	}

	public void handleProxyDebugBreakpointSetEvent(IProxyDebugBreakpointSetEvent e) {
		completeCommand(e.getBitSet(), e.getBreakpoint());
	}

	public void handleProxyDebugDataEvent(IProxyDebugDataEvent e) {
		completeCommand(e.getBitSet(), e.getData());
	}

	public void handleProxyDebugDataExpValueEvent(IProxyDebugDataExpValueEvent e) {
		completeCommand(e.getBitSet(), e);
	}

	public void handleProxyDebugExitEvent(IProxyDebugExitEvent e) {
		PDebugUtils.println("======================= EVENT_DBG_EXIT ====================");
		handleProcessTerminatedEvent(e.getBitSet(), e.getExitStatus());
	}

	public void handleProxyDebugErrorEvent(IProxyDebugErrorEvent e) {
		String errMsg = e.getErrorMessage();
		if (errMsg == null || errMsg.length() ==0)
			errMsg = "Unknown Error";
		
		int err_code = getErrorCode(e.getErrorCode());
		completeCommand(e.getBitSet(), new PCDIException(errMsg, err_code));
		//handleErrorEvent(e.getBitSet(), errMsg, err_code);
		//handleException(e.getBitSet(), err_code);
	}

	public void handleProxyDebugInfoThreadsEvent(IProxyDebugInfoThreadsEvent e) {
		completeCommand(e.getBitSet(), e.getThreadIds());			
	}

	public void handleProxyDebugInitEvent(IProxyDebugInitEvent e) {
		int numServers = ((ProxyDebugInitEvent)e).getNumServers();
		PDebugUtils.println("num servers = " + numServers);
		completeCommand(e.getBitSet(), IDebugCommand.RETURN_OK);
	}

	public void handleProxyDebugMemoryInfoEvent(IProxyDebugMemoryInfoEvent e) {
		completeCommand(e.getBitSet(), e.getMemoryInfo());			
	}

	public void handleProxyDebugOKEvent(IProxyDebugOKEvent e) {
		completeCommand(e.getBitSet(), IDebugCommand.RETURN_OK);
	}
	
	public void handleProxyDebugPartialAIFEvent(IProxyDebugPartialAIFEvent e) {
		completeCommand(e.getBitSet(), e);
	}

	public void handleProxyDebugSetThreadSelectEvent(IProxyDebugSetThreadSelectEvent e) {
		Object[] objects = new Object[2];
		objects[0] = new Integer(e.getThreadId());
		objects[1] = e.getFrame();
		completeCommand(e.getBitSet(), objects);
	}

	public void handleProxyDebugSignalEvent(IProxyDebugSignalEvent e) {
		IDebugCommand cmd = getInterruptCommand();
		if (cmd != null) {
			cmd.setReturn(e.getBitSet(), IDebugCommand.RETURN_OK);
		}
		else {
			//send signal if the current command is not terminate command.
			handleProcessSignaledEvent(e.getBitSet(), e.getLocator(), e.getThreadId(), e.getChangedVars());
		}
	}

	public void handleProxyDebugSignalExitEvent(IProxyDebugSignalExitEvent e) {
		PDebugUtils.println("======================= EVENT_DBG_EXIT_SIGNAL ====================");
		handleProcessTerminatedEvent(e.getBitSet(), e.getSignalName(), e.getSignalMeaning());
	}

	public void handleProxyDebugSignalsEvent(IProxyDebugSignalsEvent e) {
		IPCDISignal[] pcdiSignals = new IPCDISignal[0];
		IPProcess[] sigProcs = getProcesses(e.getBitSet());
		if (sigProcs.length > 0) {
			String taskIdStr = sigProcs[0].getProcessNumber();
			try {
				int taskId = Integer.parseInt(taskIdStr);
				IPCDITarget target = getSession().getTarget(taskId);
				ProxyDebugSignal[] signals = e.getSignals();
				pcdiSignals = new IPCDISignal[signals.length];
				for (int j=0; j<signals.length; j++) {
					pcdiSignals[j] = new Signal((Target)target, signals[j].getName(), signals[j].isStop(), signals[j].isPrint(), signals[j].isPass(), signals[j].getDescription()); 
				}
			} catch (NumberFormatException e1) {
			}
		}
		completeCommand(e.getBitSet(), pcdiSignals);
	}

	public void handleProxyDebugStackInfoDepthEvent(IProxyDebugStackInfoDepthEvent e) {
		completeCommand(e.getBitSet(), new Integer(e.getDepth()));			
	}

	public void handleProxyDebugStackframeEvent(IProxyDebugStackframeEvent e) {
		completeCommand(e.getBitSet(), e.getFrames());
	}

	public void handleProxyDebugStepEvent(IProxyDebugStepEvent e) {
		handleEndSteppingEvent(e.getBitSet(), e.getFrame().getLocator().getLineNumber(), e.getFrame().getLocator().getFile(), e.getThreadId(), e.getChangedVars());
	}

	public void handleProxyDebugSuspendEvent(IProxyDebugSuspendEvent e) {
		handleSuspendEvent(e.getBitSet(), e.getLocator(), e.getThreadId(), e.getChangedVars());
	}

	public void handleProxyDebugTypeEvent(IProxyDebugTypeEvent e) {
		completeCommand(e.getBitSet(), e.getType());
	}

	public void handleProxyDebugVarsEvent(IProxyDebugVarsEvent e) {
		completeCommand(e.getBitSet(), e.getVariables());			
	}

}
