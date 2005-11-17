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
package org.eclipse.ptp.debug.external.simulator2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;
import org.eclipse.cdt.debug.core.cdi.model.ICDIArgument;
import org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDIFunctionBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDILineBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDILocalVariable;
import org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDIThread;
import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.aif.AIF;
import org.eclipse.ptp.debug.core.aif.IAIF;
import org.eclipse.ptp.debug.core.cdi.PCDIException;
import org.eclipse.ptp.debug.external.AbstractDebugger;
import org.eclipse.ptp.debug.external.IDebugger;
import org.eclipse.ptp.debug.external.cdi.model.StackFrame;
import org.eclipse.ptp.debug.external.cdi.model.Target;
import org.eclipse.ptp.debug.external.cdi.model.variable.Argument;
import org.eclipse.ptp.debug.external.cdi.model.variable.LocalVariable;
import org.eclipse.ptp.debug.external.commands.IDebugCommand;

/**
 * @author Clement chu
 * 
 */
public class DebugSimulation2 extends AbstractDebugger implements IDebugger, Observer {
	public static final String HIT_BPT_STATE = "HIT_BPT";
	public static final String EXIT_STATE = "EXIT";
	public static final String STEP_END_STATE = "STEP_END";
	private static final String APP_NAME = "../main.c";
	private final boolean EVENT_BY_EACH_PROC = false;
	private List sim_list = new ArrayList();
	private final long TIME_RANGE = 100;
	private int total_process = 0;
	private InternalEventQueue intQueue = null;
	private Map variables = new HashMap();
	private Map arguments = new HashMap();
	private ICDIStackFrame current_frame = null;

	public DebugSimulation2() {
		intQueue = new InternalEventQueue(TIME_RANGE);
		intQueue.addObserver(this);
		createArguments();
		createVariables();
	}
	public void connection() {
		completeCommand(IDebugCommand.OK);
	}
	public int startDebuggerListener() {
		return 0;
	}
	private void createArguments() {
		arguments.put("argc", new SimVariable("argc", "char **", ""));
		arguments.put("argv", new SimVariable("argv", "int", "-"));
	}
	private void createVariables() {
		variables.put("name", new SimVariable("name", "string", "hello"));
		variables.put("number", new SimVariable("number", "int", "101"));
	}
	private SimVariable findVariable(String name) {
		return (SimVariable)variables.get(name);
	}
	private SimVariable findArgument(String name) {
		return (SimVariable)arguments.get(name);
	}
	private SimVariable[] getArguments() {
		return (SimVariable[])arguments.values().toArray(new SimVariable[0]);
	}
	private SimVariable[] getVariables() {
		return (SimVariable[])variables.values().toArray(new SimVariable[0]);
	}
	
	public void startDebugger(final IPJob job) {
		new Thread(new Runnable() {
			public void run() {
				total_process = job.size();
				for (int i = 0; i < total_process; i++) {
					SimulateProgram sim_program = new SimulateProgram(i, APP_NAME);
					sim_program.addObserver(DebugSimulation2.this);
					sim_list.add(sim_program);
					sim_program.startProgram();
				}
				if (!EVENT_BY_EACH_PROC) {
					intQueue.startTimer();
				}
				
				completeCommand(IDebugCommand.OK);
			}
		}).start();
	}
	public void stopDebugger() {
		new Thread(new Runnable() {
			public void run() {
				if (!EVENT_BY_EACH_PROC) {
					intQueue.stopTimer();
				}
		
				for (Iterator i = sim_list.iterator(); i.hasNext();) {
					SimulateProgram sim_program = (SimulateProgram) i.next();
					sim_program.deleteObservers();
				}
				intQueue.deleteObservers();
				sim_list.clear();
			}
		}).start();
	}
	/***************************************************************************************************************************************************************************************************
	 * not implement yet
	 **************************************************************************************************************************************************************************************************/
	public Process getDebuggerProcess() {
		return null;
	}
	public void restart() throws PCDIException {
		throw new PCDIException(PCDIException.NOT_IMPLEMENTED, "restart");
	}
	public void run(String[] args) throws PCDIException {
		throw new PCDIException(PCDIException.NOT_IMPLEMENTED, "run");
	}
	public void deleteBreakpoints(ICDIBreakpoint[] bp) throws PCDIException {
		throw new PCDIException(PCDIException.NOT_IMPLEMENTED, "deleteBreakpoints");
	}	
	/***************************************************************************************************************************************************************************************************
	 * not implement yet
	 **************************************************************************************************************************************************************************************************/
	
	public void getAIFValue(final BitList tasks, final String expr) throws PCDIException {
		new Thread(new Runnable() {
			public void run() {
				IAIF aif = null;
				SimVariable variable = findVariable(expr);
				if (variable == null)
					variable = findArgument(expr);
				if (variable == null)
					aif = new AIF("unknown", new byte[0]);
		
				if (aif == null) {
					String value = "" + random(10,20);
					aif = new AIF("is4", value.getBytes());
				}
				completeCommand(aif);
			}
		}).start();
	}
	public void go(final BitList tasks) throws PCDIException {
		new Thread(new Runnable() {
			public void run() {
				int[] taskArray = tasks.toArray();
				for (int i=0; i<taskArray.length; i++) {
					getSimProg(taskArray[i]).go();
				}
			}
		}).start();
	}
	public void kill(final BitList tasks) throws PCDIException {
		halt(tasks);
		new Thread(new Runnable() {
			public void run() {
				int[] taskArray = tasks.toArray();
				for (int i=0; i<taskArray.length; i++) {
					getSimProg(taskArray[i]).stopProgram();
				}
			}
		}).start();
	}
	public void halt(final BitList tasks) throws PCDIException {
		new Thread(new Runnable() {
			public void run() {
				int[] taskArray = tasks.toArray();
				for (int i=0; i<taskArray.length; i++) {
					getSimProg(taskArray[i]).suspend();
				}
			}
		}).start();
	}
	public void stepInto(final BitList tasks, final int count) throws PCDIException {
		new Thread(new Runnable() {
			public void run() {
				int[] taskArray = tasks.toArray();
				for (int i=0; i<taskArray.length; i++) {
					getSimProg(taskArray[i]).stepLine();
				}
			}
		}).start();
	}
	public void stepOver(final BitList tasks, final int count) throws PCDIException {
		new Thread(new Runnable() {
			public void run() {
				int[] taskArray = tasks.toArray();
				for (int i=0; i<taskArray.length; i++) {
					getSimProg(taskArray[i]).stepOverLine();
				}
			}
		}).start();
	}
	public void stepFinish(final BitList tasks, final int count) throws PCDIException {
		new Thread(new Runnable() {
			public void run() {
				int[] taskArray = tasks.toArray();
				for (int i=0; i<taskArray.length; i++) {
					getSimProg(taskArray[i]).stepFinish();
				}
			}
		}).start();
	}	
	public void setLineBreakpoint(final BitList tasks, final ICDILineBreakpoint bpt) throws PCDIException {
		new Thread(new Runnable() {
			public void run() {
				int line = bpt.getLocator().getLineNumber();
				int[] taskArray = tasks.toArray();
				for (int i=0; i<taskArray.length; i++) {
					getSimProg(taskArray[i]).setBpt(line);
				}
				completeCommand(IDebugCommand.OK);
			}
		}).start();
	}
	//current support main function breakpoint only
	public void setFunctionBreakpoint(final BitList tasks, final ICDIFunctionBreakpoint bpt) throws PCDIException {
		new Thread(new Runnable() {
			public void run() {
				int[] taskArray = tasks.toArray();
				for (int i=0; i<taskArray.length; i++) {
					SimulateProgram sim_prog = getSimProg(taskArray[i]);
					sim_prog.setStopInMain();
				}
				completeCommand(IDebugCommand.OK);
			}
		}).start();
	}
	public void listStackFrames(final BitList tasks) throws PCDIException {
		new Thread(new Runnable() {
			public void run() {
				int[] taskArray = tasks.toArray();
				List frameList = new ArrayList();
				for (int i=0; i<taskArray.length; i++) {
					ICDITarget target = getSession().getTarget(taskArray[i]);
				    ICDIThread thread = new org.eclipse.ptp.debug.external.cdi.model.Thread((Target) target, 0);
				    SimulateFrame[] frames = getSimProg(taskArray[i]).getSimStackFrames();
				    for (int j=0; j<frames.length; j++) {
				    	frameList.add(new StackFrame((org.eclipse.ptp.debug.external.cdi.model.Thread) thread, frames[j].getLevel(), frames[j].getFile(), frames[j].getFunc(), frames[j].getLine(), frames[j].getAddr()));
				    }
				}
				completeCommand((ICDIStackFrame[]) frameList.toArray(new ICDIStackFrame[0]));
			}
		}).start();
	}
	
	public void setCurrentStackFrame(final BitList tasks, final ICDIStackFrame frame) throws PCDIException {
		new Thread(new Runnable() {
			public void run() {
				current_frame = frame;
				completeCommand(IDebugCommand.OK);
			}
		}).start();
	}
	public void evaluateExpression(final BitList tasks, final String expression) throws PCDIException {
		new Thread(new Runnable() {
			public void run() {
				SimVariable variable = findVariable(expression);
				if (variable == null)
					variable = findArgument(expression);
				if (variable == null) {
					completeCommand(null);
					//throw new PCDIException("No expression value found");				
				}
				else {
					completeCommand(variable.getValue());
				}
			}
		}).start();
	}
	public void getVariableType(final BitList tasks, final String varName) throws PCDIException {
		new Thread(new Runnable() {
			public void run() {
				SimVariable variable = findVariable(varName);
				if (variable == null)
					variable = findArgument(varName);
				if (variable == null) {
					completeCommand(null);
					//throw new PCDIException("No variable type found");
				}
				else {
					completeCommand(variable.getType());
				}
			}
		}).start();
	}
	public void listArguments(final BitList tasks, final ICDIStackFrame frame) throws PCDIException {
		new Thread(new Runnable() {
			public void run() {
				current_frame = frame;

				int[] taskArray = tasks.toArray();
				List argList = new ArrayList();
				for (int i=0; i<taskArray.length; i++) {
					ICDITarget target = getSession().getTarget(taskArray[i]);
				    ICDIThread thread = new org.eclipse.ptp.debug.external.cdi.model.Thread((Target) target, 0);
				    SimVariable[] args = getArguments();
				    for (int j=0; j<args.length; j++) {
						argList.add(new Argument((Target) target, (org.eclipse.ptp.debug.external.cdi.model.Thread) thread, (StackFrame)current_frame, args[j].getVariable(), args[j].getVariable(), args.length - j, frame.getLevel(), null));
				    }
				}
				completeCommand((ICDIArgument[]) argList.toArray(new ICDIArgument[0]));
			}
		}).start();
	}
	public void listLocalVariables(final BitList tasks, final ICDIStackFrame frame) throws PCDIException {
		new Thread(new Runnable() {
			public void run() {
				current_frame = frame;
		
				int[] taskArray = tasks.toArray();
				List varList = new ArrayList();
				for (int i=0; i<taskArray.length; i++) {
					ICDITarget target = getSession().getTarget(taskArray[i]);
				    ICDIThread thread = new org.eclipse.ptp.debug.external.cdi.model.Thread((Target) target, 0);
				    SimVariable[] vars = getVariables();
				    for (int j=0; j<vars.length; j++) {
				    	varList.add(new LocalVariable((Target) target, (org.eclipse.ptp.debug.external.cdi.model.Thread) thread, (StackFrame)current_frame, vars[j].getVariable(), vars[j].getVariable(), vars.length - j, frame.getLevel(), null));
				    }
				}
				completeCommand((ICDILocalVariable[])varList.toArray(new ICDILocalVariable[0]));
			}
		}).start();
	}
	public void listGlobalVariables(BitList tasks) throws PCDIException {
		throw new PCDIException(PCDIException.NOT_IMPLEMENTED, "listGlobalVariables");
	}
	
	public synchronized void update(Observable obs, Object obj) {
		if (obs instanceof SimulateProgram) {
			if (obj != null && obj instanceof String[]) {
				String[] args = (String[]) obj;
				int task = convertInt(args[0]);
				String state = args[1];
				String file = args[2];
				int line = convertInt(args[3]);
				if (EVENT_BY_EACH_PROC) {
					updateEvent(new QueueItem(total_process, state, file, line, task)); 
				}
				else {
					intQueue.addItem(new QueueItem(total_process, state, file, line, task));
				}
			}
		}
		else if (obs instanceof InternalEventQueue) {
			updateEvent((QueueItem)obj);
		}
	}
	private SimulateProgram getSimProg(int id) {
		synchronized (sim_list) {
			return (SimulateProgram)sim_list.get(id);
		}
	}
	private synchronized void updateEvent(QueueItem qItem) {
		String state = qItem.getState();
		//System.out.println("**** Event Update: " + state + ", tasks: " + qItem.getTasks().cardinality());
		if (state.equals(EXIT_STATE)) {
			handleProcessTerminatedEvent(qItem.getTasks());
		} else if (state.equals(HIT_BPT_STATE)) {
			handleBreakpointHitEvent(qItem.getTasks(), qItem.getLine(), qItem.getFile());
		} else if (state.equals(STEP_END_STATE)) {
			handleEndSteppingEvent(qItem.getTasks(), qItem.getLine(), qItem.getFile());
		}
	}
	public static int convertInt(String s_id) {
		try {
			return Integer.parseInt(s_id);
		} catch (NumberFormatException e) {
			return -1;
		}
	}
	public static int random(int min, int max) {
	    Random generator = new Random();
	    long range = (long)max - (long)min + 1;
	    long fraction = (long)(range * generator.nextDouble());
	    return (int)(fraction + min);
	}
	
	//Currently, all tasks are using the same variable
	private class SimVariable {
		private String var = "";
		private String type = "";
		private String val = "";
		
		public SimVariable(String var, String type, String val) {
			this.var = var;
			this.type = type;
			this.val = val;
		}
		public String getVariable() {
			return var;
		}
		public String getType() {
			return type;
		}
		public String getValue() {
			return val;
		}
	}
}
