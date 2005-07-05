package org.eclipse.ptp.debug.external.simulator;

public class SimThread {

	SimStackFrame[] stackFrames;
	int threadId;
	
	public SimThread(int numStackFrames, int id) {
		threadId = id;
		
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
}
