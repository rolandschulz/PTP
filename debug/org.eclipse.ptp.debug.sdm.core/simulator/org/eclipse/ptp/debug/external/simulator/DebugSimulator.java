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

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDIArgument;
import org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDIExpression;
import org.eclipse.cdt.debug.core.cdi.model.ICDIGlobalVariable;
import org.eclipse.cdt.debug.core.cdi.model.ICDILocalVariable;
import org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDIThread;
import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIDebugProcess;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIDebugProcessSet;
import org.eclipse.ptp.debug.external.AbstractDebugger;
import org.eclipse.ptp.debug.external.cdi.model.Argument;
import org.eclipse.ptp.debug.external.cdi.model.DebugProcess;
import org.eclipse.ptp.debug.external.cdi.model.LocalVariable;
import org.eclipse.ptp.debug.external.cdi.model.StackFrame;
import org.eclipse.ptp.debug.external.cdi.model.Target;
import org.eclipse.ptp.debug.external.cdi.model.Thread;
import org.eclipse.ptp.rtsystem.simulation.SimProcess;
import org.eclipse.ptp.rtsystem.simulation.SimStackFrame;
import org.eclipse.ptp.rtsystem.simulation.SimThread;
import org.eclipse.ptp.rtsystem.simulation.SimVariable;

public class DebugSimulator extends AbstractDebugger {

	final int RUNNING = 10;
	final int SUSPENDED = 11;
	
	int state = 0;
	boolean finished = false;
	
	private Process debuggerProcess = null;
	
	DQueue debuggerCommands = null;
	
	private void initializeSimulatedProcessesCode(DQueue dQ) {
		ArrayList cmd;
		
		cmd = new ArrayList();
		cmd.add(0, "0");
		cmd.add(1, "print");
		cmd.add(2, "DebuggerOutput");
		
		for (int i = 0; i < 30; i++) {
			dQ.addItem(cmd);
		}
	}
	
	protected void startDebugger(IPJob job) {
		state = SUSPENDED;
		debuggerCommands = new DQueue();
		initializeSimulatedProcessesCode(debuggerCommands);
		debuggerProcess = new DProcess("Debugger", -1, 1, debuggerCommands, this);
	}
	
	protected void stopDebugger() {
		// Auto-generated method stub
		System.out.println("DebugSimulator.stopDebugger()");
		finished = true;
	}

	public Process getDebuggerProcess() {
		return debuggerProcess;
	}

	public ICDIStackFrame[] listStackFrames(IPCDIDebugProcessSet procs) throws CDIException {
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
		return (ICDIStackFrame[]) list.toArray();
	}

	public void setCurrentStackFrame(IPCDIDebugProcessSet procs, ICDIStackFrame frame) throws CDIException {
		// TODO Auto-generated method stub
		
	}

	public ICDIExpression evaluateExpression(IPCDIDebugProcessSet procs, String expr) throws CDIException {
		// TODO Auto-generated method stub
		return null;
	}

	public ICDIArgument[] listArguments(IPCDIDebugProcessSet procs, ICDIStackFrame frame) throws CDIException {
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
				if (newFrame.equals(frame)) {
					SimVariable[] args = simFrames[j].getArgs();
					for (int k = 0; k < args.length; k++) {
						String aName = args[k].getName();
						String aVal = args[k].getValue();
						Argument arg = new Argument((Target) target, (Thread) thread, 
								(StackFrame) frame, aName, aName,
								args.length - k, frame.getLevel(), aVal);
						list.add(arg);
					}
				}
			}
		}
		return (ICDIArgument[]) list.toArray();
	}

	public ICDILocalVariable[] listLocalVariables(IPCDIDebugProcessSet procs, ICDIStackFrame frame) throws CDIException {
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
				if (newFrame.equals(frame)) {
					SimVariable[] args = simFrames[j].getLocalVars();
					for (int k = 0; k < args.length; k++) {
						String aName = args[k].getName();
						String aVal = args[k].getValue();
						LocalVariable arg = new LocalVariable((Target) target, (Thread) thread, 
								(StackFrame) frame, aName, aName,
								args.length - k, frame.getLevel(), aVal);
						list.add(arg);
					}
				}
			}
		}
		return (ICDILocalVariable[]) list.toArray();
	}

	public ICDIGlobalVariable[] listGlobalVariables(IPCDIDebugProcessSet procs) throws CDIException {
		// TODO Auto-generated method stub
		return null;
	}

	public void stepInto(IPCDIDebugProcessSet procs, int count) throws CDIException {
		// TODO Auto-generated method stub
		
	}

	public void stepOver(IPCDIDebugProcessSet procs, int count) throws CDIException {
		// TODO Auto-generated method stub
		
	}

	public void stepFinish(IPCDIDebugProcessSet procs, int count) throws CDIException {
		// TODO Auto-generated method stub
		
	}

	public void go(IPCDIDebugProcessSet procs) throws CDIException {
		// Currently we apply this method globally for all procs
		// Auto-generated method stub
		System.out.println("DebugSimulator.go()");
		state = RUNNING;
	}

	public void halt(IPCDIDebugProcessSet procs) throws CDIException {
		// Currently we apply this method globally for all procs
		// Auto-generated method stub
		System.out.println("DebugSimulator.halt()");
		state = SUSPENDED;
	}

	public void kill(IPCDIDebugProcessSet procs) throws CDIException {
		// Currently we apply this method globally for all procs
		IPCDIDebugProcess[] list = getProcesses();
		for (int i = 0; i < list.length; i++) {
			list[i].getProcess().destroy();
		}
	}

	public void run(String[] args) throws CDIException {
		// TODO Auto-generated method stub
		
	}

	public void restart() throws CDIException {
		// TODO Auto-generated method stub
		
	}

	public void setLineBreakpoint(IPCDIDebugProcessSet procs, ICDIBreakpoint bpt) throws CDIException {
		// TODO Auto-generated method stub
		
	}

	public void setFunctionBreakpoint(IPCDIDebugProcessSet procs, ICDIBreakpoint bpt) throws CDIException {
		// TODO Auto-generated method stub
		
	}

	public void deleteBreakpoints(ICDIBreakpoint[] bp) throws CDIException {
		// TODO Auto-generated method stub
		
	}
}
