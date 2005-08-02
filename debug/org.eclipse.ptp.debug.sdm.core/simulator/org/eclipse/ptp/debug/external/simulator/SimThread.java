package org.eclipse.ptp.debug.external.simulator;

public class SimThread {

	int curLine;
	int breakLine;
	SimStackFrame[] stackFrames;
	int threadId;
	
	public SimThread(int id) {
		curLine = 1;
		breakLine = 0;
		threadId = id;
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
	
	public void incrementCurrentLine() {
		curLine++;
		if (curLine == breakLine) {
			// Do Something
		}
	}
	
	public int getCurrentLine() {
		return curLine;
	}
	
	public void setBreakLine(int l) {
		breakLine = l;
	}
}
