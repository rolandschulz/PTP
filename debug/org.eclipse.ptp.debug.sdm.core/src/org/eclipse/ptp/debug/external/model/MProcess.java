/*
 * Created on Feb 21, 2005
 *
 */
package org.eclipse.ptp.debug.external.model;


/**
 * @author donny
 *
 */
public class MProcess {
	/* debugInfo holds an internal state of the debugger associated
	 * with this MProcess
	 * It is an Object so that it's generic and not tied to any
	 * particular debugger, (of course to be useful, it must casted
	 * accordingly).
	 */
	private Object debugInfo;
	private static int globalCounter = 0;
	int id;
	int pid;
	String name = "";
	
	String debuggerOutput = "";
	
	public Object getDebugInfo() {
		return debugInfo;
	}
	
	public void setDebugInfo(Object info) {
		debugInfo = info;
	}
	
	public MProcess() {
		id = getUniqId();
		pid = 0;
		name = "process" + Integer.toString(id);
	}
	
	public static synchronized void resetGlobalCounter() {
		globalCounter = 0;
	}
	
	private static synchronized int getUniqId() {
		int count = globalCounter;
		// If we ever wrap around.
		if (count < 0) {
			count = globalCounter = 0;
		}
		globalCounter++;
		return count;
	}
	
	public int getId() {
		return id;
	}

	public int getPid() {
		return pid;
	}
	
	public void setPid(int p) {
		pid = p;
	}

	public String getName() {
		return name;
	}
	
	public void setDebuggerOutput(String out) {
		debuggerOutput = out;
	}
	
	public String getDebuggerOutput() {
		return debuggerOutput;
	}
}
