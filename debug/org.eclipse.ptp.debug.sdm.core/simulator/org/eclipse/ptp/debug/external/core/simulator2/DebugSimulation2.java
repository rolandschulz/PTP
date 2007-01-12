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
package org.eclipse.ptp.debug.external.core.simulator2;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.IDebugCommand;
import org.eclipse.ptp.debug.core.IDebugger;
import org.eclipse.ptp.debug.core.aif.AIF;
import org.eclipse.ptp.debug.core.aif.IAIF;
import org.eclipse.ptp.debug.core.cdi.PCDIException;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIErrorEvent;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIArgument;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIBreakpoint;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIFunctionBreakpoint;
import org.eclipse.ptp.debug.core.cdi.model.IPCDILineBreakpoint;
import org.eclipse.ptp.debug.core.cdi.model.IPCDILocalVariable;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIStackFrame;
import org.eclipse.ptp.debug.core.cdi.model.IPCDITarget;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIWatchpoint;
import org.eclipse.ptp.debug.external.core.AbstractDebugger;
import org.eclipse.ptp.debug.external.core.cdi.breakpoints.LineBreakpoint;
import org.eclipse.ptp.debug.external.core.cdi.model.LineLocation;
import org.eclipse.ptp.debug.external.core.cdi.model.StackFrame;
import org.eclipse.ptp.debug.external.core.cdi.model.Target;
import org.eclipse.ptp.debug.external.core.cdi.model.variable.Argument;
import org.eclipse.ptp.debug.external.core.cdi.model.variable.LocalVariable;

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
	private IPCDIStackFrame current_frame = null;
	private int bpt_id = 0;

	public DebugSimulation2() {
		intQueue = new InternalEventQueue(TIME_RANGE);
		intQueue.addObserver(this);
		createArguments();
		createVariables();
	}
	public int getBreakpointId() {
		return bpt_id++;
	}
	public void connection() throws CoreException {
		completeCommand(null, IDebugCommand.RETURN_OK);
	}
	public int getDebuggerPort() throws CoreException {
		return 0;
	}
	private void createArguments() {
		arguments.put("argc", new SimVariable("argc", "char **", ""));
		arguments.put("argv", new SimVariable("argv", "int", "-"));
	}
	private void createVariables() {
		variables.put("name", new SimVariable("name", "string", "hello"));
		variables.put("number", new SimVariable("number", "boolean", "-1"));
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
				total_process = job.totalProcesses();
				for (int i = 0; i < total_process; i++) {
					SimulateProgram sim_program = new SimulateProgram(i, APP_NAME);
					sim_program.addObserver(DebugSimulation2.this);
					sim_list.add(sim_program);
					sim_program.startProgram();
				}
				if (!EVENT_BY_EACH_PROC) {
					intQueue.startTimer();
				}
				completeCommand(null, IDebugCommand.RETURN_OK);
			}
		}).start();
	}
	public void stopDebugger() throws CoreException {
		if (sim_list.isEmpty())
			return;

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
				completeCommand(null, IDebugCommand.RETURN_OK);
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
	public void deleteBreakpoint(final BitList tasks, final int bpid) throws PCDIException {
		new Thread(new Runnable() {
			public void run() {
				int[] taskArray = tasks.toArray();
				for (int i=0; i<taskArray.length; i++) {
					getSimProg(taskArray[i]).deleteBpt(bpid);
				}
			}
		}).start();
	}	
	/***************************************************************************************************************************************************************************************************
	 * not implement yet
	 **************************************************************************************************************************************************************************************************/
	
	public void getAIF(final BitList tasks, final String expr) throws PCDIException {
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
					aif = new AIF("b", value.getBytes());
					//Integer value = new Integer(random(10,20));
					//aif = new AIF("is4", new byte[] {value.byteValue()});
					
				}
				completeCommand(tasks, aif);
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
				completeCommand(tasks, IDebugCommand.RETURN_OK);
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
				completeCommand(tasks, IDebugCommand.RETURN_OK);
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
				completeCommand(tasks, IDebugCommand.RETURN_OK);
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
				completeCommand(tasks, IDebugCommand.RETURN_OK);
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
				completeCommand(tasks, IDebugCommand.RETURN_OK);
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
				completeCommand(tasks, IDebugCommand.RETURN_OK);
			}
		}).start();
	}	
	public void setLineBreakpoint(final BitList tasks, final IPCDILineBreakpoint bpt) throws PCDIException {
		new Thread(new Runnable() {
			public void run() {
				int[] taskArray = tasks.toArray();
				int line = bpt.getLocator().getLineNumber();
				IPCDIBreakpoint new_bpt = createBreakpoint(bpt.getLocator().getFile(), line, bpt);
				for (int i=0; i<taskArray.length; i++) {
					getSimProg(taskArray[i]).setBpt(line, new_bpt.getBreakpointId());
				}
				completeCommand(tasks, new_bpt);
			}
		}).start();
	}
	public void enableBreakpoint(BitList tasks, int bpid) throws PCDIException {
		//TODO
	}
	public void disableBreakpoint(BitList tasks, int bpid) throws PCDIException {
		//TODO
	}
	public void conditionBreakpoint(BitList tasks, int bpid, String expr) throws PCDIException {
		//TODO
	}
	public void getListSignals(BitList tasks, String name) throws PCDIException {
		//TODO
	}
	public void getSignalInfo(BitList tasks, String arg) throws PCDIException {
		//TODO
	}
	public void cliHandle(BitList tasks, String arg) throws PCDIException {
		//TODO
	}
	
	//current support main function breakpoint only
	public void setFunctionBreakpoint(final BitList tasks, final IPCDIFunctionBreakpoint bpt) throws PCDIException {
		new Thread(new Runnable() {
			public void run() {
				int[] taskArray = tasks.toArray();
				int line = SimulateProgram.MAIN_METHOD_LINE;
				IPCDIBreakpoint new_bpt = createBreakpoint(bpt.getLocator().getFile(), line, bpt);
				for (int i=0; i<taskArray.length; i++) {
					getSimProg(taskArray[i]).setStopInMain(line, new_bpt.getBreakpointId());
				}
				completeCommand(tasks, new_bpt);
			}
		}).start();
	}
	public void setWatchpoint(final BitList tasks, final IPCDIWatchpoint bpt) throws PCDIException {
		throw new PCDIException(PCDIException.NOT_IMPLEMENTED, "setWatchpoint");
	}
	private IPCDIBreakpoint createBreakpoint(String file, int line, IPCDIBreakpoint oldBpt) {
		try {
			IPCDILineBreakpoint bpt = new LineBreakpoint(oldBpt.getType(), new LineLocation(file, line), oldBpt.getCondition());
			bpt.setBreakpointId(getBreakpointId());
			return bpt;
		} catch (PCDIException e) {
			return null;
		}
	}
	
	public void listStackFrames(final BitList tasks, int low, int high) throws PCDIException {
		new Thread(new Runnable() {
			public void run() {
				int[] taskArray = tasks.toArray();
				List frameList = new ArrayList();
				for (int i=0; i<taskArray.length; i++) {
					IPCDITarget target = getSession().getTarget(taskArray[i]);
					try {
						org.eclipse.ptp.debug.external.core.cdi.model.Thread t = (org.eclipse.ptp.debug.external.core.cdi.model.Thread)target.getCurrentThread();
						SimulateFrame[] frames = getSimProg(taskArray[i]).getSimStackFrames();
					    for (int j=0; j<frames.length; j++) {
					    	frameList.add(new StackFrame(t, frames[j].getLevel(), frames[j].getFile(), frames[j].getFunc(), frames[j].getLine(), new BigInteger(frames[j].getAddr()), null));
					    }
					} catch (PCDIException e) {
						
					}
				}
				completeCommand(tasks, (IPCDIStackFrame[]) frameList.toArray(new IPCDIStackFrame[0]));
			}
		}).start();
	}
	
	public void setCurrentStackFrame(final BitList tasks, final int level) throws PCDIException {
		new Thread(new Runnable() {
			public void run() {
				completeCommand(tasks, IDebugCommand.RETURN_OK);
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
					completeCommand(tasks, null);
					//throw new PCDIException("No expression value found");				
				}
				else {
					completeCommand(tasks, variable.getValue());
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
					completeCommand(tasks, null);
					//throw new PCDIException("No variable type found");
				}
				else {
					completeCommand(tasks, variable.getType());
				}
			}
		}).start();
	}
	public void listArguments(final BitList tasks, int low, int high) throws PCDIException {
		new Thread(new Runnable() {
			public void run() {
				int[] taskArray = tasks.toArray();
				List argList = new ArrayList();
				for (int i=0; i<taskArray.length; i++) {
					IPCDITarget target = getSession().getTarget(taskArray[i]);
				    SimVariable[] args = getArguments();
				    for (int j=0; j<args.length; j++) {
						argList.add(new Argument((Target) target, null, (StackFrame)current_frame, args[j].getVariable(), args[j].getVariable(), args.length - j, 1, null));
				    }
				}
				completeCommand(tasks, (IPCDIArgument[]) argList.toArray(new IPCDIArgument[0]));
			}
		}).start();
	}
	public void listLocalVariables(final BitList tasks) throws PCDIException {
		new Thread(new Runnable() {
			public void run() {
				int[] taskArray = tasks.toArray();
				List varList = new ArrayList();
				for (int i=0; i<taskArray.length; i++) {
					IPCDITarget target = getSession().getTarget(taskArray[i]);
				    SimVariable[] vars = getVariables();
				    for (int j=0; j<vars.length; j++) {
				    	varList.add(new LocalVariable((Target) target, null, (StackFrame)current_frame, vars[j].getVariable(), vars[j].getVariable(), vars.length - j, 1, null));
				    }
				}
				completeCommand(tasks, (IPCDILocalVariable[])varList.toArray(new IPCDILocalVariable[0]));
			}
		}).start();
	}
	public void listGlobalVariables(BitList tasks) throws PCDIException {
		completeCommand(tasks, null);
		handleErrorEvent(tasks, PCDIException.NOT_IMPLEMENTED + " - listGlobalVariables", IPCDIErrorEvent.DBG_FATAL);
		//throw new PCDIException(PCDIException.NOT_IMPLEMENTED, "listGlobalVariables");
	}
	public void getInfothreads(final BitList tasks) throws PCDIException {
		new Thread(new Runnable() {
			public void run() {
				completeCommand(tasks, new String[] { "0" });			
			}
		}).start();
	}
	public void setThreadSelect(final BitList tasks, final int threadNum) throws PCDIException {
		new Thread(new Runnable() {
			public void run() {
				completeCommand(tasks, new Object[] { new Integer(threadNum),  });
			}
		}).start();
	}
	public void getStackInfoDepth(final BitList tasks) throws PCDIException {
		new Thread(new Runnable() {
			public void run() {
				completeCommand(tasks, new Integer(1));			
			}
		}).start();
	}
	
	public void setDataReadMemoryCommand(final BitList tasks, long offset, String address, int wordFormat, int wordSize, int rows, int cols, Character asChar) throws PCDIException {
		throw new PCDIException("not supported in simulator");
	}
	public void setDataWriteMemoryCommand(final BitList tasks, long offset, String address, int wordFormat, int wordSize, String value) throws PCDIException {
		throw new PCDIException("not supported in simulator");
	}
	
	public void dataEvaluateExpression(BitList tasks, String expression) throws PCDIException {
		throw new PCDIException("dataEvaluateExpression - not supported in simulator yet");
	}
	public void getPartialAIF(BitList tasks, String expr, String key, boolean listChildren, boolean express) throws PCDIException {
		throw new PCDIException("getPartialAIF - not supported in simulator yet");
	}
	public void variableDelete(BitList tasks, String varname) throws PCDIException {
		throw new PCDIException("variableDelete - not supported in simulator yet");
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
		//System.out.println("**** Event Update: " + state + ", tasks: " + qItem.getTasks().cardinality() + ", line: " + qItem.getLine());
		if (state.equals(EXIT_STATE)) {
			handleProcessTerminatedEvent(qItem.getTasks(), 0);
		} else if (state.equals(HIT_BPT_STATE)) {
			handleBreakpointHitEvent(qItem.getTasks(), qItem.getLine(), 0, new String[0]);
			//handleBreakpointHitEvent(qItem.getTasks(), qItem.getLine(), qItem.getFile());
		} else if (state.equals(STEP_END_STATE)) {
			handleEndSteppingEvent(qItem.getTasks(), qItem.getLine(), qItem.getFile(), 0, new String[0]);
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
