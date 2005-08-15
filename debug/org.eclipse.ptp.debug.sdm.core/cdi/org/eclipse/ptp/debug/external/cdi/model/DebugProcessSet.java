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

import org.eclipse.ptp.debug.core.cdi.model.IPCDIDebugProcess;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIDebugProcessSet;
import org.eclipse.ptp.debug.external.model.MProcess;
import org.eclipse.ptp.debug.external.model.MProcessSet;

public class DebugProcessSet implements IPCDIDebugProcessSet {
	
	private MProcessSet mSet;

	public DebugProcessSet(MProcessSet set) {
		mSet = set;
	}
	
	public IPCDIDebugProcess[] getProcesses() {
		MProcess[] mProcs = mSet.getProcessList();
		IPCDIDebugProcess[] result = new IPCDIDebugProcess[mProcs.length];
		for (int i = 0; i < mProcs.length; i++) {
			result[i] = new DebugProcess(mProcs[i]);
		}
		return result;
	}
	
	public IPCDIDebugProcess getProcess(int number) {
		MProcess proc = mSet.getProcess(number);
		return new DebugProcess(proc);
	}
	
	public void addProcess(IPCDIDebugProcess proc) {
		mSet.addProcess(((DebugProcess) proc).getMProcess());
	}
	
	public void removeProcess(IPCDIDebugProcess proc) {
		mSet.delProcess(((DebugProcess) proc).getMProcess());
	}
	
	public String getName() {
		return mSet.getName();
	}
}
