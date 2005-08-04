package org.eclipse.ptp.debug.external.simulator;

import org.eclipse.ptp.debug.external.DebugSession;
import org.eclipse.ptp.debug.external.event.EBreakpointHit;

public class SimThread {

	final int RUNNING = 10;
	final int SUSPENDED = 11;
	final int TERMINATED = 12;
	
	int state;
	
	int curLine;
	int breakLine;
	SimStackFrame[] stackFrames;
	int threadId;
	int processId;
	
	DebugSimulator dSim;
	DebugSession dSes;
	
	public SimThread(int id, int pId, DebugSimulator debugger, DebugSession dSession) {
		dSim = debugger;
		dSes = dSession;
		
		state = RUNNING;
		curLine = 1;
		breakLine = 0;
		threadId = id;
		processId = pId;
		int numStackFrames = 1;
		
		stackFrames = new SimStackFrame[numStackFrames];
		for (int i = 0; i < numStackFrames; i++) {
			stackFrames[i] = new SimStackFrame(i, "123456", "main", "main.c", 6);
		}
	}
	
	public int getThreadId() {
		return threadId;
	}
	
	public int getStackFrameCount() {
		return stackFrames.length;
	}
	
	public SimStackFrame[] getStackFrames() {
		return stackFrames;
	}

	public void runCommand(SimInputStream in, String cmd, String arg) {
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
			dSim.fireEvent(new EBreakpointHit(processId, threadId));
			// Do Something
		}
	}
}
