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


public class DThread {

	final int RUNNING = 10;
	final int SUSPENDED = 11;
	final int TERMINATED = 12;
	
	int state;
	
	int curLine;
	int breakLine;
	DStackFrame[] stackFrames;
	int threadId;
	int processId;
	
	DebugSimulator dD;
	
	public DThread(int id, int pId, DebugSimulator debugger) {
		dD = debugger;
		
		state = RUNNING;
		curLine = 1;
		breakLine = 0;
		threadId = id;
		processId = pId;
		int numStackFrames = 1;
		
		stackFrames = new DStackFrame[numStackFrames];
		for (int i = 0; i < numStackFrames; i++) {
			stackFrames[i] = new DStackFrame(i, "123456", "main", "main.c", 6);
		}
	}
	
	public int getThreadId() {
		return threadId;
	}
	
	public int getStackFrameCount() {
		return stackFrames.length;
	}
	
	public DStackFrame[] getStackFrames() {
		return stackFrames;
	}

	public void runCommand(DInputStream in, String cmd, String arg) {
		if (cmd.equals("print")) {
			checkBreakpoint();
			in.printString(arg + " from process " + processId + " & thread " + threadId);
			curLine++;
		} else	if (cmd.equals("break")) {
			breakLine = Integer.parseInt(arg);
		}
	}
	
	
	public void checkBreakpoint() {
		if (curLine == breakLine) {
			state = SUSPENDED;
			//BitList bitList = new BitList();
			//bitList.set(processId);
			//dD.fireEvent(new EBreakpointHit(bitSet));
			//dD.fireEvent(new BreakpointHitEvent(dD.getSession(), bitList));
			// Do Something
		}
	}
}
