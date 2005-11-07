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

package org.eclipse.ptp.debug.external.debugger;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import org.eclipse.cdt.debug.core.cdi.model.ICDIArgument;
import org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDIFunctionBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDILineBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDILocalVariable;
import org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDIThread;
import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.core.IPProcess;
import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.core.util.Queue;
import org.eclipse.ptp.debug.core.cdi.PCDIException;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIEvent;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIExitedEvent;
import org.eclipse.ptp.debug.core.cdi.event.IPCDISuspendedEvent;
import org.eclipse.ptp.debug.external.AbstractDebugger;
import org.eclipse.ptp.debug.external.IDebugger;
import org.eclipse.ptp.debug.external.cdi.event.BreakpointHitEvent;
import org.eclipse.ptp.debug.external.cdi.event.EndSteppingRangeEvent;
import org.eclipse.ptp.debug.external.cdi.event.InferiorExitedEvent;
import org.eclipse.ptp.debug.external.cdi.event.InferiorSignaledEvent;
import org.eclipse.ptp.debug.external.cdi.model.LineLocation;
import org.eclipse.ptp.debug.external.cdi.model.StackFrame;
import org.eclipse.ptp.debug.external.cdi.model.Target;
import org.eclipse.ptp.debug.external.cdi.model.Thread;
import org.eclipse.ptp.debug.external.cdi.model.variable.Argument;
import org.eclipse.ptp.debug.external.cdi.model.variable.LocalVariable;
import org.eclipse.ptp.debug.external.proxy.ProxyDebugClient;
import org.eclipse.ptp.debug.external.proxy.ProxyDebugStackframe;
import org.eclipse.ptp.debug.external.proxy.event.IProxyDebugEvent;
import org.eclipse.ptp.debug.external.proxy.event.IProxyDebugEventListener;
import org.eclipse.ptp.debug.external.proxy.event.ProxyDebugArgsEvent;
import org.eclipse.ptp.debug.external.proxy.event.ProxyDebugBreakpointHitEvent;
import org.eclipse.ptp.debug.external.proxy.event.ProxyDebugBreakpointSetEvent;
import org.eclipse.ptp.debug.external.proxy.event.ProxyDebugDataEvent;
import org.eclipse.ptp.debug.external.proxy.event.ProxyDebugInitEvent;
import org.eclipse.ptp.debug.external.proxy.event.ProxyDebugSignalEvent;
import org.eclipse.ptp.debug.external.proxy.event.ProxyDebugStackframeEvent;
import org.eclipse.ptp.debug.external.proxy.event.ProxyDebugStepEvent;
import org.eclipse.ptp.debug.external.proxy.event.ProxyDebugTypeEvent;
import org.eclipse.ptp.debug.external.proxy.event.ProxyDebugVarsEvent;
import org.eclipse.ptp.debug.external.target.ITargetEvent;


public class ParallelDebugger extends AbstractDebugger implements IDebugger, IProxyDebugEventListener {
	private class BreakpointMapping {
		private ICDIBreakpoint		bpObject;
		private BitList				bpSet;
		private int					bpId;
		
		public BreakpointMapping(int bpid, BitList set, ICDIBreakpoint bpt) {
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
		
		public ICDIBreakpoint getBreakpoint() {
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
	
	private ProxyDebugClient		proxy;
	private int					numServers;
	private Queue				events = new Queue();
	private HashMap				bpMap = new HashMap();
	private ArrayList			bpArray = new ArrayList();
	private int					bpId = 0;
	private ICDIStackFrame		currFrame = null;
	
	/*
	 * Wait for any event
	 */
	private synchronized void waitForEvent() {
		try {
			while (this.events.isEmpty())
				wait();
			this.events.removeItem();
		} catch (InterruptedException e) {
		}
	}
	
	/*
	 * Wait until events have been received from all processes in 'procs'
	 */
	private synchronized void waitForEvents(BitList procs) throws PCDIException {
		BitList remain = procs.copy();
		
		try {
			while (!remain.isEmpty()) {
				wait();
				while (!this.events.isEmpty()) {
					IProxyDebugEvent e = (IProxyDebugEvent)this.events.removeItem();
					remain.andNot(e.getBitSet());		
				}
			}
		} catch (InterruptedException e) {
		}
	}

	private String join(String[] strs, String delim) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < strs.length; i++) {
			if (i > 0)
				buf.append(delim);
			buf.append(strs[i]);
		}
		return buf.toString();
	}
	
	public void startDebuggerListener() {
		proxy = new ProxyDebugClient("localhost", 12346);
		try {
			proxy.sessionCreate();
		} catch (IOException e) {
			return;
		}
		
	}
	
	public void startDebugger(IPJob job) {
		try {
			proxy.waitForConnect(); // PROXY_DEBUG_CONNECT
			proxy.addEventListener(this);

			waitForEvent(); // PROXY_DEBUG_INIT
			
			String app = (String) job.getAttribute("app");
			//String dir = (String) job.getAttribute("dir");
			String[] args = (String[]) job.getAttribute("args");
			
			proxy.debugStartSession(app, join(args, " "));
			waitForEvent(); // PROXY_DEBUG_OK
		} catch (IOException e) {
			return;
		}
	}
	
	public void stopDebugger() {
		try {
			proxy.sessionFinish();
		} catch (IOException e) {
			return;
		}
		//waitForEvent();
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
		}
	}

	public void kill(BitList tasks) throws PCDIException {
		halt(tasks);
		try {
			proxy.debugTerminate(tasks);
		} catch (IOException e) {
			// TODO deal with IOException (maybe should be dealt with in ProxyClient?)
		}
		waitForEvents(tasks);		
		IPCDIExitedEvent e = new InferiorExitedEvent(getSession(), tasks);
		super.fireEvent(e);
	}

	public void halt(BitList tasks) throws PCDIException {
		try {
			proxy.debugInterrupt(tasks);
		} catch (IOException e) {
			// TODO deal with IOException (maybe should be dealt with in ProxyClient?)
		}
	}
	
	public void stepInto(BitList tasks, int count) throws PCDIException {
		try {
			proxy.debugStep(tasks, count, ProxyDebugClient.STEP_INTO);
		} catch (IOException e) {
		}
	}
	
	public void stepOver(BitList tasks, int count) throws PCDIException {
		try {
			proxy.debugStep(tasks, count, ProxyDebugClient.STEP_OVER);
		} catch (IOException e) {
		}
	}
	
	public void stepFinish(BitList tasks, int count) throws PCDIException {
		try {
			proxy.debugStep(tasks, count, ProxyDebugClient.STEP_FINISH);
		} catch (IOException e) {
		}
	}
	
	public void setLineBreakpoint(BitList tasks, ICDILineBreakpoint bpt) throws PCDIException {
		try {
			proxy.debugSetLineBreakpoint(tasks, newBreakpointId(), bpt.getLocator().getFile(), bpt.getLocator().getLineNumber());
		} catch (IOException e1) {
			return;
		}
		waitForEvents(tasks);
	}

	public void setFunctionBreakpoint(BitList tasks, ICDIFunctionBreakpoint bpt) throws PCDIException {
		try {
			proxy.debugSetFuncBreakpoint(tasks, newBreakpointId(), bpt.getLocator().getFile(), bpt.getLocator().getFunction());
		} catch (IOException e1) {
			return;
		}
		waitForEvents(tasks);
	}

	private void deleteBreakpoint(BitList tasks, int bpid)  throws PCDIException {
		try {
			proxy.debugDeleteBreakpoint(tasks, bpid);
		} catch (IOException e1) {
			return;
		}
		waitForEvents(tasks);
	}
	
	private void deleteBreakpoint(ICDIBreakpoint bp)  throws PCDIException {
		BreakpointMapping bpm = findBreakpointInfo(bp);
		
		if (bpm != null) {
			deleteBreakpoint(bpm.getBreakpointProcs(), bpm.getBreakpointId());				
		}
	}
	
	public void deleteBreakpoints(ICDIBreakpoint[] bp) throws PCDIException {
		if (bp != null) {
			for (int i = 0; i < bp.length; i++) {
				deleteBreakpoint(bp[i]);
			}
		}
	}
	
	/**
	 * list stack frames for first process in procs
	 */
	public void listStackFrames(BitList tasks) throws PCDIException {
		System.out.println(" *********** call stack frame");
		try {
			proxy.debugListStackframes(tasks, 0);
		} catch (IOException e) {
			throw new PCDIException(e.getMessage());
		}
		//waitForEvents(tasks);
	}
	
	public void setCurrentStackFrame(BitList tasks, ICDIStackFrame frame) throws PCDIException {
		/*
		try {
			proxy.debugSetCurrentStackframe(tasks, frame.getLevel());
		} catch (IOException e) {
			throw new PCDIException(e.getMessage());
		}
		waitForEvents(tasks);
		*/
	}
	
	/**
	 * evaluate expression for first process in procs
	 */
	public void evaluateExpression(BitList tasks, String expr) throws PCDIException {
		throw new PCDIException(PCDIException.NOT_IMPLEMENTED, "evaluateExpression");
	}
	
	public void getAIFValue(BitList tasks, String expr) throws PCDIException {
		System.out.println(" *********** call getAIF");
		try {
			proxy.debugEvaluateExpression(tasks, expr);
		} catch (IOException e1) {
			throw new PCDIException(e1.getMessage());
		}
		//waitForEvents(tasks);
	}
	
	/**
	 * get variable type for first process in procs
	 */
	public void getVariableType(BitList tasks, String varName) throws PCDIException {
		System.out.println(" *********** call getVariableType");
		try {
			proxy.debugGetType(tasks, varName);
		} catch (IOException e1) {
			throw new PCDIException(e1.getMessage());
		}
		//waitForEvents(tasks);
	}
	
	/**
	 * list local variables for first process in procs
	 */
	public void listLocalVariables(BitList tasks, ICDIStackFrame frame) throws PCDIException {
		System.out.println(" *********** call listLocalVariables");
		this.currFrame = frame;
		try {
			proxy.debugListLocalVariables(tasks);
		} catch (IOException e1) {
			throw new PCDIException(e1.getMessage());
		}
		//waitForEvents(tasks);
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
	public void listArguments(BitList tasks, ICDIStackFrame frame) throws PCDIException {
		System.out.println(" *********** call listArguments");
		this.currFrame = frame;
		try {
			proxy.debugListArguments(tasks, frame.getLevel());
		} catch (IOException e1) {
			throw new PCDIException(e1.getMessage());
		}
		//waitForEvents(tasks);
	}

	public synchronized void handleEvent(IProxyDebugEvent e) {
		System.out.println("got debug event: " + e.toString());
		switch (e.getEventID()) {
		case IProxyDebugEvent.EVENT_DBG_INIT:
			numServers = ((ProxyDebugInitEvent)e).getNumServers();
			System.out.println("num servers = " + numServers);
			break;
			
		case IProxyDebugEvent.EVENT_DBG_BPHIT:
			/*
			 * Retrieve the breakpoint object.
			 */
			BreakpointMapping bp = findBreakpointInfo(((ProxyDebugBreakpointHitEvent)e).getBreakpointId());
			if (bp != null) {
				IPCDIEvent ev = new BreakpointHitEvent(getSession(), e.getBitSet(), bp.bpObject);
				super.fireEvent(ev);
			}
			break;
			
		case IProxyDebugEvent.EVENT_DBG_STEP:
			ProxyDebugStepEvent stepEvent = (ProxyDebugStepEvent)e;
			LineLocation loc = new LineLocation(stepEvent.getFrame().getLocator().getFile(), stepEvent.getFrame().getLocator().getLineNumber());
			IPCDIEvent ev = new EndSteppingRangeEvent(getSession(), e.getBitSet(), loc);
			super.fireEvent(ev);
			break;	
			
		case IProxyDebugEvent.EVENT_DBG_BPSET:
			ProxyDebugBreakpointSetEvent bpEvt = (ProxyDebugBreakpointSetEvent)e;	
			updateBreakpointInfo(bpEvt.getBreakpointId(), e.getBitSet(), bpEvt.getBreakpoint());
			break;
			
		case IProxyDebugEvent.EVENT_DBG_FRAMES:
			ProxyDebugStackframeEvent frameEvent = (ProxyDebugStackframeEvent)e;
			
			IPProcess[] frameProcs = getProcesses(e.getBitSet());
			if (frameProcs.length > 0) {
				try {
					System.out.println("########### set stack frame to target event");
					setTargetResult(e.getBitSet(), convertFrames(frameProcs[0], frameEvent.getFrames()), ITargetEvent.STACKFRAME_TYPE);
				} catch (PCDIException pe) {
					pe.printStackTrace();
				}
			}
			break;

		case IProxyDebugEvent.EVENT_DBG_TYPE:
			ProxyDebugTypeEvent type = (ProxyDebugTypeEvent)e;
			try {
				System.out.println("########### set variable type to target event");
				setTargetResult(e.getBitSet(), type.getType(), ITargetEvent.VARIABLETYPE_TYPE);
			} catch (PCDIException pe) {
				pe.printStackTrace();
			}
			break;
			
		case IProxyDebugEvent.EVENT_DBG_DATA:
			ProxyDebugDataEvent data = (ProxyDebugDataEvent)e;
			try {
				System.out.println("########### set AIF data to target event");
				setTargetResult(e.getBitSet(), data.getData(), ITargetEvent.AIFVALUE_TYPE);		
			} catch (PCDIException pe) {
				pe.printStackTrace();
			}
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
				ICDITarget target = getSession().getTarget(taskId);
				ICDIThread thread = new Thread((Target) target, 0);
				String vars[] = varsEvent.getVariables();
				for (int j = 0; j < vars.length; j++) {
					LocalVariable var = new LocalVariable((Target) target, (Thread) thread, (StackFrame) this.currFrame, vars[j], vars[j], vars.length - j, this.currFrame.getLevel(), null);
					varList.add(var);
				}
			}
			try {
				System.out.println("########### set local variable to target event");
				setTargetResult(e.getBitSet(), (ICDILocalVariable[]) varList.toArray(new ICDILocalVariable[0]), ITargetEvent.LOCALVARIABLES_TYPE);
			} catch (PCDIException pe) {
				pe.printStackTrace();
			}
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
				ICDITarget target = getSession().getTarget(taskId);
				ICDIThread thread = new Thread((Target) target, 0);
				String args[] = argsEvent.getVariables();
				for (int j = 0; j < args.length; j++) {
					Argument arg = new Argument((Target) target, (Thread) thread, (StackFrame) this.currFrame, args[j], args[j], args.length - j, this.currFrame.getLevel(), null);
					argList.add(arg);
				}
			}
			try {
				System.out.println("########### set arguments to target event");
				setTargetResult(e.getBitSet(), (ICDIArgument[]) argList.toArray(new ICDIArgument[0]), ITargetEvent.ARGUMENTS_TYPE);
			} catch (PCDIException pe) {
				pe.printStackTrace();
			}
			break;
			
		case IProxyDebugEvent.EVENT_DBG_EXIT:
			IPCDIExitedEvent ee = new InferiorExitedEvent(getSession(), e.getBitSet());
			super.fireEvent(ee);
			break;
			
		case IProxyDebugEvent.EVENT_DBG_SIGNAL:
			ProxyDebugSignalEvent sigEvent = (ProxyDebugSignalEvent)e;
			IPCDISuspendedEvent suspEv = new InferiorSignaledEvent(getSession(), e.getBitSet(), sigEvent.getLocator());
			super.fireEvent(suspEv);
			break;
			
		case IProxyDebugEvent.EVENT_DBG_ERROR:
			System.out.println("got error from server");
			IPCDIExitedEvent ex = new InferiorExitedEvent(getSession(), e.getBitSet());
			super.fireEvent(ex);
		}
		
		this.events.addItem(e);
		notifyAll();
	}
	
	private ICDIStackFrame convertFrame(ICDIThread thread, ProxyDebugStackframe frame) {
		int level = frame.getLevel();
		String file = frame.getLocator().getFile();
		String func = frame.getLocator().getFunction();
		int line = frame.getLocator().getLineNumber();
		BigInteger addr = frame.getLocator().getAddress();
		System.out.println("frame " + level + " " + file + " " + func + " " + line + " " + addr);
		return new StackFrame((Thread) thread, level, file, func, line, addr.toString(16));
	}
	
	private ICDIStackFrame[] convertFrames(IPProcess proc, ProxyDebugStackframe[] frames) {
		ArrayList frameList = new ArrayList();
		int taskId = proc.getTaskId();
		ICDITarget target = getSession().getTarget(taskId);
	    ICDIThread thread = new Thread((Target) target, 0);
		for (int j = 0; j < frames.length; j++) {
			frameList.add(convertFrame(thread, frames[j]));
		}
		return (ICDIStackFrame[]) frameList.toArray(new ICDIStackFrame[0]);
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
	private void updateBreakpointInfo(int bpid, BitList bpset, ICDIBreakpoint bpt) {
		/*
		 * First update bpMap
		 */
		BreakpointMapping bp = (BreakpointMapping) bpMap.get(bpt);
		
		if (bp == null) {
			bp = new BreakpointMapping(bpid, bpset, bpt);
			bpMap.put(bpt, bp);
		} else
			bp.updateProcs(bpset);
		
		/*
		 * Next update bpArray
		 */
		if (bpid >= bpArray.size() || bpArray.get(bpid) == null) {
			bpArray.add(bpid, bp);
		}
	}
	
	private BreakpointMapping findBreakpointInfo(int bpid) {
		return (BreakpointMapping)bpArray.get(bpid);
	}

	
	private BreakpointMapping findBreakpointInfo(ICDIBreakpoint bpt) {
		return (BreakpointMapping)bpMap.get(bpt);
	}

	private int newBreakpointId() {
		return this.bpId++;
	}
}
