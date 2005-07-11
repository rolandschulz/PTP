package org.eclipse.ptp.debug.core.cdi.model;

import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;

public interface IPCDITarget extends ICDITarget {
	public IPCDIDebugProcessGroup newProcessGroup(String name);
	public void delProcessGroup(String name);
	public void setCurrentFocus(int procNum);
	public void setCurrentFocus(String name);

	public Process[] getProcesses();
	public Process getProcess(int num);
}
