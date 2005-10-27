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

import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.cdi.IPCDISession;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIDebugProcess;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIDebugProcessSet;
import org.eclipse.ptp.debug.external.cdi.Session;
import org.eclipse.ptp.debug.external.cdi.SessionObject;
/**
 * @deprecated
 */
public class DebugProcessSet extends SessionObject implements IPCDIDebugProcessSet {
	static private int tempId = 0;
	final private String PREFIX = "TempSet_";
	protected String setName;
	protected BitList processList = null;

	public DebugProcessSet(DebugProcessSet set) {
		super((Session) set.getSession());
		processList = set.toBitList().copy();
		setName = PREFIX + tempId++;
	}
	
	public DebugProcessSet(IPCDISession s) {
		super((Session) s);
		processList = new BitList(s.getTotalProcesses());
		setName = PREFIX + tempId++;
	}

	public DebugProcessSet(IPCDISession s, BitList list) {
		super((Session) s);
		processList = list;
		setName = PREFIX + tempId++;
	}

	public DebugProcessSet(IPCDISession s, int proc) {
		this(s, new int[] {proc});
	}
	
	public DebugProcessSet(IPCDISession s, int[] procs) {
		super((Session) s);
		processList = new BitList(s.getTotalProcesses());
		processList.set(procs);
		setName = PREFIX + tempId++;
	}

	public IPCDIDebugProcess[] getProcesses() {
		int[] list = toIntArray();
		IPCDIDebugProcess[] retVal = new IPCDIDebugProcess[list.length];
		for (int i = 0; i < list.length; i++) {
			retVal[i] = getProcess(list[i]);
		}
		return retVal;
	}

	public IPCDIDebugProcess getProcess(int number) {
		return null;
		//return ((Session) getSession()).getModelManager().getProcess(number);
	}

	public int getSize() {
		return processList.cardinality();
	}

	public int[] toIntArray() {
		return processList.toArray();
	}

	public String getName() {
		return setName;
	}

	public BitList toBitList() {
		return processList;
	}
	
	public void addProcess(int proc) {
		processList.set(proc);
	}
	
	public void addProcess(int[] proc) {
		processList.set(proc);
	}
	
	public void removeProcess(int proc) {
		processList.clear(proc);
	}

	public void removeProcess(int[] proc) {
		processList.clear(proc);
	}
}
