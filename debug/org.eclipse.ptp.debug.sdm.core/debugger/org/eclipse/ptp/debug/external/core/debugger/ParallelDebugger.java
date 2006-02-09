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
import java.util.ArrayList;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.core.IPProcess;
import org.eclipse.ptp.core.PreferenceConstants;
import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.IDebugCommand;
import org.eclipse.ptp.debug.core.IDebugger;
import org.eclipse.ptp.debug.core.PTPDebugCorePlugin;
import org.eclipse.ptp.debug.core.cdi.PCDIException;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIErrorEvent;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIArgument;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIFunctionBreakpoint;
import org.eclipse.ptp.debug.core.cdi.model.IPCDILineBreakpoint;
import org.eclipse.ptp.debug.core.cdi.model.IPCDILocalVariable;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIStackFrame;
import org.eclipse.ptp.debug.core.cdi.model.IPCDITarget;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIThread;
import org.eclipse.ptp.debug.external.core.AbstractDebugger;
import org.eclipse.ptp.debug.external.core.cdi.model.StackFrame;
import org.eclipse.ptp.debug.external.core.cdi.model.Target;
import org.eclipse.ptp.debug.external.core.cdi.model.Thread;
import org.eclipse.ptp.debug.external.core.cdi.model.variable.Argument;
import org.eclipse.ptp.debug.external.core.cdi.model.variable.LocalVariable;
import org.eclipse.ptp.debug.external.core.proxy.ProxyDebugClient;
import org.eclipse.ptp.debug.external.core.proxy.ProxyDebugStackframe;
import org.eclipse.ptp.debug.external.core.proxy.event.IProxyDebugEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.IProxyDebugEventListener;
import org.eclipse.ptp.debug.external.core.proxy.event.ProxyDebugArgsEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.ProxyDebugBreakpointHitEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.ProxyDebugBreakpointSetEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.ProxyDebugDataEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.ProxyDebugErrorEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.ProxyDebugInitEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.ProxyDebugSignalEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.ProxyDebugStackframeEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.ProxyDebugStepEvent;
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
			String app = (String) job.getAttribute(PreferenceConstants.JOB_APP);
			String dir = (String) job.getAttribute(PreferenceConstants.JOB_WORK_DIR);
			String[] args = (String[]) job.getAttribute(PreferenceConstants.JOB_ARGS);
			proxy.waitForConnect();
			proxy.addEventListener(this);
			proxy.debugStartSession(dir, app, join(args, " "));
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
		halt(tasks);
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
			proxy.debugSetLineBreakpoint(tasks, newBreakpointId(), bpt.getLocator().getFile(), bpt.getLocator().getLineNumber());
		} catch (IOException e) {
			throw new PCDIException(e.getMessage());
		}
	}
	public void setFunctionBreakpoint(BitList tasks, IPCDIFunctionBreakpoint bpt) throws PCDIException {
		try {
			proxy.debugSetFuncBreakpoint(tasks, newBreakpointId(), bpt.getLocator().getFile(), bpt.getLocator().getFunction());
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
	public void setCurrentStackFrame(BitList tasks, IPCDIStackFrame frame) throws PCDIException {
		try {
			proxy.debugSetCurrentStackframe(tasks, frame.getLevel());
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
	public void listArguments(BitList tasks, IPCDIStackFrame frame) throws PCDIException {
		this.currFrame = frame;
		try {
			proxy.debugListArguments(tasks, frame.getLevel());
		} catch (IOException e) {
			throw new PCDIException(e.getMessage());
		}
	}
	public synchronized void handleEvent(IProxyDebugEvent e) {
		System.out.println("got debug event: " + e.toString());
		switch (e.getEventID()) {
		case IProxyDebugEvent.EVENT_DBG_OK:
			completeCommand(e.getBitSet(), IDebugCommand.OK);
			break;
			
		case IProxyDebugEvent.EVENT_DBG_INIT:
			numServers = ((ProxyDebugInitEvent)e).getNumServers();
			System.out.println("num servers = " + numServers);
			completeCommand(e.getBitSet(), IDebugCommand.OK);
			break;
			
		case IProxyDebugEvent.EVENT_DBG_BPHIT:
			ProxyDebugBreakpointHitEvent bptHitEvent = (ProxyDebugBreakpointHitEvent)e;
			handleBreakpointHitEvent(e.getBitSet(), bptHitEvent.getBreakpointId());
			break;
			
		case IProxyDebugEvent.EVENT_DBG_STEP:
			ProxyDebugStepEvent stepEvent = (ProxyDebugStepEvent)e;
			handleEndSteppingEvent(e.getBitSet(), stepEvent.getFrame().getLocator().getLineNumber(), stepEvent.getFrame().getLocator().getFile());
			//LineLocation loc = new LineLocation(stepEvent.getFrame().getLocator().getFile(), stepEvent.getFrame().getLocator().getLineNumber());
			//fireEvent(new EndSteppingRangeEvent(getSession(), e.getBitSet(), loc));
			break;	
			
		case IProxyDebugEvent.EVENT_DBG_BPSET:
			ProxyDebugBreakpointSetEvent bpEvt = (ProxyDebugBreakpointSetEvent)e;
			completeCommand(e.getBitSet(), bpEvt.getBreakpoint());
			//updateBreakpointInfo(bpEvt.getBreakpointId(), e.getBitSet(), bpEvt.getBreakpoint());
			break;
			
		case IProxyDebugEvent.EVENT_DBG_FRAMES:
			ProxyDebugStackframeEvent frameEvent = (ProxyDebugStackframeEvent)e;
			
			IPProcess[] frameProcs = getProcesses(e.getBitSet());
			if (frameProcs.length > 0) {
				completeCommand(e.getBitSet(), convertFrames(frameProcs[0], frameEvent.getFrames()));
			}
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
			if (this.currFrame == null)
				return;

			ProxyDebugVarsEvent varsEvent = (ProxyDebugVarsEvent)e;
			ArrayList varList = new ArrayList();
			
			IPProcess[] varProcs = getProcesses(varsEvent.getBitSet());
			if(varProcs.length > 0) {
				// ICDITarget target = procList[i].getTarget();
				int taskId = varProcs[0].getTaskId();
				IPCDITarget target = getSession().getTarget(taskId);
				IPCDIThread thread = new Thread((Target) target, 0);
				String vars[] = varsEvent.getVariables();
				for (int j = 0; j < vars.length; j++) {
					LocalVariable var = new LocalVariable((Target) target, (Thread) thread, (StackFrame) this.currFrame, vars[j], vars[j], vars.length - j, this.currFrame.getLevel(), null);
					varList.add(var);
				}
			}
			completeCommand(e.getBitSet(), (IPCDILocalVariable[])varList.toArray(new IPCDILocalVariable[0]));			
			break;
			
		case IProxyDebugEvent.EVENT_DBG_ARGS:
			if (this.currFrame == null)
				return;

			ProxyDebugArgsEvent argsEvent = (ProxyDebugArgsEvent)e;
			ArrayList argList = new ArrayList();
			
			IPProcess[] argProcs = getProcesses(argsEvent.getBitSet());
			if(argProcs.length > 0) {
				// ICDITarget target = procList[i].getTarget();
				int taskId = argProcs[0].getTaskId();
				IPCDITarget target = getSession().getTarget(taskId);
				IPCDIThread thread = new Thread((Target) target, 0);
				String args[] = argsEvent.getVariables();
				for (int j = 0; j < args.length; j++) {
					Argument arg = new Argument((Target) target, (Thread) thread, (StackFrame) this.currFrame, args[j], args[j], args.length - j, this.currFrame.getLevel(), null);
					argList.add(arg);
				}
			}
			completeCommand(e.getBitSet(), (IPCDIArgument[]) argList.toArray(new IPCDIArgument[0]));			
			break;
			
		case IProxyDebugEvent.EVENT_DBG_EXIT:
			System.out.println("======================= EVENT_DBG_EXIT ====================");
			handleProcessTerminatedEvent(e.getBitSet());
			break;
			
		case IProxyDebugEvent.EVENT_DBG_SIGNAL:
			ProxyDebugSignalEvent sigEvent = (ProxyDebugSignalEvent)e;
			completeCommand(e.getBitSet(), sigEvent.getLocator());
			//handleProcessSignaledEvent(e.getBitSet(), sigEvent.getLocator());
			break;
			
		case IProxyDebugEvent.EVENT_DBG_ERROR:
			System.err.println("======================= EVENT_DBG_ERROR ====================");
			ProxyDebugErrorEvent errEvent = (ProxyDebugErrorEvent)e;
			completeCommand(e.getBitSet(), null);
			int code = errEvent.getErrorCode();
			if (code == IParallelDebuggerConstants.DBGERR_DEBUGGER || code == IParallelDebuggerConstants.DBGERR_NOFILEDIR || code == IParallelDebuggerConstants.DBGERR_CHDIR) {
				code = IPCDIErrorEvent.DBG_FATAL;
			}
			else {
				code = IPCDIErrorEvent.DBG_ERROR;
			}
			handleErrorEvent(e.getBitSet(), errEvent.getErrorMessage(), code);
			break;
		}
	}
	
	private IPCDIStackFrame convertFrame(IPCDIThread thread, ProxyDebugStackframe frame) {
		int level = frame.getLevel();
		String file = frame.getLocator().getFile();
		String func = frame.getLocator().getFunction();
		int line = frame.getLocator().getLineNumber();
		BigInteger addr = frame.getLocator().getAddress();
		System.out.println("frame " + level + " " + file + " " + func + " " + line + " " + addr);
		return new StackFrame((Thread) thread, level, file, func, line, addr.toString(16));
	}
	private IPCDIStackFrame[] convertFrames(IPProcess proc, ProxyDebugStackframe[] frames) {
		ArrayList frameList = new ArrayList();
		int taskId = proc.getTaskId();
		IPCDITarget target = getSession().getTarget(taskId);
	    IPCDIThread thread = new Thread((Target) target, 0);
		for (int j = 0; j < frames.length; j++) {
			frameList.add(convertFrame(thread, frames[j]));
		}
		return (IPCDIStackFrame[]) frameList.toArray(new IPCDIStackFrame[0]);
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
