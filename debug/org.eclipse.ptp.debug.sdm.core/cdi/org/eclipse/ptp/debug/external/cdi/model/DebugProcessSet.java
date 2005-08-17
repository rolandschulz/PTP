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
package org.eclipse.ptp.debug.external.cdi.model;

import java.util.ArrayList;

import org.eclipse.ptp.debug.core.cdi.model.IPCDIDebugProcess;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIDebugProcessSet;

public class DebugProcessSet implements IPCDIDebugProcessSet {
	
	private String setName;
	private ArrayList processList = null;

	public DebugProcessSet(String name) {
		processList = new ArrayList();
		setName = name;
	}
	
	public DebugProcessSet(String name, IPCDIDebugProcess[] procs) {
		processList = new ArrayList();
		setName = name;
		for (int i = 0; i < procs.length; i++) {
			addProcess(procs[i]);
		}
	}
	
	public DebugProcessSet(String name, IPCDIDebugProcess proc) {
		processList = new ArrayList();
		setName = name;
		addProcess(proc);
	}
	
	public IPCDIDebugProcess[] getProcesses() {
		return (IPCDIDebugProcess[]) processList.toArray();
	}
	
	public IPCDIDebugProcess getProcess(int number) {
		return (IPCDIDebugProcess) processList.get(number);
	}
	
	public void addProcess(IPCDIDebugProcess proc) {
		if (!processList.contains(proc))
			processList.add(proc);
	}
	
	public void removeProcess(IPCDIDebugProcess proc) {
		if (processList.contains(proc))
			processList.remove(proc);
	}
	
	public String getName() {
		return setName;
	}
	
	public int getSize() {
		return processList.size();
	}
}
