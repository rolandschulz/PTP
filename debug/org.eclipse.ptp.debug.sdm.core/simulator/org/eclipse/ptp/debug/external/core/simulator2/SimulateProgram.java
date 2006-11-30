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

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import org.eclipse.ptp.debug.core.PDebugUtils;

/**
 * @author Clement chu
 * 
 */
public class SimulateProgram extends Observable implements Runnable {
	public static final int SIM_PROGRAM_LINE = 37;
	public static final int MAIN_METHOD_LINE = 6;
	private final int END_STEP_LINE = 15;
	private int start_step_line = 0;
	private int current_line = 0;
	private boolean isStepping = false;
	private int tid = -1;
	private String file = "";
	private List lines = new ArrayList();
	private List bpts = new ArrayList();
	private boolean isStopInMain = false;
	private boolean isPause = false;

	public SimulateProgram(int tid, String file) {
		this.tid = tid;
		this.file = file;
	}
	public SimulateFrame[] getSimStackFrames() {
		int frameLength = DebugSimulation2.random(1, 3);
		String addr = "" + DebugSimulation2.random(10000, 50000);
		SimulateFrame[] frames = new SimulateFrame[frameLength];
		for (int i=0; i<frames.length; i++) {
			frames[i] = new SimulateFrame((i), file, file, (current_line-i), addr); 
		}
		return frames;
	}
	private synchronized void waitForNotify() {
		try {
			SimulateProgram.this.wait();
		} catch (InterruptedException e) {
			PDebugUtils.println("----- Err in waiting: " + e.getMessage());
		}
	}
	private void waitForWhile(long timeout) {
		try {
			Thread.sleep(timeout);
		} catch (InterruptedException e) {
			PDebugUtils.println("----- Err in waiting: " + e.getMessage());
		}
	}
	public synchronized void run() {
		SimulateProgram.this.notifyAll();
		waitForNotify();
		while (current_line < SIM_PROGRAM_LINE) {
			if (isStopInMain) {
				isStopInMain = false;
				printMessage();
				gotoLine(MAIN_METHOD_LINE);
			}
			else if (isPause) {
				printMessage();
				setChanged();
				notifyObservers(new String[] { String.valueOf(tid), DebugSimulation2.STEP_END_STATE, file, String.valueOf(current_line) });
				waitForNotify();
			}
			else if (isHitBreakpoint(current_line)) {
				isPause = true;
				printMessage();
				setChanged();
				notifyObservers(new String[] { String.valueOf(tid), DebugSimulation2.HIT_BPT_STATE, file, String.valueOf(getHitBreakpointID(current_line)) });
				waitForNotify();
			}
			else if (isStepping) {
				printMessage();
				setChanged();
				notifyObservers(new String[] { String.valueOf(tid), DebugSimulation2.STEP_END_STATE, file, String.valueOf(current_line) });					
				waitForNotify();
			}
			else {
				printMessage();
				nextLine();
				waitForWhile(50);
			}
		}
		PDebugUtils.println("==== finished: " + tid);
		setChanged();
		notifyObservers(new String[] { String.valueOf(tid), DebugSimulation2.EXIT_STATE, file, String.valueOf(current_line) });
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
		current_line = SIM_PROGRAM_LINE;
		if (isPause || isStepping) {
			go();
		}
	}
	private synchronized void printMessage() {
		//System.out.println("Task: " + tid + " - Message at current line: " + current_line);
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
		if (isStepping && start_step_line > 0 && current_line == END_STEP_LINE) {
			stepFinish();
		}
		else {
			isPause = false;
			isStepping = true;
			nextLine();
			SimulateProgram.this.notifyAll();
		}
	}		
	public synchronized void stepLine() {
		if (!isStepping) {
			start_step_line = current_line;
		}
		isPause = false;
		isStepping = true;
		nextLine();
		SimulateProgram.this.notifyAll();
	}
	public void suspend() {
		isPause = true;
	}
	public synchronized void nextLine() {
		current_line++;
	}
	public boolean isHitBreakpoint(int line) {
		return lines.contains(new Integer(line));
	}
	public int getHitBreakpointID(int line) {
		int index = lines.indexOf(new Integer(line));
		return ((Integer)bpts.get(index)).intValue();
	}
	public void deleteBpt(int bpt_id) {
		int index = bpts.indexOf(new Integer(bpt_id));
		bpts.remove(index);
		lines.remove(index);
	}
	
	public void setBpt(int line, int bpt_id) {
		Integer lineInt = new Integer(line);
		Integer bptInt = new Integer(bpt_id); 
		if (!bpts.contains(bptInt)) {
			if (!lines.contains(lineInt)) {
				lines.add(lineInt);
				bpts.add(bptInt);
			}
		}
	}
	public void setStopInMain(int line, int bpt_id) {
		setBpt(line, bpt_id);
		isStopInMain = true;
	}	
}
