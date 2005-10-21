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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.cdt.debug.core.cdi.model.ICDIArgument;
import org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDIFunctionBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDIGlobalVariable;
import org.eclipse.cdt.debug.core.cdi.model.ICDILineBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDILocalVariable;
import org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDIThread;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.core.proxy.event.IProxyEvent;
import org.eclipse.ptp.core.proxy.event.IProxyEventListener;
import org.eclipse.ptp.core.proxy.event.ProxyErrorEvent;
import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.core.util.Queue;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIEvent;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIExitedEvent;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIDebugProcess;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIDebugProcessSet;
import org.eclipse.ptp.debug.external.AbstractDebugger;
import org.eclipse.ptp.debug.external.cdi.PCDIException;
import org.eclipse.ptp.debug.external.cdi.event.BreakpointHitEvent;
import org.eclipse.ptp.debug.external.cdi.event.EndSteppingRangeEvent;
import org.eclipse.ptp.debug.external.cdi.event.InferiorExitedEvent;
import org.eclipse.ptp.debug.external.cdi.model.DebugProcess;
import org.eclipse.ptp.debug.external.cdi.model.DebugProcessSet;
import org.eclipse.ptp.debug.external.cdi.model.LineLocation;
import org.eclipse.ptp.debug.external.cdi.model.StackFrame;
import org.eclipse.ptp.debug.external.cdi.model.Target;
import org.eclipse.ptp.debug.external.cdi.model.Thread;
import org.eclipse.ptp.debug.external.proxy.ProxyDebugClient;
import org.eclipse.ptp.debug.external.proxy.ProxyDebugStackframe;
import org.eclipse.ptp.debug.external.proxy.event.IProxyDebugEvent;
import org.eclipse.ptp.debug.external.proxy.event.ProxyDebugBreakpointHitEvent;
import org.eclipse.ptp.debug.external.proxy.event.ProxyDebugBreakpointSetEvent;
import org.eclipse.ptp.debug.external.proxy.event.ProxyDebugExitEvent;
import org.eclipse.ptp.debug.external.proxy.event.ProxyDebugInitEvent;
import org.eclipse.ptp.debug.external.proxy.event.ProxyDebugStackframeEvent;
import org.eclipse.ptp.debug.external.proxy.event.ProxyDebugStepEvent;
import org.eclipse.ptp.rtsystem.simulation.SimProcess;
import org.eclipse.ptp.rtsystem.simulation.SimStackFrame;
import org.eclipse.ptp.rtsystem.simulation.SimThread;


public class ParallelDebugger extends AbstractDebugger implements IProxyEventListener {
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
	private ICDIStackFrame[]		lastFrames = null;
	private IPCDIDebugProcessSet	currProcs = null;
	
	/*
	 * Wait for any event
	 */
	private synchronized void waitForEvent() {
		try {
			wait();
		} catch (InterruptedException e) {
		}
	}
	
	/*
	 * Wait until 'type' events have been received from all processes in 'procs'
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
	
	protected void startDebugger(IPJob job) {
		proxy = new ProxyDebugClient("localhost", 12345);
		proxy.addEventListener(this);
		try {
			proxy.sessionCreate();
			waitForEvent();
			
			String app = (String) job.getAttribute("app");
			//String dir = (String) job.getAttribute("dir");
			String[] args = (String[]) job.getAttribute("args");
			
			proxy.debugStartSession(app, join(args, " "));
			waitForEvent();
		} catch (IOException e) {
			return;
		}
	}
	
	protected void stopDebugger() {
		try {
			proxy.sessionFinish();
		} catch (IOException e) {
			return;
		}
		waitForEvent();
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
	
	public void go(IPCDIDebugProcessSet procs) throws PCDIException {
		try {
			proxy.debugGo(procs.toBitList());
		} catch (IOException e) {
			// TODO deal with IOException (maybe should be dealt with in ProxyClient?)
		}
	}

	public void kill(IPCDIDebugProcessSet procs) throws PCDIException {
		throw new PCDIException(PCDIException.NOT_IMPLEMENTED, "kill");
	}

	public void halt(IPCDIDebugProcessSet procs) throws PCDIException {
		throw new PCDIException(PCDIException.NOT_IMPLEMENTED, "halt");
	}
	
	public void stepInto(IPCDIDebugProcessSet procs, int count) throws PCDIException {
		try {
			proxy.debugStep(procs.toBitList(), count, 0);
		} catch (IOException e) {
		}
	}

	public void stepOver(IPCDIDebugProcessSet procs, int count) throws PCDIException {
		try {
			proxy.debugStep(procs.toBitList(), count, 1);
		} catch (IOException e) {
		}
	}

	public void stepFinish(IPCDIDebugProcessSet procs, int count) throws PCDIException {
		throw new PCDIException(PCDIException.NOT_IMPLEMENTED, "stepFinish");
	}

	public void setLineBreakpoint(IPCDIDebugProcessSet procs, ICDILineBreakpoint bpt) throws PCDIException {
		try {
			proxy.debugSetLineBreakpoint(procs.toBitList(), newBreakpointId(), bpt.getLocator().getFile(), bpt.getLocator().getLineNumber());
		} catch (IOException e1) {
			return;
		}
		
		waitForEvents(procs.toBitList());
	}

	public void setFunctionBreakpoint(IPCDIDebugProcessSet procs, ICDIFunctionBreakpoint bpt) throws PCDIException {
		try {
			proxy.debugSetFuncBreakpoint(procs.toBitList(), newBreakpointId(), bpt.getLocator().getFile(), bpt.getLocator().getFunction());
		} catch (IOException e1) {
			return;
		}
		
		waitForEvents(procs.toBitList());
	}

	private void deleteBreakpoint(BitList procs, int bpid)  throws PCDIException {
		try {
			proxy.debugDeleteBreakpoint(procs, bpid);
		} catch (IOException e1) {
			return;
		}

		waitForEvents(procs);
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
	 * TODO: extend to support multiple processes
	 */
	public ICDIStackFrame[] listStackFrames(IPCDIDebugProcessSet procs) throws PCDIException {
		this.currProcs = procs;
		
		try {
			proxy.debugListStackframes(procs.toBitList(), 0);
		} catch (IOException e) {
		}
		
		waitForEvents(procs.toBitList());
		
		return this.lastFrames;
	}
	
	public void setCurrentStackFrame(IPCDIDebugProcessSet procs, ICDIStackFrame frame) throws PCDIException {
		try {
			proxy.debugSetCurrentStackframe(procs.toBitList(), frame.getLevel());
		} catch (IOException e) {
		}
		
		waitForEvents(procs.toBitList());
	}
	
	/**
	 * evaluate expression for first process in procs
	 * TODO: extend to support multiple processes
	 */
	public String evaluateExpression(IPCDIDebugProcessSet procs, String expr) throws PCDIException {
		throw new PCDIException(PCDIException.NOT_IMPLEMENTED, "evaluateExpression");
	}
	
	/**
	 * get variable type for first process in procs
	 * TODO: extend to support multiple processes
	 */
	public String getVariableType(IPCDIDebugProcessSet procs, String varName) throws PCDIException {
		throw new PCDIException(PCDIException.NOT_IMPLEMENTED, "getVariableType");
	}
	
	/**
	 * list local variables for first process in procs
	 * TODO: extend to support multiple processes
	 */
	public ICDILocalVariable[] listLocalVariables(IPCDIDebugProcessSet procs, ICDIStackFrame frame) throws PCDIException {
		throw new PCDIException(PCDIException.NOT_IMPLEMENTED, "listLocalVariables");
	}
	
	/**
	 * list global variables for first process in procs
	 * TODO: extend to support multiple processes
	 */
	public ICDIGlobalVariable[] listGlobalVariables(IPCDIDebugProcessSet procs) throws PCDIException {
		throw new PCDIException(PCDIException.NOT_IMPLEMENTED, "listGlobalVariables");
	}
	
	/**
	 * list arguments for first process in procs
	 * TODO: extend to support multiple processes
	 */
	public ICDIArgument[] listArguments(IPCDIDebugProcessSet procs, ICDIStackFrame frame) throws PCDIException {
		throw new PCDIException(PCDIException.NOT_IMPLEMENTED, "listGlobalVariables");
	}

	public IPCDIEvent handleBreakpointHitEvent(BitList procs, String[] args) {
		// Handled by direct call to super.fireEvent();
		return null;
	}

	public IPCDIEvent handleEndSteppingEvent(BitList procs, String[] args) {
		// Auto-generated method stub
		System.out.println("ParallelDebugger.handleEndSteppingEvent()");
		return null;
	}

	public IPCDIEvent handleProcessResumedEvent(BitList procs, String[] args) {
		// Auto-generated method stub
		System.out.println("ParallelDebugger.handleProcessResumedEvent()");
		return null;
	}

	public IPCDIEvent handleProcessTerminatedEvent(BitList procs, String[] args) {
		// Auto-generated method stub
		System.out.println("ParallelDebugger.handleProcessTerminatedEvent()");
		return null;
	}

	public synchronized void fireEvent(IProxyEvent e) {
		IProxyDebugEvent de = (IProxyDebugEvent)e;
		System.out.println("got event: " + e.toString());
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
				IPCDIEvent ev = new BreakpointHitEvent(getSession(), new DebugProcessSet(session, de.getBitSet()), bp.bpObject);
				super.fireEvent(ev);
			}
			break;
			
		case IProxyDebugEvent.EVENT_DBG_STEP:
			ProxyDebugStepEvent stepEvent = (ProxyDebugStepEvent)e;
			LineLocation loc = new LineLocation(stepEvent.getFrame().getFile(), stepEvent.getFrame().getLine());
			IPCDIEvent ev = new EndSteppingRangeEvent(getSession(), new DebugProcessSet(session, de.getBitSet()), loc);
			super.fireEvent(ev);
			break;	
			
		case IProxyDebugEvent.EVENT_DBG_BPSET:
			ProxyDebugBreakpointSetEvent bpEvt = (ProxyDebugBreakpointSetEvent)e;	
			updateBreakpointInfo(bpEvt.getBreakpointId(), de.getBitSet(), bpEvt.getBreakpoint());
			break;
			
		case IProxyDebugEvent.EVENT_DBG_FRAMES:
			if (this.currProcs == null)
				return;
			
			ProxyDebugStackframeEvent frameEvent = (ProxyDebugStackframeEvent)e;
			ArrayList list = new ArrayList();
			
			IPCDIDebugProcess[] procList = this.currProcs.getProcesses();
			for (int i = 0; i < procList.length; i++) {
				int taskId = ((DebugProcess) procList[i]).getPProcess().getTaskId();
				ICDITarget target = getSession().getTarget(taskId);
				ICDIThread thread = new Thread((Target) target, 0);
				for (int j = 0; j < frameEvent.getFrames().length; j++) {
					ProxyDebugStackframe f = frameEvent.getFrames()[j];
					int level = f.getLevel();
					String file = f.getFile();
					String func = f.getFunc();
					int line = f.getLine();
					String addr = f.getAddr();
					System.out.println("frame " + level + " " + file + " " + func + " " + line + " " + addr);
					StackFrame frame = new StackFrame((Thread) thread, level, file, func, line, addr);
					list.add(frame);
				}
			}
			this.lastFrames = (ICDIStackFrame[]) list.toArray(new ICDIStackFrame[0]);
			break;
			
		case IProxyDebugEvent.EVENT_DBG_EXIT:
			IPCDIExitedEvent ee = new InferiorExitedEvent(getSession(), new DebugProcessSet(session, de.getBitSet()));
			super.fireEvent(ee);
		}
		
		this.events.addItem(e);
		notify();
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
