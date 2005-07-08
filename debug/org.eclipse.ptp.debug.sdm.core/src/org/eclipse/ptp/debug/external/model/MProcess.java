/*
 * Created on Feb 21, 2005
 *
 */
package org.eclipse.ptp.debug.external.model;

import org.eclipse.ptp.core.IPProcess;


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
	private IPProcess pproc;
	
	private static int globalCounter = 0;
	int id;
	String name = "";

	public IPProcess getPProcess() {
		return pproc;
	}
	
	public void setPProcess(IPProcess p) {
		pproc = p;
	}
	
	public Object getDebugInfo() {
		return debugInfo;
	}
	
	public void setDebugInfo(Object info) {
		debugInfo = info;
	}
	
	public MProcess() {
		id = getUniqId();
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

	public String getName() {
		return name;
	}
}
