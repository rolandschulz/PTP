package org.eclipse.ptp.debug.core.cdi;

import org.eclipse.ptp.debug.core.cdi.model.IPCDIDebugProcess;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIDebugProcessSetNamed;
import org.eclipse.ptp.debug.core.utils.BitList;

public interface IPCDIModelManager {
	public void delProcessSet(String setName);
	public IPCDIDebugProcess getProcess(int proc);
	public IPCDIDebugProcessSetNamed getProcessSet(String setName);
	public IPCDIDebugProcessSetNamed[] getProcessSets();
	public IPCDIDebugProcessSetNamed newProcessSet(String setName, int[] procs);
	public IPCDIDebugProcessSetNamed newProcessSet(String setName, BitList list);
}
