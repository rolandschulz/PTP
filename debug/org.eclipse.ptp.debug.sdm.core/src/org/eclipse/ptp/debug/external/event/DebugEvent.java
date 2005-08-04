/*
 * Created on Feb 21, 2005
 *
 */
package org.eclipse.ptp.debug.external.event;


/**
 * @author donny
 *
 */
public class DebugEvent {
	String eventName = "";
	int processId;
	int threadId;
	
	public DebugEvent(String eName, int pId, int tId) {
		eventName = eName;
		processId = pId;
		threadId = tId;
	}
	
	public int getProcessId() {
		return processId;
	}
	
	public int getThreadId() {
		return threadId;
	}
}
