package org.eclipse.ptp.debug.external.cdi.model;

import java.util.ArrayList;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIDebugProcess;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIDebugProcessSet;
import org.eclipse.ptp.debug.external.model.MProcess;
import org.eclipse.ptp.debug.external.model.MProcessSet;

public class DebugProcessSet implements IPCDIDebugProcessSet {
	
	private MProcessSet pSet;

	public DebugProcessSet(MProcessSet set) {
		pSet = set;
	}
	
	public IPCDIDebugProcess[] getProcesses() {
		MProcess[] mProcs = pSet.getProcessList();
		IPCDIDebugProcess[] result = new IPCDIDebugProcess[mProcs.length];
		for (int i = 0; i < mProcs.length; i++) {
			result[i] = new DebugProcess(mProcs[i]);
		}
		return result;
	}
	
	public IPCDIDebugProcess getProcess(int number) {
		MProcess proc = pSet.getProcess(number);
		return new DebugProcess(proc);
	}
	
	public void addProcess(IPCDIDebugProcess proc) {
		pSet.addProcess(((DebugProcess) proc).getMProcess());
	}
	
	public void removeProcess(IPCDIDebugProcess proc) {
		pSet.delProcess(((DebugProcess) proc).getMProcess());
	}
	
	public String getName() {
		return pSet.getName();
	}
}
