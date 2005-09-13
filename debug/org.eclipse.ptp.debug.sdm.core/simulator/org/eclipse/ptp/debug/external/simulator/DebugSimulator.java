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
import org.eclipse.ptp.debug.core.cdi.model.IPCDIDebugProcess;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIDebugProcessSet;
import org.eclipse.ptp.debug.external.AbstractDebugger;
import org.eclipse.ptp.debug.external.cdi.PCDIException;
import org.eclipse.ptp.debug.external.cdi.breakpoints.FunctionBreakpoint;
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
		// Auto-generated method stub
		System.out.println("DebugSimulator.stopDebugger()");
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
		// Currently we apply this method globally for all procs
		// Auto-generated method stub
		System.out.println("DebugSimulator.stepOver()");
		state = RUNNING;
		
		if (procs == null)
			return;
		
		IPCDIDebugProcess[] procList = procs.getProcesses();
		for (int i = 0; i < procList.length; i++) {
			((SimProcess) ((DebugProcess) procList[i]).getPProcess()).getThread(0).stepInto(count);
		}
	}

	public void stepOver(IPCDIDebugProcessSet procs, int count) throws PCDIException {
		// Currently we apply this method globally for all procs
		// Auto-generated method stub
		System.out.println("DebugSimulator.stepOver()");
		state = RUNNING;
		
		if (procs == null)
			return;
		
		IPCDIDebugProcess[] procList = procs.getProcesses();
		for (int i = 0; i < procList.length; i++) {
			((SimProcess) ((DebugProcess) procList[i]).getPProcess()).getThread(0).stepOver(count);
		}
	}

	public void stepFinish(IPCDIDebugProcessSet procs, int count) throws PCDIException {
		// Currently we apply this method globally for all procs
		// Auto-generated method stub
		System.out.println("DebugSimulator.stepOver()");
		state = RUNNING;
		
		if (procs == null)
			return;
		
		IPCDIDebugProcess[] procList = procs.getProcesses();
		for (int i = 0; i < procList.length; i++) {
			((SimProcess) ((DebugProcess) procList[i]).getPProcess()).getThread(0).stepFinish(count);
		}
	}

	public void go(IPCDIDebugProcessSet procs) throws PCDIException {
		// Currently we apply this method globally for all procs
		// Auto-generated method stub
		System.out.println("DebugSimulator.go()");
		state = RUNNING;
		
		if (procs == null)
			return;
		
		IPCDIDebugProcess[] procList = procs.getProcesses();
		for (int i = 0; i < procList.length; i++) {
			((SimProcess) ((DebugProcess) procList[i]).getPProcess()).getThread(0).resume();
		}
	}

	public void halt(IPCDIDebugProcessSet procs) throws PCDIException {
		// Currently we apply this method globally for all procs
		// Auto-generated method stub
		System.out.println("DebugSimulator.halt()");
		state = SUSPENDED;
	}

	public void kill(IPCDIDebugProcessSet procs) throws PCDIException {
		// Currently we apply this method globally for all procs
		IPCDIDebugProcess[] list = getProcesses();
		for (int i = 0; i < list.length; i++) {
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
		System.out.println("DebugSimulator.setLineBreakpoint() : " +
				((LineBreakpoint) bpt).getLineNumber());
		int line = ((LineBreakpoint) bpt).getLineNumber();
		
		IPCDIDebugProcess[] procList = procs.getProcesses();
		for (int i = 0; i < procList.length; i++) {
			//((SimProcess) ((DebugProcess) procList[i]).getPProcess()).getThread(0).addObserver(this);
			((SimProcess) ((DebugProcess) procList[i]).getPProcess()).getThread(0).addBreakpoint(line);
		}

		//((SimProcess) ((DebugProcess) getProcess(0)).getPProcess()).getThread(0).addBreakpoint(line);

	}

	public void setFunctionBreakpoint(IPCDIDebugProcessSet procs, ICDIFunctionBreakpoint bpt) throws PCDIException {
		System.out.println("DebugSimulator.setFunctionBreakpoint() : " +
				((FunctionBreakpoint) bpt).getFunction());
	}

	public void deleteBreakpoints(ICDIBreakpoint[] bp) throws PCDIException {
		// TODO Auto-generated method stub
		
	}

	public void update(Observable o, Object arg) {
		ArrayList list = (ArrayList) arg;
		int procId = ((Integer) list.get(0)).intValue();
		String event = (String) list.get(1);
		
		if (event.equals("BREAKPOINTHIT")) {
			String file = (String) list.get(2);
			int line = ((Integer) list.get(3)).intValue();
			LineLocation loc = new LineLocation(file, line);
			LineBreakpoint bpt = new LineBreakpoint(ICDIBreakpoint.REGULAR, loc, null);
			fireEvent(new BreakpointHitEvent(getSession(), new DebugProcessSet(session, procId), bpt));
			debuggerOutput.addItem("BreakpointHit Event for " + procId);
		} else if (event.equals("ENDSTEPPINGRANGE")) {
			//String file = (String) list.get(2);
			//int line = ((Integer) list.get(3)).intValue();
			//LineLocation loc = new LineLocation(file, line);
			//LineBreakpoint bpt = new LineBreakpoint(ICDIBreakpoint.REGULAR, loc, null);
			fireEvent(new EndSteppingRangeEvent(getSession(), new DebugProcessSet(session, procId)));
			//fireEvent(new BreakpointHitEvent(getSession(), new DebugProcessSet(session, procId), bpt));
			debuggerOutput.addItem("EndSteppingRange Event for " + procId);
		} else if (event.equals("RESUMED")) {
			fireEvent(new InferiorResumedEvent(getSession(), new DebugProcessSet(session, procId)));
			debuggerOutput.addItem("InferiorResumed Event for " + procId);
		} else if (event.equals("TERMINATED")) {
			fireEvent(new InferiorExitedEvent(getSession(), new DebugProcessSet(session, procId)));
			debuggerOutput.addItem("InferiorExited Event for " + procId);
		}
			
		// Do Something
	}

}
