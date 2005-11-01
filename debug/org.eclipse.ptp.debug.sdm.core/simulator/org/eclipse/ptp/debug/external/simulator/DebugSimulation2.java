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
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import org.eclipse.cdt.debug.core.cdi.model.ICDIArgument;
import org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDIFunctionBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDIGlobalVariable;
import org.eclipse.cdt.debug.core.cdi.model.ICDILineBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDILocalVariable;
import org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame;
import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.IDebugger;
import org.eclipse.ptp.debug.core.cdi.PCDIException;
import org.eclipse.ptp.debug.external.AbstractDebugger;

/**
 * @author Clement chu
 * 
 */
public class DebugSimulation2 extends AbstractDebugger implements IDebugger, Observer {
	private final boolean EVENT_BY_EACH_PROC = false;
	private static final String HIT_BPT_STATE = "HIT_BPT";
	private static final String EXIT_STATE = "EXIT";
	private static final String STEP_END_STATE = "STEP_END";
	private static final String APP_NAME = "../main.c";
	private List sim_list = new ArrayList();
	private final long time_range = 5;
	private long current_time = 0;
	private int total_process = 0;
	private Thread timer = null;
	private boolean running_timer = true;
	private InternalEventQueue intQueue = null;

	public DebugSimulation2() {
		intQueue = new InternalEventQueue();
	}
	public void startDebugger(IPJob job) {
		total_process = job.size();
		for (int i = 0; i < total_process; i++) {
			SimulateProgram sim_program = new SimulateProgram(i);
			sim_program.addObserver(DebugSimulation2.this);
			sim_list.add(sim_program);
			sim_program.startProgram();
		}
		start_time_range();
	}
	public void stopDebugger() {
		stop_time_range();
		for (Iterator i = sim_list.iterator(); i.hasNext();) {
			SimulateProgram sim_program = (SimulateProgram) i.next();
			sim_program.deleteObservers();
		}
		sim_list.clear();
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
	
	public void go(BitList tasks) throws PCDIException {
		final int[] taskArray = tasks.toArray();
		new Thread(new Runnable() {
			public void run() {
				for (int i=0; i<taskArray.length; i++) {
					((SimulateProgram)sim_list.get(taskArray[i])).go();
				}
			}
		}).start();
	}
	public void kill(BitList tasks) throws PCDIException {
		final int[] taskArray = tasks.toArray();
		new Thread(new Runnable() {
			public void run() {
				for (int i=0; i<taskArray.length; i++) {
					((SimulateProgram)sim_list.get(taskArray[i])).stopProgram();
				}
			}
		}).start();
	}
	public void halt(BitList tasks) throws PCDIException {
		final int[] taskArray = tasks.toArray();
		new Thread(new Runnable() {
			public void run() {
				for (int i=0; i<taskArray.length; i++) {
					((SimulateProgram)sim_list.get(taskArray[i])).suspend();
				}
			}
		}).start();
	}
	public void stepInto(BitList tasks, int count) throws PCDIException {
		final int[] taskArray = tasks.toArray();
		new Thread(new Runnable() {
			public void run() {
				for (int i=0; i<taskArray.length; i++) {
					((SimulateProgram)sim_list.get(taskArray[i])).stepLine();
				}
			}
		}).start();
	}
	public void stepOver(BitList tasks, int count) throws PCDIException {
		final int[] taskArray = tasks.toArray();
		new Thread(new Runnable() {
			public void run() {
				for (int i=0; i<taskArray.length; i++) {
					((SimulateProgram)sim_list.get(taskArray[i])).stepOverLine();
				}
			}
		}).start();
	}
	public void stepFinish(BitList tasks, int count) throws PCDIException {
		final int[] taskArray = tasks.toArray();
		new Thread(new Runnable() {
			public void run() {
				for (int i=0; i<taskArray.length; i++) {
					getSimProg(taskArray[i]).stepFinish();
				}
			}
		}).start();
	}	
	public void setLineBreakpoint(BitList tasks, ICDILineBreakpoint bpt) throws PCDIException {
		final int line = bpt.getLocator().getLineNumber();
		final int[] taskArray = tasks.toArray();
		new Thread(new Runnable() {
			public void run() {
				for (int i=0; i<taskArray.length; i++) {
					getSimProg(taskArray[i]).setBpt(line);
				}
			}
		}).start();
	}
	//current support main function breakpoint only
	public void setFunctionBreakpoint(BitList tasks, ICDIFunctionBreakpoint bpt) throws PCDIException {
		final int[] taskArray = tasks.toArray();
		new Thread(new Runnable() {
			public void run() {
				for (int i=0; i<taskArray.length; i++) {
					SimulateProgram sim_prog = getSimProg(taskArray[i]);
					sim_prog.setStopInMain();
				}
			}
		}).start();
	}
	public ICDIStackFrame[] listStackFrames(BitList tasks) throws PCDIException {
		int[] taskArray = tasks.toArray();
		for (int i=0; i<taskArray.length; i++) {
			//((SimulateProgram)sim_list.get(taskArray[i])).stopProgram();
		}
		throw new PCDIException(PCDIException.NOT_IMPLEMENTED, "listStackFrames");
	}
	
	public void setCurrentStackFrame(BitList tasks, ICDIStackFrame frame) throws PCDIException {
		int[] taskArray = tasks.toArray();
		for (int i=0; i<taskArray.length; i++) {
			//((SimulateProgram)sim_list.get(taskArray[i])).stopProgram();
		}
		throw new PCDIException(PCDIException.NOT_IMPLEMENTED, "setCurrentStackFrame");
	}
	public String evaluateExpression(BitList tasks, String expression) throws PCDIException {
		throw new PCDIException(PCDIException.NOT_IMPLEMENTED, "evaluateExpression");
	}
	public String getVariableType(BitList tasks, String varName) throws PCDIException {
		throw new PCDIException(PCDIException.NOT_IMPLEMENTED, "getVariableType");
	}
	public ICDIArgument[] listArguments(BitList tasks, ICDIStackFrame frame) throws PCDIException {
		throw new PCDIException(PCDIException.NOT_IMPLEMENTED, "listArguments");
	}
	public ICDILocalVariable[] listLocalVariables(BitList tasks, ICDIStackFrame frame) throws PCDIException {
		throw new PCDIException(PCDIException.NOT_IMPLEMENTED, "listLocalVariables");
	}
	public ICDIGlobalVariable[] listGlobalVariables(BitList tasks) throws PCDIException {
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
					updateEvent(new QueueItem(state, file, line, task)); 
				}
				else {
					intQueue.addItem(new QueueItem(state, file, line, task));
				}
			}
		}
	}
	private SimulateProgram getSimProg(int id) {
		return (SimulateProgram)sim_list.get(id);		
	}
	private void start_time_range() {
		if (timer == null && !EVENT_BY_EACH_PROC) {
			running_timer = true;
			current_time = System.currentTimeMillis();
			Runnable runnable = new Runnable() {
				public synchronized void run() {
					while (running_timer) {
						if (System.currentTimeMillis() - current_time >= time_range) {
							current_time = System.currentTimeMillis();
							updateEvent();
						}
					}
				}
			};
			timer = new Thread(runnable);
			timer.start();
		}
	}
	private void stop_time_range() {
		running_timer = false;
		if (timer != null) {
			timer.interrupt();
		} 
		timer = null;
	}
	private synchronized void updateEvent(QueueItem qItem) {
		String state = qItem.getState();
		System.out.println("**** Event Update: " + state + ", tasks: " + qItem.getTasks().cardinality());
		if (state.equals(EXIT_STATE)) {
			handleProcessTerminatedEvent(qItem.getTasks());
		} else if (state.equals(HIT_BPT_STATE)) {
			handleBreakpointHitEvent(qItem.getTasks(), qItem.getLine(), qItem.getFile());
		} else if (state.equals(STEP_END_STATE)) {
			handleEndSteppingEvent(qItem.getTasks(), qItem.getLine(), qItem.getFile());
		}
	}
	private synchronized void updateEvent() {
		if (!intQueue.isEmpty())
			updateEvent(intQueue.getItem());
	}
	private int convertInt(String s_id) {
		try {
			return Integer.parseInt(s_id);
		} catch (NumberFormatException e) {
			return -1;
		}
	}
	
	private class InternalEventQueue {
		List queue = null;
		InternalEventQueue() {
			queue = Collections.synchronizedList(new LinkedList());
		}
		public QueueItem getItem() {
			synchronized (queue) {
				while (queue.isEmpty()) {
					try {
						queue.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				return (QueueItem)queue.remove(0);
			}
		}
		public void addItem(QueueItem item) {
			synchronized (queue) {
				if (same(item)) {
					queue.add(item);
				} else {
					queue.notifyAll();
					updateEvent();
				}
			}
		}
		public boolean same(QueueItem qItem) {
			synchronized (queue) {
				if (queue.isEmpty())
					return true;
				QueueItem item = getItem();
				if (item.equals(qItem)) {
					qItem.getTasks().or(item.getTasks());
					return true;
				}
				return false;
			}
		}
		public boolean isEmpty() {
			synchronized (queue) {
				return queue.isEmpty();
			}
		}
	}
	private class QueueItem {
		private String file = "";
		private String state = "";
		private int line = -1;
		private BitList tasks = new BitList(total_process);
		public QueueItem(String state, String file, int line, int task) {
			this.state = state;
			this.file = file;
			this.line = line;
			tasks.set(task);
		}
		public boolean equals(QueueItem qItem) {
			return (state.equals(qItem.getState()) && file.equals(qItem.getFile()) && line == qItem.getLine());
		}
		public String getFile() {
			return file;
		}
		public String getState() {
			return state;
		}
		public int getLine() {
			return line;
		}
		public BitList getTasks() {
			return tasks;
		}
	}

	private class SimulateProgram extends Observable implements Runnable {
		private final int sim_program_line = 37;
		private final int main_method_line = 5;
		private final int end_step_line = 15;
		private int start_step_line = 0;
		private int current_line = 0;
		private boolean isStepping = false;
		private int tid = -1;
		private List bpts = new ArrayList();
		private boolean isStopInMain = false;
		private boolean isPause = false;

		public SimulateProgram(int tid) {
			this.tid = tid;
		}
		private synchronized void waitForNotify() {
			try {
				SimulateProgram.this.wait();
			} catch (InterruptedException e) {
				System.out.println("----- Err in waiting: " + e.getMessage());
			}
		}
		private void waitForWhile(long timeout) {
			try {
				Thread.sleep(timeout);
			} catch (InterruptedException e) {
				System.out.println("----- Err in waiting: " + e.getMessage());
			}
		}
		public synchronized void run() {
			SimulateProgram.this.notifyAll();
			waitForNotify();
			while (current_line < sim_program_line) {
				if (isStopInMain) {
					isStopInMain = false;
					gotoLine(main_method_line);
				}
				else if (isPause) {
					setChanged();
					notifyObservers(new String[] { String.valueOf(tid), STEP_END_STATE, APP_NAME, String.valueOf(current_line) });
					waitForNotify();
				}
				else if (isHitBreakpoint(current_line)) {
					isPause = true;
					printMessage();
					setChanged();
					notifyObservers(new String[] { String.valueOf(tid), HIT_BPT_STATE, APP_NAME, String.valueOf(current_line) });
					waitForNotify();
				}
				else if (isStepping) {
					printMessage();
					setChanged();
					notifyObservers(new String[] { String.valueOf(tid), STEP_END_STATE, APP_NAME, String.valueOf(current_line) });					
					waitForNotify();
				}
				else {
					printMessage();
					nextLine();
					waitForWhile(50);
				}
			}
			System.out.println("==== finished: " + tid);
			setChanged();
			notifyObservers(new String[] { String.valueOf(tid), EXIT_STATE, APP_NAME, String.valueOf(current_line) });
		}
		public synchronized void startProgram() {
			new Thread(this).start();
			try {
				//wait the program is ready to start
				SimulateProgram.this.wait(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		public void stopProgram() {
			current_line = sim_program_line;
			if (isPause || isStepping) {
				go();
			}
		}
		private synchronized void printMessage() {
			System.out.println("Task: " + tid + " - Message at current line: " + current_line);
		}
		public synchronized void gotoLine(int line) {
			this.current_line = line;
		}
		public synchronized void go() {
			isPause = false;
			isStepping = false;
			SimulateProgram.this.notifyAll();
			nextLine();
		}
		public synchronized void stepFinish() {
			if (isStepping) {
				if (start_step_line > 0) {
					gotoLine(start_step_line);
					start_step_line = 0;
				}
				isPause = true;
				isStepping = false;
				SimulateProgram.this.notifyAll();					
			}
		}
		public synchronized void stepOverLine() {
			if (isStepping && start_step_line > 0 && current_line == end_step_line) {
				stepFinish();
			}
			else {
				isPause = false;
				isStepping = true;
				SimulateProgram.this.notifyAll();
				nextLine();
			}
		}		
		public synchronized void stepLine() {
			if (!isStepping) {
				start_step_line = current_line;
			}
			isPause = false;
			isStepping = true;
			SimulateProgram.this.notifyAll();
			nextLine();
		}
		public void suspend() {
			isPause = true;
		}
		public synchronized void nextLine() {
			current_line++;
		}
		public boolean isHitBreakpoint(int line) {
			return bpts.contains(new Integer(line));
		}
		public void setBpt(int bpt_line) {
			if (!isHitBreakpoint(bpt_line)) {
				bpts.add(new Integer(bpt_line));
			}
		}
		public void setStopInMain() {
			setBpt(main_method_line);
			isStopInMain = true;
		}
	}
}
