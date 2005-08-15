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
