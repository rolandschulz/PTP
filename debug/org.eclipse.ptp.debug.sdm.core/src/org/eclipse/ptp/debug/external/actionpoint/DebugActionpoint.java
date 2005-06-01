/*
 * Created on Feb 21, 2005
 *
 */
package org.eclipse.ptp.debug.external.actionpoint;

import java.util.ArrayList;

import org.eclipse.ptp.debug.external.model.MProcess;



/**
 * @author donny
 *
 */
public class DebugActionpoint {
	private static int globalCounter;
	int id;
	String status = ""; /* ENABLED | DISABLED | DELETED */
	ArrayList processLst = null;
	ArrayList debuggerIdLst; /* actionpoint id from the debugger */
	
	public DebugActionpoint() {
		id = getUniqId();
		status = "ENABLED";
		processLst = new ArrayList();
		debuggerIdLst = new ArrayList();
	}
	
	private static synchronized int getUniqId() {
		int count = ++globalCounter;
		// If we ever wrap around.
		if (count <= 0) {
			count = globalCounter = 1;
		}
		return count;
	}
	
	public int getId() {
		return id;
	}

	public ArrayList getProcessList() {
		return processLst;
	}
	
	public ArrayList getDebuggerIdList() {
		return debuggerIdLst;
	}
	
	public void addProcess(MProcess proc, int anId) {
		processLst.add(proc);
		debuggerIdLst.add(new Integer(anId));
	}
	
	public String getStatus() {
		return status;
	}
	
	public void setStatus(String stat) {
		status = stat;
	}
	
	public boolean isEnabled() {
		return status.equals("ENABLED");
	}
	
	public boolean isDisabled() {
		return status.equals("DISABLED");
	}

	public boolean isDeleted() {
		return status.equals("DELETED");
	}
}
