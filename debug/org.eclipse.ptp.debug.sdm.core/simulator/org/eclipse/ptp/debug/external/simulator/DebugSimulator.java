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
 package org.eclipse.ptp.debug.external.simulator;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.cdt.debug.core.cdi.model.ICDIArgument;
import org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDIFunctionBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDIGlobalVariable;
import org.eclipse.cdt.debug.core.cdi.model.ICDILineBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDILocalVariable;
import org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDIThread;
import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.core.IPProcess;
import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIEvent;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIDebugProcess;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIDebugProcessSet;
import org.eclipse.ptp.debug.external.AbstractDebugger;
import org.eclipse.ptp.debug.external.IAbstractDebugger;
import org.eclipse.ptp.debug.external.PTPDebugExternalPlugin;
import org.eclipse.ptp.debug.external.cdi.PCDIException;
import org.eclipse.ptp.debug.external.cdi.breakpoints.LineBreakpoint;
import org.eclipse.ptp.debug.external.cdi.event.BreakpointHitEvent;
import org.eclipse.ptp.debug.external.cdi.event.EndSteppingRangeEvent;
import org.eclipse.ptp.debug.external.cdi.event.InferiorExitedEvent;
import org.eclipse.ptp.debug.external.cdi.event.InferiorResumedEvent;
import org.eclipse.ptp.debug.external.cdi.model.DebugProcess;
import org.eclipse.ptp.debug.external.cdi.model.DebugProcessSet;
import org.eclipse.ptp.debug.external.cdi.model.LineLocation;
import org.eclipse.ptp.debug.external.cdi.model.StackFrame;
import org.eclipse.ptp.debug.external.cdi.model.Target;
import org.eclipse.ptp.debug.external.cdi.model.Thread;
import org.eclipse.ptp.debug.external.cdi.model.variable.Argument;
import org.eclipse.ptp.debug.external.cdi.model.variable.LocalVariable;
import org.eclipse.ptp.rtsystem.simulation.SimProcess;
import org.eclipse.ptp.rtsystem.simulation.SimStackFrame;
import org.eclipse.ptp.rtsystem.simulation.SimThread;
import org.eclipse.ptp.rtsystem.simulation.SimVariable;

public class DebugSimulator extends AbstractDebugger implements Observer {

	final int RUNNING = 10;
	final int SUSPENDED = 11;
	
	int state = 0;
	boolean finished = false;
	
	private Process debuggerProcess = null;
	
	DQueue debuggerOutput = null;
	
	private void initializeSimulatedProcessesCode(DQueue dQ) {
		dQ.addItem("DEBUG SIMULATOR");
		dQ.addItem("Look at this console window for output from the Debug Simulator");
		dQ.addItem("  ");
	}
	
	protected void startDebugger(IPJob job) {
		state = SUSPENDED;
		debuggerOutput = new DQueue();
		initializeSimulatedProcessesCode(debuggerOutput);
		debuggerProcess = new DProcess("Debugger", -1, 1, debuggerOutput, this);
		
		for (int i = 0; i < procs.length; i++) {
			((SimProcess) procs[i]).getThread(0).addObserver(this);
		}
	}
	
	protected void stopDebugger() {
		PTPDebugExternalPlugin.getDefault().getLogger().finer("");
		finished = true;
	}

	public Process getDebuggerProcess() {
		return debuggerProcess;
	}

	public ICDIStackFrame[] listStackFrames(IPCDIDebugProcessSet procs) throws PCDIException {
		ArrayList list = new ArrayList();
		IPCDIDebugProcess[] procList = procs.getProcesses();
		for (int i = 0; i < procList.length; i++) {
			//ICDITarget target = procList[i].getTarget();
			int taskId = ((DebugProcess) procList[i]).getPProcess().getTaskId();
			ICDITarget target = getSession().getTarget(taskId);
			SimThread simThread = ((SimProcess) ((DebugProcess) procList[i]).getPProcess()).getThread(0);
			ICDIThread thread = new Thread((Target) target, simThread.getThreadId());
			SimStackFrame[] simFrames = simThread.getStackFrames();
			for (int j = 0; j < simFrames.length; j++) {
				int level = simFrames[j].getLevel();
				String file = simFrames[j].getFile();
				String func = simFrames[j].getFunction();
				int line = simFrames[j].getLine();
				String addr = simFrames[j].getAddress();
				StackFrame frame = new StackFrame((Thread) thread, level, file, func, line, addr);
				list.add(frame);
			}
		}
		return (ICDIStackFrame[]) list.toArray(new ICDIStackFrame[0]);
	}

	public void setCurrentStackFrame(IPCDIDebugProcessSet procs, ICDIStackFrame frame) throws PCDIException {
		// TODO Auto-generated method stub
		
	}

	public String evaluateExpression(IPCDIDebugProcessSet procs, String expr) throws PCDIException {
		String retVal = null;
		IPCDIDebugProcess[] procList = procs.getProcesses();
		for (int i = 0; i < procList.length; i++) {
			SimThread simThread = ((SimProcess) ((DebugProcess) procList[i]).getPProcess()).getThread(0);
			SimStackFrame[] simFrames = simThread.getStackFrames();
			for (int j = 0; j < simFrames.length; j++) {
				SimVariable[] args = simFrames[j].getArgs();
				for (int k = 0; k < args.length; k++) {
					String aName = args[k].getName();
					String aVal = args[k].getValue();
					if (aName.equals(expr))
						return aVal;
				}
				SimVariable[] local = simFrames[j].getLocalVars();
				for (int k = 0; k < local.length; k++) {
					String aName = local[k].getName();
					String aVal = local[k].getValue();
					if (aName.equals(expr))
						return aVal;
				}
			}
		}
		return retVal;
	}
	
	public String getVariableType(IPCDIDebugProcessSet procs, String varName) throws PCDIException {
		String retVal = null;
		IPCDIDebugProcess[] procList = procs.getProcesses();
		for (int i = 0; i < procList.length; i++) {
			SimThread simThread = ((SimProcess) ((DebugProcess) procList[i]).getPProcess()).getThread(0);
			SimStackFrame[] simFrames = simThread.getStackFrames();
			for (int j = 0; j < simFrames.length; j++) {
				SimVariable[] args = simFrames[j].getArgs();
				for (int k = 0; k < args.length; k++) {
					String aName = args[k].getName();
					String aType = args[k].getType();
					if (aName.equals(varName))
						return aType;
				}
				SimVariable[] local = simFrames[j].getLocalVars();
				for (int k = 0; k < local.length; k++) {
					String aName = local[k].getName();
					String aType = local[k].getType();
					if (aName.equals(varName))
						return aType;
				}
			}
		}
		return retVal;
	}

	public ICDIArgument[] listArguments(IPCDIDebugProcessSet procs, ICDIStackFrame frame) throws PCDIException {
		ArrayList list = new ArrayList();
		IPCDIDebugProcess[] procList = procs.getProcesses();
		for (int i = 0; i < procList.length; i++) {
			//ICDITarget target = procList[i].getTarget();
			int taskId = ((DebugProcess) procList[i]).getPProcess().getTaskId();
			ICDITarget target = getSession().getTarget(taskId);
			SimThread simThread = ((SimProcess) ((DebugProcess) procList[i]).getPProcess()).getThread(0);
			ICDIThread thread = new Thread((Target) target, simThread.getThreadId());
			SimStackFrame[] simFrames = simThread.getStackFrames();
			for (int j = 0; j < simFrames.length; j++) {
				int level = simFrames[j].getLevel();
				String file = simFrames[j].getFile();
				String func = simFrames[j].getFunction();
				int line = simFrames[j].getLine();
				String addr = simFrames[j].getAddress();
				StackFrame newFrame = new StackFrame((Thread) thread, level, file, func, line, addr);
				if (newFrame.getLocator().equals(frame.getLocator())) {
					SimVariable[] args = simFrames[j].getArgs();
					for (int k = 0; k < args.length; k++) {
						String aName = args[k].getName();
						Argument arg = new Argument((Target) target, (Thread) thread, 
								(StackFrame) frame, aName, aName,
								args.length - k, frame.getLevel());
						list.add(arg);
					}
				}
			}
		}
		return (ICDIArgument[]) list.toArray(new ICDIArgument[0]);
	}

	public ICDILocalVariable[] listLocalVariables(IPCDIDebugProcessSet procs, ICDIStackFrame frame) throws PCDIException {
		ArrayList list = new ArrayList();
		IPCDIDebugProcess[] procList = procs.getProcesses();
		for (int i = 0; i < procList.length; i++) {
			//ICDITarget target = procList[i].getTarget();
			int taskId = ((DebugProcess) procList[i]).getPProcess().getTaskId();
			ICDITarget target = getSession().getTarget(taskId);
			SimThread simThread = ((SimProcess) ((DebugProcess) procList[i]).getPProcess()).getThread(0);
			ICDIThread thread = new Thread((Target) target, simThread.getThreadId());
			SimStackFrame[] simFrames = simThread.getStackFrames();
			for (int j = 0; j < simFrames.length; j++) {
				int level = simFrames[j].getLevel();
				String file = simFrames[j].getFile();
				String func = simFrames[j].getFunction();
				int line = simFrames[j].getLine();
				String addr = simFrames[j].getAddress();
				StackFrame newFrame = new StackFrame((Thread) thread, level, file, func, line, addr);
				if (newFrame.getLocator().equals(frame.getLocator())) {
					SimVariable[] args = simFrames[j].getLocalVars();
					for (int k = 0; k < args.length; k++) {
						String aName = args[k].getName();
						LocalVariable arg = new LocalVariable((Target) target, (Thread) thread, 
								(StackFrame) frame, aName, aName,
								args.length - k, frame.getLevel());
						list.add(arg);
					}
				}
			}
		}
		return (ICDILocalVariable[]) list.toArray(new ICDILocalVariable[0]);
	}

	public ICDIGlobalVariable[] listGlobalVariables(IPCDIDebugProcessSet procs) throws PCDIException {
		// TODO Auto-generated method stub
		return null;
	}

	public void stepInto(IPCDIDebugProcessSet procs, int count) throws PCDIException {
		PTPDebugExternalPlugin.getDefault().getLogger().finer("");
		state = RUNNING;
		
		if (procs == null)
			return;
		
		IPCDIDebugProcess[] procList = procs.getProcesses();
		for (int i = 0; i < procList.length; i++) {
			((SimProcess) ((DebugProcess) procList[i]).getPProcess()).getThread(0).stepInto(count);
		}
	}

	public void stepOver(IPCDIDebugProcessSet procs, int count) throws PCDIException {
		PTPDebugExternalPlugin.getDefault().getLogger().finer("");
		state = RUNNING;
		
		if (procs == null)
			return;
		
		IPCDIDebugProcess[] procList = procs.getProcesses();
		for (int i = 0; i < procList.length; i++) {
			((SimProcess) ((DebugProcess) procList[i]).getPProcess()).getThread(0).stepOver(count);
		}
	}

	public void stepFinish(IPCDIDebugProcessSet procs, int count) throws PCDIException {
		PTPDebugExternalPlugin.getDefault().getLogger().finer("");
		state = RUNNING;
		
		if (procs == null)
			return;
		
		IPCDIDebugProcess[] procList = procs.getProcesses();
		for (int i = 0; i < procList.length; i++) {
			((SimProcess) ((DebugProcess) procList[i]).getPProcess()).getThread(0).stepFinish(count);
		}
	}

	public void go(IPCDIDebugProcessSet procs) throws PCDIException {
		PTPDebugExternalPlugin.getDefault().getLogger().finer("");
		state = RUNNING;
		IPCDIDebugProcess[] procList = procs.getProcesses();
		for (int i = 0; i < procList.length; i++) {
			SimProcess p = ((SimProcess) ((DebugProcess) procList[i]).getPProcess());
			if (p.getStatus().equals(IPProcess.EXITED))
				continue;
			p.getThread(0).resume();
		}
	}

	public void halt(IPCDIDebugProcessSet procs) throws PCDIException {
		PTPDebugExternalPlugin.getDefault().getLogger().finer("");
		state = SUSPENDED;
		IPCDIDebugProcess[] list = procs.getProcesses();
		for (int i = 0; i < list.length; i++) {
			SimProcess p = ((SimProcess) ((DebugProcess) list[i]).getPProcess());
			if (p.getStatus().equals(IPProcess.EXITED))
				continue;
			p.getThread(0).suspend();
		}
	}

	public void kill(IPCDIDebugProcessSet procs) throws PCDIException {
		IPCDIDebugProcess[] list = procs.getProcesses();
		for (int i = 0; i < list.length; i++) {
			((DebugProcess) list[i]).getPProcess().setTerminated(true);
			list[i].getProcess().destroy();
		}
	}

	public void run(String[] args) throws PCDIException {
		// TODO Auto-generated method stub
		
	}

	public void restart() throws PCDIException {
		// TODO Auto-generated method stub
		
	}

	public void setLineBreakpoint(IPCDIDebugProcessSet procs, ICDILineBreakpoint bpt) throws PCDIException {
		PTPDebugExternalPlugin.getDefault().getLogger().finer("");
		int line = ((LineBreakpoint) bpt).getLineNumber();
		
		IPCDIDebugProcess[] procList = procs.getProcesses();
		for (int i = 0; i < procList.length; i++) {
			//((SimProcess) ((DebugProcess) procList[i]).getPProcess()).getThread(0).addObserver(this);
			((SimProcess) ((DebugProcess) procList[i]).getPProcess()).getThread(0).addBreakpoint(line);
		}

		//((SimProcess) ((DebugProcess) getProcess(0)).getPProcess()).getThread(0).addBreakpoint(line);

	}

	public void setFunctionBreakpoint(IPCDIDebugProcessSet procs, ICDIFunctionBreakpoint bpt) throws PCDIException {
		PTPDebugExternalPlugin.getDefault().getLogger().finer("");
	}

	public void deleteBreakpoints(ICDIBreakpoint[] bp) throws PCDIException {
		// TODO Auto-generated method stub
		
	}

	/* Do not worry about this method, this method is only peculiar to this DebugSimulator */
	public void update(Observable o, Object arg) {
		ArrayList list = (ArrayList) arg;
		int procId = ((Integer) list.get(0)).intValue();
		String event = (String) list.get(1);
		
		String[] args = null;
		BitList procs = new BitList(this.procs.length);
		procs.set(procId);
		
		if (event.equals("BREAKPOINTHIT")) {
			args = new String[2];
			args[0] = (String) list.get(2);
			args[1] = ((Integer) list.get(3)).toString();
			handleDebugEvent(IAbstractDebugger.IDBGEV_BPHIT, procs, args);
			debuggerOutput.addItem("BreakpointHit Event for " + procId);
			
		} else if (event.equals("ENDSTEPPINGRANGE")) {
			args = new String[2];
			args[0] = (String) list.get(2);
			args[1] = ((Integer) list.get(3)).toString();
			handleDebugEvent(IAbstractDebugger.IDBGEV_ENDSTEPPING, procs, args);
			debuggerOutput.addItem("EndSteppingRange Event for " + procId);
			
		} else if (event.equals("RESUMED")) {
			handleDebugEvent(IAbstractDebugger.IDBGEV_PROCESSRESUMED, procs, args);
			debuggerOutput.addItem("InferiorResumed Event for " + procId);
			
		} else if (event.equals("TERMINATED")) {
			handleDebugEvent(IAbstractDebugger.IDBGEV_PROCESSTERMINATED, procs, args);
			debuggerOutput.addItem("InferiorExited Event for " + procId);
			
		} else if (event.equals("SUSPENDED")) {
			args = new String[2];
			args[0] = (String) list.get(2);
			args[1] = ((Integer) list.get(3)).toString();
			handleDebugEvent(IAbstractDebugger.IDBGEV_ENDSTEPPING, procs, args);
			debuggerOutput.addItem("EndSteppingRange Event for " + procId);

		}

	}
	
	public IPCDIEvent handleBreakpointHitEvent(BitList procs, String[] args) {
		String file = args[0];
		int line = Integer.parseInt(args[1]);
		LineLocation loc = new LineLocation(file, line);
		LineBreakpoint bpt = new LineBreakpoint(ICDIBreakpoint.REGULAR, loc, null);
		IPCDIEvent ev = new BreakpointHitEvent(getSession(), new DebugProcessSet(session, procs), bpt);
		return ev;
	}

	public IPCDIEvent handleEndSteppingEvent(BitList procs, String[] args) {
		String file = args[0];
		int line = Integer.parseInt(args[1]);
		LineLocation loc = new LineLocation(file, line);
		IPCDIEvent ev = new EndSteppingRangeEvent(getSession(), new DebugProcessSet(session, procs), loc);
		return ev;
	}

	public IPCDIEvent handleProcessResumedEvent(BitList procs, String[] args) {
		IPCDIEvent ev = new InferiorResumedEvent(getSession(), new DebugProcessSet(session, procs));
		return ev;
	}

	public IPCDIEvent handleProcessTerminatedEvent(BitList procs, String[] args) {
		IPCDIEvent ev = new InferiorExitedEvent(getSession(), new DebugProcessSet(session, procs));
		return ev;
	}

}
